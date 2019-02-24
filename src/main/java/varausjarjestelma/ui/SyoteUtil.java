package varausjarjestelma.ui;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Sisältää apumetodeja syötteen lukemiseen.
 * 
 * @author Matias
 */
public class SyoteUtil {

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

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

    /**
     * Lukee päivämäärän muodossa "yyyy-MM-dd HH:mm".
     * @param input
     * @return Palauttaa päivämäärän tai null, mikäli syötettä ei pystynyt lukemaan
     */
    public static LocalDateTime parseDateTime(String input) {
        try {
            return LocalDateTime.parse(input, dateFormatter);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}