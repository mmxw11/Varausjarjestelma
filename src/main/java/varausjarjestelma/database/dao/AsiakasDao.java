package varausjarjestelma.database.dao;

import org.springframework.stereotype.Repository;

import varausjarjestelma.database.Tietokantahallinta;
import varausjarjestelma.domain.Asiakas;
import varausjarjestelma.domain.serialization.LuokkaSerializer;

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
}