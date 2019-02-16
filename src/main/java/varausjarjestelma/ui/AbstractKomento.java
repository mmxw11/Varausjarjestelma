package varausjarjestelma.ui;

import java.util.Scanner;

import varausjarjestelma.VarausjarjestelmaHallinta;

public interface AbstractKomento {

    void suorita(Scanner scanner, VarausjarjestelmaHallinta vhallinta);

    String getKuvaus();
}