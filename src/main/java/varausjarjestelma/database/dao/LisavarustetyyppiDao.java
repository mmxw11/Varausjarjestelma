package varausjarjestelma.database.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Repository;

import varausjarjestelma.database.SQLJoinVarasto;
import varausjarjestelma.database.SQLKyselyRakentaja;
import varausjarjestelma.database.Tietokantahallinta;
import varausjarjestelma.domain.Lisavarustetyyppi;
import varausjarjestelma.domain.serialization.LuokkaSerializer;
import varausjarjestelma.domain.serialization.TauluSarake;
import varausjarjestelma.domain.serialization.TulosLuokkaRakentaja;

/**
 * @author Matias
 */
@Repository
public class LisavarustetyyppiDao extends Dao<Lisavarustetyyppi, Integer> {

    public LisavarustetyyppiDao(Tietokantahallinta thallinta) {
        super(thallinta, "Lisavarustetyyppi", "id", Lisavarustetyyppi.class);
    }

    @Override
    protected void initalizeSerializerSettings(LuokkaSerializer<Lisavarustetyyppi> serializer) {
        serializer.setJoinClauseType("JOIN");
        // Lisää dynaamiset kyselyt.
        serializer.registerDynamicTypeQueryStrategy("lisavarustemaara", "COUNT(Lisavaruste.lisavarustetyyppi_id)");
    }

    /**
     * Luo tietokantaan uusia lisävarustetyyppejä.
     * @param varustetyyppit
     * @throws SQLException
     */
    public void createLisavarustetyypit(Collection<Lisavarustetyyppi> varustetyyppit) throws SQLException {
        for (Lisavarustetyyppi tyyppi : varustetyyppit) {
            if (tyyppi.getId() != -1) { // Jo luotu.
                continue;
            }
            create(tyyppi);
        }
    }

    @Override
    public Lisavarustetyyppi read(Integer key) throws SQLException {
        SQLJoinVarasto joinVarasto = buildJoinVarasto();
        List<TauluSarake> columns = serializer.convertClassFieldsToColumns(tableName, joinVarasto);
        joinVarasto.addSQLJoinClause("Lisavaruste", "LEFT JOIN Lisavaruste ON Lisavaruste.lisavarustetyyppi_id = Lisavarustetyyppi.id");
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
     * Hakee tietokannasta tyyppejä vastaavat lisävarusteetyypit.
     * @param varustetyyppit
     * @return Palauttaa lisävarusteetyypit listalla
     * @throws SQLException
     */
    public List<Lisavarustetyyppi> readLisavarustetyyppit(Collection<String> varustetyyppit) throws SQLException {
        if (varustetyyppit.isEmpty()) {
            return new ArrayList<>();
        }
        SQLJoinVarasto joinVarasto = buildJoinVarasto();
        List<TauluSarake> columns = serializer.convertClassFieldsToColumns(tableName, joinVarasto);
        // Rakennetaan placeholderit parametreille.
        String argsToFill = "";
        for (int i = 0; i < varustetyyppit.size(); i++) {
            if (i != 0) {
                argsToFill += ", ";
            }
            argsToFill += "?";
        }
        joinVarasto.addSQLJoinClause("Lisavaruste", "LEFT JOIN Lisavaruste ON Lisavaruste.lisavarustetyyppi_id = Lisavarustetyyppi.id");
        String sql = SQLKyselyRakentaja.buildSelectQuery(resultClass, tableName, columns, joinVarasto)
                .append(" WHERE ")
                .append(tableName)
                .append(".varustetyyppi IN (")
                .append(argsToFill)
                .append(")")
                .append(" GROUP BY ")
                .append(tableName)
                .append(".")
                .append(primaryKeyColumn)
                .toString();
        return thallinta.executeQuery(jdbcTemp -> jdbcTemp.query(sql, new TulosLuokkaRakentaja<>(this, thallinta), varustetyyppit.toArray()));
    }

    /**
     * Hakee tietokannasta suosituimmat lisävarustetyypit.
     * @param limit Kuinka monta lisävarustetyyppiä pitäisi palauttaa. Jos -1 niin palautetaan kaikki
     * @return Palauttaa suosituimmat lisävarusteetyypit listalla järjestettynä suosion perusteella
     * @throws SQLException
     */
    public List<Lisavarustetyyppi> readMostPopularLisavarutetyypit(int limit) throws SQLException {
        SQLJoinVarasto joinVarasto = buildJoinVarasto();
        List<TauluSarake> columns = serializer.convertClassFieldsToColumns(tableName, joinVarasto);
        joinVarasto.addSQLJoinClause("Lisavaruste", "LEFT JOIN Lisavaruste ON Lisavaruste.lisavarustetyyppi_id = Lisavarustetyyppi.id");
        StringBuilder sqlBuilder = SQLKyselyRakentaja.buildSelectQuery(resultClass, tableName, columns, joinVarasto)
                .append(" GROUP BY ")
                .append(tableName)
                .append(".")
                .append(primaryKeyColumn)
                .append(" ORDER BY COUNT(Lisavaruste.lisavarustetyyppi_id) DESC");
        if (limit != -1) {
            sqlBuilder.append(" LIMIT ?");
        }
        String sql = sqlBuilder.toString();
        TulosLuokkaRakentaja<Lisavarustetyyppi> trakentaja = new TulosLuokkaRakentaja<>(this, thallinta);
        if (limit != -1) {
            return thallinta.executeQuery(jdbcTemp -> jdbcTemp.query(sql, trakentaja, limit));
        } else {
            return thallinta.executeQuery(jdbcTemp -> jdbcTemp.query(sql, trakentaja));
        }
    }
}