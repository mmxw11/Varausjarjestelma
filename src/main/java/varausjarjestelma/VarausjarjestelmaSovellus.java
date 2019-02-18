package varausjarjestelma;

import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import varausjarjestelma.database.Tietokantahallinta;
import varausjarjestelma.ui.Tekstikayttoliittyma;

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
        /**
        AsiakasDao adao= thallinta.getDao(AsiakasDao.class);
        Asiakas a = new Asiakas("Maija Meikäläinen", "0554043850", "maija.meikalainen@example.com");
        System.out.println("id1 " + a.getId());
        adao.create(a);
        System.out.println("id2 " + a.getId());*/
        Scanner scanner = new Scanner(System.in);
        tekstikayttoliittyma.start(scanner);
    }
}