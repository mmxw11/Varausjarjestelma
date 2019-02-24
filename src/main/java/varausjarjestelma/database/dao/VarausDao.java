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
    public Varaus bookHotelliHuoneita(List<Huone> huoneet, LocalDateTime alkupaivamaara, LocalDateTime loppupaivamaara) throws SQLException {
        // Muuta päiviksi, joten kellonaika ei häiritse laskua.
        long bookedDays = ChronoUnit.DAYS.between(alkupaivamaara.toLocalDate(), loppupaivamaara.toLocalDate());
        System.out.println("bookedDays: " + bookedDays);
        
        //TODO: ASIAKAS, LISÄVARUSTEET!
        // DUM ASIAKAS
        Asiakas testAsiakas = new Asiakas(null, null, null);
        testAsiakas.setId(1);
        // DUM ASIAKAS
        List<String> varausQueries = new ArrayList<>();
        double yhteishinta = huoneet.stream().mapToDouble(e -> e.getPaivahinta().doubleValue() * bookedDays).sum();
        Varaus varaus = new Varaus(testAsiakas, alkupaivamaara, loppupaivamaara,
                new BigDecimal(yhteishinta), huoneet.size(), -1);
        // Luo huone
        create(varaus);
        for (Huone huone : huoneet) {
            varausQueries.add("INSERT INTO HuoneVaraus (varaus_id, huonenumero) "
                    + "VALUES (" + varaus.getId() + ", " + huone.getHuonenumero() + ")");
        }
        // Luo liitostauluun huoneet.
        thallinta.executeQuery(jdbcTemp -> jdbcTemp.batchUpdate(varausQueries.toArray(new String[varausQueries.size()])));
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