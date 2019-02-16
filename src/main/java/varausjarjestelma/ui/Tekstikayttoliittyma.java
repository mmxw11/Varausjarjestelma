package varausjarjestelma.ui;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

import org.springframework.stereotype.Component;

import varausjarjestelma.Varaushallinta;
import varausjarjestelma.ui.commands.HaeHuoneitaKomento;
import varausjarjestelma.ui.commands.LisaaHuoneKomento;
import varausjarjestelma.ui.commands.LisaaVarausKomento;
import varausjarjestelma.ui.commands.ListaaHuoneetKomento;
import varausjarjestelma.ui.commands.ListaaVarauksetKomento;
import varausjarjestelma.ui.commands.TilastoKomento;

@Component
public class Tekstikayttoliittyma {

    private Varaushallinta vhallinta;
    private Map<String, AbstractKomento> commands;

    public Tekstikayttoliittyma(Varaushallinta vhallinta) {
        this.vhallinta = vhallinta;
        // Jotta saadaan järjestys pysymään käytetään linked data structurea.
        this.commands = new LinkedHashMap<>();
        rekisteroiKomennot();
    }

    private void rekisteroiKomennot() {
        commands.put("1", new LisaaHuoneKomento()); // TODO: IMPLEMENT
        commands.put("2", new ListaaHuoneetKomento()); // TODO: IMPLEMENT
        commands.put("3", new HaeHuoneitaKomento()); // TODO: IMPLEMENT
        commands.put("4", new LisaaVarausKomento()); // TODO: IMPLEMENT
        commands.put("5", new ListaaVarauksetKomento()); // TODO: IMPLEMENT
        commands.put("6", new TilastoKomento()); // TODO: IMPLEMENT
    }

    public void kaynnista(Scanner scanner) {
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
            komento.suorita(scanner, vhallinta);
            System.out.println();
        }
    }
}