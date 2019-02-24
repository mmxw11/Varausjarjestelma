package varausjarjestelma.domain;

/**
 * @author Matias
 */
public class Huonetyyppi {

    private int id;
    private String tyyppi;

    public Huonetyyppi() {
        this.id = -1;
    }

    public Huonetyyppi(String tyyppi) {
        this.id = -1;
        this.tyyppi = tyyppi;
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

    public void setTyyppi(String tyyppi) {
        this.tyyppi = tyyppi;
    }

    public int getId() {
        return id;
    }

    public String getTyyppi() {
        return tyyppi;
    }

    @Override
    public String toString() {
        return tyyppi;
    }
}