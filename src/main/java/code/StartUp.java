package code;

import java.io.Console;
import java.io.InputStream;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.jar.Manifest;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.formdev.flatlaf.FlatLightLaf;

import gui.MainWindow;

/**
 * Ohne Argumente startet die GUI.
 * Kommandozeile: java -jar textlist-merger.jar &lt;eingabe.xml&gt; &lt;zielordner&gt; [baseName]
 */
public final class StartUp {
	
	private final static String appName = "TIA V21 Textlist-Merger for SINUMERIK One";
	private final static String infoName = "/help/info.html";
	private final static String gplName = "/help/gpl-3.0-standalone.html";

	private static final String DISCLAIMER_TEXT = "Dieses Tool ist experimentell, Haftungen für evtl. Fehler werden nicht übernommen. "
			+ "Die Nutzung erfolgt auf eigenes Risiko.";
	private static final String[] DISCLAIMER_OPTIONS = {"OK", "Nein, Programm beenden"};
	private static final int OPTION_OK = 0;
	private static final String ANSI_YELLOW_BOLD = "\u001B[1;33m";
	private static final String ANSI_RESET = "\u001B[0m";
	
	private static volatile String[] manifest = new String[2];

	private StartUp() {
    }

    public static void main(String[] args) throws Exception {
    	manifest = readManifest(); // Informationen aus dem MANIFEST.MF laden
        if (args.length == 0) {
            startGui();
        } else if (args.length >= 2) {
        	System.out.println(highlight(DISCLAIMER_TEXT));
            String baseName = args.length > 2 ? args[2] : TextListMergeService.DEFAULT_BASE_NAME;
            new TextListMergeService(baseName, System.out::println).run(toPath(args[0]), toPath(args[1]));
        } else {
            System.out.println("Aufruf: java -jar textlist-merger.jar [<eingabe.xml> <zielordner> [baseName]]");
            System.exit(1);
        }
    }
    
	// ###################################################################################################################################################

    /**
     * Wandelt ein Kommandozeilen-Argument in einen Pfad.
     * Unter Windows wird aus "X:\" das Argument X:" (das \" gilt als maskiertes Anführungszeichen).
     * Das abschließende " wird deshalb wieder zum gemeinten \.
     */
    private static Path toPath(String arg) {
        String cleaned = arg.strip();
        if (cleaned.endsWith("\"")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1) + "\\";
        }
        try {
            return Path.of(cleaned);
        } catch (InvalidPathException e) {
            System.err.println("Ungültiger Pfad: " + arg);
            System.exit(1);
            return null; // nicht erreichbar
        }
    }

    /** Hebt den Text gelb/fett hervor, aber nur in einem echten Terminal (nicht bei Umleitung in eine Datei). */
    private static String highlight(String text) {
        Console console = System.console();
        boolean isTerminal = console != null && console.isTerminal();
        return isTerminal ? ANSI_YELLOW_BOLD + text + ANSI_RESET : text;
    }

    private static void startGui() {
        FlatLightLaf.setup();
        SwingUtilities.invokeLater(() -> {
            if (!confirmDisclaimer()) {
                System.exit(0);
            }
            new MainWindow().setVisible(true);
        });
    }

    /** Zeigt den Haftungshinweis; true, wenn der Benutzer mit "OK" bestätigt. */
    private static boolean confirmDisclaimer() {
        int choice = JOptionPane.showOptionDialog(null, DISCLAIMER_TEXT, "Information",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null,
                DISCLAIMER_OPTIONS, DISCLAIMER_OPTIONS[OPTION_OK]);
        return choice == OPTION_OK;
    }
    
	// ###################################################################################################################################################
    
    /** Liest Titel und Version aus dem MANIFEST.MF. */
    private static String[] readManifest() {
    	String[] info = new String[2];
		try (InputStream manifest = StartUp.class.getResourceAsStream("/META-INF/MANIFEST.MF")) {
			Manifest mf = new Manifest(manifest);
			String build = mf.getMainAttributes().getValue("Built-Date");
			Instant instant = Instant.parse(build); // formatter for date-and-time
			ZonedDateTime local = instant.atZone(ZoneId.systemDefault());
			info[0] = local.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
			info[1] = mf.getMainAttributes().getValue("Version");
		} catch (Exception e) {
			info[0] = "no date";
			info[1] = "no version";
		}
		return info;
	}
	
	// ###################################################################################################################################################
	// Getter und Setter
	// ###################################################################################################################################################
    
    public static String getAppname() {
		return appName;
	}

	public static String getInfoname() {
		return infoName;
	}

	public static String getGplname() {
		return gplName;
	}
	
	public static String[] getManifest() {
		return manifest;
	}
}
