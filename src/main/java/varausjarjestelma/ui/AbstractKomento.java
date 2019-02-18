package varausjarjestelma.ui;

import java.util.Scanner;

import varausjarjestelma.database.Tietokantahallinta;

public interface AbstractKomento {

    void execute(Scanner scanner, Tietokantahallinta thallinta);

    String getKuvaus();
}