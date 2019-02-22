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
import varausjarjestelma.database.dao.HuonetyyppiDao;
import varausjarjestelma.database.dao.LisavarustetyyppiDao;

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
        // WIP varaus
        daos.put(LisavarustetyyppiDao.class, new LisavarustetyyppiDao(this));
        // WIP huone
        daos.put(HuonetyyppiDao.class, new HuonetyyppiDao(this));
    }

    /**
     * Luo tietokantataulut.
     * @throws Exception
     */
    private void setupTables() throws Exception { // TODO: Use wrapper?
        List<Tietokantataulu> tables = buildTables();
        // Poista vanhat taulut, mikäli sellaisia on.
        jdbcTemplate.batchUpdate(tables.stream().map(t -> "DROP TABLE IF EXISTS " + t.getTable()).toArray(String[]::new));
        // Luo uudet taulut.
        jdbcTemplate.batchUpdate(tables.stream().map(Tietokantataulu::getCreateTableQuery).toArray(String[]::new));
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
     * Palauttaa komennot taulujen luontiin listana oikeassa järjestyksessä.
     * @return List<Tietokantataulu>
     */
    private List<Tietokantataulu> buildTables() {
        List<Tietokantataulu> tables = new ArrayList<>();
        // Asiakas-taulu
        tables.add(Tietokantataulu.newTable("Asiakas")
                .addColumn("id", "INTEGER", "AUTO_INCREMENT")
                .addColumn("nimi", "VARCHAR(200)", "NOT NULL")
                .addColumn("puhelinnumero", "VARCHAR(20)")
                .addColumn("sahkopostiosoite", "VARCHAR(50)")
                .setPrimaryKey("id")
                // TODO: h2 tekee automaattisesti?
                // .addPostProcessStep("CREATE INDEX idx_id ON Asiakas (id);")
                .addPostProcessStep("CREATE INDEX idx_sahkopostiosoite ON Asiakas (sahkopostiosoite)"));
        // Varaus-taulu
        tables.add(Tietokantataulu.newTable("Varaus")
                .addColumn("id", "INTEGER", "AUTO_INCREMENT")
                .addColumn("asiakas_id", "INTEGER", "NOT NULL")
                .addColumn("alkupaivamaara", "DATE")
                .addColumn("loppupaivamaara", "DATE")
                .addColumn("varauksenkesto", "INTEGER")
                .addColumn("yhteishinta", "NUMERIC(12, 2)")
                .setPrimaryKey("id")
                .setForeignKey("asiakas_id", "Asiakas(id)")
                .addPostProcessStep("CREATE INDEX idx_asiakas_id ON Varaus (asiakas_id)")
                .addPostProcessStep("CREATE INDEX idx_alkupaivamaara ON Varaus (alkupaivamaara)")
                .addPostProcessStep("CREATE INDEX idx_loppupaivamaara ON Varaus (loppupaivamaara)"));
        // Lisavarustetyyppi-taulu
        tables.add(Tietokantataulu.newTable("Lisavarustetyyppi")
                .addColumn("id", "INTEGER", "AUTO_INCREMENT")
                .addColumn("varustetyyppi", "VARCHAR(200)", "NOT NULL")
                .setPrimaryKey("id")
                .addPostProcessStep("CREATE INDEX idx_varustetyyppi ON Lisavarustetyyppi (varustetyyppi)"));
        // Lisavaruste-(liitostaulu(?)
        tables.add(Tietokantataulu.newTable("Lisavaruste")
                .addColumn("varaus_id", "INTEGER")
                .addColumn("lisavarustetyyppi_id", "INTEGER")
                .setPrimaryKey("varaus_id", "lisavarustetyyppi_id") // TODO hmm.. index?
                .setForeignKey("varaus_id", "Varaus(id)")
                .setForeignKey("lisavarustetyyppi_id", "Lisavarustetyyppi(id)"));
        // Huonetyyppi-taulu
        tables.add(Tietokantataulu.newTable("Huonetyyppi")
                .addColumn("id", "INTEGER", "AUTO_INCREMENT")
                .addColumn("tyyppi", "VARCHAR(200)", "NOT NULL")
                .setPrimaryKey("id")
                .addPostProcessStep("CREATE INDEX idx_tyyppi ON Huonetyyppi (tyyppi)"));
        // Huone-taulu
        tables.add(Tietokantataulu.newTable("Huone")
                .addColumn("huonenumero", "INTEGER")
                .addColumn("huonetyyppi_id", "INTEGER", "NOT NULL")
                .addColumn("paivahinta", "NUMERIC(12, 2)")
                .setPrimaryKey("huonenumero")
                .setForeignKey("huonetyyppi_id", "Huonetyyppi(id)")
                .addPostProcessStep("CREATE INDEX idx_huonetyyppi_id ON Huone (huonetyyppi_id)")
                .addPostProcessStep("CREATE INDEX idx_paivahinta ON Huone (paivahinta)"));
        // Huonevaraus-liitostaulu
        tables.add(Tietokantataulu.newTable("HuoneVaraus")
                .addColumn("varaus_id", "INTEGER")
                .addColumn("huonenumero", "INTEGER")
                .setPrimaryKey("varaus_id", "huonenumero") // TODO hmm.. index?
                .setForeignKey("varaus_id", "Varaus(id)")
                .setForeignKey("huonenumero", "Huone(huonenumero)"));
        return tables;
    }
}