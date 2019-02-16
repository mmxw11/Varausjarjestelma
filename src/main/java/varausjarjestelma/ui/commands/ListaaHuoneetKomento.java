package varausjarjestelma.ui.commands;

import java.util.Scanner;

import varausjarjestelma.Varaushallinta;
import varausjarjestelma.ui.AbstractKomento;

public class ListaaHuoneetKomento implements AbstractKomento {

    @Override
    public void suorita(Scanner scanner, Varaushallinta vhallinta) {
        System.out.println("Listataan huoneet");
        System.out.println("");
        
        // esimerkkitulostus -- tässä oletetaan, että huoneita on 4
        // tulostuksessa tulostetaan huoneen tyyppi, huoneen numero sekä hinta
        System.out.println("Excelsior, 604, 119 euroa");
        System.out.println("Excelsior, 605, 119 euroa");
        System.out.println("Superior, 705, 159 euroa");
        System.out.println("Commodore, 128, 229 euroa");
        //TODO: IMPLEMENT
    }

    @Override
    public String getKuvaus() {
        return "listaa huoneet";
    }
}