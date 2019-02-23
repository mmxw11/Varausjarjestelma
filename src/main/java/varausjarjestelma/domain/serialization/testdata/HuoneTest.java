package varausjarjestelma.domain.serialization.testdata;

import java.math.BigDecimal;
import java.math.RoundingMode;

import varausjarjestelma.domain.Huonetyyppi;
import varausjarjestelma.domain.serialization.parser.SarakeAsetukset;
import varausjarjestelma.domain.serialization.parser.SarakeTyyppi;

/**
 * @author Matias
 */
public class HuoneTest {

    private int huonenumero;
    @SarakeAsetukset(columnName = "huonetyyppi_id", tyyppi = SarakeTyyppi.FOREIGN_KEY)
    private Huonetyyppi huonetyyppi;
    private BigDecimal paivahinta;

    public HuoneTest() {
        this.huonenumero = -1;
    }

    public HuoneTest(int huonenumero, Huonetyyppi huonetyyppi, BigDecimal paivahinta) {
        this.huonenumero = huonenumero;
        this.huonetyyppi = huonetyyppi;
        this.paivahinta = paivahinta.setScale(2, RoundingMode.HALF_EVEN);
    }

    /**
     * Asettaa taulukon pääavaimen.
     * Tämä metodi on tarkoitettu vain Springin luokan rakentamista varten.
     * @param huonenumero
     */
    public void setHuonenumero(int huonenumero) {
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
        return "HuoneTest [huonenumero=" + huonenumero + ", huonetyyppi=" + huonetyyppi + ", paivahinta=" + paivahinta + "]";
    }
}