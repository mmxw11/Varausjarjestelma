package varausjarjestelma.domain.serialization;

import varausjarjestelma.domain.serialization.parser.SarakeTyyppi;

public class TauluSarake {

    private String queryStrategy;
    private SarakeTyyppi tyyppi;
    private Class<?> targetClass;
    private String fieldName;

    public TauluSarake(String queryStrategy, SarakeTyyppi tyyppi, Class<?> targetClass, String fieldName) {
        this.queryStrategy = queryStrategy;
        this.tyyppi = tyyppi;
        this.targetClass = targetClass;
        this.fieldName = fieldName;
    }

    /**
     * @return Palauttaa SQL-kyselyn sarakkeen hamiseen tarvittavan strategia. 
     * Yleensä sarakkeen nimi tai lause, kuten SUM(...) (jos yhteenvetokysely)
     */
    public String getQueryStrategy() {
        return queryStrategy;
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
        return "TauluSarake [queryStrategy=" + queryStrategy + ", tyyppi=" + tyyppi + ", targetClass=" + targetClass + ", fieldName=" + fieldName + "]";
    }
}