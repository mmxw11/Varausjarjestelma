package varausjarjestelma.database.dao;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import varausjarjestelma.database.SQLJoinVarasto;
import varausjarjestelma.database.SQLKyselyRakentaja;
import varausjarjestelma.database.Tietokantahallinta;
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
    public List<Huone> readAllHuoneet() throws SQLException {
        SQLJoinVarasto joinVarasto = buildJoinVarasto();
        List<TauluSarake> columns = serializer.convertClassFieldsToColumns(tableName, joinVarasto);
        String sql = SQLKyselyRakentaja.buildSelectQuery(resultClass, tableName, columns, joinVarasto)
                .append(" ORDER BY Huonetyyppi.id")
                .toString();
        return thallinta.executeQuery(jdbcTemp -> jdbcTemp.query(sql, new TulosLuokkaRakentaja<>(this, thallinta)));
    }

    // TODO: PROTOTYPE UNDER DEVELOPMENT!
    public List<Huone> getNonReservedHuoneet(LocalDateTime startDate, LocalDateTime endDate, Huonetyyppi htyyppi, int maksimihinta) throws SQLException {
        System.out.println("Syötetty data: alku: " + startDate + " | loppu: " + endDate + " | tyyppi: " + htyyppi +
                " | maksimihinta: " + maksimihinta);
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
        System.out.println("vaihe2");
        // Rakenna SQL-kysely.
        SQLJoinVarasto joinVarasto = buildJoinVarasto();
        List<Object> queryParams = new ArrayList<>(Arrays.asList(startDate, endDate, startDate, endDate));
        List<TauluSarake> columns = serializer.convertClassFieldsToColumns(tableName, joinVarasto);
        // Lisätään tarvittavat JOIN-lausekkeet.
        joinVarasto.addSQLJoinClause("HuoneVaraus", "LEFT JOIN HuoneVaraus ON HuoneVaraus.huonenumero = Huone.huonenumero")
                .addSQLJoinClause("Varaus", "LEFT JOIN Varaus ON Varaus.id = HuoneVaraus.varaus_id");
        StringBuilder sqlBuilder = SQLKyselyRakentaja.buildSelectQuery(resultClass, "SELECT DISTINCT", tableName, columns, joinVarasto)
                .append(" WHERE (")
                .append("(Varaus.alkupaivamaara IS NULL OR (Varaus.alkupaivamaara NOT BETWEEN ? AND ?))")
                .append(" AND ")
                .append("(Varaus.loppupaivamaara IS NULL OR (Varaus.loppupaivamaara NOT BETWEEN ? AND ?))")
                .append(")");
        if (htyyppi != null) {
            sqlBuilder.append(" AND Huonetyyppi.id = ?");
            queryParams.add(htyyppi.getId());
        }
        if (maksimihinta != -1) {
            sqlBuilder.append(" AND Huone.paivahinta <= ?");
            queryParams.add(maksimihinta);
        }
        String sql = sqlBuilder.toString();
        System.out.println(queryParams);
        System.out.println("vaihe3: " + sql);
        // TODO: ORDER BY?
        return thallinta.executeQuery(jdbcTemp -> jdbcTemp.query(sql, new TulosLuokkaRakentaja<>(this, thallinta), queryParams.toArray()));
    }
}