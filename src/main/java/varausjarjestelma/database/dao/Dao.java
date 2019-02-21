package varausjarjestelma.database.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import varausjarjestelma.database.SQLKyselyRakentaja;
import varausjarjestelma.database.Tietokantahallinta;
import varausjarjestelma.domain.parser.LuokkaParser;

/**
 * Abstrakti DAO-luokka, joka tarjoaa yleiset CRUD-metodit POJO-luokille (domain).
 * Tämä luokka on tarkoitettu perittäväksi, johon perivät luokat lisäävät ominaisuuksia
 * hyödyntäen tarjottuja metodeja.
 * 
 * @author Matias
 */
public abstract class Dao<T, K> {

    protected Tietokantahallinta thallinta;
    protected String tableName;
    protected String primaryKeyColumn;
    protected Class<T> resultClass;
    protected LuokkaParser parser;

    public Dao(Tietokantahallinta thallinta, String tableName, String primaryKeyColumn, Class<T> resultClass) {
        this.thallinta = thallinta;
        this.tableName = tableName;
        this.primaryKeyColumn = primaryKeyColumn;
        this.resultClass = resultClass;
        this.parser = new LuokkaParser();
    }

    /**
     * Lisää olion tiedot tietokantaan.
     * @param object
     * @throws SQLException
     */
    public void create(T object) throws SQLException {
        Map<String, Object> fields = parser.parseClassFields(object);
        // Poista pääavain, koska tietokanta luo sen automaattisesti.
        // Riippuen datatyypistä, mutta pääavaimen oletusarvo on yleensä esim. -1.
        fields.remove(primaryKeyColumn);
        String sql = SQLKyselyRakentaja.buildCreateQuery(tableName, fields.keySet());
        KeyHolder keyHolder = new GeneratedKeyHolder();
        thallinta.executeQuery(jdbcTemp -> jdbcTemp.update(conn -> {
            PreparedStatement pstatement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            int i = 0;
            for (Object data : fields.values()) {
                pstatement.setObject(++i, data);
            }
            return pstatement;
        }, keyHolder));
        // Päivitetään juuri luodun rivin pääavain target objektiin.
        int id = keyHolder.getKey().intValue();
        parser.setField(primaryKeyColumn, id, object);
    }

    /**
     * Hakee tietokannasta tietueen, jonka pääavain vastaa parametrina saatua.
     * @param key
     * @return Palauttaa tiedoista luodun olion
     * @throws SQLException
     */
    public T read(K key) throws SQLException {
        T result = thallinta.executeQuery(jdbcTemp -> {
            try {
                // BeanPropertyRowMapper käyttää settereitä arvojen lisäämiseen,
                // siksi kaikissa luokissa on oltava setterit mukana :/
                return jdbcTemp.queryForObject("SELECT * FROM " + tableName + " WHERE "
                        + primaryKeyColumn + " = ?", new BeanPropertyRowMapper<>(resultClass), key);
            } catch (EmptyResultDataAccessException e) {
                // Tietokannasta ei löytynyt mitään kyselyyn vastaavaa.
                return null;
            }
        });
        return result;
    }

    /**
     * Päivittää tietokantaan objektin muuttujien arvot.
     * @param object
     * @throws SQLException
     */
    public void update(T object) throws SQLException {
        Map<String, Object> fields = parser.parseClassFields(object);
        // Poista pääavain, koska sitä ei haluta päivittää.
        // Pääavainta tarvitaan kuitenkin lopuksi rajausehtoon, joten otetaan se talteen.
        Object primaryKeyData = fields.remove(primaryKeyColumn);
        String sql = SQLKyselyRakentaja.buildUpdateQuery(tableName, fields.keySet())
                + " WHERE " + primaryKeyColumn + " = ?";
        // Lisätään pääavain takaisin hajautustauluun,
        // joten Spring täydentää sen automaattisesti rajausehtoon.
        fields.put("primary_key_holder", primaryKeyData);
        thallinta.executeQuery(jdbcTemp -> jdbcTemp.update(sql, fields.values().toArray()));
    }

    /**
     * Poistaa tietokannasta rivin, jonka pääavain vastaa parametrina saatua.
     * @param key
     * @throws SQLException
     */
    public void delete(K key) throws SQLException {
        thallinta.executeQuery(jdbcTemp -> jdbcTemp.update("DELETE FROM " + tableName
                + " WHERE " + primaryKeyColumn + " = ?", key));
    }
}