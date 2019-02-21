package varausjarjestelma.domain;

/**
 * @author Matias
 */
public class Lisavaruste {

    private int id;
    private String varuste;

    public Lisavaruste() {
        this.id = -1;
    }

    public Lisavaruste(String varuste) {
        this.id = -1;
        this.varuste = varuste;
    }

    public void setId(int id) {
        if (this.id != -1) {
            throw new RuntimeException("Pääavainta ei voi muuttaa!");
        }
        this.id = id;
    }

    public void setVaruste(String varuste) {
        this.varuste = varuste;
    }

    public int getId() {
        return id;
    }

    public String getVaruste() {
        return varuste;
    }

    @Override
    public String toString() {
        return "Lisavaruste [id=" + id + ", varuste=" + varuste + "]";
    }
}