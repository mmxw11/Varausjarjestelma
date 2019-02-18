package varausjarjestelma;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import varausjarjestelma.database.Tietokantahallinta;

@Component
public class Varaushallinta {

    @Autowired
    private Tietokantahallinta tietokantahallinta;

    public Varaushallinta() {}

    public void initialize() throws Exception {
        tietokantahallinta.setupTables();
    }
}