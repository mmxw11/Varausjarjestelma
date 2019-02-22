package varausjarjestelma.domain.builder;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import varausjarjestelma.database.Tietokantahallinta;
import varausjarjestelma.database.dao.Dao;

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
public class LuokkaParser<T> {

    private static class ParserVarasto<V> {

        private String remappedName;
        private Class<V> fieldType;
        private MuuttujaParser<V> muuttujaParser;

        public ParserVarasto(String remappedName, Class<V> fieldType, MuuttujaParser<V> muuttujaParser) {
            this.remappedName = remappedName;
            this.fieldType = fieldType;
            this.muuttujaParser = muuttujaParser;
        }
    }

    private Dao<T, ?> dao;
    private Map<String, ParserVarasto<?>> parsers;

    public LuokkaParser(Dao<T, ?> dao) {
        this.dao = dao;
        this.parsers = new HashMap<>();
    }

    /**
     * Lisää tietylle muuttujalle parserin, joka määrittää miten se pitäisi käsitellä.
     * @param fieldName Luokassa olevan sarakkeen nimi
     * @param remappedFieldName Uusi sarakkeen nimi, kuten tietokannassa olevan sarakkeen nimi
     * @param fieldType Luokassa olevan muuttujan tyyppi
     * @param muuttujaParser Muuttujan käsittelijä
     */
    public <V> void addMuuttujaParser(String fieldName, String remappedFieldName, Class<V> fieldType, MuuttujaParser<V> muuttujaParser) {
        ParserVarasto<V> varasto = new ParserVarasto<>(remappedFieldName, fieldType, muuttujaParser);
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
    public <V> Map<String, Object> parseClassFields(T classInstance) {
        Map<String, Object> fields = new LinkedHashMap<>(); // Pidetään järjestys oikeana.
        for (Field field : getAllFields(classInstance.getClass())) {
            String fieldName = field.getName();
            ParserVarasto<V> parserVarasto = (ParserVarasto<V>) parsers.get(fieldName);
            Object value = getFieldValue(field, classInstance);
            if (parserVarasto != null) {
                V cvalue = value != null ? parserVarasto.fieldType.cast(value) : null;
                value = parserVarasto.muuttujaParser.parseField(cvalue);
                fieldName = parserVarasto.remappedName;
            }
            fields.put(fieldName, value);
        }
        return fields;
    }

    /**
     * Noutaa luokasta muuttujien nimet. Tämä metodi ottaa huomioon, myös oliomuuttujien 
     * muuttujat, jotka ovat merkitty {@link JoinLuokka}-annotaatiolla.
     * Tämä mahdollistaa JOIN-kyselyjen tulosten automaattisen lukemisen.
     * @param thallinta
     * @return list
     */
    public List<String> parseClassFieldNamesToColumns(Tietokantahallinta thallinta) {
        return parseClassFieldNamesToColumns(dao.getResultClass(), dao.getTableName(), thallinta);
    }

    private List<String> parseClassFieldNamesToColumns(Class<?> targetClass, String tableName, Tietokantahallinta thallinta) {
        List<String> selectColumns = new ArrayList<>();
        for (Field field : getAllFields(targetClass)) {
            JoinLuokka jluokka = field.getAnnotation(JoinLuokka.class);
            if (jluokka != null) { // Tarkista onko "sisäinen" luokka.
                Dao<?, ?> tdao = thallinta.getDao(jluokka.value());
                selectColumns.addAll(parseClassFieldNamesToColumns(field.getType(), tdao.getTableName(), thallinta));
                continue;
            }
            String fieldName = field.getName();
            ParserVarasto<?> parserVarasto = parsers.get(fieldName);
            String column = parserVarasto != null ? parserVarasto.remappedName : fieldName;
            String targetPrefix = tableName.equals(dao.getTableName()) ? "" : targetClass.getSimpleName().toLowerCase() + ".";
            String select = tableName + "." + column + " AS \"" + targetPrefix + fieldName + "\"";
            selectColumns.add(select);
        }
        return selectColumns;
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