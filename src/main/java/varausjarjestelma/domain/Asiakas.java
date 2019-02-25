package varausjarjestelma.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

import varausjarjestelma.domain.serialization.parser.SarakeAsetukset;
import varausjarjestelma.domain.serialization.parser.SarakeTyyppi;

/**
 * @author Matias
 */
public class Asiakas {

    private int id;
    private String nimi;
    private String puhelinnumero;
    private String sahkopostiosoite;
    @SarakeAsetukset(tyyppi = SarakeTyyppi.DYNAMICALLY_GENERATED)
    private BigDecimal rahaaKaytetty;

    public Asiakas() {
        this.id = -1;
        this.rahaaKaytetty = new BigDecimal(-1);
    }

    public Asiakas(String nimi, String puhelinnumero, String sahkopostiosoite) {
        this.id = -1;
        this.nimi = nimi;
        this.puhelinnumero = puhelinnumero;
        this.sahkopostiosoite = sahkopostiosoite;
        this.rahaaKaytetty = new BigDecimal(-1);
    }

    /**
     * Asettaa taulukon pääavaimen.
     * Tämä metodi on tarkoitettu vain luokan rakentamista varten.
     * @param id
     */
    public void setId(int id) {
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

    /**
     * Asettaa asiakkaan varauksiin käyttämän rahamäärän. 
     * Määrä voi koostua korkeintaan 10:stä kokonaisluvusta ja se tallennetaan aina kahden desimaalin tarkkuudella.
     * @param rahaaKaytetty
     */
    public void setRahaaKaytetty(BigDecimal rahaaKaytetty) {
        if (rahaaKaytetty == null) {
            rahaaKaytetty = new BigDecimal(0);
        }
        this.rahaaKaytetty = rahaaKaytetty.setScale(2, RoundingMode.HALF_EVEN);
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

    public BigDecimal getRahaaKaytetty() {
        return rahaaKaytetty;
    }

    @Override
    public String toString() {
        String raha = null;
        if (rahaaKaytetty.intValue() != -1) {
            // Jätetään sentit (nollat) pois mikäli niitä ei ole.
            raha = rahaaKaytetty.stripTrailingZeros().toPlainString();
        }
        return nimi + ", " + sahkopostiosoite + ", " + puhelinnumero + (raha != null ? ", " + raha + " euroa" : "");
    }
}