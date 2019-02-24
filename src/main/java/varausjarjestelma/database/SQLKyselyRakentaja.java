package varausjarjestelma.database;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import varausjarjestelma.domain.serialization.TauluSarake;

/**
 * Sisältää apumetodeja SQL-kyselyjen rakentamiseen.
 * 
 * @author Matias
 */
public class SQLKyselyRakentaja {

    /**
     * Luo uuden lisäyskyselyn.
     * @param table
     * @param columns
     * @return string
     */
    public static String buildCreateQuery(String table, Set<String> columns) {
        if (columns.isEmpty()) {
            throw new IllegalArgumentException("Kyselyä ei voi rakentaa, koska sarakkeita ei löytynyt!");
        }
        StringBuilder builder = new StringBuilder();
        builder.append("INSERT INTO ").append(table).append(" (");
        String values = "";
        for (String columnName : columns) {
            if (!values.isEmpty()) {
                values += ", ";
                builder.append(", ");
            }
            builder.append(columnName);
            values += "?";
        }
        builder.append(") VALUES (").append(values).append(")");
        return builder.toString();
    }

    /**
     * Luo uuden päivityskyselyn.
     * @param table
     * @param columns
     * @return string
     */
    public static String buildUpdateQuery(String table, Set<String> columns) {
        if (columns.isEmpty()) {
            throw new IllegalArgumentException("Kyselyä ei voi rakentaa, koska sarakkeita ei löytynyt!");
        }
        StringBuilder builder = new StringBuilder();
        builder.append("UPDATE ").append(table).append(" SET ");
        boolean insertComma = false;
        for (String columnName : columns) {
            if (insertComma) {
                builder.append(", ");
            }
            builder.append(columnName).append(" = ?");
            insertComma = true;
        }
        return builder.toString();
    }

    /**
     * Luo uuden hakukyselun.
     * @param resultClass
     * @param tableName Taulun nimi
     * @param columns Haettavat sarakkeet
     * @return string 
     */
    public static String buildSelectQuery(Class<?> resultClass, String tableName, List<TauluSarake> columns) {
        return buildSelectQuery(resultClass, tableName, columns, null);
    }

    /**
     * Luo uuden hakukyselun.
     * @param resultClass
     * @param tableName Taulun nimi
     * @param columns Haettavat sarakkeet
     * @param varasto Jos tietoa haetaan monesta taulusta, tulee liitoslausekkeet laittaa tänne
     * @return string 
     */
    public static String buildSelectQuery(Class<?> resultClass, String tableName, List<TauluSarake> columns, SQLJoinVarasto varasto) {
        if (columns.isEmpty()) {
            throw new IllegalArgumentException("Kyselyä ei voi rakentaa, koska sarakkeita ei löytynyt!");
        }
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT ");
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            TauluSarake sarake = columns.get(i);
            Class<?> targetClass = sarake.getTargetClass();
            // Sarakkeet tallennetaan muodossa Taulu.sarake AS "Luokka.sarake".
            // Tällöin ne voidaan automaattisesti liittää luokkiin. Kts. TulosLuokkaRakentaja
            String innerClassPrefix = targetClass == resultClass ? "" : targetClass.getSimpleName().toLowerCase() + ".";
            String columnSelect = sarake.getQueryStrategy()
                    + " AS \"" + innerClassPrefix + sarake.getFieldName() + "\"";
            builder.append(columnSelect);
        }
        builder.append(" FROM ").append(tableName);
        if (varasto != null) {
            Map<String, List<String>> joinClauses = varasto.getJoinClauses();
            if (!joinClauses.isEmpty()) {
                for (List<String> tableJoinCLauses : joinClauses.values()) {
                    // Automaattisesti generoidut JOIN-lausekkeet lisätään viimeksi, jotta
                    // mahdolliset manuaalisesti lisätyt liitoslausekkeet saadaan väliin.
                    Collections.reverse(tableJoinCLauses);
                    builder.append(" ").append(String.join(" ", tableJoinCLauses));
                }
            }
        }
        return builder.toString();
    }
}