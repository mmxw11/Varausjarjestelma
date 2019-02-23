package varausjarjestelma.domain.serialization.testdata;

import java.sql.SQLException;

import org.springframework.dao.EmptyResultDataAccessException;

import varausjarjestelma.database.Tietokantahallinta;
import varausjarjestelma.database.dao.Dao;
import varausjarjestelma.domain.Huone;
import varausjarjestelma.domain.Huonetyyppi;
import varausjarjestelma.domain.builder.TulosLuokkaRakentaja;

/**
 * @author Matias
 */
public class HuoneTestDao extends Dao<HuoneTest, Integer> {

    public HuoneTestDao(Tietokantahallinta thallinta) {
        super(thallinta, "Huone", "huonenumero", HuoneTest.class);
        setAutoGeneratePrimaryKey(false);
        // Muunna huonetyyppi-luokka ID:ksi.
        parser.addMuuttujaParser("huonetyyppi", "huonetyyppi_id", Huonetyyppi.class, Huonetyyppi::getId);
    }

    @Override
    public HuoneTest read(Integer key) throws SQLException {
        HuoneTest result = thallinta.executeQuery(jdbcTemp -> {
            try {
                // BeanPropertyRowMapper käyttää settereitä arvojen lisäämiseen,
                // siksi kaikissa luokissa on oltava setterit mukana :/
                String columns = String.join(", ", parser.convertClassFieldsToColumns(thallinta));
                return jdbcTemp.queryForObject("SELECT " + columns + " FROM " + tableName
                        + " JOIN Huonetyyppi ON Huonetyyppi.id = " + tableName + ".huonetyyppi_id"
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