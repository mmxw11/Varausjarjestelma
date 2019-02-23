package varausjarjestelma.ui;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

import org.springframework.stereotype.Component;

import varausjarjestelma.database.Tietokantahallinta;
import varausjarjestelma.ui.commands.HaeHuoneitaKomento;
import varausjarjestelma.ui.commands.LisaaHuoneKomento;
import varausjarjestelma.ui.commands.LisaaVarausKomento;
import varausjarjestelma.ui.commands.ListaaHuoneetKomento;
import varausjarjestelma.ui.commands.ListaaVarauksetKomento;
import varausjarjestelma.ui.commands.TilastoKomento;

/**
 * @author Matias
 */
@Component
public class Tekstikayttoliittyma {

    private Tietokantahallinta thallinta;
    private Map<String, AbstractKomento> commands;

    public Tekstikayttoliittyma(Tietokantahallinta thallinta) {
        this.thallinta = thallinta;
        // Jotta saadaan järjestys pysymään käytetään linked data structurea.
        this.commands = new LinkedHashMap<>();
        registerCommands();
    }

    private void registerCommands() {
        commands.put("1", new LisaaHuoneKomento()); // TODO: IMPLEMENT
        commands.put("2", new ListaaHuoneetKomento()); // TODO: IMPLEMENT
        commands.put("3", new HaeHuoneitaKomento()); // TODO: IMPLEMENT
        commands.put("4", new LisaaVarausKomento()); // TODO: IMPLEMENT
        commands.put("5", new ListaaVarauksetKomento()); // TODO: IMPLEMENT
        commands.put("6", new TilastoKomento()); // TODO: IMPLEMENT
    }

    /**
     * Käynnistää tekstikäyttöliittymän.
     * @param scanner
     */
    public void start(Scanner scanner) {
        while (true) {
            System.out.println("Komennot:");
            System.out.println(" x - lopeta"); // lopeta spesiaali tapaus.
            commands.forEach((k, v) -> System.out.println(" " + k + " - " + v.getKuvaus()));
            System.out.println("");
            String komentoSyote = scanner.nextLine();
            if (komentoSyote.equals("x")) { // lopeta
                break;
            }
            // Komentojen nimet kirjoitetaan aina pienellä.
            AbstractKomento komento = commands.get(komentoSyote.toLowerCase());
            if (komento == null) {
                System.out.println("Komentoa \"" + komentoSyote + "\" ei löytynyt!\n");
                continue;
            }
            komento.execute(scanner, thallinta);
            System.out.println();
        }
    }
}