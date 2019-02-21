package varausjarjestelma.database;

import java.util.Set;

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
}