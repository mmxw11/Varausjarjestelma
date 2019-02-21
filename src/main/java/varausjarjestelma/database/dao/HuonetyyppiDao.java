package varausjarjestelma.database.dao;

import varausjarjestelma.database.Tietokantahallinta;
import varausjarjestelma.domain.Huonetyyppi;

/**
 * @author Matias
 */
public class HuonetyyppiDao extends Dao<Huonetyyppi, Integer> {

    public HuonetyyppiDao(Tietokantahallinta thallinta) {
        super(thallinta, "Huonetyyppi", "id", Huonetyyppi.class);
    }
}