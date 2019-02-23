package varausjarjestelma.domain.serialization;

import varausjarjestelma.domain.serialization.parser.ParsedMuuttuja;

/**
 * Määrittää miten muuttuja pitäisi käsitellä.
 * @see varausjarjestelma.domain.serialization.parser.LuokkaParser
 * 
 * @author Matias
 */
@FunctionalInterface
public interface MuuttujaSerializer<T> {

    Object serializeField(T data, ParsedMuuttuja pmuuttuja);
}