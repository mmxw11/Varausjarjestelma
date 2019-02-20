package varausjarjestelma.domain.parser;

/**
 * @author Matias
 */
public class Muuttuja {

    private String name;
    private Object data;

    public Muuttuja(String name, Object data) {
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
        return "Muuttuja[name=" + name + ", data=" + data + "]";
    }
}