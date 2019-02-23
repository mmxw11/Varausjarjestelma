package varausjarjestelma.domain.serialization.parser;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

/**
 * Tämän annotaation avulla voidaan määrittää muuttujille lisätietoa.
 * 
 * @author Matias
 */
@Target(value = ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SarakeAsetukset {

    /**
     * Palauttaa tietokannassa muuttujaa vastaavan sarakkeen nimen.
     * Jos tyhjä, niin oletuksena käytetään {@link Field#getName()}-metodin palauttamaa arvoa.
     * @return String
     */
    String columnName() default "";

    /**
     * Palauttaa sarakkeen tyypin. 
     * Oletusarvo: {@link varausjarjestelma.domain.serialization.parser.SarakeTyyppi#NORMAL}
     * @return SarakeTyyppi
     */
    SarakeTyyppi tyyppi() default SarakeTyyppi.NORMAL;
}