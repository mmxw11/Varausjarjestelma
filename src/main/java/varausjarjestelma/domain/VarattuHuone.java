package varausjarjestelma.domain;

/**
 * Tämä luokka pitää kirjaa, siitä mihin varaukseen huone kuuluu.
 * Näin voidaan hakea moneen varaukseen liittyvät huoneet samalla kyselyllä.
 * 
 * @author Matias
 */
public class VarattuHuone extends Huone {

    private int varausId;

    public VarattuHuone(int varausId, Huone huone) {
        super(huone.getHuonenumero(), huone.getHuonetyyppi(), huone.getPaivahinta());
        this.varausId = varausId;
    }

    /**
     * @return Palauttaa varauksen pääavaimen, johon huone liittyy
     */
    public int getVarausId() {
        return varausId;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}