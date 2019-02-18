package varausjarjestelma.database;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class Tietokantahallinta {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Palauttaa universaalin JdbcTemplaten,
     * jota käytetään koko sovelluskelle.
     * @return JdbcTemplate
     */
    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    /**
     * Luo tietokantataulut.
     * @throws Exception
     */
    public void setupTables() throws Exception {
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
                // .addPostProcessStep("CREATE INDEX idx_asiakas_id ON Asiakas (id);")
                .addPostProcessStep("CREATE INDEX idx_asiakas_sahkopostiosoite ON Asiakas (sahkopostiosoite)"));
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
                .addPostProcessStep("CREATE INDEX idx_varaus_asiakas_id ON Varaus (asiakas_id)")
                .addPostProcessStep("CREATE INDEX idx_varaus_alkupaivamaara ON Varaus (alkupaivamaara)")
                .addPostProcessStep("CREATE INDEX idx_varaus_loppupaivamaara ON Varaus (loppupaivamaara)"));
        // Lisavaruste-taulu
        tables.add(Tietokantataulu.newTable("Lisavaruste")
                .addColumn("id", "INTEGER", "AUTO_INCREMENT")
                .addColumn("varuste", "VARCHAR(200)", "NOT NULL")
                .setPrimaryKey("id")
                .addPostProcessStep("CREATE INDEX idx_lisavaruste_varuste ON Lisavaruste (varuste)"));
        // VarausLisavaruste-liitostaulu
        tables.add(Tietokantataulu.newTable("VarausLisavaruste")
                .addColumn("varaus_id", "INTEGER")
                .addColumn("lisavaruste_id", "INTEGER")
                .setPrimaryKey("varaus_id", "lisavaruste_id") // TODO hmm.. index?
                .setForeignKey("varaus_id", "Varaus(id)")
                .setForeignKey("lisavaruste_id", "Lisavaruste(id)"));
        // Huonetyyppi-taulu
        tables.add(Tietokantataulu.newTable("Huonetyyppi")
                .addColumn("id", "INTEGER", "AUTO_INCREMENT")
                .addColumn("tyyppi", "VARCHAR(200)", "NOT NULL")
                .setPrimaryKey("id")
                .addPostProcessStep("CREATE INDEX idx_huonetyyppi_tyyppi ON Huonetyyppi (tyyppi)"));
        // Huone-taulu
        tables.add(Tietokantataulu.newTable("Huone")
                .addColumn("huonenumero", "INTEGER")
                .addColumn("huonetyyppi_id", "INTEGER", "NOT NULL")
                .addColumn("paivahinta", "NUMERIC(12, 2)")
                .setPrimaryKey("huonenumero")
                .setForeignKey("huonetyyppi_id", "Huonetyyppi(id)")
                .addPostProcessStep("CREATE INDEX idx_huone_huonetyyppi_id ON Huone (huonetyyppi_id)")
                .addPostProcessStep("CREATE INDEX idx_huone_paivahinta ON Huone (paivahinta)"));
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