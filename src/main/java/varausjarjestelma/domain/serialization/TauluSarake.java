package varausjarjestelma.domain.serialization;

import varausjarjestelma.domain.serialization.parser.SarakeTyyppi;

/**
 * Tallentaa SELECT-kyselyyn vaadittavat sarakkeen tiedot.
 * 
 * @author Matias
 */
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
     * @return Palauttaa SQL-kyselyn sarakkeen hakemiseen tarvittavan hakustrategian. 
     * Yleensä sarakkeen nimi tai lause, kuten SUM(...), COUNT(...) (jos yhteenvetokysely)
     */
    public String getQueryStrategy() {
        return queryStrategy;
    }

    /**
     * @return Palautaa sarakkeen tyypin
     */
    public SarakeTyyppi getTyyppi() {
        return tyyppi;
    }

    /**
     * @return Palauttaa luokan, jossa kyseinen muuttuja sijaitsee
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