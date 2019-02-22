package varausjarjestelma.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

import varausjarjestelma.database.dao.HuonetyyppiDao;
import varausjarjestelma.domain.builder.JoinLuokka;

/**
 * @author Matias
 */
public class Huone {

    private int huonenumero;
    @JoinLuokka(HuonetyyppiDao.class)
    private Huonetyyppi huonetyyppi;
    private BigDecimal paivahinta;

    public Huone() {
        this.huonenumero = -1;
    }

    public Huone(int huonenumero, Huonetyyppi huonetyyppi, BigDecimal paivahinta) {
        this.huonenumero = huonenumero;
        this.huonetyyppi = huonetyyppi;
        this.paivahinta = paivahinta.setScale(2, RoundingMode.HALF_EVEN);
    }

    public void setHuonenumero(int huonenumero) {
        // Tämä metodi on vain Springia varten.
        if (this.huonenumero != -1) {
            throw new RuntimeException("Pääavainta ei voi muuttaa!");
        }
        this.huonenumero = huonenumero;
    }

    public void setHuonetyyppi(Huonetyyppi huonetyyppi) {
        this.huonetyyppi = huonetyyppi;
    }

    /**
     * Asettaa huoneen päivähinnan. Hinta voi koostua korkeintaan 10:stä kokonaisluvusta
     * ja se tallennetaan aina kahden desimaalin tarkkuudella.
     * @param paivahinta
     */
    public void setPaivahinta(BigDecimal paivahinta) {
        this.paivahinta = paivahinta.setScale(2, RoundingMode.HALF_EVEN);
    }

    public int getHuonenumero() {
        return huonenumero;
    }

    public Huonetyyppi getHuonetyyppi() {
        return huonetyyppi;
    }

    public BigDecimal getPaivahinta() {
        return paivahinta;
    }

    @Override
    public String toString() {
        return "Huone [huonenumero=" + huonenumero + ", huonetyyppi=" + huonetyyppi + ", paivahinta=" + paivahinta + "]";
    }
}