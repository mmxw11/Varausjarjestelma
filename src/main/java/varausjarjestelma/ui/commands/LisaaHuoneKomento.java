package varausjarjestelma.ui.commands;

import java.util.Scanner;

import varausjarjestelma.database.Tietokantahallinta;
import varausjarjestelma.ui.AbstractKomento;

public class LisaaHuoneKomento implements AbstractKomento {

    @Override
    public void execute(Scanner scanner, Tietokantahallinta thallinta) {
        System.out.println("Lisätään huone");
        System.out.println("");
        System.out.println("Minkä tyyppinen huone on?");
        String tyyppi = scanner.nextLine();
        System.out.println("Mikä huoneen numeroksi asetetaan?");
        int numero = Integer.valueOf(scanner.nextLine());
        System.out.println("Kuinka monta euroa huone maksaa yöltä?");
        int hinta = Integer.valueOf(scanner.nextLine());
        // TODO: IMPLEMENT
    }

    @Override
    public String getKuvaus() {
        return "lisaa huone";
    }
}