package varausjarjestelma.domain.serialization;

import java.sql.ResultSet;

/**
 * Tallentaa luokasta jäsennetyn muuttujan tiedot.
 * 
 * @author Matias
 *
 * @param <K>
 */
public class ParsedMuuttuja<K> {

    private String fieldName;
    private String remappedName;
    private SarakeTyyppi tyyppi;
    private MuuttujaSerializer<K> serializer;
    private MuuttujaSerializer<ResultSet> deserializer;

    public ParsedMuuttuja(String fieldName, SarakeTyyppi tyyppi) {
        this.fieldName = fieldName;
        this.tyyppi = tyyppi;
    }

    public void setRemappedName(String remappedName) {
        this.remappedName = remappedName;
    }

    public void setSerializer(MuuttujaSerializer<K> serializer) {
        this.serializer = serializer;
    }

    public void setDeserializer(MuuttujaSerializer<ResultSet> deserializer) {
        this.deserializer = deserializer;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getRemappedName() {
        return remappedName;
    }

    public SarakeTyyppi getTyyppi() {
        return tyyppi;
    }

    public MuuttujaSerializer<K> getSerializer() {
        return serializer;
    }

    public MuuttujaSerializer<ResultSet> getDeserializer() {
        return deserializer;
    }

    @Override
    public String toString() {
        return "ParsedMuuttuja [fieldName=" + fieldName + ", remappedName=" + remappedName + ", tyyppi=" + tyyppi
                + ", customSerializer=" + (serializer != null) + ", customDeSeserializer=" + (deserializer != null) + "]";
    }
}