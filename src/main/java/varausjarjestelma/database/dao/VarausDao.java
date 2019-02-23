package varausjarjestelma.database.dao;

import java.sql.SQLException;

import org.springframework.dao.EmptyResultDataAccessException;

import varausjarjestelma.database.Tietokantahallinta;
import varausjarjestelma.domain.Asiakas;
import varausjarjestelma.domain.Varaus;
import varausjarjestelma.domain.builder.TulosLuokkaRakentaja;

/**
 * @author Matias
 */
public class VarausDao extends Dao<Varaus, Integer> {

    public VarausDao(Tietokantahallinta thallinta) {
        super(thallinta, "Varaus", "id", Varaus.class);
        // Muunna asiakas-luokka ID:ksi.
        parser.addMuuttujaParser("asiakas", "asiakas_id", Asiakas.class, Asiakas::getId);
        serializer.registerDeserializerStrategy("alkupaivamaara", rs -> rs.getTimestamp("alkupaivamaara").toLocalDateTime());
        serializer.registerDeserializerStrategy("loppupaivamaara", rs -> rs.getTimestamp("loppupaivamaara").toLocalDateTime());
    }

    @Override
    public Varaus read(Integer key) throws SQLException {
        Varaus result = thallinta.executeQuery(jdbcTemp -> {
            try {
                // BeanPropertyRowMapper käyttää settereitä arvojen lisäämiseen,
                // siksi kaikissa luokissa on oltava setterit mukana :/
                String columns = String.join(", ", parser.convertClassFieldsToColumns(thallinta));
                return jdbcTemp.queryForObject("SELECT " + columns + " FROM " + tableName
                        + " JOIN Asiakas ON Asiakas.id = " + tableName + ".asiakas_id"
                        + " WHERE " + tableName + "." + primaryKeyColumn + " = ?",
                        new TulosLuokkaRakentaja<>(this, thallinta), key);
            } catch (EmptyResultDataAccessException e) {
                // Tietokannasta ei löytynyt mitään kyselyyn vastaavaa.
                return null;
            }
        });
        return result;
    }
}