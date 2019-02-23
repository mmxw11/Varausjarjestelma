package varausjarjestelma.database;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import varausjarjestelma.database.dao.AsiakasDao;
import varausjarjestelma.database.dao.Dao;
import varausjarjestelma.database.dao.HuoneDao;
import varausjarjestelma.database.dao.HuonetyyppiDao;
import varausjarjestelma.database.dao.LisavarustetyyppiDao;
import varausjarjestelma.database.dao.VarausDao;
import varausjarjestelma.domain.serialization.testdata.HuoneTestDao;

/**
 * @author Matias
 */
@Component
public class Tietokantahallinta {

    private Map<Class<?>, Dao<?, ?>> daos;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Tietokantahallinta() {
        this.daos = new HashMap<>();
    }

    public void initialize() throws Exception {
        setupTables();
        registerDaos();
    }

    private void registerDaos() {
        daos.put(AsiakasDao.class, new AsiakasDao(this));
        daos.put(VarausDao.class, new VarausDao(this));
        daos.put(LisavarustetyyppiDao.class, new LisavarustetyyppiDao(this));
        daos.put(HuoneDao.class, new HuoneDao(this));
        daos.put(HuonetyyppiDao.class, new HuonetyyppiDao(this));
        //
        daos.put(HuoneTestDao.class, new HuoneTestDao(this));
    }

    /**
     * Luo tietokantataulut.
     * @throws Exception
     */
    private void setupTables() throws Exception { // TODO: Use wrapper?
        List<TietokantatauluRakentaja> tables = buildTables();
        // Poista vanhat taulut, mikäli sellaisia on.
        jdbcTemplate.batchUpdate(tables.stream().map(t -> "DROP TABLE IF EXISTS " + t.getTable()).toArray(String[]::new));
        // Luo uudet taulut.
        jdbcTemplate.batchUpdate(tables.stream().map(TietokantatauluRakentaja::getCreateTableQuery).toArray(String[]::new));
        // Luo indeksit jne.
        tables.stream().filter(t -> !t.getPostProcessSteps().isEmpty())
                .forEach(t -> jdbcTemplate.batchUpdate(t.getPostProcessSteps()
                        .toArray(new String[t.getPostProcessSteps().size()])));
    }

    /**
     * Metodi joka käärii tietokantakyselyt.
     * @see varausjarjestelma.database.JdbcSpringKysely
     * @param kysely
     * @return T
     * @throws SQLException
     */
    public <T> T executeQuery(JdbcSpringKysely<T> kysely) throws SQLException {
        try {
            return kysely.query(jdbcTemplate);
        } catch (DataAccessException e) {
            throw new SQLException(e);
        }
    }

    /**
     * Palauttaa universaalin JdbcTemplaten,
     * jota käytetään koko sovellukselle.
     * @return JdbcTemplate
     */
    public JdbcTemplate getJdbcTemplate() { // TODO: Should this be exposed?
        return jdbcTemplate;
    }

    /**
     * @param classz DAO-tyyppi
     * @return Palauttaa luokkatyyppiä vastaavan Data Access Objektin.
     */
    public <T extends Dao<?, ?>> T getDao(Class<T> classz) {
        Dao<?, ?> dao = daos.get(classz);
        if (dao == null) {
            return null;
        }
        return classz.cast(dao);
    }

