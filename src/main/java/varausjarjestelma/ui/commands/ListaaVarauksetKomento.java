package varausjarjestelma.ui.commands;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

import varausjarjestelma.database.Tietokantahallinta;
import varausjarjestelma.database.dao.VarausDao;
import varausjarjestelma.domain.Varaus;
import varausjarjestelma.ui.AbstractKomento;

/**
 * @author Matias
 */
public class ListaaVarauksetKomento implements AbstractKomento {

    @Override
    public void execute(Scanner scanner, Tietokantahallinta thallinta) {
        System.out.println("Listataan varaukset");
        System.out.println("");
        VarausDao dao = thallinta.getDao(VarausDao.class);
        try {
            List<Varaus> varaukset = dao.readVaraukset();
            if (varaukset.isEmpty()) {
                System.out.println("Yhtään varusta ei löytynyt!");
                return;
            }
            varaukset.forEach(v -> {
                System.out.println(v + ". Huoneet:");
                v.getHuoneet().forEach(h -> System.out.println("\t" + h));
                // Jätetään sentit (nollat) pois mikäli niitä ei ole.
                String hintaStr = v.getYhteishinta().stripTrailingZeros().toPlainString();
                System.out.println("\tYhteensä: " + hintaStr + " euroa");
                System.out.println("");
            });
        } catch (SQLException e) {
            System.out.println("Varauksia hakiessa tapahtui virhe: " + e.getMessage());
        }
    }

    @Override
    public String getKuvaus() {
        return "listaa varaukset";
    }
}