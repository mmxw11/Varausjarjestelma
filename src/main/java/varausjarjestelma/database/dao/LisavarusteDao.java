package varausjarjestelma.database.dao;

import varausjarjestelma.database.Tietokantahallinta;
import varausjarjestelma.domain.Lisavaruste;

/**
 * @author Matias
 */
public class LisavarusteDao extends Dao<Lisavaruste, Integer> {

    public LisavarusteDao(Tietokantahallinta thallinta) {
        super(thallinta, "Lisavaruste", "id", Lisavaruste.class);
    }
}