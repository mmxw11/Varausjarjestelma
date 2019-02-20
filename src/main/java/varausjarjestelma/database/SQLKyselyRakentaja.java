package varausjarjestelma.database;

import java.util.List;

import varausjarjestelma.domain.parser.Muuttuja;

/**
 * Sisältää apumetodeja SQL-kyselyjen rakentamiseen.
 * 
 * @author Matias
 */
public class SQLKyselyRakentaja {

    public static String buildCreateQuery(String table, List<Muuttuja> muuttujat) {
        if (muuttujat.isEmpty()) {
            throw new IllegalArgumentException("Kyselyä ei voi rakentaa, koska sarakkeita ei löytynyt!");
        }
        StringBuilder builder = new StringBuilder();
        builder.append("INSERT INTO ").append(table).append(" (");
        String values = "";
        for (int i = 0; i < muuttujat.size(); i++) {
            if (i != 0) {
                values += ", ";
                builder.append(", ");
            }
            Muuttuja muuttuja = muuttujat.get(i);
            builder.append(muuttuja.getName());
            values += "?";
        }
        builder.append(") VALUES (").append(values).append(")");
        return builder.toString();
    }

    public static String buildUpdateQuery(String table, List<Muuttuja> muuttujat) {
        if (muuttujat.isEmpty()) {
            throw new IllegalArgumentException("Kyselyä ei voi rakentaa, koska sarakkeita ei löytynyt!");
        }
        StringBuilder builder = new StringBuilder();
        builder.append("UPDATE ").append(table).append(" SET ");
        for (int i = 0; i < muuttujat.size(); i++) {
            if (i != 0) {
                builder.append(", ");
            }
            Muuttuja muuttuja = muuttujat.get(i);
            builder.append(muuttuja.getName()).append(" = ?");
        }
        return builder.toString();
    }
}