package varausjarjestelma.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import varausjarjestelma.database.dao.AsiakasDao;
import varausjarjestelma.domain.builder.JoinLuokka;

public class Varaus {

    private int id;
    @JoinLuokka(AsiakasDao.class)
    private Asiakas asiakas;
    private LocalDateTime alkupaivamaara;
    private LocalDateTime loppupaivamaara;
    private int varauksenkesto;
    private BigDecimal yhteishinta;

    public Varaus() {
        this.id = -1;
    }

    public Varaus(Asiakas asiakas, LocalDateTime alkupaivamaara, LocalDateTime loppupaivamaara, int varauksenkesto, BigDecimal yhteishinta) {
        this.id = -1;
        this.asiakas = asiakas;
        this.alkupaivamaara = alkupaivamaara;
        this.loppupaivamaara = loppupaivamaara;
        this.varauksenkesto = varauksenkesto;
        this.yhteishinta = yhteishinta.setScale(2, RoundingMode.HALF_EVEN);
    }

    /**
     * Asettaa taulukon pääavaimen.
     * Tämä metodi on tarkoitettu vain Springin luokan rakentamista varten.
     * @param id
     */
    public void setId(int id) {
        if (this.id != -1) {
            throw new RuntimeException("Pääavainta ei voi muuttaa!");
        }
        this.id = id;
    }

    public void setAsiakas(Asiakas asiakas) {
        this.asiakas = asiakas;
    }

    public void setAlkupaivamaara(LocalDateTime alkupaivamaara) {
        this.alkupaivamaara = alkupaivamaara;
    }

    public void setLoppupaivamaara(LocalDateTime loppupaivamaara) {
        this.loppupaivamaara = loppupaivamaara;
    }

    public void setVarauksenkesto(int varauksenkesto) {
        this.varauksenkesto = varauksenkesto;
    }

    /**
     * Asettaa varauksen yhteishinnan. Hinta voi koostua korkeintaan 10:stä kokonaisluvusta
     * ja se tallennetaan aina kahden desimaalin tarkkuudella.
     * @param paivahinta
     */
    public void setYhteishinta(BigDecimal yhteishinta) {
        this.yhteishinta = yhteishinta.setScale(2, RoundingMode.HALF_EVEN);
    }

    public int getId() {
        return id;
    }

    public Asiakas getAsiakas() {
        return asiakas;
    }

    public LocalDateTime getAlkupaivamaara() {
        return alkupaivamaara;
    }

    public LocalDateTime getLoppupaivamaara() {
        return loppupaivamaara;
    }

    public int getVarauksenkesto() {
        return varauksenkesto;
    }

    public BigDecimal getYhteishinta() {
        return yhteishinta;
    }

    @Override
    public String toString() {
        return "Varaus [id=" + id + ", asiakas=" + asiakas + ", alkupaivamaara=" + alkupaivamaara + ", loppupaivamaara=" + loppupaivamaara + ", varauksenkesto="
                + varauksenkesto + ", yhteishinta=" + yhteishinta + "]";
    }
}