package varausjarjestelma.domain.serialization;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import varausjarjestelma.database.SQLJoinVarasto;
import varausjarjestelma.database.Tietokantahallinta;
import varausjarjestelma.database.dao.Dao;
import varausjarjestelma.domain.serialization.parser.LuokkaParser;
import varausjarjestelma.domain.serialization.parser.ParsedMuuttuja;
import varausjarjestelma.domain.serialization.parser.SarakeTyyppi;

/**
 * Muuntaa luokan tiedot SQL-kyselyjä vastaaviksi sarakkeiksi ja toisinpäin.
 * POJO-luokat vastaavat muuttujiltaan tietokantatauluja. Lisäksi muuttujien kohdalla 
 * voidaan määrittää erikseen miten ne pitäisi käsitellä. 
 * Tällöin voidaan siis rakentaa SQL-kyselyjä dynaamisesti.
 * 
 * @author Matias
 *
 * @param <T>
 */
public class LuokkaSerializer<T> {

    private Class<T> resultClass;
    private LuokkaParser<T> parser;
    private Tietokantahallinta thallinta;
    private String joinClauseType;
    private Map<String, SerializerStorage<?>> serializers;
    private Map<String, SerializerStorage<ResultSet>> deserializers;
    private Map<String, String> queryStrategies;

    public LuokkaSerializer(Class<T> resultClass, Tietokantahallinta thallinta) {
        this.resultClass = resultClass;
        this.parser = new LuokkaParser<>(resultClass, true);
        this.thallinta = thallinta;
        this.serializers = new HashMap<>();
        this.deserializers = new HashMap<>();
        this.queryStrategies = new HashMap<>();
    }

    /**
     * Määrittää JOIN-lausekkeiden automaattisen rakentamisen.
     * Tämä ei välttämättä toimi kunnolla liitostaulujen kanssa, mutta *-1 -yhteydet onnistuvat.
     * @param joinClauseType Käytettävä JOIN-tyyppi, kuten LEFT JOIN, INNER JOIN jne
     */
    public void setJoinClauseType(String joinClauseType) {
        this.joinClauseType = joinClauseType;
    }

