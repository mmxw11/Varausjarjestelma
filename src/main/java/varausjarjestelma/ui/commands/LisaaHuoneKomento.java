package varausjarjestelma.ui.commands;

import java.sql.SQLException;
import java.util.Scanner;

import org.springframework.dao.DuplicateKeyException;

import varausjarjestelma.database.Tietokantahallinta;
import varausjarjestelma.database.dao.HuoneDao;
import varausjarjestelma.domain.Huone;
import varausjarjestelma.domain.Huonetyyppi;
import varausjarjestelma.ui.AbstractKomento;
import varausjarjestelma.ui.SyoteUtil;

/**
 * @author Matias
 */
public class LisaaHuoneKomento implements AbstractKomento {

    @Override
    public void execute(Scanner scanner, Tietokantahallinta thallinta) {
        System.out.println("Lisätään huone");
        System.out.println("");
        System.out.println("Minkä tyyppinen huone on?");
        String tyyppi = scanner.nextLine();
        if (tyyppi.isEmpty()) {
            System.out.println("Huoneen tyyppi ei voi olla tyhjä!");
            return;
        }
        System.out.println("Mikä huoneen numeroksi asetetaan?");
        int huonenumero = SyoteUtil.readInteger(scanner.nextLine(), -1);
        if (huonenumero <= 0) {
            System.out.println("Virheellinen huonenumero!");
            return;
        }
        System.out.println("Kuinka monta euroa huone maksaa yöltä?");
        int paivahinta = SyoteUtil.readInteger(scanner.nextLine(), -1);
        if (paivahinta < 0) {
            System.out.println("Virheellinen hinta!");
            return;
        }
        HuoneDao dao = thallinta.getDao(HuoneDao.class);
        try {
            Huone huone = dao.createHuone(new Huonetyyppi(tyyppi), huonenumero, paivahinta);
            System.out.println("Luotiin uusi huone: " + huone + ".");
        } catch (SQLException e) {
            if (e.getCause() != null && e.getCause() instanceof DuplicateKeyException) {
                System.out.println("Tällä huonenumerolla on jo rekisteröity huone!");
                return;
            }
            System.out.println("Uutta huonetta luodessa tapahtui virhe: " + e.getMessage());
        }
    }

    @Override
    public String getKuvaus() {
        return "lisaa huone";
    }
}