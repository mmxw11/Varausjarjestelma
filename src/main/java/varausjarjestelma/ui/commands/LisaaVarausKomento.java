package varausjarjestelma.ui.commands;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import varausjarjestelma.database.Tietokantahallinta;
import varausjarjestelma.database.dao.HuoneDao;
import varausjarjestelma.database.dao.VarausDao;
import varausjarjestelma.database.dao.querysettings.HuoneHakuAsetukset;
import varausjarjestelma.domain.Asiakas;
import varausjarjestelma.domain.Huone;
import varausjarjestelma.domain.Huonetyyppi;
import varausjarjestelma.domain.Varaus;
import varausjarjestelma.ui.AbstractKomento;
import varausjarjestelma.ui.SyoteUtil;

/**
 * @author Matias
 */
public class LisaaVarausKomento implements AbstractKomento {

    @Override
    public void execute(Scanner scanner, Tietokantahallinta thallinta) {
        System.out.println("Haetaan huoneita");
        System.out.println("");
        System.out.println("Milloin varaus alkaisi (yyyy-MM-dd)?");;
        LocalDateTime alku = SyoteUtil.parseDateTime(scanner.nextLine() + " " + "16:00");
        if (alku == null) {
            System.out.println("Virheellinen alkupäivämäärä! Syötä muodossa \"yyyy-MM-dd\"!");
            return;
        }
        System.out.println("Milloin varaus loppuisi (yyyy-MM-dd)?");
        LocalDateTime loppu = SyoteUtil.parseDateTime(scanner.nextLine() + " " + "10:00");
        if (loppu == null) {
            System.out.println("Virheellinen loppupäivämäärä! Syötä muodossa \"yyyy-MM-dd\"!");
            return;
        }
        if (alku.toLocalDate().equals(loppu.toLocalDate())) {
            System.out.println("Alku- ja loppupäivämäärät eivät voi olla samana päivänä!");
            return;
        } else if (loppu.isBefore(alku)) {
            System.out.println("Loppupäivämäärä ei voi olla ennen alkupäivämäärää!");
            return;
        }
        HuoneHakuAsetukset hakuAsetukset = HuoneHakuAsetukset.newSettings(alku, loppu);
        System.out.println("Minkä tyyppinen huone? (tyhjä = ei rajausta)");
        String tyyppi = scanner.nextLine();
        hakuAsetukset.setHtyyppi(tyyppi.isEmpty() ? null : new Huonetyyppi(tyyppi));
        System.out.println("Minkä hintainen korkeintaan? (tyhjä = ei rajausta)");
        String maksimihinta = scanner.nextLine();
        if (!maksimihinta.isEmpty()) {
            int hinta = SyoteUtil.readInteger(maksimihinta, -1);
            if (hinta < 0) {
                System.out.println("Virheellinen maksimihinta!");
                return;
            }
            hakuAsetukset.setMaksimihinta(hinta);
        }
        hakuAsetukset.setResultOrder("ORDER BY Huone.paivahinta DESC"); // Kalleimmat huoneet ensin.
        List<Huone> vapaatHuoneet = null;
        try {
            HuoneDao dao = thallinta.getDao(HuoneDao.class);
            vapaatHuoneet = dao.getNonBookedHuoneet(hakuAsetukset);
        } catch (SQLException e) {
            System.out.println("Huoneita hakiessa tapahtui virhe: " + e.getMessage());
            return;
        }
        // Mikäli huoneita ei ole vapaana, ohjelma tulostaa seuraavan viestin
        // ja varauksen lisääminen loppuu.
        if (vapaatHuoneet.isEmpty()) {
            System.out.println("Ei vapaita huoneita.");
            return;
        }
        // Muulloin, ohjelma kertoo vapaiden huoneiden lukumäärän.
        System.out.println("Huoneita vapaana: " + vapaatHuoneet.size());
        System.out.println("");
        // Tämän jälkeen kysytään varattavien huoneiden lukumäärää.
        int huoneita = -1;
        while (true) {
            System.out.println("Montako huonetta varataan?");
            huoneita = SyoteUtil.readInteger(scanner.nextLine(), -1);
            if (huoneita >= 1 && huoneita <= vapaatHuoneet.size()) {
                break;
            }
            System.out.println("Epäkelpo huoneiden lukumäärä.");
        }
        // Tämän jälkeen kysytään lisävarusteet.
        List<String> lisavarusteet = new ArrayList<>();
        while (true) {
            System.out.println("Syötä lisävaruste, tyhjä lopettaa");
            String lisavaruste = scanner.nextLine();
            if (lisavaruste.isEmpty()) {
                break;
            }
            lisavarusteet.add(lisavaruste);
        }
        // Lopuksi varaajan tiedot.
        System.out.println("Syötä varaajan nimi:");
        String nimi = scanner.nextLine();
        if (nimi.isEmpty()) {
            System.out.println("Nimi ei voi olla tyhjä!");
            return;
        }
        System.out.println("Syötä varaajan puhelinnumero:");
        String puhelinnumero = scanner.nextLine();
        if (puhelinnumero.isEmpty()) {
            System.out.println("Puhelinnumero ei voi olla tyhjä!");
            return;
        }
        System.out.println("Syötä varaajan sähköpostiosoite:");
        String sahkoposti = scanner.nextLine();
        if (sahkoposti.isEmpty()) {
            System.out.println("Sähköpostiosoite ei voi olla tyhjä!");
            return;
        }
        // Kun kaikki tiedot on kerätty, ohjelma lisää varauksen tietokantaan
        // -- varaukseen tulee lisätä kalleimmat vapaat huoneet!
        Asiakas asiakas = new Asiakas(nimi, puhelinnumero, sahkoposti);
        VarausDao dao = thallinta.getDao(VarausDao.class);
        try {
            // vapaatHuoneet-lista on lajiteltu kalleimmasta halvimpaan. Otetaan vain tarvittava
            // määrä mukaan.
            Varaus varaus = dao.bookHotelliHuoneita(asiakas, vapaatHuoneet.subList(0, huoneita), alku, loppu, lisavarusteet);
            System.out.println("Tehtiin varaus: " + varaus + ".");
        } catch (SQLException e) {
            System.out.println("Varausta tehdessä tapahtui virhe: " + e.getMessage());
        }
    }

    @Override
    public String getKuvaus() {
        return "lisaa varaus";
    }
}