    /**
     * Määritä strategia, miten tietty muuttuja pitäisi serialisoida tietokantaan. 
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
     * Määritä strategia, miten tietty muuttuja pitäisi deserialisoida takaisin olioksi.
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
     * Rakentaa luokan muuttujista SQL-kyselyyn käytettävän sarakkeet.
     * Tämä metodi ottaa huomioon, myös oliomuuttujien sisäiset muuttujat. (aka viiteavain-muuttujat)
     * 
     * Jos "viiteavain-muuttujien" sisällä on viitteitä muihin POJO-luokkiin,
     * niin näitä luokkia ei voida käsitellä automaattisesti.
     * Myöskään mahdollisia dynaamisesti generoituja sarakkeita ei voida täydentään,
     * mutta se ei kuitenkaan estä automaattistä käsittelyä. 
     * Tälläisissä tilanteissa nämä arvot vain jätetään tyhjäksi.
     * Tosin näiden toteuttaminen voisi olla mahdollista, jos kohde luokasta otetaan mukaan JOIN-lausekkeet
     * ja kun kaikki lausekkeet ovat kerätty, niin ne lajiteltaisiin.
     * Aika ei kuitenkaan riittänyt tämän toteuttamiseen, joten olkoot näin hyvä :)
     * 
     * @param tableName Taulun nimi
     * @param joinVarasto Varasto JOIN-lausekkeille, mikäli niitä käytetään
     */
    public List<TauluSarake> convertClassFieldsToColumns(String tableName, SQLJoinVarasto joinVarasto) {
        List<TauluSarake> columns = new ArrayList<>();
        for (ParsedMuuttuja pmuuttuja : parser.getParsedFields()) {
            SarakeTyyppi styyppi = pmuuttuja.getTyyppi();
            Field field = pmuuttuja.getField();
            String fieldName = field.getName();
            if (styyppi == SarakeTyyppi.FOREIGN_KEY) { // Viiteavain.
                // Hae sarakkeet oliomuuttujasta.
                Dao<?, ?> targetDao = thallinta.getDaoByResultClass(field.getType());
                if (targetDao == null) {
                    throw new RuntimeException("Muuttujalle \"" + fieldName + "\" (" + field.getType().getSimpleName() + ") ei löytynyt DAO-luokkaa!");
                }
                List<TauluSarake> innerClassColumns = targetDao.getSerializer().convertClassFieldsToColumns(targetDao.getTableName(), null);
                for (Iterator<TauluSarake> it = innerClassColumns.iterator(); it.hasNext();) {
                    TauluSarake tsarake = it.next();
                    if (tsarake.getTyyppi() == SarakeTyyppi.NORMAL) {
                        continue;
                    }
                    if (tsarake.getTyyppi() == SarakeTyyppi.FOREIGN_KEY) {
                        // Luokan sisällä on toinen POJO-luokka.
                        throw new UnsupportedOperationException(resultClass.getSimpleName() + " > " + field.getType().getSimpleName()
                                + ": Monikerroksisia sarakkeita ei tueta! (" + tsarake.getTargetClass().getName() + ")");
                    } else if (tsarake.getTyyppi() == SarakeTyyppi.DYNAMICALLY_GENERATED) {
                        // Luokan sisällä on dynaamisesti generoituja sarakkeita, joita ei voi
                        // täydentää ainakaan tällä hetkellä.
                        // Poistetaan se, joten muuntoa voidaan jatkaa. Kts. Javadoc
                        it.remove();
                        continue;
                    }
                    throw new UnsupportedOperationException(resultClass.getSimpleName() + " > " + tsarake.getTargetClass().getName()
                            + " > " + field.getType().getSimpleName() + ": Tuntematon saraketyyppi \"" + tsarake.getTyyppi() + "\"!");
                }
                // Luo liitostaulu-lauseke.
                if (joinClauseType != null && joinVarasto != null) {
                    String columnName = pmuuttuja.getRemappedName() != null ? pmuuttuja.getRemappedName() : fieldName;
                    String joinSQL = joinClauseType + " " + targetDao.getTableName() + " ON "
                            + targetDao.getTableName() + "." + targetDao.getPrimaryKeyColumn()
                            + " = " + tableName + "." + columnName;
                    joinVarasto.addSQLJoinClause(targetDao.getTableName(), joinSQL);
                }
                // Lisää oliomuuttujan sarakkeet.
                columns.addAll(innerClassColumns);
                continue;
            }
            String queryStrategy = null;
            if (styyppi == SarakeTyyppi.NORMAL) { // Normaali datatyyppi.
                String columnName = pmuuttuja.getRemappedName() != null ? pmuuttuja.getRemappedName() : fieldName;
                queryStrategy = tableName + "." + columnName;
            } else if (styyppi == SarakeTyyppi.DYNAMICALLY_GENERATED) { // Dynaaminen datatyyppi.
                queryStrategy = queryStrategies.get(fieldName);
                if (queryStrategy == null) {
                    // Dynaamisesti generoiduihin sarakkeisiin tarvitaan hakustrategia.
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

    /**
     * Serialisoi olion muuttujat sopiviksi luonti- ja päivityskyselyhin.
     * Alla olevana hajautustauluna toimii LinkedHashMap, joka pitää huolen siitä,
     * että palautettavat muuttujat ovat aina oikeassa järjestyksessä.
     * Tämä on oleellista SQL-kyselyjen kannalta.
     * @param instance
     * @return Palauttaa olion muuttujat
     * @throws SQLException
     */
    @SuppressWarnings("unchecked")
    public <V> Map<String, Object> serializeObject(T instance) throws SQLException {
        Map<String, Object> fields = new LinkedHashMap<>();
        for (ParsedMuuttuja pmuuttuja : parser.getParsedFields()) {
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
        }
        return fields;
    }

    /**
     * Apumetodi, joka asettaa muuttujan arvon.
     * @param fieldName
     * @param value
     * @param instance
     */
    public void setField(String fieldName, Object value, T instance) {
        try {
            Field field = instance.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(instance, value);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            // Palauta runtime-virhe, koska ne voidaan heti korjata kehitysvaiheessa,
            // koska ne eivät johdu ns. ulkoisista tekijöistä.
            throw new RuntimeException(e);
        }
    }

    /**
     * @return Palauttaa JOIN-lausekkeiden tyypin, kuten LEFT JOIN, INNER JOIN jne
     */
    public String getJoinClauseType() {
        return joinClauseType;
    }

    /**
     * @param fieldName
     * @return Palauttaa muuttujan deserialisoijan
     */
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

    /**
     * Sisäinen luokka serialisointi-rajapintojen tallentamiseen.
     * 
     * @author Matias
     *
     * @param <V>
     */
    private static class SerializerStorage<V> {

        private Class<V> fieldClass;
        private MuuttujaSerializer<V> serializer;

        private SerializerStorage(Class<V> fieldClass, MuuttujaSerializer<V> serializer) {
            this.fieldClass = fieldClass;
            this.serializer = serializer;
        }
    }
}