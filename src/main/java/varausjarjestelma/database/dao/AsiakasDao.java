package varausjarjestelma.database.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import varausjarjestelma.database.SQLKyselyRakentaja;
import varausjarjestelma.database.Tietokantahallinta;
import varausjarjestelma.domain.Asiakas;
import varausjarjestelma.domain.parser.MuuttujaData;

/**
 * @author Matias
 */
public class AsiakasDao extends Dao<Asiakas, Integer> {

    public AsiakasDao(Tietokantahallinta thallinta) {
        super(thallinta);
    }

    @Override
    public void create(Asiakas object) throws SQLException {
        if (object.getId() != -1) { // Kaikilla uusilla olioilla on oletusarvona -1 pääavaimena.
            throw new RuntimeException("Asiakas on jo luotu aikaisemmin!");
        }
        KeyHolder keyHolder = new GeneratedKeyHolder();
        List<MuuttujaData> muuttujat = parser.parseClassVariables(object, "id");
        thallinta.executeQuery(jdbcTemp -> jdbcTemp.update(conn -> {
            String sql = SQLKyselyRakentaja.buildCreateQuery("Asiakas", muuttujat);
            PreparedStatement pstatement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < muuttujat.size(); i++) {
                pstatement.setObject(i + 1, muuttujat.get(i).getData());
            }
            return pstatement;
        }, keyHolder));
        // Aseta asiakkaan pääavain.
        object.setId(keyHolder.getKey().intValue());
    }

    @Override
    public void update(Asiakas object) throws SQLException {
        List<MuuttujaData> muuttujat = parser.parseClassVariables(object, "id");
        Object[] muuttujaData = new Object[muuttujat.size() + 1];
        for (int i = 0; i < muuttujat.size(); i++) {
            muuttujaData[i] = muuttujat.get(i).getData();
        }
        muuttujaData[muuttujaData.length - 1] = object.getId();
        String sql = SQLKyselyRakentaja.buildUpdateQuery("Asiakas", muuttujat) + " WHERE id = ?";
        thallinta.executeQuery(jdbcTemp -> jdbcTemp.update(sql, muuttujaData));
    }

    @Override
    public void delete(Integer key) throws SQLException {
        thallinta.executeQuery(jdbcTemp -> jdbcTemp.update("DELETE FROM Asiakas WHERE id = ?", key));
    }

    @Override
    public Asiakas read(Integer key) throws SQLException {
        Asiakas asiakas = thallinta.executeQuery(jdbcTemp -> {
            try {
                // BeanPropertyRowMapper käyttää settereitä arvojen lisäämiseen,
                // siksi kaikissa luokissa onsetterit mukana :/
                return jdbcTemp.queryForObject("SELECT * FROM Asiakas WHERE id = ?",
                        new BeanPropertyRowMapper<>(Asiakas.class), key);
            } catch (EmptyResultDataAccessException e) {
                // Tietokannasta ei löytynyt mitään kyselyyn vastaavaa.
                return null;
            }
        });
        return asiakas;
    }
}