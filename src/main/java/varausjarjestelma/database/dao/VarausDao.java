package varausjarjestelma.database.dao;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import varausjarjestelma.database.SQLJoinVarasto;
import varausjarjestelma.database.SQLKyselyRakentaja;
import varausjarjestelma.database.Tietokantahallinta;
import varausjarjestelma.domain.Asiakas;
import varausjarjestelma.domain.Huone;
import varausjarjestelma.domain.Lisavarustetyyppi;
import varausjarjestelma.domain.Varaus;
import varausjarjestelma.domain.serialization.LuokkaSerializer;
import varausjarjestelma.domain.serialization.TauluSarake;

/**
 * @author Matias
 */
@Repository
public class VarausDao extends Dao<Varaus, Integer> {

    public VarausDao(Tietokantahallinta thallinta) {
        super(thallinta, "Varaus", "id", Varaus.class);
    }

    @Override
    protected void initalizeSerializerSettings(LuokkaSerializer<Varaus> serializer) {
        serializer.setJoinClauseType("JOIN");
        // Muunna asiakas-luokka ID:ksi.
        serializer.registerSerializerStrategy("asiakas", Asiakas.class, Asiakas::getId);
        // Muutetaan päivämäärät LocalDateTime-olioiksi.
        serializer.registerDeserializerStrategy("alkupaivamaara", rs -> rs.getTimestamp("alkupaivamaara").toLocalDateTime());
        serializer.registerDeserializerStrategy("loppupaivamaara", rs -> rs.getTimestamp("loppupaivamaara").toLocalDateTime());
        // Lisää dynaamiset kyselyt.
        serializer.registerDynamicTypeQueryStrategy("huonemaara",
                "SUM(case WHEN Huonevaraus.varaus_id = Varaus.id then 1 else 0 end)");
        serializer.registerDynamicTypeQueryStrategy("lisavarustemaara",
                "SUM(case WHEN Lisavaruste.varaus_id = Varaus.id then 1 else 0 end)");
    }

    /**
     * Luo uuden varauksen ja siihen liittyvät muut asiat tietokantaan.
     * Lisää siis tarvittaessa varuksen lisäksi asiakkaan ja lisävarusteet.
     * <p>
     * <b>Note</b>: Tämä metodi suorittaa kaikki kyselyt samassa tietokantatransaktionissa!
     * Mikäli virhe siis tapahtuu, tullaan kaikki muutokset peruuttamaan automaattisesti.
     * </p>
     * @param asiakas
     * @param huoneet
     * @param alkupaivamaara
     * @param loppupaivamaara
     * @param lisavarusteet
     * @return Palauttaa juuri luodun varauksen
     * @throws SQLException
     */
    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public Varaus bookHotelliHuoneita(Asiakas asiakas, List<Huone> huoneet, LocalDateTime alkupaivamaara, LocalDateTime loppupaivamaara, List<String> lisavarusteet)
            throws SQLException {
        // Asiakas.
        if (asiakas.getId() == -1) {
            AsiakasDao asiakasDao = thallinta.getDao(AsiakasDao.class);
            Asiakas lasiakas = asiakasDao.readBySahkopostiosoite(asiakas.getSahkopostiosoite());
            if (lasiakas != null) {
                // Yhdella asiakkaalla voi olla vain sama sähköpostiosoite. Mikäli asiakkaan muut
                // tiedot täsmäävät, niin hän on sama asiakas.
                // Muuten palautetaan virhe, jossa ilmoitetaan, että sähköpostiosoite on jo
                // käytössä.
                if (!lasiakas.getNimi().equals(asiakas.getNimi()) || !lasiakas.getPuhelinnumero().equals(asiakas.getPuhelinnumero())) {
                    throw new SQLException("Tällä sähköpostiosoitteella on jo rekisteröitynyt asiakas!");
                }
                // Sama asiakas.
                asiakas.setId(lasiakas.getId());
            } else {
                // Luo uusi asiakas.
                asiakasDao.create(asiakas);
            }
        }
        // Lisävarusteet.
        Map<String, VarausLisavarusteMaara> lvarustetyypit = null;
        if (!lisavarusteet.isEmpty()) {
            lvarustetyypit = new HashMap<>();
            // Lajittele lisävarusteet
            // Sama lisävarustetyyppi voi olla yhdessä varauksessa monesti esim. 2 silitysrautaa.
            for (String varustetyyppi : lisavarusteet) {
                VarausLisavarusteMaara vlmaara = lvarustetyypit.get(varustetyyppi);
                if (vlmaara == null) {
                    vlmaara = new VarausLisavarusteMaara();
                    lvarustetyypit.put(varustetyyppi, vlmaara);
                }
                vlmaara.addLisavaruste();
            }
            LisavarustetyyppiDao lvarusteDao = thallinta.getDao(LisavarustetyyppiDao.class);
            // Päivitä löydetyt lisävarustetyypit.
            for (Lisavarustetyyppi loydetytLvTyyppi : lvarusteDao.readLisavarustetyyppit(lvarustetyypit.keySet())) {
                lvarustetyypit.get(loydetytLvTyyppi.getVarustetyyppi()).setLisavarustetyyppi(loydetytLvTyyppi);
            }
            // Luo uudet lisävarustetyypit niille, joita ei tietokannasta löytynyt.
            for (Map.Entry<String, VarausLisavarusteMaara> entry : lvarustetyypit.entrySet()) {
                VarausLisavarusteMaara lvmaara = entry.getValue();
                if (lvmaara.lisavarustetyyppi == null) {
                    lvmaara.setLisavarustetyyppi(new Lisavarustetyyppi(entry.getKey()));
                }
            }
            // Luo uudet lisävarustetyypit tietokantaan.
            lvarusteDao.createLisavarustetyypit(lvarustetyypit.values().stream().map(VarausLisavarusteMaara::getLisavarustetyyppi).collect(Collectors.toList()));
        }
        // Muuta päiviksi, joten kellonaika ei häiritse laskua.
        long bookedDays = ChronoUnit.DAYS.between(alkupaivamaara.toLocalDate(), loppupaivamaara.toLocalDate());
        double yhteishinta = huoneet.stream().mapToDouble(e -> e.getPaivahinta().doubleValue() * bookedDays).sum();
        Varaus varaus = new Varaus(asiakas, alkupaivamaara, loppupaivamaara, new BigDecimal(yhteishinta), huoneet.size(),
                lvarustetyypit == null ? 0 : lvarustetyypit.values().stream().mapToInt(VarausLisavarusteMaara::getLisavarusteMaara).sum());
        // Luo uusi varaus.
        create(varaus);
        // Lisää Liitostaulu kyselyt.
        List<String> junctionTableQueries = new ArrayList<>();
        // Lisää HuoneVaraus-liitostaulu kyselyt.
        huoneet.forEach(huone -> junctionTableQueries.add("INSERT INTO HuoneVaraus (varaus_id, huonenumero) "
                + "VALUES (" + varaus.getId() + ", " + huone.getHuonenumero() + ")"));
        // Lisää Lisävaruste-liitostaulu kyselyt.
        if (lvarustetyypit != null) {
            lvarustetyypit.forEach((k, v) -> {
                // Sama lisävarustetyyppi voi olla monesti varauksessa, vaikka 2 silitysrautaa.
                for (int i = 0; i < v.getLisavarusteMaara(); i++) {
                    junctionTableQueries.add("INSERT INTO Lisavaruste (varaus_id, lisavarustetyyppi_id) "
                            + "VALUES (" + varaus.getId() + ", " + v.lisavarustetyyppi.getId() + ")");
                }
            });
        }
        // Lisää tiedot liitostauluihin.
        thallinta.executeQuery(jdbcTemp -> jdbcTemp.batchUpdate(junctionTableQueries.toArray(new String[junctionTableQueries.size()])));
        return varaus;
    }

