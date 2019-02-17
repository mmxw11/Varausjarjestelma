package varausjarjestelma.domain;

public class Asiakas {

    private int id;
    private String nimi;
    private String puhelinnumero;
    private String sahkopostiosoite;

    public Asiakas() {}

    public int getId() {
        return id;
    }

    public String getNimi() {
        return nimi;
    }

    public String getPuhelinnumero() {
        return puhelinnumero;
    }

    public String getSahkopostiosoite() {
        return sahkopostiosoite;
    }
}