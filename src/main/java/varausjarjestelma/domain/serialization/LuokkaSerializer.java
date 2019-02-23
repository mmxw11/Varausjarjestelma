package varausjarjestelma.domain.serialization;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import varausjarjestelma.domain.serialization.parser.LuokkaParser;
import varausjarjestelma.domain.serialization.parser.ParsedMuuttuja;
import varausjarjestelma.domain.serialization.parser.SarakeTyyppi;

/**
 * Muuntaa luokan tiedot SQL-kyselyjä vastaaviksi sarakkeiksi ja toisinpäin.
 * POJO-luokat vastaavat muuttujiltaa tietokantatauluja. Lisäksi muuttujien kohdalla 
 * voidaan määrittää erikseen miten ne pitäisi käsitellä. 
 * Tällöin voidaan siis rakentaa SQL-kyselyjä dynaamisesti.
 * 
 * @author Matias
 *
 * @param <T>
 */
public class LuokkaSerializer<T> {

    private LuokkaParser<T> parser;
    private Map<String, SerializerStorage<?>> serializers;

    public LuokkaSerializer(Class<T> resultClass) {
        this.parser = new LuokkaParser<>(resultClass);
        this.serializers = new HashMap<>();
    }

    public <V> void registerSerializerStrategy(String fieldName, Class<V> fieldClass, MuuttujaSerializer<V> serializer) {
        SerializerStorage<V> storage = new SerializerStorage<>(fieldClass, serializer);
        serializers.put(fieldName, storage);
    }

    /**
     * Serialisoi olion muuttujat sopivaksi tietokanta luonti- ja päivityskyselyhin.
     * Alla olevana hajautustauluna toimii LinkedHashMap, joka pitää huolen siitä,
     * että palautettavat muuttujat ovat aina oikeassa järjestyksessä, 
     * joka on erittäin tärkeää SQL-kyselyjen kannalta.
     * @param instance
     * @return Palauttaa olion muuttujat
     */
    @SuppressWarnings("unchecked")
    public <V> Map<String, Object> serializeObject(T instance) {
        Map<String, Object> fields = new LinkedHashMap<>();
        for (ParsedMuuttuja pmuuttuja : parser.getMuuttujat()) {
            SarakeTyyppi styyppi = pmuuttuja.getTyyppi();
            if (styyppi == SarakeTyyppi.DYNAMICALLY_GENERATED) {
                // Nimensä mukaisesti näitä ei tallenneta tietokantaan.
                continue;
            }
            Field field = pmuuttuja.getField();
            Object value = getFieldValue(field, instance);
            SerializerStorage<V> storage = (SerializerStorage<V>) serializers.get(field.getName());
            if (storage != null) {
                V serializableValue = value == null ? null : storage.fieldClass.cast(value);
                value = storage.serializer.serializeField(serializableValue, pmuuttuja);
            } else if (styyppi == SarakeTyyppi.FOREIGN_KEY) {
                // Viiteavaimille on pakko olla oma serialisointi.
                throw new RuntimeException("Muuttuja \"" + field.getName() + "\" on viiteavain, mutta sille ei löytynnyt serialisointi strategiaa!");
            }
            String columnName = pmuuttuja.getRemappedName() != null ? pmuuttuja.getRemappedName() : field.getName();
            fields.put(columnName, value);
            // TODO: DEBUG
            System.out.println("Serialisointiin [" + field.getName() + "]: columnName: " + columnName + " | value: " + value);
        }
        return fields;
    }

    // TODO: GENERATE UPDATE QUERY!
    // TODO: GENERATE SELECT QUERY AKA DESERIALIZER
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

    private static class SerializerStorage<V> {

        private Class<V> fieldClass;
        private MuuttujaSerializer<V> serializer;

        private SerializerStorage(Class<V> fieldClass, MuuttujaSerializer<V> serializer) {
            this.fieldClass = fieldClass;
            this.serializer = serializer;
        }
    }
}