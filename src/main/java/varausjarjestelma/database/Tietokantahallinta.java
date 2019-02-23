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

/**
 * Sovelluksen toinen "pääluokka".
 * Tämän luokan tehtävä on vastata tietokantaan pääsystä, taulujen luonnista ja DAO-luokista.
 * 
 * @author Matias
 */
@Component
public class Tietokantahallinta {

    private Map<Class<?>, Dao<?, ?>> daos;
    private Map<String, Dao<?, ?>> daosByDatatypeName;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Tietokantahallinta() {
        this.daos = new HashMap<>();
        this.daosByDatatypeName = new HashMap<>();
    }

    /**
     * Tekee tarvittavat alkutoimet, kuten luotaulut ja rekisteröi DAO-luokat.
     * @throws Exception
     */
    public void initialize() throws Exception {
        setupTables();
        registerDaos();
    }

    /**
     * Rekisteröi DAO-luokat.
     */
    private void registerDaos() {
        registerDao(new AsiakasDao(this));
        registerDao(new HuoneDao(this));
        registerDao(new HuonetyyppiDao(this));
        registerDao(new LisavarustetyyppiDao(this));
        registerDao(new VarausDao(this));
    }

    /**
     * Rekisteröi DAO-luokan.
     * @param dao
     */
    private void registerDao(Dao<?, ?> dao) {
        String resultClassName = dao.getResultClass().getSimpleName().toLowerCase();
        if (daosByDatatypeName.containsKey(resultClassName)) {
            throw new RuntimeException("Tyypille \"" + resultClassName + "\" on jo rekisteröity DAO-luokka!");
        }
        daos.put(dao.getClass(), dao);
        daosByDatatypeName.put(resultClassName, dao);
    }

    /**
     * Luo tietokantataulut.
     * @throws Exception
     */
    private void setupTables() throws Exception {
        List<TietokantatauluRakentaja> tables = buildTables();
        // Poista vanhat taulut, mikäli sellaisia on. TODO: Pitäisikö?
        jdbcTemplate.batchUpdate(tables.stream().map(t -> "DROP TABLE IF EXISTS " + t.getTable()).toArray(String[]::new));
        // Luo uudet taulut.
        jdbcTemplate.batchUpdate(tables.stream().map(TietokantatauluRakentaja::getCreateTableQuery).toArray(String[]::new));
        // Luo indeksit jne.
        tables.stream().filter(t -> !t.getPostProcessSteps().isEmpty())
                .forEach(t -> jdbcTemplate.batchUpdate(t.getPostProcessSteps()
                        .toArray(new String[t.getPostProcessSteps().size()])));
    }

    /**
     * Suorita tietokantakysely.
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
     * @param classz DAO-tyyppi
     * @return Palauttaa DAO-tyyppiä vastaavan Data Access Objektin
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
     * @return Palauttaa POJO-luokkaa vastaavan geneerisen Data Access Objektin
     */
    public Dao<?, ?> getDaoByResultClass(Class<?> resultClass) {
        return getDaoByResultClassName(resultClass.getSimpleName());
    }

    /**
     * @param resultClassName POJO-luokan datatyypin nimi, kuten Asiakas
     * @return Palauttaa POJO-luokan nimeä vastaavan geneerisen Data Access Objektin
     */
    public Dao<?, ?> getDaoByResultClassName(String resultClassName) {
        resultClassName = resultClassName.toLowerCase();
        Dao<?, ?> dao = daosByDatatypeName.get(resultClassName);
        return dao;
    }

    /**
     * @return Palauttaa SQL-kyselyt tietokantataulujen luontiin listassa oikeassa järjestyksessä
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