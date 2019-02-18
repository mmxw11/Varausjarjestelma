package varausjarjestelma.database.dao;

import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;

public abstract class Dao<V, K> {

    protected JdbcTemplate jdbcTemplate;

    public Dao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public abstract void create(V object) throws SQLException;

    public abstract void update(V object) throws SQLException;

    public abstract void delete(K key) throws SQLException;

    public abstract V read(K key) throws SQLException;

    public abstract List<V> readAll(int rowLimit) throws SQLException;
}