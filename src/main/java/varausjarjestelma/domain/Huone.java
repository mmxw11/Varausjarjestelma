package varausjarjestelma.domain;

public class Huone {

    private int id;
    private Huonetyyppi huonetyyppi;
    private int huonenumero;
    private double paivahinta;

    public Huone() {}

    public int getId() {
        return id;
    }

    public Huonetyyppi getHuonetyyppi() {
        return huonetyyppi;
    }

    public int getHuonenumero() {
        return huonenumero;
    }

    public double getPaivahinta() {
        return paivahinta;
    }
}