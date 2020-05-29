package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import model.Kontrast;
import org.apache.commons.math3.distribution.FDistribution;
import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.util.Combinations;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Stream;

public class PodaciController implements Initializable {
    @FXML
    private Button izracunajButton;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private Button povratakButton;

    private Stage primaryStage;
    private int brojAlternativa;
    private int brojMjerenja;

    private TextField[][] matrixNodes;
    private Label[] arrayOfAverages;
    private Label[] arrayOfEffects;

    public PodaciController(Stage primaryStage, int brojAlternativa, int brojMjerenja) {
        this.primaryStage = primaryStage;
        this.brojAlternativa = brojAlternativa;
        this.brojMjerenja = brojMjerenja;
        matrixNodes = new TextField[brojAlternativa][brojMjerenja];
        arrayOfAverages = new Label[brojAlternativa];
        arrayOfEffects = new Label[brojAlternativa];
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        povratakButton.setOnAction(e -> back());
        initTable();
        initOnChange();
        izracunajButton.setOnAction(e -> izracunajDetaljno());
    }
    private double zaokruzi(double broj,int brojDecimala){
        return Double.parseDouble(String.format("%."+brojDecimala+"f",broj));
    }

    private void izracunajDetaljno() {
        StringBuilder sb = new StringBuilder();
        double ssa = izracunajSsa();
        sb.append("SSA: " + zaokruzi(ssa,2) + System.lineSeparator());
        double sse = izracunajSse();
        sb.append("SSE: " + zaokruzi(sse,2) + System.lineSeparator());
        double sst = ssa + sse;
        sb.append("SST: " + zaokruzi(sst,2) + System.lineSeparator());
        double percentageOfVariance = ssa / sst;
        sb.append("Udio varijanse u razlicitosti alternativa:" + zaokruzi(percentageOfVariance*100,2) +"%" + System.lineSeparator());
        double percentageOfErrors = sse / sst;
        sb.append("Udio varijanse u greskama pri mjerenju:" + zaokruzi(percentageOfErrors*100,2) +"%"+ System.lineSeparator());
        int degresOfFreedomOfMeasurement = brojAlternativa - 1;
        sb.append("Stepen slobode mjerenja:" + degresOfFreedomOfMeasurement + System.lineSeparator());
        int degresOFFreedomOfError = brojAlternativa * (brojMjerenja - 1);
        sb.append("Stepen slobode greske:" + degresOFFreedomOfError + System.lineSeparator());
        int degresOfFreedomOfTotal = brojAlternativa * brojMjerenja - 1;
        sb.append("Stepen slobode totala:" + degresOfFreedomOfTotal + System.lineSeparator());
        double varianceOfMeasurement = ssa / degresOfFreedomOfMeasurement;
        sb.append("Varijansa mjerenja:" + zaokruzi(varianceOfMeasurement,2) + System.lineSeparator());
        double varianceOfErrors = sse / degresOFFreedomOfError;
        sb.append("Varijansa greske:" + zaokruzi(varianceOfErrors,2) + System.lineSeparator());
        double varianceOfTotal = sst / degresOfFreedomOfTotal;
        sb.append("Varijansa totala:" + zaokruzi(varianceOfTotal,2) + System.lineSeparator());
        double FCalculated = varianceOfMeasurement / varianceOfErrors;
        sb.append("F izracunato:" + zaokruzi(FCalculated,2) + System.lineSeparator());
        FDistribution dist = new FDistribution(null, degresOfFreedomOfMeasurement, degresOFFreedomOfError);
        double FTabular = dist.inverseCumulativeProbability(0.95);
        sb.append("F tabelarno:" + zaokruzi(FTabular,2) + System.lineSeparator());
        sb.append("Odnos F-ova je:" + zaokruzi(FCalculated / FTabular,2)  + " pa mozemo sa 95% sigurnosti tvrditi da " + (FCalculated > FTabular ? "su" : "nisu") + " razlike izmedju alternativa statisticki znacajne."+System.lineSeparator()+System.lineSeparator());
        List<Kontrast> listOfContrasts = izracunajKontraste();
        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        sb.append(Kontrast.ispis(listOfContrasts));
        textArea.setText("Rezultati:" +System.lineSeparator()+ sb);
        Pane pane=new Pane();
        pane.getChildren().add(textArea);
        Stage resultStage = new Stage();
        resultStage.setScene(new Scene(pane, 800, 450));
        textArea.setPrefWidth(resultStage.getScene().getWidth());
        textArea.setPrefHeight(resultStage.getScene().getHeight());
        resultStage.setResizable(false);
        resultStage.show();
    }

