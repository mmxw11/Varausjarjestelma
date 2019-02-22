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

    private static class ParserVarasto<T> {

        private String remappedName;
        private Class<T> fieldType;
        private MuuttujaParser<T> muuttujaParser;

        public ParserVarasto(String remappedName, Class<T> fieldType) {
            this.remappedName = remappedName;
            this.fieldType = fieldType;
        }
    }

    private Map<String, ParserVarasto<?>> parsers;

    public LuokkaParser() {
        this.parsers = new HashMap<>();
    }

    /**
     * Lisää tietylle muuttujalle parserin, joka määrittää miten se pitäisi käsitellä.
     * @param fieldName Luokassa olevan sarakkeen nimi
     * @param remappedFieldName Uusi sarakkeen nimi, kuten tietokannassa olevan sarakkeen nimi
     * @param fieldType Luokassa olevan muuttujan tyyppi
     * @param muuttujaParser Muuttujan käsittelijä. Mikäli parametriksi syötetään null,
     *  niin muuttujaa ei oteta mukaan
     */
    public <T> void addMuuttujaParser(String fieldName, String remappedFieldName, Class<T> fieldType, MuuttujaParser<T> muuttujaParser) {
        ParserVarasto<T> varasto = new ParserVarasto<>(remappedFieldName, fieldType);
        varasto.muuttujaParser = muuttujaParser;
        parsers.put(fieldName, varasto);
    }

    /**
     * Noutaa oliosta sen muuttujat ja niiden arvot.
     * Alla olevana hajautustauluna toimii LinkedHashMap, joka pitää huolen siitä,
     * että palautettavat muuttujat ovat aina oikeassa järjestyksessä, 
     * joka on erittäin tärkeää SQL-kyselyjen vuoksi.
     * @param classInstance
     * @return Palauttaa olion muuttujat ja arvot hajautustauluna
     */
    @SuppressWarnings("unchecked")
    public <T> Map<String, Object> parseClassFields(Object classInstance) {
        Map<String, Object> fields = new LinkedHashMap<>(); // Pidetään järjestys oikeana.
        for (Field field : getAllFields(classInstance.getClass())) {
            String fieldName = field.getName();
            ParserVarasto<T> parserVarasto = (ParserVarasto<T>) parsers.get(fieldName);
            if (parserVarasto != null && parserVarasto.muuttujaParser == null) {
                continue;
            }
            Object value = getFieldValue(field, classInstance);
            if (parserVarasto != null) {
                T cvalue = value != null ? parserVarasto.fieldType.cast(value) : null;
                value = parserVarasto.muuttujaParser.parseField(cvalue);
                fieldName = parserVarasto.remappedName;
            }
            fields.put(fieldName, value);
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