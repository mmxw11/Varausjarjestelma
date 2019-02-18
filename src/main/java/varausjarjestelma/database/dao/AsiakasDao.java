package varausjarjestelma.database.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import varausjarjestelma.database.Tietokantahallinta;
import varausjarjestelma.domain.Asiakas;

public class AsiakasDao extends Dao<Asiakas, Integer> {

    public AsiakasDao(Tietokantahallinta thallinta) {
        super(thallinta);
    }

    @Override
    public void create(Asiakas object) throws SQLException {
        if (object.getId() != -1) {
            throw new RuntimeException("Asiakas on jo luotu aikaisemmin!");
        }
        KeyHolder keyHolder = new GeneratedKeyHolder();
        thallinta.executeQuery(jdbcTemplate -> jdbcTemplate.update(conn -> {
            PreparedStatement pstatement = conn.prepareStatement("INSERT INTO Asiakas "
                    + "(nimi, puhelinnumero, sahkopostiosoite) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            pstatement.setString(1, object.getNimi());
            pstatement.setString(2, object.getPuhelinnumero());
            pstatement.setString(3, object.getSahkopostiosoite());
            return pstatement;
        }, keyHolder));
        object.setId(keyHolder.getKey().intValue());
    }

    @Override
    public void update(Asiakas object) throws SQLException {
        // TODO Auto-generated method stub
    }

    @Override
    public void delete(Integer key) throws SQLException {
        thallinta.executeQuery(jdbcTemplate -> jdbcTemplate.update("DELETE FROM Asiakas WHERE id = ?", key));
    }

    @Override
    public Asiakas read(Integer key) throws SQLException {
        Asiakas asiakas =  thallinta.executeQuery(jdbcTemplate -> jdbcTemplate.queryForObject("SELECT * FROM Asiakas WHERE id = ?", new
        BeanPropertyRowMapper<>(Asiakas.class), key));
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Asiakas> readAll(int rowLimit) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }
}