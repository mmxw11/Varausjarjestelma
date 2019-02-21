package varausjarjestelma;

import java.sql.SQLException;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import varausjarjestelma.database.Tietokantahallinta;
import varausjarjestelma.database.dao.AsiakasDao;
import varausjarjestelma.database.dao.HuonetyyppiDao;
import varausjarjestelma.database.dao.LisavarusteDao;
import varausjarjestelma.domain.Asiakas;
import varausjarjestelma.domain.Huonetyyppi;
import varausjarjestelma.domain.Lisavaruste;
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
        // TESTI KOODIA
        asiakasDaoTest();
        lisavarusteDaoTest();
        huonetyyppiDaoTest();
        // END OF TESTI KOODIA
        Scanner scanner = new Scanner(System.in);
        tekstikayttoliittyma.start(scanner);
    }

    private void asiakasDaoTest() throws SQLException {
        System.out.println("------------------------- ASIAKAS TEST -------------------------");
        AsiakasDao dao = thallinta.getDao(AsiakasDao.class);
        Asiakas asiakas = new Asiakas("Matti Meikäläinen", "064765456", "matti.testi@example.com");
        dao.create(asiakas);
        Asiakas asiakas2 = new Asiakas("Tero Pitkänen", "0493473956", "tero.pitkanen@example.com");
        dao.create(asiakas2);
        Asiakas asiakas1Get = dao.read(asiakas.getId());
        System.out.println("create: asiakas > " + asiakas1Get);
        Asiakas asiakas2Get = dao.read(asiakas2.getId());
        System.out.println("create: asiakas3 > " + asiakas2Get);
        // dao.delete(asiakas1Get.getId());
        asiakas2Get.setSahkopostiosoite("uusi.sahkoposti@test.com");
        dao.update(asiakas2Get);
        asiakas2Get = dao.read(asiakas2Get.getId());
        System.out.println("update: asiakas > " + asiakas2Get);
        System.out.println("------------------------- ASIAKAS TEST -------------------------");
    }

    private void lisavarusteDaoTest() throws SQLException {
        System.out.println("------------------------- LISÄVARUSTE TEST -------------------------");
        LisavarusteDao dao = thallinta.getDao(LisavarusteDao.class);
        for (int i = 0; i < 10; i++) {
            Lisavaruste varuste = new Lisavaruste(i + " test!");
            dao.create(varuste);
            System.out.println("create: varuste > " + varuste);
        }
        for (int i = 1; i <= 5; i++) {
            dao.delete(i);
        }
        Lisavaruste varuste = dao.read(9);
        System.out.println("read: varuste > " + varuste);
        varuste.setVaruste("Silitysrauta");
        dao.update(varuste);
        varuste = dao.read(9);
        System.out.println("read update: varuste > " + varuste);
        System.out.println("------------------------- LISÄVARUSTE TEST -------------------------");
    }

    private void huonetyyppiDaoTest() throws SQLException {
        System.out.println("------------------------- HUONETYYPPI TEST -------------------------");
        HuonetyyppiDao dao = thallinta.getDao(HuonetyyppiDao.class);
        for (int i = 0; i < 10; i++) {
            Huonetyyppi tyyppi = new Huonetyyppi(i + " huonetyyppi");
            dao.create(tyyppi);
            System.out.println("create: huonetyyppi > " + tyyppi);
        }
        for (int i = 1; i <= 5; i++) {
            dao.delete(i);
        }
        Huonetyyppi tyyppi = dao.read(9);
        System.out.println("read: huonetyyppi > " + tyyppi);
        tyyppi.setTyyppi("Testi tyyppi");
        dao.update(tyyppi);
        tyyppi = dao.read(9);
        System.out.println("read update: huonetyyppi > " + tyyppi);
        System.out.println("------------------------- HUONETYYPPI TEST -------------------------");
    }
}