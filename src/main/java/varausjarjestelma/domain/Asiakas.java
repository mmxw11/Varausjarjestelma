package varausjarjestelma.domain;

/**
 * @author Matias
 */
public class Asiakas {

    private int id;
    private String nimi;
    private String puhelinnumero;
    private String sahkopostiosoite;

    public Asiakas() {
        this.id = -1;
    }

    public Asiakas(String nimi, String puhelinnumero, String sahkopostiosoite) {
        this.id = -1;
        this.nimi = nimi;
        this.puhelinnumero = puhelinnumero;
        this.sahkopostiosoite = sahkopostiosoite;
    }

    public void setId(int id) {
        // Tämä metodi on vain Springia varten.
        if (this.id != -1) {
            throw new RuntimeException("Pääavainta ei voi muuttaa!");
        }
        this.id = id;
    }

    public void setNimi(String nimi) {
        this.nimi = nimi;
    }

    public void setPuhelinnumero(String puhelinnumero) {
        this.puhelinnumero = puhelinnumero;
    }

    public void setSahkopostiosoite(String sahkopostiosoite) {
        this.sahkopostiosoite = sahkopostiosoite;
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

    @Override
    public String toString() {
        return "Asiakas [id=" + id + ", nimi=" + nimi + ", puhelinnumero=" + puhelinnumero + ", sahkopostiosoite=" + sahkopostiosoite + "]";
    }
}