    @Override
    public Varaus read(Integer key) throws SQLException {
        SQLJoinVarasto joinVarasto = buildJoinVarasto();
        List<TauluSarake> columns = serializer.convertClassFieldsToColumns(tableName, joinVarasto);
        // Näitä tarvitaan huone- ja lisävarustemäärän laskentaan.
        joinVarasto.addSQLJoinClause("Lisavaruste", "LEFT JOIN Lisavaruste ON Lisavaruste.varaus_id = Varaus.id")
                .addSQLJoinClause("Lisavarustetyyppi", "LEFT JOIN Lisavarustetyyppi ON Lisavarustetyyppi.id = Lisavaruste.lisavarustetyyppi_id")
                .addSQLJoinClause("Huonevaraus", "JOIN Huonevaraus ON Huonevaraus.varaus_id = Varaus.id")
                .addSQLJoinClause("Huone", "JOIN Huone ON Huone.huonenumero = Huonevaraus.huonenumero");
        String sql = SQLKyselyRakentaja.buildSelectQuery(resultClass, tableName, columns, joinVarasto)
                .append(" WHERE ")
                .append(tableName)
                .append(".")
                .append(primaryKeyColumn)
                .append(" = ?")
                .append(" GROUP BY ")
                .append(tableName)
                .append(".")
                .append(primaryKeyColumn)
                .toString();
        return queryObjectFromDatabase(sql, key);
    }

    /**
     * Sisäinen luokka, joka pitää kirjaa lisävarusteista varausta tehdessä.
     * 
     * @author Matias
     */
    private static class VarausLisavarusteMaara {

        private Lisavarustetyyppi lisavarustetyyppi;
        private int amount;

        public void setLisavarustetyyppi(Lisavarustetyyppi lisavarustetyyppi) {
            this.lisavarustetyyppi = lisavarustetyyppi;
        }

        public void addLisavaruste() {
            this.amount += 1;
        }

        public Lisavarustetyyppi getLisavarustetyyppi() {
            return lisavarustetyyppi;
        }

        public int getLisavarusteMaara() {
            return amount;
        }
    }
}