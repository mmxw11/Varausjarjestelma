package varausjarjestelma.database.dao;

import java.sql.SQLException;
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
        // Ei mitään tehtävää.
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

    /**
     * Hakee tietokannasta tyyppejä vastaavat lisävarusteetyypit.
     * @param varustetyyppit
     * @return Palauttaa lisävarusteetyypit listalla
     * @throws SQLException
     */
    public List<Lisavarustetyyppi> readLisavarustetyyppit(Collection<String> varustetyyppit) throws SQLException {
        SQLJoinVarasto joinVarasto = buildJoinVarasto();
        List<TauluSarake> columns = serializer.convertClassFieldsToColumns(tableName, joinVarasto);
        String argsToFill = "";
        for (int i = 0; i < varustetyyppit.size(); i++) {
            if (i != 0) {
                argsToFill += ", ";
            }
            argsToFill += "?";
        }
        String sql = SQLKyselyRakentaja.buildSelectQuery(resultClass, tableName, columns, joinVarasto)
                .append(" WHERE ")
                .append(tableName)
                .append(".varustetyyppi IN (")
                .append(argsToFill)
                .append(")")
                .toString();
        return thallinta.executeQuery(jdbcTemp -> jdbcTemp.query(sql, new TulosLuokkaRakentaja<>(this, thallinta), varustetyyppit.toArray()));
    }
}