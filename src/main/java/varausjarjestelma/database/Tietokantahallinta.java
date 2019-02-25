package varausjarjestelma.database;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.aop.framework.Advised;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import varausjarjestelma.database.dao.Dao;

/**
 * Sovelluksen toinen "pääluokka".
 * Tämän luokan tehtävä on vastata tietokantaan pääsystä, taulujen luonnista ja DAO-luokista.
 * 
 * @author Matias
 */
@Component
public class Tietokantahallinta {

    @Autowired
    private List<Dao<?, ?>> daos; // Spring täydentää tämän automaattisesti.
    private Map<Class<?>, Dao<?, ?>> daosByClass;
    private Map<String, Dao<?, ?>> daosByDatatypeName;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Tietokantahallinta() {
        this.daosByClass = new HashMap<>();
        this.daosByDatatypeName = new HashMap<>();
    }

    /**
     * Tekee tarvittavat alkutoimet, kuten luotaulut ja rekisteröi DAO-luokat.
     * @throws Exception
     */
    public void initialize() throws Exception {
        setupTables();
        daos.forEach(this::registerDao);
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
        Advised advised = (Advised) dao;
        // Spring alustaa luokat eri tavalla, jolloin getClass()-metodi ei palauta todellista
        // luokkaa.
        Class<?> targetClass = advised.getTargetSource().getTargetClass();
        daosByClass.put(targetClass, dao);
        daosByDatatypeName.put(resultClassName, dao);
    }

    /**
     * Luo tietokantataulut.
     * @throws Exception
     */
    private void setupTables() throws Exception {
        List<TietokantatauluRakentaja> tables = buildTables();
        /** for (TietokantatauluRakentaja rakentaja : tables) {
            System.out.println("table create query: " + rakentaja.getCreateTableQuery());
            rakentaja.getPostProcessSteps().forEach(System.out::println);
        }
        */
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
     * Palauttaa Data Access Objecktin. DAO-luokkkiin on nivottu yhteen kummankin Data Access Layerin
     * ja Service Layerin (Application Layer) ominaisuudet.
     * @param classz DAO-tyyppi
     * @return Palauttaa DAO-tyyppiä vastaavan Data Access Objektin
     */
    public <T extends Dao<?, ?>> T getDao(Class<T> classz) {
        Dao<?, ?> dao = daosByClass.get(classz);
        if (dao == null) {
            return null;
        }
        return classz.cast(dao);
    }

    /**
     * Palauttaa Data Access Objecktin. DAO-luokkkiin on nivottu yhteen kummankin Data Access Layerin
     * ja Service Layerin (Application Layer) ominaisuudet.
     * @param resultClass POJO-luokka
     * @return Palauttaa POJO-luokkaa vastaavan geneerisen Data Access Objektin
     */
    public Dao<?, ?> getDaoByResultClass(Class<?> resultClass) {
        return getDaoByResultClassName(resultClass.getSimpleName());
    }

    /**
     * Palauttaa Data Access Objecktin. DAO-luokkkiin on nivottu yhteen kummankin Data Access Layerin
     * ja Service Layerin (Application Layer) ominaisuudet.
     * @param resultClassName POJO-luokan datatyypin nimi, kuten Asiakas
     * @return Palauttaa POJO-luokan nimeä vastaavan geneerisen Data Access Objektin
     */
    public Dao<?, ?> getDaoByResultClassName(String resultClassName) {
        resultClassName = resultClassName.toLowerCase();
        return daosByDatatypeName.get(resultClassName);
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
                .addColumn("sahkopostiosoite", "VARCHAR(50)", "UNIQUE")
                .setPrimaryKey("id")
                .addPostProcessStep("CREATE INDEX idx_sahkopostiosoite ON Asiakas (sahkopostiosoite)"));
        // Varaus-taulu
        tables.add(TietokantatauluRakentaja.newTable("Varaus")
                .addColumn("id", "INTEGER", "AUTO_INCREMENT")
                .addColumn("asiakas_id", "INTEGER", "NOT NULL")
                .addColumn("alkupaivamaara", "TIMESTAMP", "NOT NULL")
                .addColumn("loppupaivamaara", "TIMESTAMP", "NOT NULL")
                .addColumn("yhteishinta", "NUMERIC(12, 2)", "NOT NULL")
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
                .addColumn("varaus_id", "INTEGER", "NOT NULL")
                .addColumn("lisavarustetyyppi_id", "INTEGER", "NOT NULL")
                .setForeignKey("varaus_id", "Varaus(id)")
                .setForeignKey("lisavarustetyyppi_id", "Lisavarustetyyppi(id)")
                .addPostProcessStep("CREATE INDEX idx_varaus_id ON Lisavaruste (varaus_id)")
                .addPostProcessStep("CREATE INDEX idx_lisavarustetyyppi_id ON Lisavaruste (lisavarustetyyppi_id)"));
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