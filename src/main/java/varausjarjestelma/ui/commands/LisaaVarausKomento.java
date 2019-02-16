package varausjarjestelma.ui.commands;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import varausjarjestelma.Varaushallinta;
import varausjarjestelma.ui.AbstractKomento;

public class LisaaVarausKomento implements AbstractKomento {

    @Override
    public void suorita(Scanner scanner, Varaushallinta vhallinta) {
        System.out.println("Haetaan huoneita");
        System.out.println("");

        System.out.println("Milloin varaus alkaisi (yyyy-MM-dd)?");;
        LocalDateTime alku = LocalDateTime.parse(scanner.nextLine() + " " + "16:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        System.out.println("Milloin varaus loppuisi (yyyy-MM-dd)?");
        LocalDateTime loppu = LocalDateTime.parse(scanner.nextLine() + " " + "10:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        System.out.println("Minkä tyyppinen huone? (tyhjä = ei rajausta)");
        String tyyppi = scanner.nextLine();
        System.out.println("Minkä hintainen korkeintaan? (tyhjä = ei rajausta)");
        String maksimihinta = scanner.nextLine();

        // mikäli huoneita ei ole vapaana, ohjelma tulostaa seuraavan viestin
        // ja varauksen lisääminen loppuu
        System.out.println("Ei vapaita huoneita.");

        // muulloin, ohjelma kertoo vapaiden huoneiden lukumäärän. Tässä 
        // oletetaan että vapaita huoneita on 2.
        System.out.println("Huoneita vapaana: 2");
        System.out.println("");

        // tämän jälkeen kysytään varattavien huoneiden lukumäärää
        // luvuksi tulee hyväksyä vain sopiva luku, esimerkissä 3 ei esim
        // kävisi, sillä vapaita huoneita vain 2
        int huoneita = -1;
        while (true) {
            System.out.println("Montako huonetta varataan?");
            huoneita = Integer.valueOf(scanner.nextLine());
            if (huoneita >= 1 && huoneita <= 2) {
                break;
            }

            System.out.println("Epäkelpo huoneiden lukumäärä.");
        }

        // tämän jälkeen kysytään lisävarusteet
        List<String> lisavarusteet = new ArrayList<>();
        while (true) {
            System.out.println("Syötä lisävaruste, tyhjä lopettaa");
            String lisavaruste = scanner.nextLine();
            if (lisavaruste.isEmpty()) {
                break;
            }

            lisavarusteet.add(lisavaruste);
        }

        // ja lopuksi varaajan tiedot
        System.out.println("Syötä varaajan nimi:");
        String nimi = scanner.nextLine();
        System.out.println("Syötä varaajan puhelinnumero:");
        String puhelinnumero = scanner.nextLine();
        System.out.println("Syötä varaajan sähköpostiosoite:");
        String sahkoposti = scanner.nextLine();

        // kun kaikki tiedot on kerätty, ohjelma lisää varauksen tietokantaan
        // -- varaukseen tulee lisätä kalleimmat vapaat huoneet!
        // TODO: IMPLEMENT
    }

    @Override
    public String getKuvaus() {
        return "lisaa varaus";
    }
}