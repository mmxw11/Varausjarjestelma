package varausjarjestelma.database.dao;

import varausjarjestelma.database.Tietokantahallinta;
import varausjarjestelma.domain.Lisavarustetyyppi;

/**
 * @author Matias
 */
public class LisavarustetyyppiDao extends Dao<Lisavarustetyyppi, Integer> {

    public LisavarustetyyppiDao(Tietokantahallinta thallinta) {
        super(thallinta, "Lisavarustetyyppi", "id", Lisavarustetyyppi.class);
    }
}