package varausjarjestelma.ui.commands;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

import varausjarjestelma.database.Tietokantahallinta;
import varausjarjestelma.database.dao.HuoneDao;
import varausjarjestelma.database.dao.querysettings.HuoneHakuAsetukset;
import varausjarjestelma.domain.Huone;
import varausjarjestelma.domain.Huonetyyppi;
import varausjarjestelma.ui.AbstractKomento;
import varausjarjestelma.ui.SyoteUtil;

/**
 * @author Matias
 */
public class HaeHuoneitaKomento implements AbstractKomento {

    @Override
    public void execute(Scanner scanner, Tietokantahallinta thallinta) {
        System.out.println("Haetaan huoneita");
        System.out.println("");
        System.out.println("Milloin varaus alkaisi (yyyy-MM-dd)?");
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
        HuoneDao dao = thallinta.getDao(HuoneDao.class);
        try {
            List<Huone> huoneet = dao.getNonBookedHuoneet(hakuAsetukset);
            if (huoneet.isEmpty()) {
                System.out.println("Ei vapaita huoneita.");
                return;
            }
            System.out.println("Vapaat huoneet:");
            huoneet.forEach(System.out::println);
        } catch (SQLException e) {
            System.out.println("Huoneita hakiessa tapahtui virhe: " + e.getMessage());
        }
    }

    @Override
    public String getKuvaus() {
        return "hae huoneita";
    }
}