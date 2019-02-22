package varausjarjestelma.domain.builder;

/**
 * Luokan muuttujille voidaan lisätä erillinen parser, 
 * jonka avulla voidaan määrittää miten ne pitäisi käsitellä.
 * @see varausjarjestelma.domain.builder.LuokkaParser
 * 
 * @author Matias
 */
@FunctionalInterface
public interface MuuttujaParser<T> {

    Object parseField(T data);
}