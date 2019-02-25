package varausjarjestelma.domain;

import varausjarjestelma.domain.serialization.parser.SarakeAsetukset;
import varausjarjestelma.domain.serialization.parser.SarakeTyyppi;

/**
 * @author Matias
 */
public class Lisavarustetyyppi {

    private int id;
    private String varustetyyppi;
    @SarakeAsetukset(tyyppi = SarakeTyyppi.DYNAMICALLY_GENERATED)
    private int lisavarustemaara;

    public Lisavarustetyyppi() {
        this.id = -1;
    }

    public Lisavarustetyyppi(String varustetyyppi) {
        this.id = -1;
        this.varustetyyppi = varustetyyppi;
        this.lisavarustemaara = -1;
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

    public void setVarustetyyppi(String varustetyyppi) {
        this.varustetyyppi = varustetyyppi;
    }

    /**
     * Asettaa kuinka monta tätä lisävarustetta on varauksissa.
     * @param lisavarustesmaara
     */
    public void setLisavarustemaara(int lisavarustemaara) {
        this.lisavarustemaara = lisavarustemaara;
    }

    public int getId() {
        return id;
    }

    public String getVarustetyyppi() {
        return varustetyyppi;
    }

    public int getLisavarustemaara() {
        return lisavarustemaara;
    }

    @Override
    public String toString() {
        return varustetyyppi + ", " + lisavarustemaara + (lisavarustemaara > 1 ? " varausta" : " varaus");
    }
}