package varausjarjestelma.domain;

public class Asiakas {

    private int id;
    private String etunimi;
    private String sukunimi;
    private String puhelinnumero;
    private String sahkopostiosoite;

    public Asiakas() {}

    public int getId() {
        return id;
    }

    public String getEtunimi() {
        return etunimi;
    }

    public String getSukunimi() {
        return sukunimi;
    }

    public String getPuhelinnumero() {
        return puhelinnumero;
    }

    public String getSahkopostiosoite() {
        return sahkopostiosoite;
    }
}