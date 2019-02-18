package varausjarjestelma.domain;

public class Huone {

    private int huonenumero;
    private Huonetyyppi huonetyyppi;
    private double paivahinta;

    public Huone() {}

    public int getHuonenumero() {
        return huonenumero;
    }

    public Huonetyyppi getHuonetyyppi() {
        return huonetyyppi;
    }

    public double getPaivahinta() {
        return paivahinta;
    }
}