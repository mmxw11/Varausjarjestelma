package varausjarjestelma.domain.serialization;

import java.lang.reflect.Field;
import java.util.Map;

import varausjarjestelma.domain.serialization.parser.LuokkaParser;
import varausjarjestelma.domain.serialization.parser.ParsedMuuttuja;

public class LuokkaSerializer<T> {

    private LuokkaParser<T> parser;

    public LuokkaSerializer(Class<T> resultClass) {
        this.parser = new LuokkaParser<>(resultClass);
    }

    /**
     * Serialisoi olion muuttujat sopivaksi tietokanta luonti- ja päivityskyselyhin.
     * Alla olevana hajautustauluna toimii LinkedHashMap, joka pitää huolen siitä,
     * että palautettavat muuttujat ovat aina oikeassa järjestyksessä, 
     * joka on erittäin tärkeää SQL-kyselyjen kannalta.
     * @param instance
     * @return Palauttaa olion muuttujat
     */
    public Map<String, Object> serializeObject(T instance) {
        for (ParsedMuuttuja muuttuja : parser.getMuuttujat()) {
            System.out.println("serialisoidaan: " + muuttuja);
            // TODO: WIP
        }
        return null;
    }

    // TODO: GENERATE UPDATE QUERY!
    /**
     * Noutaa muuttujan arvon.
     * @param field
     * @param instance
     * @return muuttujan arvo
     */
    private Object getFieldValue(Field field, Object instance) {
        field.setAccessible(true);
        Object value = null;
        try {
            value = field.get(instance);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            // Palauta runtime-virhe, koska ne voidaan heti korjata kehitysvaiheessa,
            // koska ne eivät johdu ns. ulkoisista tekijöistä.
            throw new RuntimeException(e);
        }
        return value;
    }
}