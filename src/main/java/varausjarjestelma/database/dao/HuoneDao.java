package varausjarjestelma.database.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;

import varausjarjestelma.database.Tietokantahallinta;
import varausjarjestelma.domain.Huone;
import varausjarjestelma.domain.Huonetyyppi;

/**
 * @author Matias
 */
public class HuoneDao extends Dao<Huone, Integer> implements RowMapper<Huone> {

    // private DecimalFormat df;
    public HuoneDao(Tietokantahallinta thallinta) {
        super(thallinta, "Huone", "huonenumero", Huone.class);
        // this.df = new DecimalFormat("#0.00");
        setAutoGeneratePrimaryKey(false);
        // Muunna huonetyyppi-luokka IDeksi.
        parser.addMuuttujaParser("huonetyyppi", "huonetyyppi_id", Huonetyyppi.class, Huonetyyppi::getId);
        /*
         * parser.addMuuttujaParser("paivahinta", "paivahinta", //043.5604235456456456464545647859
         * Double.class, (hinta) -> new BigDecimal(hinta).setScale(2, RoundingMode.UP));
         */
    }

    @Override
    public Huone read(Integer key) throws SQLException {
        // Huone huone = (new BeanPropertyRowMapper<>(Huone.class)).mapRow(rs, rowNum);
        // Huonetyyppi huonetyyppi = (new BeanPropertyRowMapper<>(Huonetyyppi.class)).mapRow(rs,
        // rowNum);
        Huone result = thallinta.executeQuery(jdbcTemp -> {
            try {
                // BeanPropertyRowMapper käyttää settereitä arvojen lisäämiseen,
                // siksi kaikissa luokissa on oltava setterit mukana :/
                /**
                 *  return jdbcTemp.queryForObject("SELECT " + tableName + ".*"
                        + ", Huonetyyppi.* FROM " + tableName 
                        + " JOIN Huonetyyppi ON Huonetyyppi.id = " + tableName + ".huonetyyppi_id"
                        + " WHERE "
                        + tableName + "." + primaryKeyColumn + " = ?",
                        new BeanPropertyRowMapper<>(resultClass), key);
                 */
                // String columns = "Huone.huonenumero AS \"Huone.huonenumero\", Huonetyyppi.id,
                // Huonetyyppi.tyyppi, Huone.paivahinta AS \"huone.paivahinta\"";
                return jdbcTemp.queryForObject("SELECT * FROM " + tableName
                        + " JOIN Huonetyyppi ON Huonetyyppi.id = " + tableName + ".huonetyyppi_id"
                        + " WHERE "
                        + tableName + "." + primaryKeyColumn + " = ?",
                        this, key);
            } catch (EmptyResultDataAccessException e) {
                // Tietokannasta ei löytynyt mitään kyselyyn vastaavaa.
                return null;
            }
        });
        return result;
    }

    @Override
    public Huone mapRow(ResultSet rs, int rowNum) throws SQLException {
        Huone huone = (new BeanPropertyRowMapper<>(Huone.class)).mapRow(rs, rowNum);
        Huonetyyppi huonetyyppi = (new BeanPropertyRowMapper<>(Huonetyyppi.class)).mapRow(rs, rowNum);
        huone.setHuonetyyppi(huonetyyppi);
        return huone;
    }
}