package varausjarjestelma.database.dao.parser;

/**
 * @author Matias
 */
public class MuuttujaData {

    private String nimi;
    private Object data;

    public MuuttujaData(String nimi, Object data) {
        this.nimi = nimi;
        this.data = data;
    }

    public String getNimi() {
        return nimi;
    }

    public Object getData() {
        return data;
    }

    @Override
    public String toString() {
        return "MuuttujaData[nimi=" + nimi + ", data=" + data + "]";
    }
}