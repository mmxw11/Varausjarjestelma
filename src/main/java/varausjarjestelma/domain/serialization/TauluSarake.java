package varausjarjestelma.domain.serialization;

import varausjarjestelma.domain.serialization.parser.SarakeTyyppi;

public class TauluSarake {

    private String fetchTarget;
    private SarakeTyyppi tyyppi;
    private Class<?> targetClass;
    private String fieldName;

    public TauluSarake(String fetchTarget, SarakeTyyppi tyyppi, Class<?> targetClass, String fieldName) {
        this.fetchTarget = fetchTarget;
        this.tyyppi = tyyppi;
        this.targetClass = targetClass;
        this.fieldName = fieldName;
    }

    /**
     * @return Palauttaa SQL-kyselyyn tarvittavan sarakkeen tai lauseen (jos yhteenvetokysely)
     */
    public String getFetchTarget() {
        return fetchTarget;
    }

    public SarakeTyyppi getTyyppi() {
        return tyyppi;
    }

    /**
     * @return Palauttaa luokan jossa kyseinen muuttuja sijaitsee
     */
    public Class<?> getTargetClass() {
        return targetClass;
    }

    /**
     * @return Palauttaa muuttujan nimen
     */
    public String getFieldName() {
        return fieldName;
    }

    @Override
    public String toString() {
        return "TauluSarake [fetchTarget=" + fetchTarget + ", tyyppi=" + tyyppi + ", targetClass=" + targetClass + ", fieldName=" + fieldName + "]";
    }
}