package varausjarjestelma.ui;

/**
 * Sisältää apumetodeja syötteen lukemiseen.
 * 
 * @author Matias
 */
public class SyoteUtil {

    /**
     * Lukee kokonaisluvun.
     * @param input
     * @param failNumber Määrää mikä numero palautetaan, jos lukeminen ei onnistu
     * @return int
     */
    public static int readInteger(String input, int failNumber) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return failNumber;
        }
    }
}