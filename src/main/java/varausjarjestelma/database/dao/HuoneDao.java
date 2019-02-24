package varausjarjestelma.database.dao;

import java.math.BigDecimal;
import java.sql.SQLException;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import varausjarjestelma.database.Tietokantahallinta;
import varausjarjestelma.domain.Huone;
import varausjarjestelma.domain.Huonetyyppi;
import varausjarjestelma.domain.serialization.LuokkaSerializer;

/**
 * @author Matias
 */
@Repository
public class HuoneDao extends Dao<Huone, Integer> {

    public HuoneDao(Tietokantahallinta thallinta) {
        super(thallinta, "Huone", "huonenumero", Huone.class);
        setAutoGeneratePrimaryKey(false);
    }

    @Override
    protected void initalizeSerializerSettings(LuokkaSerializer<Huone> serializer) {
        serializer.setJoinClauseType("JOIN");
        // Muunna huonetyyppi-luokka ID:ksi.
        serializer.registerSerializerStrategy("huonetyyppi", Huonetyyppi.class, Huonetyyppi::getId);
    }

    /**
     * Luo uuden huoneen tietokantaan.
     * Lisää myös huonetyypin, mikäli sitä ei löydy.
     * 
     * <p>
     * <b>Note</b>: Tämä metodi suorittaa kaikki kyselyt samassa tietokantatransaktionissa!
     * Mikäli virhe siis tapahtuu, tullaan kaikki muutokset peruuttamaan automaattisesti.
     * </p>
     * @param htyyppi
     * @param huonenumero
     * @param paivahinta
     * @return Palauttaa juuri luodun huoneen
     * @throws SQLException
     */
    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public Huone createHuone(Huonetyyppi htyyppi, int huonenumero, int paivahinta) throws SQLException {
        if (htyyppi.getId() == -1) {
            // Tarkista onko huonetyyppi jo tunnettu.
            HuonetyyppiDao htyyppiDao = thallinta.getDao(HuonetyyppiDao.class);
            Huonetyyppi lhtyyppi = htyyppiDao.readByTyyppi(htyyppi.getTyyppi());
            if (lhtyyppi != null) {
                htyyppi.setId(lhtyyppi.getId());
            } else {
                htyyppiDao.create(htyyppi);
            }
        }
        /**
         * Käyttöliittymä antaa hinnat kokonaislukuina, 
         * mutta tallennetaan ne desimaaleina kuitenkin.
         * Jos sovellus olisi oikeasti käytössä, niin myös sentitkin otettaisiin todennäköisesti mukaan. :)
         */
        // Luo uuden huoneen.
        Huone huone = new Huone(huonenumero, htyyppi, new BigDecimal(paivahinta));
        create(huone);
        return huone;
    }
}