    private List<Kontrast> izracunajKontraste() {
        List<Kontrast> kontrasti = new ArrayList<>();
        double standardDeviation = Math.sqrt(izracunajSse()/(brojAlternativa * (brojMjerenja - 1))) * Math.sqrt(2.0 / (brojAlternativa * brojMjerenja));
        double coeficient = new TDistribution(brojAlternativa * (brojMjerenja - 1)).inverseCumulativeProbability(0.95);
        for (int[] elems : new Combinations(brojAlternativa, 2)) {
            double c = Double.valueOf(arrayOfEffects[elems[0]].getText()) - Double.valueOf(arrayOfEffects[elems[1]].getText());
            double intPov1 = c - coeficient * standardDeviation;
            double intPov2 = c + coeficient * standardDeviation;
            Kontrast kontrast = new Kontrast(elems[0], elems[1], intPov1, intPov2);
            kontrasti.add(kontrast);
        }
        return kontrasti;
    }

    private double izracunajSse() {
        double s = 0;
        for (int i = 0; i < brojAlternativa; i++) {
            for (int j = 0; j < brojMjerenja; j++) {
                s += Math.pow((Double.valueOf(matrixNodes[i][j].getText()) - Double.valueOf(arrayOfAverages[i].getText())), 2);
            }
        }
        return s;
    }

    private double izracunajSsa() {
        double s = 0;
        for (int i = 0; i < brojAlternativa; i++) {
            s += Math.pow(Double.valueOf(arrayOfEffects[i].getText()), 2);
        }
        return brojMjerenja * s;
    }

