package varausjarjestelma.database.dao;

import varausjarjestelma.database.Tietokantahallinta;
import varausjarjestelma.domain.Lisavarustetyyppi;
import varausjarjestelma.domain.serialization.LuokkaSerializer;

/**
 * @author Matias
 */
public class LisavarustetyyppiDao extends Dao<Lisavarustetyyppi, Integer> {

    public LisavarustetyyppiDao(Tietokantahallinta thallinta) {
        super(thallinta, "Lisavarustetyyppi", "id", Lisavarustetyyppi.class);
    }

    @Override
    protected void initalizeSerializerSettings(LuokkaSerializer<Lisavarustetyyppi> serializer) {
        // Ei mitään tehtävää.
    }
}