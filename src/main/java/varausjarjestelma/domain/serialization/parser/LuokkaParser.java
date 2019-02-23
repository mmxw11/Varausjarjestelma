package varausjarjestelma.domain.serialization.parser;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Tämän luokan tehtävä on selvittää POJO-luokista muuttujat käyttäen Reflectionia.
 * @see varausjarjestelma.domain.serialization.LuokkaSerializer
 * 
 * @author Matias   
 */
public class LuokkaParser<T> {

    private boolean parseSuperclasses;
    private List<ParsedMuuttuja> parsedFields;

    public LuokkaParser(Class<T> resultClass) {
        this.parsedFields = new ArrayList<>();
        parseResultClass(resultClass);
    }

    /**
     * Määrittää pitäisikö yliluokkien muuttujat ottaa mukaan.
     * Jos päällä, niin täytyy olla tarkkana, ettei luokissa esiinny samannimisiä muuttujia.
     * @param parseSuperclasses
     */
    public void setParseSuperclasses(boolean parseSuperclasses) {
        this.parseSuperclasses = parseSuperclasses;
    }

    /**
     * Sisäinen metodi, joka jäsentää luokasta sen muuttujat.
     * @param resultClass
     */
    private void parseResultClass(Class<T> resultClass) {
        for (Field field : getAllFields(resultClass)) {
            if (Modifier.isTransient(field.getModifiers())) {
                // Muuttujia transient-määreella ei pidä käsitellä.
                continue;
            }
            SarakeAsetukset sasetukset = field.getAnnotation(SarakeAsetukset.class);
            SarakeTyyppi styyppi = SarakeTyyppi.NORMAL;
            String columnName = "";
            if (sasetukset != null) {
                styyppi = sasetukset.tyyppi();
                columnName = sasetukset.columnName();
            }
            ParsedMuuttuja pmuuttuja = new ParsedMuuttuja(field, styyppi);
            if (!columnName.isEmpty()) {
                // Päivitä muuttujan tietokannassa sijaitseva nimi.
                pmuuttuja.setRemappedName(columnName);
            }
            parsedFields.add(pmuuttuja);
        }
    }

    /**
     * @return Palauttaa jäsennetyt luokan muuttujat
     */
    public List<ParsedMuuttuja> getParsedFields() {
        return parsedFields;
    }

    /**
     * Palauttaa luokassa olevat muuttujat.
     * @param type
     * @return Palauttaa luokan muuttujat listalla 
     */
    private List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        if (parseSuperclasses && type.getSuperclass() != null) {
            fields.addAll(getAllFields(type.getSuperclass()));
        }
        fields.addAll(Arrays.asList(type.getDeclaredFields()));
        return fields;
    }
}