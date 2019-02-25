package varausjarjestelma;

import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import varausjarjestelma.database.Tietokantahallinta;
import varausjarjestelma.ui.Tekstikayttoliittyma;

/**
 * Sovelluksen pääluokka.
 * Koko projekti löytyy myös GitHubissa, josta löytyy kehityksen vaiheet ja selvennystä sovelluksen rakenteesta.
 * Tämän löytää osoitteesta: https://github.com/mmxw11/Varausjarjestelma
 * 
 * @author Matias
 */
@SpringBootApplication
public class VarausjarjestelmaSovellus implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(VarausjarjestelmaSovellus.class);
    }

    @Autowired
    Tietokantahallinta thallinta;
    @Autowired
    Tekstikayttoliittyma tekstikayttoliittyma;

    @Override
    public void run(String... args) throws Exception {
        thallinta.initialize();
        Scanner scanner = new Scanner(System.in);
        tekstikayttoliittyma.start(scanner);
    }
}