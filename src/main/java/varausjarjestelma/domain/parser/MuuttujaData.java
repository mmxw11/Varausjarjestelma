package varausjarjestelma.domain.parser;

/**
 * @author Matias
 */
public class MuuttujaData {

    private String name;
    private Object data;

    public MuuttujaData(String name, Object data) {
        this.name = name;
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public Object getData() {
        return data;
    }

    @Override
    public String toString() {
        return "MuuttujaData[name=" + name + ", data=" + data + "]";
    }
}