package varausjarjestelma.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import varausjarjestelma.domain.serialization.parser.SarakeAsetukset;
import varausjarjestelma.domain.serialization.parser.SarakeTyyppi;

public class Varaus {

    private int id;
    @SarakeAsetukset(columnName = "asiakas_id", tyyppi = SarakeTyyppi.FOREIGN_KEY)
    private Asiakas asiakas;
    private LocalDateTime alkupaivamaara;
    private LocalDateTime loppupaivamaara;
    private BigDecimal yhteishinta;
    @SarakeAsetukset(tyyppi = SarakeTyyppi.DYNAMICALLY_GENERATED)
    private int lisavarustemaara;
    private transient List<Huone> huoneet;

    public Varaus() {
        this.id = -1;
        this.huoneet = new ArrayList<>();
    }

    public Varaus(Asiakas asiakas, LocalDateTime alkupaivamaara, LocalDateTime loppupaivamaara, BigDecimal yhteishinta, int lisavarustemaara, List<Huone> huoneet) {
        this.id = -1;
        this.asiakas = asiakas;
        this.alkupaivamaara = alkupaivamaara;
        this.loppupaivamaara = loppupaivamaara;
        this.yhteishinta = yhteishinta.setScale(2, RoundingMode.HALF_EVEN);
        this.lisavarustemaara = lisavarustemaara;
        this.huoneet = huoneet;
    }

    /**
     * Asettaa taulukon pääavaimen.
     * Tämä metodi on tarkoitettu vain luokan rakentamista varten.
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

    /**
     * Asettaa varauksen yhteishinnan. Hinta voi koostua korkeintaan 10:stä kokonaisluvusta
     * ja se tallennetaan aina kahden desimaalin tarkkuudella.
     * @param paivahinta
     */
    public void setYhteishinta(BigDecimal yhteishinta) {
        this.yhteishinta = yhteishinta.setScale(2, RoundingMode.HALF_EVEN);
    }

    public void setLisavarustemaara(int lisavarustemaara) {
        this.lisavarustemaara = lisavarustemaara;
    }

    public void setHuoneet(List<Huone> huoneet) {
        if (huoneet == null) {
            return;
        }
        this.huoneet = huoneet;
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

    public BigDecimal getYhteishinta() {
        return yhteishinta;
    }

    public List<Huone> getHuoneet() {
        return huoneet;
    }

    public int getLisavarustemaara() {
        return lisavarustemaara;
    }

    @Override
    public String toString() {
        LocalDate alkupaivamaaraDate = alkupaivamaara.toLocalDate();
        LocalDate loppumaaraDate = loppupaivamaara.toLocalDate();
        // Muuta päiviksi, joten kellonaika ei häiritse laskua.
        long bookedDays = ChronoUnit.DAYS.between(alkupaivamaaraDate, loppumaaraDate);
        StringBuilder sbuilder = new StringBuilder();
        sbuilder.append(asiakas.getNimi())
                .append(", ")
                .append(asiakas.getSahkopostiosoite())
                .append(", ")
                // Jätetään kellonaika pois mahdollisia automaattisia testejä varten(?)
                .append(alkupaivamaaraDate)
                .append(", ")
                .append(loppumaaraDate)
                .append(", ")
                .append(bookedDays)
                .append(bookedDays > 1 ? " päivää" : " päivä")
                .append(", ")
                .append(lisavarustemaara)
                .append(" lisävarustetta")
                .append(", ")
                .append(huoneet.size())
                .append(huoneet.size() > 1 ? " huonetta" : " huone");
        return sbuilder.toString();
    }
}