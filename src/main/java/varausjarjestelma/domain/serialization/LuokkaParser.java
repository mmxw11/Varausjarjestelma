package varausjarjestelma.domain.serialization;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tämän luokan tehtävä on selvittää POJO-luokista muuttujat käyttäen Reflectionia.
 * 
 * POJO-luokat vastaavat muuttujiltaa tietokantatauluja, ja muuttujien kohdalla 
 * voidaan määrittää miten ne pitäisi käsitellä. Tällöin voidaan siis rakentaa SQL-kyselyjä dynaamisesti.
 * 
 * @author Matias   
 */
public class LuokkaParser<T> {

    private Map<String, ParsedMuuttuja> parsedFields;

    public LuokkaParser(Class<T> resultClass) {
        this.parsedFields = new LinkedHashMap<>();
    }

    private void parseResultClass(Class<T> resultClass) {}

    /**
     * Palauttaa luokassa olevat muuttujat.
     * Metodi ottaa myös mukaan yliluokat.
     * @param type
     * @return palauttaa listan luokan muuttujista 
     */
    private List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        if (type.getSuperclass() != null) {
            fields.addAll(getAllFields(type.getSuperclass()));
        }
        fields.addAll(Arrays.asList(type.getDeclaredFields()));
        return fields;
    }

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