package varausjarjestelma.domain.serialization;

/**
 * @author Matias
 */
public enum SarakeTyyppi {
    /**
     * Vastaa normaalia taulussa olevaa saraketta.
     */
    NORMAL,
    /**
     * Vastaa dynaamisesti luotuja sarakkeita, joita ei talleneta tietokantaan, 
     * mutta kysely kuitenkin palautaa. Tälläisiä olisi esim. COUNT, SUM jne.
     */
    DYNAMICALLY_GENERATED,
    /**
     * Viiteavain toiseen tauluun.
     */
    FOREIGN_KEY;
}