    private void initOnChange() {
        for (int i = 0; i < brojAlternativa; i++) {
            for (int j = 0; j < brojMjerenja; j++) {
                TextField textField = matrixNodes[i][j];
                textField.setOnKeyTyped(e -> {
                    String text = textField.getText();
                    if (text.matches("^[0-9]+(\\.[0-9]+)?$")) {
                        textField.setBorder(new Border(new BorderStroke(Color.TRANSPARENT, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
                    } else {
                        textField.setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
                        return;
                    }
                    izracunaj();
                });
            }
        }
    }

    public void back() {
        Parent root = null;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/page.fxml"));
        PageController controller = new PageController(primaryStage);
        loader.setController(controller);
        try {
            root = loader.load();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        primaryStage.setScene(new Scene(root, 800, 450));
        primaryStage.show();
    }

    private void initTable() {
        GridPane gridPane = new GridPane();
        gridPane.addColumn(brojAlternativa + 1);
        gridPane.addRow(brojMjerenja + 3);
        for (int i = 0; i < brojAlternativa + 1; i++) {
            for (int j = 0; j < brojMjerenja + 1; j++) {
                if (i == 0) {
                    Label label = new Label();
                    if (j == 0) {
                        label.setText("Mjerenja / Alternative");
                    } else
                        label.setText(String.valueOf(j));
                    label.setAlignment(Pos.CENTER);
                    VBox vBox = new VBox();
                    vBox.setAlignment(Pos.CENTER);
                    vBox.getChildren().add(label);
                    gridPane.getChildren().add(vBox);
                    gridPane.setColumnIndex(vBox, i);
                    gridPane.setRowIndex(vBox, j);
                } else if (j == 0) {
                    Label label = new Label();
                    label.setText(String.valueOf(i));
                    //label.setBackground(new Background(new BackgroundFill(Color.BLUE, CornerRadii.EMPTY, Insets.EMPTY)));
                    label.setTextAlignment(TextAlignment.CENTER);
                    label.setAlignment(Pos.CENTER);
                    VBox vBox = new VBox();
                    vBox.setAlignment(Pos.CENTER);
                    vBox.getChildren().add(label);
                    gridPane.getChildren().add(vBox);
                    gridPane.setColumnIndex(vBox, i);
                    gridPane.setRowIndex(vBox, j);
                } else {
                    TextField textField = new TextField();
                    textField.setText(String.valueOf(0));
                    textField.setAlignment(Pos.CENTER);
                    textField.setOnInputMethodTextChanged(e -> {
                        izracunaj();
                    });
                    VBox vBox = new VBox();
                    vBox.setAlignment(Pos.CENTER);
                    vBox.getChildren().add(textField);
                    gridPane.getChildren().add(vBox);
                    gridPane.setColumnIndex(vBox, i);
                    gridPane.setRowIndex(vBox, j);
                    matrixNodes[i - 1][j - 1] = textField;
                }

            }

        }

        Label label1 = new Label();
        label1.setText("Srednja vrijednost kolone");
        label1.setTextAlignment(TextAlignment.CENTER);
        label1.setAlignment(Pos.CENTER);
        VBox vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);
        vBox.getChildren().add(label1);
        gridPane.getChildren().add(vBox);
        gridPane.setColumnIndex(vBox, 0);
        gridPane.setRowIndex(vBox, brojMjerenja + 1);
        Label label2 = new Label();
        label2.setText("Efekat");
        label2.setTextAlignment(TextAlignment.CENTER);
        label2.setAlignment(Pos.CENTER);
        VBox vBox1 = new VBox();
        vBox1.setAlignment(Pos.CENTER);
        vBox1.getChildren().add(label2);
        gridPane.getChildren().add(vBox1);
        gridPane.setColumnIndex(vBox1, 0);
        gridPane.setRowIndex(vBox1, brojMjerenja + 2);

        for (int i = 1; i < brojAlternativa + 1; i++) {
            Label label = new Label();
            label.setText("0");
            label.setTextAlignment(TextAlignment.CENTER);
            label.setAlignment(Pos.CENTER);
            VBox vBox2 = new VBox();
            vBox2.setAlignment(Pos.CENTER);
            vBox2.getChildren().add(label);
            gridPane.getChildren().add(vBox2);
            gridPane.setColumnIndex(vBox2, i);
            gridPane.setRowIndex(vBox2, brojMjerenja + 1);
            arrayOfAverages[i - 1] = label;
        }

        for (int i = 1; i < brojAlternativa + 1; i++) {
            Label label = new Label();
            label.setText("0");
            label.setTextAlignment(TextAlignment.CENTER);
            label.setAlignment(Pos.CENTER);
            VBox vBox2 = new VBox();
            vBox2.setAlignment(Pos.CENTER);
            vBox2.getChildren().add(label);
            gridPane.getChildren().add(vBox2);
            gridPane.setColumnIndex(vBox2, i);
            gridPane.setRowIndex(vBox2, brojMjerenja + 2);
            arrayOfEffects[i - 1] = label;
        }
        Double prosjek;
        /*for (int i = 0; i < brojAlternativa; i++) {
            Label label = new Label();
            for (int j = 0; j < brojAlternativa; j++) {
                for (int c = 0; c < brojMjerenja; c++)
                    s += matrixNodes[c][j];
            }
            prosjek = s / brojMjerenja;
            label.setText(prosjek.toString());
            VBox vBox2 = new VBox();
            vBox2.setAlignment(Pos.CENTER);
            vBox2.getChildren().add(label);
            gridPane.getChildren().add(vBox2);
            gridPane.setColumnIndex(vBox2, i + 1);
            gridPane.setRowIndex(vBox2, brojMjerenja + 1);
        }*/
        scrollPane.setContent(gridPane);
    }


    private void izracunaj() {
        izracunajAritmetickeSredine();
        izracunajEfekte();
    }

    private double izracunajUkupnuAritmetickuSredinu() {
        double s = 0;
        for (int i = 0; i < brojAlternativa; i++) {
            for (int j = 0; j < brojMjerenja; j++) {
                s += Double.valueOf(matrixNodes[i][j].getText());
            }
        }
        return s / (brojMjerenja * brojAlternativa);
    }

    private void izracunajEfekte() {
        for (int i = 0; i < brojAlternativa; i++) {
            double s = 0;
            for (int j = 0; j < brojMjerenja; j++) {
                s += Double.valueOf(matrixNodes[i][j].getText());
                arrayOfEffects[i].setText(String.valueOf(zaokruzi(s / brojMjerenja - izracunajUkupnuAritmetickuSredinu(),2)));
            }
        }
    }

    private void izracunajAritmetickeSredine() {
        for (int i = 0; i < brojAlternativa; i++) {
            double s = 0;
            for (int j = 0; j < brojMjerenja; j++) {
                s += Double.valueOf(matrixNodes[i][j].getText());
                arrayOfAverages[i].setText(String.valueOf(zaokruzi(s / brojMjerenja,2)));
            }
        }
    }
}

