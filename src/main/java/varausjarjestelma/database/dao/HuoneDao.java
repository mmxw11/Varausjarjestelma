package varausjarjestelma.database.dao;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import varausjarjestelma.database.SQLJoinVarasto;
import varausjarjestelma.database.SQLKyselyRakentaja;
import varausjarjestelma.database.Tietokantahallinta;
import varausjarjestelma.database.dao.querysettings.HuoneHakuAsetukset;
import varausjarjestelma.domain.Huone;
import varausjarjestelma.domain.Huonetyyppi;
import varausjarjestelma.domain.serialization.LuokkaSerializer;
import varausjarjestelma.domain.serialization.TauluSarake;
import varausjarjestelma.domain.serialization.TulosLuokkaRakentaja;

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
         * Käyttöliittymä antaa hinnat kokonaislukuina, mutta tallennetaan ne kuitenkin desimaaleina.
         * Jos sovellus olisi oikeasti käytössä, niin myös sentitkin otettaisiin todennäköisesti mukaan. :)
         */
        // Luo uuden huoneen.
        Huone huone = new Huone(huonenumero, htyyppi, new BigDecimal(paivahinta));
        create(huone);
        return huone;
    }

    /**
     * Hakee tietokannasta kaikki huoneet.
     * @return Palauttaa huoneet listalla järjesteltynä huonetyypin perusteella
     * @throws SQLException
     */
    public List<Huone> readHuoneet() throws SQLException {
        SQLJoinVarasto joinVarasto = buildJoinVarasto();
        List<TauluSarake> columns = serializer.convertClassFieldsToColumns(tableName, joinVarasto);
        String sql = SQLKyselyRakentaja.buildSelectQuery(resultClass, tableName, columns, joinVarasto)
                .append(" ORDER BY Huonetyyppi.id")
                .toString();
        return thallinta.executeQuery(jdbcTemp -> jdbcTemp.query(sql, new TulosLuokkaRakentaja<>(this, thallinta)));
    }

    /**
     * Hakee tietokannasta varaamattomat huoneet, jotka vastaavat hakuehtoja.
     * @param hakuAsetukset
     * @return Palauttaa varaamattomat huoneet listalla
     * @throws SQLException
     */
    public List<Huone> getNonBookedHuoneet(HuoneHakuAsetukset hakuAsetukset) throws SQLException {
        Huonetyyppi htyyppi = hakuAsetukset.getHtyyppi();
        if (htyyppi != null && htyyppi.getId() == -1) {
            // Tarkista löytyykö huonetyyppiä.
            HuonetyyppiDao htyyppiDao = thallinta.getDao(HuonetyyppiDao.class);
            Huonetyyppi lhtyyppi = htyyppiDao.readByTyyppi(htyyppi.getTyyppi());
            if (lhtyyppi == null) {
                // Tuntematon huonetyyppi.
                return new ArrayList<>();
            }
            htyyppi.setId(lhtyyppi.getId());
        }
        // Rakenna SQL-kysely.
        SQLJoinVarasto joinVarasto = buildJoinVarasto();
        List<Object> queryParams = new ArrayList<>(Arrays.asList(
                hakuAsetukset.getAlkupaivamaara(),
                hakuAsetukset.getLoppupaivamaara()));
        List<TauluSarake> columns = serializer.convertClassFieldsToColumns(tableName, joinVarasto);
        // Käytetään alikyselyä.
        StringBuilder sqlBuilder = SQLKyselyRakentaja.buildSelectQuery(resultClass, tableName, columns, joinVarasto)
                .append(" WHERE NOT EXISTS (")
                .append("SELECT HuoneVaraus.huonenumero FROM HuoneVaraus JOIN Varaus ON Varaus.id = HuoneVaraus.varaus_id")
                .append(" WHERE HuoneVaraus.huonenumero = Huone.huonenumero")
                .append(" AND (")
                .append("? <= Varaus.loppupaivamaara")
                .append(" AND ")
                .append("? >= Varaus.alkupaivamaara")
                .append("))");
        if (htyyppi != null) {
            sqlBuilder.append(" AND Huonetyyppi.id = ?");
            queryParams.add(htyyppi.getId());
        }
        if (hakuAsetukset.getMaksimihinta() != -1) {
            sqlBuilder.append(" AND Huone.paivahinta <= ?");
            queryParams.add(hakuAsetukset.getMaksimihinta());
        }
        if (hakuAsetukset.getResultOrder() != null) {
            sqlBuilder.append(" ").append(hakuAsetukset.getResultOrder());
        }
        String sql = sqlBuilder.toString();
        return thallinta.executeQuery(jdbcTemp -> jdbcTemp.query(sql, new TulosLuokkaRakentaja<>(this, thallinta), queryParams.toArray()));
    }
}