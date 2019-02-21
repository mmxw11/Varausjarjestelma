package varausjarjestelma.database.dao;

import varausjarjestelma.database.Tietokantahallinta;
import varausjarjestelma.domain.Asiakas;

/**
 * @author Matias
 */
public class AsiakasDao extends Dao<Asiakas, Integer> {

    public AsiakasDao(Tietokantahallinta thallinta) {
        super(thallinta, "Asiakas", "id", Asiakas.class);
    }
}