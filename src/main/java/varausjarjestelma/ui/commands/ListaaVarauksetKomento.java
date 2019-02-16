package varausjarjestelma.ui.commands;

import java.util.Scanner;

import varausjarjestelma.Varaushallinta;
import varausjarjestelma.ui.AbstractKomento;

public class ListaaVarauksetKomento implements AbstractKomento {

    @Override
    public void suorita(Scanner scanner, Varaushallinta vhallinta) {
        System.out.println("Listataan varaukset");
        System.out.println("");
        
        // alla olevassa esimerkissä oletetaan, että tietokannassa on
        // kolme varausta
        System.out.println("Essi Esimerkki, essi@esimerkki.net, 2019-02-14, 2019-02-15, 1 päivä, 2 lisävarustetta, 1 huone. Huoneet:");
        System.out.println("\tCommodore, 128, 229 euroa");
        System.out.println("\tYhteensä: 229 euroa");
        System.out.println("");
        System.out.println("Anssi Asiakas, anssi@asiakas.net, 2019-02-14, 2019-02-15, 1 päivä, 0 lisävarustetta, 1 huone. Huoneet:");
        System.out.println("\tSuperior, 705, 159 euroa");
        System.out.println("\tYhteensä: 159 euroa");
        System.out.println("");
        System.out.println("Anssi Asiakas, anssi@asiakas.net, 2020-03-18, 2020-03-21, 3 päivää, 6 lisävarustetta, 2 huonetta. Huoneet:");
        System.out.println("\tSuperior, 705, 159 euroa");
        System.out.println("\tCommodore, 128, 229 euroa");
        System.out.println("\tYhteensä: 1164 euroa");
        // TODO: IMPLEMENT
    }

    @Override
    public String getKuvaus() {
        return "listaa varaukset";
    }
}