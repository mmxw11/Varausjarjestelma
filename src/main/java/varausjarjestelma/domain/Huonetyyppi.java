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

    public void setId(int id) {
        // Tämä metodi on vain Springia varten.
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
        return "Huonetyyppi [id=" + id + ", tyyppi=" + tyyppi + "]";
    }
}