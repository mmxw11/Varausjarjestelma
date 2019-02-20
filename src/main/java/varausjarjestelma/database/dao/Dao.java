package varausjarjestelma.database.dao;

import java.sql.SQLException;

import varausjarjestelma.database.Tietokantahallinta;
import varausjarjestelma.domain.parser.LuokkaDataParser;

/**
 * @author Matias
 */
public abstract class Dao<V, K> {

    protected Tietokantahallinta thallinta;
    protected LuokkaDataParser parser;

    public Dao(Tietokantahallinta thallinta) {
        this.thallinta = thallinta;
        this.parser = new LuokkaDataParser();
    }

    public abstract void create(V object) throws SQLException;

    public abstract V read(K key) throws SQLException;

    public abstract void update(V object) throws SQLException;

    public abstract void delete(K key) throws SQLException;
}