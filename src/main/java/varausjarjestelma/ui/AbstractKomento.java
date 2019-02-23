package varausjarjestelma.ui;

import java.util.Scanner;

import varausjarjestelma.database.Tietokantahallinta;

/**
 * @author Matias
 */
public interface AbstractKomento {

    /**
     * Suorittaa komennon.
     * @param scanner
     * @param thallinta
     */
    void execute(Scanner scanner, Tietokantahallinta thallinta);

    /**
     * @return Palauttaa kuvauksen komennosta
     */
    String getKuvaus();
}