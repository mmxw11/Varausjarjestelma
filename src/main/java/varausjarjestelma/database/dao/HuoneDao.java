package varausjarjestelma.database.dao;

import varausjarjestelma.database.Tietokantahallinta;
import varausjarjestelma.domain.Huone;
import varausjarjestelma.domain.Huonetyyppi;
import varausjarjestelma.domain.serialization.LuokkaSerializer;

/**
 * @author Matias
 */
public class HuoneDao extends Dao<Huone, Integer> {

    public HuoneDao(Tietokantahallinta thallinta) {
        super(thallinta, "Huone", "huonenumero", Huone.class);
        setAutoGeneratePrimaryKey(false);
    }

    @Override
    protected void initalizeSerializerSettings(LuokkaSerializer<Huone> serializer) {
        serializer.setJoinClauseType("JOIN");
        // Muunna huonetyyppi-luokka ID:ksi.
        serializer.registerSerializerStrategy("huonetyyppi", Huonetyyppi.class, Huonetyyppi::getId);
    }
}