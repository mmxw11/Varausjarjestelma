package varausjarjestelma.ui.commands;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

import varausjarjestelma.Varaushallinta;
import varausjarjestelma.ui.AbstractKomento;

public class HaeHuoneitaKomento implements AbstractKomento {

    @Override
    public void execute(Scanner scanner, Varaushallinta vhallinta) {
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
        
        // esimerkkitulostus -- tässä oletetaan, että vapaita huoneita löytyy 2
        System.out.println("Vapaat huoneet: ");
        System.out.println("Excelsior, 604, 119 euroa");
        System.out.println("Excelsior, 605, 119 euroa");
        
        // vaihtoehtoisesti, mikäli yhtäkään huonetta ei ole vapaana, ohjelma
        // tulostaa
        System.out.println("Ei vapaita huoneita.");
        // TODO: IMPLEMENT
    }

    @Override
    public String getKuvaus() {
        return "hae huoneita";
    }
}