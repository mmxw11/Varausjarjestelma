package varausjarjestelma.database;

import java.util.List;
import java.util.Set;

import varausjarjestelma.database.dao.Dao;
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

    public static String buildSelectQuery(Dao<?, ?> dao, List<TauluSarake> columns, SQLLiitoslauseVarasto varasto) {
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
            String columnSelect = sarake.getQueryStrategy()
                    + " AS \"" + targetClass.getSimpleName().toLowerCase() + "." + sarake.getFieldName() + "\"";
            System.out.println("columnSelect: " + columnSelect);
            builder.append(columnSelect);
        }
        builder.append(" FROM ").append(dao.getTableName());
        builder.append(" WHERE ").append(dao.getPrimaryKeyColumn()).append(" = ?");
        if (varasto != null) {
            List<String> joinLiitokset = varasto.getJoinLiitokset();
            if (!joinLiitokset.isEmpty()) {
                builder.append(" ").append(String.join(" ", varasto.getJoinLiitokset()));
            }
        }
        return builder.toString();
    }
}