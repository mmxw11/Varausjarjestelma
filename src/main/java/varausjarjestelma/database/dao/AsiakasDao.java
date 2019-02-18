package varausjarjestelma.database.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import varausjarjestelma.domain.Asiakas;

public class AsiakasDao implements Dao<Asiakas, String> {

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
    public List<Asiakas> readAll(int rowLimit) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     *  TEST CODE
        // Connection connection = getConnection();
        // connection.setAutoCommit(false);
        /*
         * String sql =
         * "INSERT INTO Asiakas (nimi, puhelinnumero, sahkopostiosoite) VALUES (?, ?, ?)";
         * jdbcTemplate.update(sql, "Tero Viitasaari", "04490440325256",
         * "tero.viitasaari@example.com"); jdbcTemplate.update(sql, "Jarmo Korhola",
         * "0553906704393255", "jarmo.korhola@example.com");
         *
        updateAsiakasTable(connection);
       createAsiakkaat(connection)
    */
    private void createAsiakkaat(Connection connection) throws SQLException {
        String sql = "INSERT INTO Asiakas (nimi, puhelinnumero, sahkopostiosoite) VALUES (?, ?, ?)";
        PreparedStatement statement1 = connection.prepareStatement(sql);
        statement1.setString(1, "Tero Viitasaari");
        statement1.setString(2, "04490440325256");
        statement1.setString(3, "tero.viitasaari@example.com");
        statement1.executeUpdate();
        PreparedStatement statement2 = connection.prepareStatement(sql);
        statement2.setString(1, "Jarmo Korhola");
        statement2.setString(2, "0553906704393255");
        statement2.setString(3, "jarmo.korhola@example.com");
        statement2.executeUpdate();
    }
}