package varausjarjestelma.database.dao;

import java.sql.SQLException;
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
    public void createLisavarustetyypit(List<Lisavarustetyyppi> varustetyyppit) throws SQLException {
        for (Lisavarustetyyppi tyyppi : varustetyyppit) {
            if (tyyppi.getId() != -1) {
                continue;
            }
            create(tyyppi);
        }
    }

    /**
     * Hakee tietokannasta tyyppejä vastaavat lisävarusteet.
     * @param varustetyyppit
     * @return Palauttaa lisävarusteet listalla
     * @throws SQLException
     */
    public List<Lisavarustetyyppi> readLisavarustetyyppit(List<String> varustetyyppit) throws SQLException {
        SQLJoinVarasto joinVarasto = buildJoinVarasto();
        List<TauluSarake> columns = serializer.convertClassFieldsToColumns(tableName, joinVarasto);
        String sql = SQLKyselyRakentaja.buildSelectQuery(resultClass, tableName, columns, joinVarasto)
                .append(" WHERE ")
                .append(tableName)
                .append(".varustetyyppi IN (?)")
                .toString();
        return thallinta.executeQuery(jdbcTemp -> jdbcTemp.query(sql, new TulosLuokkaRakentaja<>(this, thallinta), varustetyyppit));
    }
}