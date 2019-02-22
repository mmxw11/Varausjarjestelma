package varausjarjestelma;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
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
import varausjarjestelma.domain.Asiakas;
import varausjarjestelma.domain.Huone;
import varausjarjestelma.domain.Huonetyyppi;
import varausjarjestelma.domain.Lisavarustetyyppi;
import varausjarjestelma.domain.builder.LuokkaParser;
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
        LuokkaParser<Huone> parser = new LuokkaParser<>(thallinta.getDao(HuoneDao.class));
        List<String> columns = parser.convertClassFieldNamesToColumns(thallinta);
        System.out.println(columns);
        // TESTI KOODIA
        // asiakasDaoTest();
        // lisavarustetyyppiDaoTest();
        huonetyyppiDaoTest();
        huoneDaoTest();
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
        Lisavarustetyyppi varuste = dao.read(9);
        System.out.println("read: varuste > " + varuste);
        varuste.setVarustetyyppi("Silitysrauta");
        dao.update(varuste);
        varuste = dao.read(9);
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
        Huonetyyppi tyyppi = dao.read(9);
        System.out.println("read: huonetyyppi > " + tyyppi);
        tyyppi.setTyyppi("Testi tyyppi");
        dao.update(tyyppi);
        tyyppi = dao.read(9);
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
            // random.nextInt() + random.nextDouble()
            dao.create(huone);
            System.out.println("create: huone > " + huone);
        }
        for (int i = 1; i <= 5; i++) {
            dao.delete(i);
        }
        Huone huone = dao.read(9);
        System.out.println("read: huone > " + huone);
        /**
        
        tyyppi.setTyyppi("Testi tyyppi");
        dao.update(tyyppi);
        tyyppi = dao.read(9);
        System.out.println("read update: huonetyyppi > " + tyyppi);*/
        System.out.println("------------------------- HUONETYYPPI TEST -------------------------");
    }
}