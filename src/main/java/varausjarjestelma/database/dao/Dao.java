package varausjarjestelma.database.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import varausjarjestelma.database.SQLJoinVarasto;
import varausjarjestelma.database.SQLKyselyRakentaja;
import varausjarjestelma.database.Tietokantahallinta;
import varausjarjestelma.domain.serialization.LuokkaSerializer;
import varausjarjestelma.domain.serialization.TauluSarake;
import varausjarjestelma.domain.serialization.TulosLuokkaRakentaja;

/**
 * Abstrakti DAO-luokka, joka tarjoaa yleiset CRUD-metodit POJO-luokille (domain).
 * Tämä luokka on tarkoitettu perittäväksi, johon perivät luokat lisäävät ominaisuuksia
 * hyödyntäen tarjottuja metodeja. DAO-luokat toimivat kumpinakin Data Access ja 
 * Service Layereina (Application Layer), joten niiden ominaisuudet ovat nivottuna näihin DAO-luokkiin.
 * 
 * @author Matias
 */
public abstract class Dao<T, K> {

    protected Tietokantahallinta thallinta;
    protected String tableName;
    protected String primaryKeyColumn;
    protected Class<T> resultClass;
    protected boolean autoGeneratePrimaryKey;
    protected LuokkaSerializer<T> serializer;

    public Dao(Tietokantahallinta thallinta, String tableName, String primaryKeyColumn, Class<T> resultClass) {
        this.thallinta = thallinta;
        this.tableName = tableName;
        this.primaryKeyColumn = primaryKeyColumn;
        this.resultClass = resultClass;
        this.autoGeneratePrimaryKey = true;
        // Alusta serializeri.
        LuokkaSerializer<T> lserializer = new LuokkaSerializer<>(resultClass, thallinta);
        initalizeSerializerSettings(lserializer);
        this.serializer = lserializer;
    }

    /**
     * Luoko tietokanta pääavaimen automaattisesti. 
     * @param value
     */
    protected void setAutoGeneratePrimaryKey(boolean value) {
        this.autoGeneratePrimaryKey = value;
    }

    /**
     * Alustaa tietotyypin käsittelyyn vaadittavat asetukset.
     * @param serializer
     */
    protected abstract void initalizeSerializerSettings(LuokkaSerializer<T> serializer);

    /**
     * Lisää olion tiedot tietokantaan.
     * @param object
     * @throws SQLException
     */
    public void create(T object) throws SQLException {
        Map<String, Object> fields = serializer.serializeObject(object);
        if (autoGeneratePrimaryKey) {
            // Poista pääavain, jos tietokanta luo sen automaattisesti.
            // Riippuen datatyypistä, mutta pääavaimen oletusarvo on yleensä esim. -1.
            fields.remove(primaryKeyColumn);
        }
        String sql = SQLKyselyRakentaja.buildCreateQuery(tableName, fields.keySet());
        KeyHolder keyHolder = new GeneratedKeyHolder();
        thallinta.executeQuery(jdbcTemp -> jdbcTemp.update(conn -> {
            PreparedStatement pstatement = conn.prepareStatement(sql,
                    autoGeneratePrimaryKey ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS);
            int i = 0;
            for (Object data : fields.values()) {
                pstatement.setObject(++i, data);
            }
            return pstatement;
        }, keyHolder));
        if (autoGeneratePrimaryKey) {
            // Päivitetään juuri luodun rivin pääavain target objektiin.
            Object key = keyHolder.getKeys().values().iterator().next();
            serializer.setField(primaryKeyColumn, key, object);
        }
    }

    /**
     * Hakee tietokannasta tietueen, jonka pääavain vastaa parametrina saatua.
     * @param key
     * @return Palauttaa tiedoista luodun olion
     * @throws SQLException
     */
    public T read(K key) throws SQLException {
        SQLJoinVarasto joinVarasto = buildJoinVarasto();
        List<TauluSarake> columns = serializer.convertClassFieldsToColumns(tableName, joinVarasto);
        String sql = SQLKyselyRakentaja.buildSelectQuery(resultClass, tableName, columns, joinVarasto)
                + " WHERE " + tableName + "." + primaryKeyColumn + " = ?";
        return queryObjectFromDatabase(sql, key);
    }

    /**
     * Tekee hakukyselyn.
     * @param sql
     * @param parameters
     * @return Palauttaa tiedoista luodun olion
     * @throws SQLException
     */
    protected T queryObjectFromDatabase(String sql, Object... parameters) throws SQLException {
        T result = thallinta.executeQuery(jdbcTemp -> {
            try {
                // TulosLuokkaRakentaja käyttää Springin BeanWrapperia, joka käyttää settereitä
                // arvojen lisäämiseen, siksi kaikissa luokissa on oltava setterit mukana :/
                return jdbcTemp.queryForObject(sql, new TulosLuokkaRakentaja<>(this, thallinta), parameters);
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
        Map<String, Object> fields = serializer.serializeObject(object);
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
    
    protected SQLJoinVarasto buildJoinVarasto() {
        if (serializer.getJoinClauseType() == null) {
            return null;
        }
        return new SQLJoinVarasto();
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

    public String getTableName() {
        return tableName;
    }

    public String getPrimaryKeyColumn() {
        return primaryKeyColumn;
    }

    public Class<T> getResultClass() {
        return resultClass;
    }

    public LuokkaSerializer<T> getSerializer() {
        return serializer;
    }
}