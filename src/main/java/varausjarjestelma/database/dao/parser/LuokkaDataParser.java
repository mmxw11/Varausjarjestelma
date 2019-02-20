package varausjarjestelma.database.dao.parser;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tämän luokan tehtävä on noutaa loukista tehdyistä olioista
 * niiden muuttujat ja muttujien arvot käyttäen Reflectionia.
 * Haluttaessa joitain muuttujia voidaan jättää myös pois tai tiettyjen muuttujien
 * kohdalla voidaan valita miten ne pitäisi käsitellä.
 * 
 * Luokissa muuttujilla on samat nimet kuin tietokannassa, joten lisäys ja päivityskyselyjen teko
 * onnistuu näin paljon nopempaa, koska sarakkeet ja niiden arvot voidaan dynaamisesti generoida.
 * 
 * @author Matias   
 */
public class LuokkaDataParser {

    private Map<String, MuuttujaParser> parsers;

    public LuokkaDataParser() {
        this.parsers = new HashMap<>();
    }

    /**
     * Lisää tietylle muuttujalle kustomoitu parser, joka määrittää miten se pitäisi käsitellä.
     * @param fieldName
     * @param muuttujaParser
     */
    public void addMuuttujaParser(String fieldName, MuuttujaParser muuttujaParser) {
        parsers.put(fieldName, muuttujaParser);
    }

    /**
     * Noutaa oliosta sen muuttujat ja niiden arvot.
     * @param classInstance
     * @param fieldsToIgnore
     * @return palauttaa listana olion muuttujat ja arvot
     */
    public List<MuuttujaData> parseClassVariables(Object classInstance, String... fieldsToIgnore) {
        List<String> fieldNames = new ArrayList<>();
        for (String fields : fieldsToIgnore) {
            fieldNames.add(fields);
        }
        return parseClassVariables(classInstance, fieldNames);
    }

    public List<MuuttujaData> parseClassVariables(Object classInstance, List<String> fieldsToIgnore) {
        List<MuuttujaData> muttujat = new ArrayList<>();
        for (Field field : getAllFields(classInstance.getClass())) {
            String name = field.getName();
            if (fieldsToIgnore.contains(name)) {
                continue;
            }
            Object value = getFieldValue(field, classInstance);
            MuuttujaParser muuttujaParser = parsers.get(name);
            if (muuttujaParser != null) {
                value = muuttujaParser.parseField(value);
            }
            muttujat.add(new MuuttujaData(name, value));
        }
        return muttujat;
    }

    private static List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        if (type.getSuperclass() != null) {
            fields.addAll(getAllFields(type.getSuperclass()));
        }
        fields.addAll(Arrays.asList(type.getDeclaredFields()));
        return fields;
    }

    private static Object getFieldValue(Field field, Object instance) {
        field.setAccessible(true);
        Object value = null;
        try {
            value = field.get(instance);
        } catch (IllegalArgumentException | IllegalAccessException e) {
        }
        return value;
    }
}