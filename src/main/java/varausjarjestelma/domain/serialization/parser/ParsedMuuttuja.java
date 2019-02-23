package varausjarjestelma.domain.serialization.parser;

import java.lang.reflect.Field;

/**
 * Tallentaa luokasta jäsennetyn muuttujan tiedot.
 * 
 * @author Matias
 */
public class ParsedMuuttuja {

    private Field field;
    private String remappedName;
    private SarakeTyyppi tyyppi;

    public ParsedMuuttuja(Field field, SarakeTyyppi tyyppi) {
        this.field = field;
        this.tyyppi = tyyppi;
    }

    public void setRemappedName(String remappedName) {
        this.remappedName = remappedName;
    }

    public Field getField() {
        return field;
    }

    public String getRemappedName() {
        return remappedName;
    }

    public SarakeTyyppi getTyyppi() {
        return tyyppi;
    }

    @Override
    public String toString() {
        return "ParsedMuuttuja [field=" + field.getName() + ", remappedName=" + remappedName + ", tyyppi=" + tyyppi + "]";
    }
}