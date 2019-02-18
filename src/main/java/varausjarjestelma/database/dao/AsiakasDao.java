package varausjarjestelma.database.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import varausjarjestelma.domain.Asiakas;

public class AsiakasDao extends Dao<Asiakas, String> {

    public AsiakasDao(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    // TODO: Spring only throws DataAccessExceptions not sql...
    // use reflection for fields instead?
    @Override
    public void create(Asiakas object) throws SQLException {
        if (object.getId() != -1) {
            throw new RuntimeException("Asiakas sijaitsee jo tietokannassa!");
        }
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            PreparedStatement pstatement = conn.prepareStatement("INSERT INTO Asiakas "
                    + "(nimi, puhelinnumero, sahkopostiosoite) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            pstatement.setString(1, object.getNimi());
            pstatement.setString(2, object.getPuhelinnumero());
            pstatement.setString(3, object.getSahkopostiosoite());
            return pstatement;
        }, keyHolder);
        object.setId(keyHolder.getKey().intValue());
    }

    @Override
    public void update(Asiakas object) throws SQLException {
        // TODO Auto-generated method stub
    }

    @Override
    public void delete(String key) throws SQLException {
        // TODO Auto-generated method stub
    }

    @Override
    public Asiakas read(String key) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Asiakas> readAll(int rowLimit) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }
}