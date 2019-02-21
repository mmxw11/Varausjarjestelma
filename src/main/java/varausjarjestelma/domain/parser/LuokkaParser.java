package varausjarjestelma.domain.parser;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tämän luokan tehtävä on noutaa luokista tehdyistä olioista
 * niiden muuttujat ja muttujien arvot käyttäen Reflectionia.
 * Haluttaessa joitain muuttujia voidaan jättää myös pois tai tiettyjen muuttujien
 * kohdalla voidaan valita miten ne pitäisi käsitellä.
 * 
 * Luokissa muuttujilla on samat nimet kuin tietokannassa, joten lisäys- ja päivityskyselyiden tekeminen
 * onnistuu näin paljon helpommin, koska sarakkeet ja niiden arvot voidaan hakea dynaamisesti.
 * 
 * @author Matias   
 */
public class LuokkaParser {

    private Map<String, MuuttujaParser> parsers;

    public LuokkaParser() {
        this.parsers = new HashMap<>();
    }

    /**
     * Lisää tietylle muuttujalle parserin, joka määrittää miten se pitäisi käsitellä.
     * @param fieldName
     * @param muuttujaParser Muuttujan käsittelijä. Mikäli parametriksi syötetään null,
     * niin muuttuja jätetään ulkopuolelle
     */
    public void addMuuttujaParser(String fieldName, MuuttujaParser muuttujaParser) {
        parsers.put(fieldName, muuttujaParser);
    }

    /**
     * Noutaa oliosta sen muuttujat ja niiden arvot.
     * Alla olevana hajautustauluna toimii LinkedHashMap, joka pitää huolen siitä,
     * että palautettavat muuttujat ovat aina oikeassa järjestyksessä, 
     * joka on erittäin tärkeää SQL-kyselyjen vuoksi.
     * @param classInstance
     * @return Palauttaa olion muuttujat ja arvot hajautustauluna
     */
    public Map<String, Object> parseClassFields(Object classInstance) {
        Map<String, Object> fields = new LinkedHashMap<>(); // Pidetään järjestys oikeana.
        for (Field field : getAllFields(classInstance.getClass())) {
            String name = field.getName();
            MuuttujaParser muuttujaParser = parsers.get(name);
            if (muuttujaParser == null && parsers.containsKey(name)) {
                continue;
            }
            Object value = getFieldValue(field, classInstance);
            if (muuttujaParser != null) {
                value = muuttujaParser.parseField(value);
            }
            fields.put(name, value);
        }
        return fields;
    }

    /**
     * Asettaa muuttujan arvon.
     * @param fieldName
     * @param value
     * @param classInstance
     */
    public void setField(String fieldName, Object value, Object classInstance) {
        Field field = getAllFields(classInstance.getClass()).stream().filter(f -> f.getName().equals(fieldName)).findAny().orElse(null);
        if (field == null) {
            throw new IllegalArgumentException("Muuttujaa \"" + fieldName + "\" ei löytynyt!");
        }
        field.setAccessible(true);
        try {
            field.set(classInstance, value);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            // Palauta runtime-virhe, koska ne voidaan heti korjata kehitysvaiheessa,
            // koska ne eivät johdu ns. ulkoisista tekijöistä.
            throw new RuntimeException(e);
        }
    }

    /**
     * Palauttaa luokassa olevat muuttujat.
     * Metodi ottaa myös mukaan yliluokissa olevat muuttujat.
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