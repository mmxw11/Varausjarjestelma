package varausjarjestelma.database.dao;

import java.sql.SQLException;
import java.util.List;

import org.springframework.stereotype.Repository;

import varausjarjestelma.database.SQLJoinVarasto;
import varausjarjestelma.database.SQLKyselyRakentaja;
import varausjarjestelma.database.Tietokantahallinta;
import varausjarjestelma.domain.Asiakas;
import varausjarjestelma.domain.serialization.LuokkaSerializer;
import varausjarjestelma.domain.serialization.TauluSarake;
import varausjarjestelma.domain.serialization.TulosLuokkaRakentaja;

/**
 * @author Matias
 */
@Repository
public class AsiakasDao extends Dao<Asiakas, Integer> {

    public AsiakasDao(Tietokantahallinta thallinta) {
        super(thallinta, "Asiakas", "id", Asiakas.class);
    }

    @Override
    protected void initalizeSerializerSettings(LuokkaSerializer<Asiakas> serializer) {
        serializer.setJoinClauseType("JOIN");
        // Lisää dynaamiset kyselyt.
        serializer.registerDynamicTypeQueryStrategy("rahaaKaytetty", "SUM(Varaus.yhteishinta)");
    }

    @Override
    public Asiakas read(Integer key) throws SQLException {
        SQLJoinVarasto joinVarasto = buildJoinVarasto();
        List<TauluSarake> columns = serializer.convertClassFieldsToColumns(tableName, joinVarasto);
        joinVarasto.addSQLJoinClause("Varaus", "LEFT JOIN Varaus ON Varaus.asiakas_id = Asiakas.id");
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
     * Hakee tietokannasta asiakkaan sähköpostiosoitteen perusteella.
     * @param sahkopostiosoite
     * @return Palauttaa tiedoista luodun Asiakas-olion
     * @throws SQLException
     */
    public Asiakas readBySahkopostiosoite(String sahkopostiosoite) throws SQLException {
        SQLJoinVarasto joinVarasto = buildJoinVarasto();
        List<TauluSarake> columns = serializer.convertClassFieldsToColumns(tableName, joinVarasto);
        joinVarasto.addSQLJoinClause("Varaus", "LEFT JOIN Varaus ON Varaus.asiakas_id = Asiakas.id");
        String sql = SQLKyselyRakentaja.buildSelectQuery(resultClass, tableName, columns, joinVarasto)
                .append(" WHERE ")
                .append(tableName)
                .append(".sahkopostiosoite = ?")
                .append(" GROUP BY ")
                .append(tableName)
                .append(".")
                .append(primaryKeyColumn)
                .toString();
        return queryObjectFromDatabase(sql, sahkopostiosoite);
    }

    /**
     * Hakee tietokannasta eniten rahaa käyttäneet asiakkaat.
     * @param limit Kuinka monta asiakasta pitäisi palauttaa. Jos -1 niin palautetaan kaikki
     * @return Palauttaa eniten rahaa käyttäneet asiakkaat listalla järjestettynä rahamäärän perusteella
     * @throws SQLException
     */
    public List<Asiakas> readAsiakkaatWithMostMoneySpent(int limit) throws SQLException {
        SQLJoinVarasto joinVarasto = buildJoinVarasto();
        List<TauluSarake> columns = serializer.convertClassFieldsToColumns(tableName, joinVarasto);
        joinVarasto.addSQLJoinClause("Varaus", "LEFT JOIN Varaus ON Varaus.asiakas_id = Asiakas.id");
        StringBuilder sqlBuilder = SQLKyselyRakentaja.buildSelectQuery(resultClass, tableName, columns, joinVarasto)
                .append(" GROUP BY ")
                .append(tableName)
                .append(".")
                .append(primaryKeyColumn)
                .append(" ORDER BY SUM(Varaus.yhteishinta) DESC");
        if (limit != -1) {
            sqlBuilder.append(" LIMIT ?");
        }
        String sql = sqlBuilder.toString();
        TulosLuokkaRakentaja<Asiakas> trakentaja = new TulosLuokkaRakentaja<>(this, thallinta);
        if (limit != -1) {
            return thallinta.executeQuery(jdbcTemp -> jdbcTemp.query(sql, trakentaja, limit));
        } else {
            return thallinta.executeQuery(jdbcTemp -> jdbcTemp.query(sql, trakentaja));
        }
    }
}