    /**
     * @param resultClass POJO-luokka
     * @return Palauttaa POJO-luokkaa vastaavan Data Access Objektin.
     */
    @SuppressWarnings("unchecked")
    public <V, T extends Dao<V, ?>> T getDaoByResultClass(Class<V> resultClass) {
        for (Dao<?, ?> dao : daos.values()) {
            if (dao.getResultClass() == resultClass) {
                return (T) dao;
            }
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    public <V, T extends Dao<V, ?>> T getDaoByResultClassName(String resultClassName) {
        for (Dao<?, ?> dao : daos.values()) {
            if (dao.getResultClass().getSimpleName().equalsIgnoreCase(resultClassName)) {
                return (T) dao;
            }
        }
        return null;
    }

    /**
     * Palauttaa komennot taulujen luontiin listana oikeassa järjestyksessä.
     * @return List<TietokantatauluRakentaja>
     */
    private List<TietokantatauluRakentaja> buildTables() {
        List<TietokantatauluRakentaja> tables = new ArrayList<>();
        // Asiakas-taulu
        tables.add(TietokantatauluRakentaja.newTable("Asiakas")
                .addColumn("id", "INTEGER", "AUTO_INCREMENT")
                .addColumn("nimi", "VARCHAR(200)", "NOT NULL")
                .addColumn("puhelinnumero", "VARCHAR(20)")
                .addColumn("sahkopostiosoite", "VARCHAR(50)")
                .setPrimaryKey("id")
                // TODO: h2 tekee automaattisesti?
                // .addPostProcessStep("CREATE INDEX idx_id ON Asiakas (id);")
                .addPostProcessStep("CREATE INDEX idx_sahkopostiosoite ON Asiakas (sahkopostiosoite)"));
        // Varaus-taulu
        tables.add(TietokantatauluRakentaja.newTable("Varaus")
                .addColumn("id", "INTEGER", "AUTO_INCREMENT")
                .addColumn("asiakas_id", "INTEGER", "NOT NULL")
                .addColumn("alkupaivamaara", "TIMESTAMP")
                .addColumn("loppupaivamaara", "TIMESTAMP")
                .addColumn("varauksenkesto", "INTEGER")
                .addColumn("yhteishinta", "NUMERIC(12, 2)")
                .setPrimaryKey("id")
                .setForeignKey("asiakas_id", "Asiakas(id)")
                .addPostProcessStep("CREATE INDEX idx_asiakas_id ON Varaus (asiakas_id)")
                .addPostProcessStep("CREATE INDEX idx_alkupaivamaara ON Varaus (alkupaivamaara)")
                .addPostProcessStep("CREATE INDEX idx_loppupaivamaara ON Varaus (loppupaivamaara)"));
        // Lisavarustetyyppi-taulu
        tables.add(TietokantatauluRakentaja.newTable("Lisavarustetyyppi")
                .addColumn("id", "INTEGER", "AUTO_INCREMENT")
                .addColumn("varustetyyppi", "VARCHAR(200)", "NOT NULL")
                .setPrimaryKey("id")
                .addPostProcessStep("CREATE INDEX idx_varustetyyppi ON Lisavarustetyyppi (varustetyyppi)"));
        // Lisavaruste-(liitostaulu(?)
        tables.add(TietokantatauluRakentaja.newTable("Lisavaruste")
                .addColumn("varaus_id", "INTEGER")
                .addColumn("lisavarustetyyppi_id", "INTEGER")
                // .setPrimaryKey("varaus_id", "lisavarustetyyppi_id") // TODO hmm.. index?
                .setForeignKey("varaus_id", "Varaus(id)")
                .setForeignKey("lisavarustetyyppi_id", "Lisavarustetyyppi(id)")
                .addPostProcessStep("CREATE INDEX idx_liitos ON Lisavaruste (varaus_id, lisavarustetyyppi_id)"));
        // Huonetyyppi-taulu
        tables.add(TietokantatauluRakentaja.newTable("Huonetyyppi")
                .addColumn("id", "INTEGER", "AUTO_INCREMENT")
                .addColumn("tyyppi", "VARCHAR(200)", "NOT NULL")
                .setPrimaryKey("id")
                .addPostProcessStep("CREATE INDEX idx_tyyppi ON Huonetyyppi (tyyppi)"));
        // Huone-taulu
        tables.add(TietokantatauluRakentaja.newTable("Huone")
                .addColumn("huonenumero", "INTEGER")
                .addColumn("huonetyyppi_id", "INTEGER", "NOT NULL")
                .addColumn("paivahinta", "NUMERIC(12, 2)")
                .setPrimaryKey("huonenumero")
                .setForeignKey("huonetyyppi_id", "Huonetyyppi(id)")
                .addPostProcessStep("CREATE INDEX idx_huonetyyppi_id ON Huone (huonetyyppi_id)")
                .addPostProcessStep("CREATE INDEX idx_paivahinta ON Huone (paivahinta)"));
        // Huonevaraus-liitostaulu
        tables.add(TietokantatauluRakentaja.newTable("HuoneVaraus")
                .addColumn("varaus_id", "INTEGER")
                .addColumn("huonenumero", "INTEGER")
                .setPrimaryKey("varaus_id", "huonenumero") // TODO hmm.. index?
                .setForeignKey("varaus_id", "Varaus(id)")
                .setForeignKey("huonenumero", "Huone(huonenumero)"));
        return tables;
    }
}