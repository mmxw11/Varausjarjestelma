package varausjarjestelma.domain.parser;

/**
 * Luokan muuttujille voidaan lisätä erillinen parser, 
 * jonka avulla voidaan määrittää miten ne pitäisi käsitellä.
 * @see varausjarjestelma.domain.parser.LuokkaParser
 * 
 * @author Matias
 */
@FunctionalInterface
public interface MuuttujaParser<T> {

    Object parseField(T data);
}