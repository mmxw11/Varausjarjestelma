package varausjarjestelma.ui.commands;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

import varausjarjestelma.database.Tietokantahallinta;
import varausjarjestelma.database.dao.LisavarustetyyppiDao;
import varausjarjestelma.domain.Lisavarustetyyppi;
import varausjarjestelma.ui.AbstractKomento;
import varausjarjestelma.ui.SyoteUtil;

/**
 * @author Matias
 */
public class TilastoKomento implements AbstractKomento {

    @Override
    public void execute(Scanner scanner, Tietokantahallinta thallinta) {
        System.out.println("Mitä tilastoja tulostetaan?");
        System.out.println("");
        // tilastoja pyydettäessä käyttäjältä kysytään tilasto
        System.out.println(" 1 - Suosituimmat lisävarusteet");
        System.out.println(" 2 - Parhaat asiakkaat");
        System.out.println(" 3 - Varausprosentti huoneittain");
        System.out.println(" 4 - Varausprosentti huonetyypeittäin");
        System.out.println("Syötä komento: ");
        int komento = SyoteUtil.readInteger(scanner.nextLine(), -1);
        if (komento <= 0 || komento > 4) {
            System.out.println("Virheellinen komento!");
            return;
        }
        if (komento == 1) {
            suosituimmatLisavarusteet(thallinta);
        } else if (komento == 2) {
            parhaatAsiakkaat(); // TODO: IMPLEMENT
        } else if (komento == 3) {
            varausprosenttiHuoneittain(scanner); // TODO: IMPLEMENT
        } else if (komento == 4) {
            varausprosenttiHuonetyypeittain(scanner); // TODO: IMPLEMENT
        }
    }

    private void suosituimmatLisavarusteet(Tietokantahallinta thallinta) {
        System.out.println("Tulostetaan suosituimmat lisävarusteet");
        System.out.println("");
        LisavarustetyyppiDao ltyyppiDao = thallinta.getDao(LisavarustetyyppiDao.class);
        try {
            List<Lisavarustetyyppi> lisavarusteet = ltyyppiDao.readMostPopularLisavarutetyypit(10);
            if (lisavarusteet.isEmpty()) {
                System.out.println("Yhtään lisävarustetta ei löytynyt!");
                return;
            }
            lisavarusteet.forEach(System.out::println);
        } catch (SQLException e) {
            System.out.println("Lisävarusteita hakiessa tapahtui virhe: " + e.getMessage());
        }
    }

    private static void parhaatAsiakkaat() {
        System.out.println("Tulostetaan parhaat asiakkaat");
        System.out.println("");
        // alla oletetaan, että asiakkaita on vain 2
        // mikäli tietokannassa niitä on enemmän, tulostetaan asiakkaita korkeintaan 10
        System.out.println("Anssi Asiakas, anssi@asiakas.net, +358441231234, 1323 euroa");
        System.out.println("Essi Esimerkki, essi@esimerkki.net, +358443214321, 229 euroa");
    }

    private static void varausprosenttiHuoneittain(Scanner lukija) {
        System.out.println("Tulostetaan varausprosentti huoneittain");
        System.out.println("");
        System.out.println("Mistä lähtien tarkastellaan?");
        LocalDateTime alku = LocalDateTime.parse(lukija.nextLine() + "-01 " + "16:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        System.out.println("Mihin asti tarkastellaan?");
        LocalDateTime loppu = LocalDateTime.parse(lukija.nextLine() + "-01 " + "10:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        // alla esimerkkitulostus
        System.out.println("Tulostetaan varausprosentti huoneittain");
        System.out.println("Excelsior, 604, 119 euroa, 0.0%");
        System.out.println("Excelsior, 605, 119 euroa, 0.0%");
        System.out.println("Superior, 705, 159 euroa, 22.8%");
        System.out.println("Commodore, 128, 229 euroa, 62.8%");
    }

    private static void varausprosenttiHuonetyypeittain(Scanner lukija) {
        System.out.println("Tulostetaan varausprosentti huonetyypeittäin");
        System.out.println("");
        System.out.println("Mistä lähtien tarkastellaan?");
        LocalDateTime alku = LocalDateTime.parse(lukija.nextLine() + "-01 " + "16:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        System.out.println("Mihin asti tarkastellaan?");
        LocalDateTime loppu = LocalDateTime.parse(lukija.nextLine() + "-01 " + "10:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        // alla esimerkkitulostus
        System.out.println("Tulostetaan varausprosentti huonetyypeittän");
        System.out.println("Excelsior, 0.0%");
        System.out.println("Superior, 22.8%");
        System.out.println("Commodore, 62.8%");
    }

    @Override
    public String getKuvaus() {
        return "tilastoja";
    }
}