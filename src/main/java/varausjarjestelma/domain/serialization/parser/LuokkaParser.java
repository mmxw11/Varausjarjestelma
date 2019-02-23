package varausjarjestelma.domain.serialization.parser;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
        parseResultClass(resultClass);
    }

    public void tulostaMuuttujat() {
        parsedFields.values().forEach(System.out::println);
    }

    private void parseResultClass(Class<T> resultClass) {
        for (Field field : getAllFields(resultClass)) {
            if (Modifier.isTransient(field.getModifiers())) {
                // Muuttujia transient-määreella ei pidä käsitellä.
                continue;
            }
            SarakeAsetukset sasetukset = field.getAnnotation(SarakeAsetukset.class);
            SarakeTyyppi styyppi = sasetukset != null ? sasetukset.tyyppi() : SarakeTyyppi.NORMAL;
            ParsedMuuttuja pmuuttuja = new ParsedMuuttuja(field, styyppi);
            // Tarkista onko muuttujalla erikoisasetuksia.
            if (sasetukset != null) {
                pmuuttuja.setRemappedName(sasetukset.columnName());
            }
            parsedFields.put(field.getName(), pmuuttuja);
        }
    }

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

    public Collection<ParsedMuuttuja> getMuuttujat() {
        return parsedFields.values();
    }
}