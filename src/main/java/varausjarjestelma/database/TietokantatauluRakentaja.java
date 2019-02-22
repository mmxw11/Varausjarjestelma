package varausjarjestelma.database;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Matias
 */
public class TietokantatauluRakentaja {

    private String table;
    private StringBuilder builder;
    private List<String> postProcessSteps;

    private TietokantatauluRakentaja(String table) {
        this.table = table;
        this.builder = new StringBuilder();
        this.postProcessSteps = new ArrayList<>();
        builder.append("CREATE TABLE ").append(table).append(" (");
    }

    public static TietokantatauluRakentaja newTable(String table) {
        return new TietokantatauluRakentaja(table);
    }

    /**
     * Lisää sarake.
     * @param column Sarakkeen nimi
     * @param type Sarakkeen tyyppi, kuten INTEGER
     * @param options Mahdolliset lisäasetukset, kuten AUTO_INCREMENT
     * @return this
     */
    public TietokantatauluRakentaja addColumn(String column, String type, String... options) {
        builder.append(column).append(" ").append(type);
        if (options.length != 0) {
            for (String o : options) {
                builder.append(" ").append(o);
            }
        }
        builder.append(", ");
        return this;
    }

    /**
     * Aseta pääavain.
     * @param column Sarakkeen nimi
     * @param columns Mahdolliset muut pääavaimen määräävät sarakkeet
     * @return this
     */
    public TietokantatauluRakentaja setPrimaryKey(String column, String... columns) {
        builder.append("PRIMARY KEY").append(" (").append(column);
        if (columns.length != 0) {
            for (String c : columns) {
                builder.append(", ").append(c);
            }
        }
        builder.append("), ");
        return this;
    }

    /**
     * Aseta viiteavain.
     * @param column Sarakkeen nimi
     * @param target Taulu ja siinä sijaitse sarake, mihin viiteavain viittaa muodossa Taulu(sarake)
     * @return this
     */
    public TietokantatauluRakentaja setForeignKey(String column, String target) {
        builder.append("FOREIGN KEY").append(" (").append(column).append(") REFERENCES ")
                .append(target).append(", ");
        return this;
    }

    /**
     * Lisää mahdollinen rajoite.
     * @param constraints 
     * @return this
     */
    public TietokantatauluRakentaja addConstraints(String constraint) {
        builder.append(constraint).append(", ");
        return this;
    }

    /**
     * Lisää taulun luontiin liittyvän vaiheen.
     * Nämä suoritetaan, kun kaikki taulut ovat luotu. 
     * Tänne voi lisätä vaikka indeksin luomisen.
     * @param step Vaihe
     * @return this
     */
    public TietokantatauluRakentaja addPostProcessStep(String step) {
        postProcessSteps.add(step);
        return this;
    }

    /**
     * Palauttaa taulun nimen.
     * @return string
     */
    public String getTable() {
        return table;
    }

    /**
     * Rakentaa taulun luontiin tarvittavan SQL-kyselyn.
     * @return string
     */
    public String getCreateTableQuery() {
        if (builder.charAt(builder.length() - 2) != ',') {
            throw new IllegalStateException("Taulukon luontiin tarvitaan vähintään yksi sarake!");
        }
        // Poista pilkku viimeisen sarakkeen/rajoitteen jälkeen.
        builder.delete(builder.length() - 2, builder.length());
        builder.append(")");
        return builder.toString();
    }

    /**
     * @return Palauttaa taulun luontiin liittyvät lisävaiheet
     */
    public List<String> getPostProcessSteps() {
        return postProcessSteps;
    }
}