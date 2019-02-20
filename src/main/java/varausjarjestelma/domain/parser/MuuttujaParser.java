package varausjarjestelma.domain.parser;

/**
 * Luokan muuttujille voidaan lisätä kustomoitu parser, 
 * jonka avulla voidaan määrittää miten se pitäisi käsitellä.
 * @see varausjarjestelma.domain.parser.LuokkaDataParser
 * 
 * @author Matias
 */
@FunctionalInterface
public interface MuuttujaParser {

    Object parseField(Object data);
}