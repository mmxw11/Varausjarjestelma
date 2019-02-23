package varausjarjestelma.domain.serialization;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import varausjarjestelma.database.SQLLiitoslauseVarasto;
import varausjarjestelma.database.Tietokantahallinta;
import varausjarjestelma.database.dao.Dao;
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
    /**
     *     /**
     * Asettaa muuttujan arvon.
     * @param fieldName
     * @param value
     * @param classInstance
     *
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
     */
    private String tableName;
    private Class<T> resultClass;
    private LuokkaParser<T> parser;
    private Tietokantahallinta thallinta;
    private boolean buildJoinClauses;
    private Map<String, SerializerStorage<?>> serializers;
    private Map<String, SerializerStorage<ResultSet>> deserializers;
    private Map<String, String> queryStrategies;

    public LuokkaSerializer(String tableName, Class<T> resultClass, Tietokantahallinta thallinta) {
        this.tableName = tableName;
        this.resultClass = resultClass;
        this.parser = new LuokkaParser<>(resultClass);
        this.thallinta = thallinta;
        this.serializers = new HashMap<>();
        this.deserializers = new HashMap<>();
        this.queryStrategies = new HashMap<>();
    }

    /**
     * Määritä strategia miten muuttuja pitäisi serialisoida tietokantaan. 
     * Esim. ns. viiteluokat pitää aina muuntaa viiteavaimiksi.
     * @param fieldName
     * @param fieldClass
     * @param serializer
     */
    public <V> void registerSerializerStrategy(String fieldName, Class<V> fieldClass, MuuttujaSerializer<V> serializer) {
        SerializerStorage<V> storage = new SerializerStorage<>(fieldClass, serializer);
        serializers.put(fieldName, storage);
    }

    /**
     * Määritä strategia miten muuttuja pitäisi deserialisoida takaisin olioksi.
     * @param fieldName
     * @param serializer
     */
    public void registerDeserializerStrategy(String fieldName, MuuttujaSerializer<ResultSet> serializer) {
        SerializerStorage<ResultSet> storage = new SerializerStorage<>(ResultSet.class, serializer);
        deserializers.put(fieldName, storage);
    }

    /**
     * Dynaamiset muuttujat, jotka haetaan esim. yhteenvetokyselyllä tarvitsevat strategian niiden hakemiseen.
     * @param fieldName
     * @param strategy Sarakkeen hakustrategia, kuten SUM(...), COUNT(...)
     */
    public void registerDynamicTypeQueryStrategy(String fieldName, String strategy) {
        queryStrategies.put(fieldName, strategy);
    }

    /**
     * Määritä pitäisikö JOIN-lausekkeet rakentaa automattisesti.
     * Tämä ei välttämättä toimi kunnolla liitostaulujen kanssa, mutta 1-* -yhteydet onnistuvat.
     * @param value
     */
    public void setBuildJoinQueries(boolean value) {
        this.buildJoinClauses = value;
    }

    /**
     * Serialisoi olion muuttujat sopivaksi tietokanta luonti- ja päivityskyselyhin.
     * Alla olevana hajautustauluna toimii LinkedHashMap, joka pitää huolen siitä,
     * että palautettavat muuttujat ovat aina oikeassa järjestyksessä, 
     * joka on erittäin tärkeää SQL-kyselyjen kannalta.
     * @param instance
     * @return Palauttaa olion muuttujat
     * @throws SQLException
     */
    @SuppressWarnings("unchecked")
    public <V> Map<String, Object> serializeObject(T instance) throws SQLException {
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
                value = storage.serializer.serializeField(serializableValue);
            } else if (styyppi == SarakeTyyppi.FOREIGN_KEY) {
                // Viiteavaimille on pakko olla oma serialisointi.
                throw new RuntimeException(resultClass.getSimpleName() + " > " + "Muuttuja \"" + field.getName()
                        + "\" on viiteavain, joka tarvitsee erillisen serialisointi strategian!");
            }
            String columnName = pmuuttuja.getRemappedName() != null ? pmuuttuja.getRemappedName() : field.getName();
            fields.put(columnName, value);
            // TODO: DEBUG
            System.out.println("Serialisointiin [" + field.getName() + "]: columnName: " + columnName + " | value: " + value);
        }
        return fields;
    }

    /**
     * Muuntaa luokan muuttujien nimet SQL-kyselyyn käytettäviksi sarakkeiksi.
     * Tämä metodi ottaa huomioon, myös oliomuuttujien sisäiset
     * muuttujat. (aka viiteavain muuttujat)
     * @return Palauttaa listan haettavista sarakkeista
     */
    public List<TauluSarake> convertFieldsToColumns() {
        return convertFieldsToColumns(null);
    }

    /**
     * Muuntaa luokan muuttujien nimet SQL-kyselyyn käytettäviksi sarakkeiksi.
     * Tämä metodi ottaa huomioon, myös oliomuuttujien sisäiset
     * muuttujat. (aka viiteavain muuttujat)
     * @param varasto Tänne tallennetaan liitoskyselyihin tarvittavat lauseet
     * @return Palauttaa listan haettavista sarakkeista
     */
    public List<TauluSarake> convertFieldsToColumns(SQLLiitoslauseVarasto varasto) {
        List<TauluSarake> columns = new ArrayList<>();
        for (ParsedMuuttuja pmuuttuja : parser.getMuuttujat()) {
            SarakeTyyppi styyppi = pmuuttuja.getTyyppi();
            Field field = pmuuttuja.getField();
            String fieldName = field.getName();
            if (styyppi == SarakeTyyppi.FOREIGN_KEY) {
                Dao<?, ?> targetDao = thallinta.getDaoByResultClass(field.getType());
                List<TauluSarake> innerClassColumns = targetDao.getSerializer().convertFieldsToColumns();
                TauluSarake multilayerSarake = innerClassColumns.stream().filter(c -> c.getTargetClass() != field.getType())
                        .findAny().orElse(null);
                if (multilayerSarake != null) {
                    throw new UnsupportedOperationException(resultClass.getSimpleName() + " > " + field.getType().getSimpleName()
                            + ": Monikerroksisia sarakkeita ei tueta! (" + multilayerSarake.getTargetClass().getName() + ")");
                }
                if (buildJoinClauses && varasto != null) {
                    String columnName = pmuuttuja.getRemappedName() != null ? pmuuttuja.getRemappedName() : fieldName;
                    String joinSQL = "LEFT JOIN " + targetDao.getTableName() + " ON "
                            + targetDao.getTableName() + "." + targetDao.getPrimaryKeyColumn()
                            + " = " + tableName + "." + columnName;
                    varasto.addSQLJoinClause(targetDao.getTableName(), joinSQL);
                    System.out.println("joinLause: " + joinSQL);
                }
                // LEFT JOIN targetDao.getTableName() ON
                // targetDao.getTableName().targetDao.getPrimaryKey()
                // = tableName. + columnName;
                columns.addAll(targetDao.getSerializer().convertFieldsToColumns());
                continue;
            }
            String queryStrategy = null;
            if (styyppi == SarakeTyyppi.NORMAL) {
                String columnName = pmuuttuja.getRemappedName() != null ? pmuuttuja.getRemappedName() : fieldName;
                queryStrategy = tableName + "." + columnName;
            } else if (styyppi == SarakeTyyppi.DYNAMICALLY_GENERATED) {
                queryStrategy = queryStrategies.get(fieldName);
                if (queryStrategy == null) {
                    // Dynaamisesti generoiduihin sarakkeisiin tarvitaan hakustrategia. on pakko
                    // olla oma serialisointi.
                    throw new RuntimeException(resultClass.getSimpleName() + " > " + "Muuttuja \"" + fieldName
                            + "\" on dynaamisesti generoitu, joka tarvitsee hakustrategian!");
                }
            } else {
                throw new UnsupportedOperationException("Saraketyyppiä \"" + styyppi + "\" ei tueta!");
            }
            TauluSarake tsarake = new TauluSarake(queryStrategy, pmuuttuja.getTyyppi(), resultClass, fieldName);
            columns.add(tsarake);
        }
        return columns;
    }

    public MuuttujaSerializer<ResultSet> getDeserializer(String fieldName) {
        SerializerStorage<ResultSet> storage = deserializers.get(fieldName);
        if (storage != null) {
            return storage.serializer;
        }
        return null;
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

    private static class SerializerStorage<V> {

        private Class<V> fieldClass;
        private MuuttujaSerializer<V> serializer;

        private SerializerStorage(Class<V> fieldClass, MuuttujaSerializer<V> serializer) {
            this.fieldClass = fieldClass;
            this.serializer = serializer;
        }
    }
}