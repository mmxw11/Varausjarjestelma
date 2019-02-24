package varausjarjestelma.domain;

/**
 * @author Matias
 */
public class Lisavarustetyyppi {

    private int id;
    private String varustetyyppi;

    public Lisavarustetyyppi() {
        this.id = -1;
    }

    public Lisavarustetyyppi(String varustetyyppi) {
        this.id = -1;
        this.varustetyyppi = varustetyyppi;
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

    public int getId() {
        return id;
    }

    public String getVarustetyyppi() {
        return varustetyyppi;
    }

    @Override
    public String toString() {
        return "Lisavarustetyyppi [id=" + id + ", varustetyyppi=" + varustetyyppi + "]";
    }
}