package varausjarjestelma.ui;

import java.util.Scanner;

import varausjarjestelma.Varaushallinta;

public interface AbstractKomento {

    void execute(Scanner scanner, Varaushallinta vhallinta);

    String getKuvaus();
}