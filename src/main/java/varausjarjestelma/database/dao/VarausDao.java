package varausjarjestelma.database.dao;

import java.sql.SQLException;
import java.util.List;

import varausjarjestelma.database.SQLJoinVarasto;
import varausjarjestelma.database.SQLKyselyRakentaja;
import varausjarjestelma.database.Tietokantahallinta;
import varausjarjestelma.domain.Asiakas;
import varausjarjestelma.domain.Varaus;
import varausjarjestelma.domain.serialization.LuokkaSerializer;
import varausjarjestelma.domain.serialization.TauluSarake;

/**
 * @author Matias
 */
public class VarausDao extends Dao<Varaus, Integer> {

    public VarausDao(Tietokantahallinta thallinta) {
        super(thallinta, "Varaus", "id", Varaus.class);
    }

    @Override
    protected void initalizeSerializerSettings(LuokkaSerializer<Varaus> serializer) {
        serializer.setJoinClauseType("JOIN");
        // Muunna asiskas-luokka ID:ksi.
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

    @Override
    public Varaus read(Integer key) throws SQLException {
        SQLJoinVarasto joinVarasto = new SQLJoinVarasto();
        List<TauluSarake> columns = serializer.convertClassFieldsToColumns(tableName, joinVarasto);
        joinVarasto.addSQLJoinClause("Lisavaruste", "LEFT JOIN Lisavaruste ON Lisavaruste.varaus_id = Varaus.id")
                .addSQLJoinClause("Lisavarustetyyppi", "LEFT JOIN Lisavarustetyyppi ON Lisavarustetyyppi.id = Lisavaruste.lisavarustetyyppi_id")
                .addSQLJoinClause("Huonevaraus", "LEFT JOIN Huonevaraus ON Huonevaraus.varaus_id = Varaus.id")
                .addSQLJoinClause("Huone", "LEFT JOIN Huone ON Huone.huonenumero = Huonevaraus.huonenumero");
        String sql = SQLKyselyRakentaja.buildSelectQuery(resultClass, tableName, primaryKeyColumn, columns, joinVarasto)
                + " GROUP BY " + tableName + "." + primaryKeyColumn;
        return queryObjectFromDatabase(sql, key);
    }
}