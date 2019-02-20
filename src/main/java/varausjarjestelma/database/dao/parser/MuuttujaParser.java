package varausjarjestelma.database.dao.parser;

/**
 * Luokan muuttujille voidaan lisätä kustomoitu parser, 
 * jonka avulla voidaan määrittää miten se pitäisi käsitellä.
 * @see varausjarjestelma.database.dao.parser.LuokkaDataParser
 * 
 * @author Matias
 */
@FunctionalInterface
public interface MuuttujaParser {

    Object parseField(Object data);
}