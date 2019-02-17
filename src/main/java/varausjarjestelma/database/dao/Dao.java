package varausjarjestelma.database.dao;

import java.sql.SQLException;
import java.util.List;

import varausjarjestelma.database.SQLKyselyAsetukset;

public interface Dao<V, K> {

    void create(V object) throws SQLException;

    void update(V object) throws SQLException;

    void delete(K key) throws SQLException;

    V read(K key) throws SQLException;

    List<V> readAll(SQLKyselyAsetukset kyselyAsetukset) throws SQLException;
}