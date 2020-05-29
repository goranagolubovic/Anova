package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class PageController implements Initializable {
    @FXML
    private TextField brojAlternativaTextField;
    @FXML
    private TextField brojMjerenjaTextField;
    @FXML
    private Button pokreniButton;
    private Stage primaryStage;

    public PageController(Stage primaryStage) {

        this.primaryStage = primaryStage;
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
      /*  pokreniButton.setOnAction(e-> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Rezultati izvrsavanja:");
            alert.setHeaderText("Look, an Information Dialog");

            String s="";
            s+= "Broj alternativa: "+ brojAlternativaTextField.getText()+System.lineSeparator();
            s+= "Broj mjerenja: "+ brojMjerenjaTextField.getText()+System.lineSeparator();
            alert.setContentText(s);
            alert.showAndWait().ifPresent(rs -> {
                if (rs == ButtonType.OK) {
                    System.out.println("Pressed OK.");
                }
            });
        });*/

        pokreniButton.setOnAction(e -> {
            String brAl = brojAlternativaTextField.getText();
            String brMj = brojMjerenjaTextField.getText();
            String title = "";
            String headerText = "";
            String contentText = "";
            if (!brAl.matches("[1-9][0-9]*") || !brMj.matches("[1-9][0-9]*")) {
                if (!brAl.matches("[1-9][0-9]*") && !brMj.matches("[1-9][0-9]*")) {
                    title = "Rezultati izvrsavanja:";
                    headerText = "Greska!";
                    contentText = "Niste unijeli tacan broj alternativa i mjerenja.";
                } else if (!brAl.matches("[1-9][0-9]*")) {
                    title = "Rezultati izvrsavanja:";
                    headerText = "Greska!";
                    contentText = "Niste unijeli tacan broj alternativa.";
                } else if (!brMj.matches("[1-9][0-9]*")) {
                    title = "Rezultati izvrsavanja:";
                    headerText = "Greska!";
                    contentText = "Niste unijeli tacan broj mjerenja.";
                }
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle(title);
                alert.setHeaderText(headerText);
                alert.setContentText(contentText);
                alert.showAndWait();
                return;
            }

            Parent root = null;
            try {
                var loader = new FXMLLoader(getClass().getResource("/view/podaci.fxml"));
                var controller = new PodaciController(primaryStage, Integer.valueOf(brAl), Integer.valueOf(brMj));
                loader.setController(controller);
                root = loader.load();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            primaryStage.setScene(new Scene(root, 800, 450));
            primaryStage.show();
        });
    }
}
