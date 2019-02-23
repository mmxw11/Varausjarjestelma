package varausjarjestelma;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import varausjarjestelma.database.Tietokantahallinta;
import varausjarjestelma.database.dao.AsiakasDao;
import varausjarjestelma.database.dao.HuoneDao;
import varausjarjestelma.database.dao.HuonetyyppiDao;
import varausjarjestelma.database.dao.LisavarustetyyppiDao;
import varausjarjestelma.database.dao.VarausDao;
import varausjarjestelma.domain.Asiakas;
import varausjarjestelma.domain.Huone;
import varausjarjestelma.domain.Huonetyyppi;
import varausjarjestelma.domain.Lisavarustetyyppi;
import varausjarjestelma.domain.Varaus;
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
        lisavarustetyyppiDaoTest();
        huonetyyppiDaoTest();
        huoneDaoTest();
        varausDaoTest();
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
        System.out.println("create: asiakas2 > " + asiakas2Get);
        // dao.delete(asiakas1Get.getId());
        asiakas2Get.setSahkopostiosoite("uusi.sahkoposti@test.com");
        dao.update(asiakas2Get);
        asiakas2Get = dao.read(asiakas2Get.getId());
        System.out.println("update: asiakas > " + asiakas2Get);
        System.out.println("------------------------- ASIAKAS TEST -------------------------");
    }

    private void lisavarustetyyppiDaoTest() throws SQLException {
        System.out.println("------------------------- LISÄVARUSTETYYPPI TEST -------------------------");
        LisavarustetyyppiDao dao = thallinta.getDao(LisavarustetyyppiDao.class);
        for (int i = 0; i < 10; i++) {
            Lisavarustetyyppi varuste = new Lisavarustetyyppi(i + " test!");
            dao.create(varuste);
            System.out.println("create: varuste > " + varuste);
        }
        for (int i = 1; i <= 5; i++) {
            dao.delete(i);
        }
        int lisavarustetyyppiId = 6 + new Random().nextInt(3);
        Lisavarustetyyppi varuste = dao.read(lisavarustetyyppiId);
        System.out.println("read: varuste > " + varuste);
        varuste.setVarustetyyppi("Silitysrauta");
        dao.update(varuste);
        varuste = dao.read(lisavarustetyyppiId);
        System.out.println("read update: varuste > " + varuste);
        System.out.println("------------------------- LISÄVARUSTETYYPPI TEST -------------------------");
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
        int huonetyyppiId = 6 + new Random().nextInt(3);
        Huonetyyppi tyyppi = dao.read(huonetyyppiId);
        System.out.println("read: huonetyyppi > " + tyyppi);
        tyyppi.setTyyppi("Testi tyyppi");
        dao.update(tyyppi);
        tyyppi = dao.read(huonetyyppiId);
        System.out.println("read update: huonetyyppi > " + tyyppi);
        System.out.println("------------------------- HUONETYYPPI TEST -------------------------");
    }

    private void huoneDaoTest() throws SQLException {
        System.out.println("------------------------- HUONE TEST -------------------------");
        HuoneDao dao = thallinta.getDao(HuoneDao.class);
        HuonetyyppiDao tyyppidao = thallinta.getDao(HuonetyyppiDao.class);
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            Huone huone = new Huone(i, tyyppidao.read(6 + random.nextInt(3)),
                    new BigDecimal(random.nextInt(1000) + random.nextDouble()));
            dao.create(huone);
            System.out.println("create: huone > " + huone);
        }
        for (int i = 1; i <= 5; i++) {
            dao.delete(i);
        }
        int huoneId = 6 + random.nextInt(3);
        Huone huone = dao.read(huoneId);
        System.out.println("read: huone > " + huone);
        huone.setPaivahinta(new BigDecimal(60000.3));
        dao.update(huone);
        huone = dao.read(huoneId);
        System.out.println("read update: huone > " + huone);
        System.out.println("------------------------- HUONE TEST -------------------------");
    }

    private void varausDaoTest() throws SQLException {
        System.out.println("------------------------- VARAUS TEST -------------------------");
        VarausDao dao = thallinta.getDao(VarausDao.class);
        AsiakasDao asiakasDao = thallinta.getDao(AsiakasDao.class);
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            Varaus varaus = new Varaus(asiakasDao.read(random.nextBoolean() ? 1 : 2),
                    LocalDateTime.now(), LocalDateTime.now().minusHours(random.nextInt(24)), random.nextInt(), new BigDecimal(random.nextInt(1000) + random.nextDouble()),
                    random.nextInt(), random.nextInt());
            dao.create(varaus);
            System.out.println("create: varaus > " + varaus);
        }
        for (int i = 1; i <= 5; i++) {
            dao.delete(i);
        }
        int varausId = 6 + random.nextInt(3);
        Varaus varaus = dao.read(varausId);
        System.out.println("read: varaus > " + varaus);
        varaus.setYhteishinta(new BigDecimal(696969));
        dao.update(varaus);
        varaus = dao.read(varausId);
        System.out.println("read update: varaus > " + varaus);
        System.out.println("------------------------- VARAUS TEST -------------------------");
    }
}