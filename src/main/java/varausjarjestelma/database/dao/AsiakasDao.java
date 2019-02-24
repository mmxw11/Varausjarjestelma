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
        // Ei mitään tehtävää.
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
        String sql = SQLKyselyRakentaja.buildSelectQuery(resultClass, tableName, columns, joinVarasto)
                .append(" WHERE ")
                .append(tableName)
                .append(".sahkopostiosoite = ?")
                .toString();
        return queryObjectFromDatabase(sql, sahkopostiosoite);
    }
}