package varausjarjestelma.database.dao;

import java.sql.SQLException;
import java.util.List;

import org.springframework.stereotype.Repository;

import varausjarjestelma.database.SQLKyselyRakentaja;
import varausjarjestelma.database.Tietokantahallinta;
import varausjarjestelma.domain.Huonetyyppi;
import varausjarjestelma.domain.serialization.LuokkaSerializer;
import varausjarjestelma.domain.serialization.TauluSarake;

/**
 * @author Matias
 */
@Repository
public class HuonetyyppiDao extends Dao<Huonetyyppi, Integer> {

    public HuonetyyppiDao(Tietokantahallinta thallinta) {
        super(thallinta, "Huonetyyppi", "id", Huonetyyppi.class);
    }

    @Override
    protected void initalizeSerializerSettings(LuokkaSerializer<Huonetyyppi> serializer) {
        // Ei mitään tehtävää.
    }

    /**
     * Hakee tietokannasta huonetyyppia vastaavan olion merkkijonona annetun tyypin perusteella.
     * @param tyyppi Huoneen tyyppi
     * @return Palauttaa tiedoista luodun olion
     * @throws SQLException
     */
    public Huonetyyppi readByTyyppi(String tyyppi) throws SQLException {
        List<TauluSarake> columns = serializer.convertClassFieldsToColumns(tableName, null);
        String sql = SQLKyselyRakentaja.buildSelectQuery(resultClass, tableName, columns)
                + " WHERE " + tableName + ".tyyppi = ?";
        return queryObjectFromDatabase(sql, tyyppi);
    }
}