package varausjarjestelma.database.dao.querysettings;

import java.time.LocalDateTime;

import varausjarjestelma.domain.Huonetyyppi;

/**
 * Sisältää huoneiden hakemiseen liittyviä asetuksia.
 * 
 * @author Matias
 */
public class HuoneHakuAsetukset {

    private LocalDateTime alkupaivamaara;
    private LocalDateTime loppupaivamaara;
    private Huonetyyppi htyyppi;
    private int maksimihinta;
    private String resultOrder;

    private HuoneHakuAsetukset(LocalDateTime alkupaivamaara, LocalDateTime loppupaivamaara) {
        this.alkupaivamaara = alkupaivamaara;
        this.loppupaivamaara = loppupaivamaara;
        this.maksimihinta = -1;
    }

    public static HuoneHakuAsetukset newSettings(LocalDateTime alkupaivamaara, LocalDateTime loppupaivamaara) {
        return new HuoneHakuAsetukset(alkupaivamaara, loppupaivamaara);
    }

    public HuoneHakuAsetukset setAlkupaivamaara(LocalDateTime alkupaivamaara) {
        this.alkupaivamaara = alkupaivamaara;
        return this;
    }

    public HuoneHakuAsetukset setLoppupaivamaara(LocalDateTime loppupaivamaara) {
        this.loppupaivamaara = loppupaivamaara;
        return this;
    }

    public HuoneHakuAsetukset setHtyyppi(Huonetyyppi htyyppi) {
        this.htyyppi = htyyppi;
        return this;
    }

    public HuoneHakuAsetukset setMaksimihinta(int maksimihinta) {
        this.maksimihinta = maksimihinta;
        return this;
    }

    public HuoneHakuAsetukset setResultOrder(String resultOrder) {
        this.resultOrder = resultOrder;
        return this;
    }

    public LocalDateTime getAlkupaivamaara() {
        return alkupaivamaara;
    }

    public LocalDateTime getLoppupaivamaara() {
        return loppupaivamaara;
    }

    public Huonetyyppi getHtyyppi() {
        return htyyppi;
    }

    public int getMaksimihinta() {
        return maksimihinta;
    }

    public String getResultOrder() {
        return resultOrder;
    }
}