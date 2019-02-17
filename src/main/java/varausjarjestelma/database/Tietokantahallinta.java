package varausjarjestelma.database;

import java.util.List;

public class Tietokantahallinta {

    private List<Tietokantataulu> buildTables() {
        // Asiakas
        Tietokantataulu taulu = Tietokantataulu.newTable("Asiakas")
                .addColumn("id", "INTEGER", "AUTO_INCREMENT")
                .addColumn("nimi", "VARCHAR(200)", "NOT NULL")
                .addColumn("puhelinnumero", "VARCHAR(20)")
                .addColumn("sahkopostiosoite", "VARCHAR(50)")
                .setPrimaryKey("id")
                .addPostProcessStep("CREATE INDEX idx_asiakas_id ON Asiakas (id);")
                .addPostProcessStep("CREATE INDEX idx_asiakas_sahkopostiosoite ON Asiakas (sahkopostiosoite);");
        // Varaus WIP
        // VarausLisavaruste WIP
        // Lisavaruste WIP
        // Huonevaraus WIP
        // Huone WIP
        // Huonetyyppi WIP
        return null;
    }
}