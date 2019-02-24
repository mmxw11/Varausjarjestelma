package varausjarjestelma.ui.commands;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

import varausjarjestelma.database.Tietokantahallinta;
import varausjarjestelma.database.dao.HuoneDao;
import varausjarjestelma.domain.Huone;
import varausjarjestelma.ui.AbstractKomento;

/**
 * @author Matias
 */
public class ListaaHuoneetKomento implements AbstractKomento {

    @Override
    public void execute(Scanner scanner, Tietokantahallinta thallinta) {
        System.out.println("Listataan huoneet");
        System.out.println("");
        HuoneDao dao = thallinta.getDao(HuoneDao.class);
        try {
            List<Huone> huoneet = dao.readHuoneet();
            if (huoneet.isEmpty()) {
                System.out.println("Yhtään huonetta ei löytynyt!");
                return;
            }
            huoneet.forEach(System.out::println);
        } catch (SQLException e) {
            System.out.println("Huoneita hakiessa tapahtui virhe: " + e.getMessage());
        }
    }

    @Override
    public String getKuvaus() {
        return "listaa huoneet";
    }
}