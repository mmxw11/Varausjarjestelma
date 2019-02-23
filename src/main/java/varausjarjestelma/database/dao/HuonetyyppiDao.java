package varausjarjestelma.database.dao;

import varausjarjestelma.database.Tietokantahallinta;
import varausjarjestelma.domain.Huonetyyppi;
import varausjarjestelma.domain.serialization.LuokkaSerializer;

/**
 * @author Matias
 */
public class HuonetyyppiDao extends Dao<Huonetyyppi, Integer> {

    public HuonetyyppiDao(Tietokantahallinta thallinta) {
        super(thallinta, "Huonetyyppi", "id", Huonetyyppi.class);
    }

    @Override
    protected void initalizeSerializerSettings(LuokkaSerializer<Huonetyyppi> serializer) {
        // Ei mitään tehtävää.
    }
}