package varausjarjestelma.database.dao;

import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import varausjarjestelma.database.SQLKyselyAsetukset;
import varausjarjestelma.domain.Asiakas;

public class AsiakasDao implements Dao<Asiakas, String> {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Override
    public void create(Asiakas object) throws SQLException {
        // TODO Auto-generated method stub
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
    public List<Asiakas> readAll(SQLKyselyAsetukset kyselyAsetukset) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }
}