package varausjarjestelma.domain;

import java.time.LocalDate;

public class Varaus {

    private int id;
    private Asiakas asiakas;
    private LocalDate alkupaivamaara;
    private LocalDate loppupaivamaara;
    private int varauksenkesto;
    private double yhteishinta;

    public Varaus() {}

    public int getId() {
        return id;
    }

    public Asiakas getAsiakas() {
        return asiakas;
    }

    public LocalDate getAlkupaivamaara() {
        return alkupaivamaara;
    }

    public LocalDate getLoppupaivamaara() {
        return loppupaivamaara;
    }

    public int getVarauksenkesto() {
        return varauksenkesto;
    }

    public double getYhteishinta() {
        return yhteishinta;
    }
}