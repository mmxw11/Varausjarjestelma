package varausjarjestelma.domain.serialization;

import java.lang.annotation.ElementType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;


/**
 * @author Matias
 */
@Target(value = ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SarakeAsetukset {

    /**
     * Palauttaa tietokannassa muuttujaa vastaan sarakkeen nimen
     * Jos tyhjä, niin oletuksena käytetään {@link Field#getName()}-metodin palauttamaa arvoa.
     * @return string
     */
    String columnName() default "";

    /**
     * Palauttaa sarakkeen tyypin. Oletusarvo: SarakeTyyppi.NORMAL
     * @see varausjarjestelma.domain.serialization.SarakeTyyppi
     */
    SarakeTyyppi tyyppi() default SarakeTyyppi.NORMAL;
}
