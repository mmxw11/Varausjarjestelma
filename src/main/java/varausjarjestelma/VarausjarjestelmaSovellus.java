package varausjarjestelma;

import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import varausjarjestelma.database.Tietokantahallinta;
import varausjarjestelma.database.dao.AsiakasDao;
import varausjarjestelma.domain.Asiakas;
import varausjarjestelma.ui.Tekstikayttoliittyma;

/**
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
        AsiakasDao adao = thallinta.getDao(AsiakasDao.class);
        Asiakas a = new Asiakas("TEsti maikalainen", "TERVELOLMOI", "matias.joo@example.com");
        adao.create(a);
        Asiakas d = adao.read(a.getId());
        System.out.println("luotu asiakas: " + d);
        d.setSahkopostiosoite("uusi.sahkoposti@test.com");
        adao.update(d);
        d = adao.read(d.getId());
        System.out.println("paivitetty asiakas: " + d);
        Scanner scanner = new Scanner(System.in);
        tekstikayttoliittyma.start(scanner);
    }
}