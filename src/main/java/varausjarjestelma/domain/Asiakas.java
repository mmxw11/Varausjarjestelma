package varausjarjestelma.domain;

public class Asiakas {

    private int id;
    private String nimi;
    private String puhelinnumero;
    private String sahkopostiosoite;

    public Asiakas(String nimi, String puhelinnumero, String sahkopostiosoite) {
        this.id = -1;
        this.nimi = nimi;
        this.puhelinnumero = puhelinnumero;
        this.sahkopostiosoite = sahkopostiosoite;
    }

    public void setId(int id) {
        this.id = id;
    }

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