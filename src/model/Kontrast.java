package model;

import java.util.List;

public class Kontrast {
    private int redBroj1, redBroj2;
    private double intPov1, intPov2;

    public Kontrast(int redBroj1, int redBroj2, double intPov1, double intPov2) {
        this.redBroj1 = redBroj1;
        this.redBroj2 = redBroj2;
        this.intPov1 = intPov1;
        this.intPov2 = intPov2;
    }

    public int getRedBroj1() {
        return redBroj1;
    }

    public void setRedBroj1(int redBroj1) {
        this.redBroj1 = redBroj1;
    }

    public int getRedBroj2() {
        return redBroj2;
    }

    public void setRedBroj2(int redBroj2) {
        this.redBroj2 = redBroj2;
    }

    public double getIntPov1() {
        return intPov1;
    }

    public void setIntPov1(double intPov1) {
        this.intPov1 = intPov1;
    }

    public double getIntPov2() {
        return intPov2;
    }

    public void setIntPov2(double intPov2) {
        this.intPov2 = intPov2;
    }

    @Override
    public String toString() {
        return "Kontrast[" + (redBroj1 + 1) +
                ", " + (redBroj2 + 1) +
                "] = {" + String.format("%.2f", intPov1) +
                ", " + String.format("%.2f", intPov2) +
                "}";
    }

    public static String ispis(List<Kontrast> list) {
        String s = "Kontrasti:" + System.lineSeparator() + System.lineSeparator();
        for (Kontrast k : list) {
            s += k.toString() + System.lineSeparator();
        }
        return s;
    }

}
