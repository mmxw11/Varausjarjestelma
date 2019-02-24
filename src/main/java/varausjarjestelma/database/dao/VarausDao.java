package varausjarjestelma.database.dao;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

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

    @Transactional
    public Varaus bookHotelliHuoneita(Asiakas asiakas, List<Huone> huoneet, LocalDateTime alkupaivamaara, LocalDateTime loppupaivamaara, List<String> lisavarusteet)
            throws SQLException {
        if (asiakas.getId() == -1) {
            AsiakasDao asiakasDao = thallinta.getDao(AsiakasDao.class);
            Asiakas lasiakas = asiakasDao.readBySahkopostiosoite(asiakas.getSahkopostiosoite());
            if (lasiakas != null) {
                // Yhdella asiakkaalla voi olla vain sama sähköpostiosoite. Mikäli asiakkaan muut
                // tiedot täsmäävät,
                // niin hän on sama asiakas. Muuten palautetaan virhe, jossa ilmoitetaan,
                // että sähköpostiosoite on jo käytössä.
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
        List<Lisavarustetyyppi> lvarustetyypit = null;
        // Lisävarusteet.
        if (!lisavarusteet.isEmpty()) {
            LisavarustetyyppiDao lvarusteDao = thallinta.getDao(LisavarustetyyppiDao.class);
            lvarustetyypit = lvarusteDao.readLisavarustetyyppit(lisavarusteet);
            // Luo mikäli lisävarustetyyppiä ei löytynyt.
            lvarusteDao.createLisavarustetyypit(lvarustetyypit);
        }
        // Muuta päiviksi, joten kellonaika ei häiritse laskua.
        long bookedDays = ChronoUnit.DAYS.between(alkupaivamaara.toLocalDate(), loppupaivamaara.toLocalDate());
        System.out.println("bookedDays: " + bookedDays);
        double yhteishinta = huoneet.stream().mapToDouble(e -> e.getPaivahinta().doubleValue() * bookedDays).sum();
        Varaus varaus = new Varaus(asiakas, alkupaivamaara, loppupaivamaara,
                new BigDecimal(yhteishinta), huoneet.size(), -1);
        // Luo uusi varaus.
        create(varaus);
        // Lisää Liitostaulu-kyselyt.
        List<String> junctionTableQueries = new ArrayList<>();
        // Lisää HuoneVaraus-liitostaulu kyselyt.
        huoneet.forEach(huone -> junctionTableQueries.add("INSERT INTO HuoneVaraus (varaus_id, huonenumero) "
                + "VALUES (" + varaus.getId() + ", " + huone.getHuonenumero() + ")"));
        // Lisää Lisävaruste-liitostaulu kyselyt.
        if (lvarustetyypit != null) {
            lvarustetyypit.forEach(lvaruste -> junctionTableQueries.add("INSERT INTO Lisavaruste "
                    + ("varaus_id, lisavarustetyyppi_id) VALUES (" + varaus.getId() + ", " + lvaruste.getId() + ")")));
        }
        // Luo liitostauluun huoneet.
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
}