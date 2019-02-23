package varausjarjestelma.domain.serialization;

/**
 * Määrittää miten muuttuja pitäisi käsitellä.
 * @see varausjarjestelma.domain.serialization.LuokkaParser
 * 
 * @author Matias
 */
@FunctionalInterface
public interface MuuttujaSerializer<T> {

    Object serializeField(T data, ParsedMuuttuja<?> pmuuttuja);
}