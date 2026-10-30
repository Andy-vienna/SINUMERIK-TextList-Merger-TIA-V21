package code;

import java.io.InputStream;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.jar.Manifest;

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
	
	private static volatile String[] manifest = new String[2];

	private StartUp() {
    }

    public static void main(String[] args) throws Exception {
    	manifest = readManifest(); // Informationen aus dem MANIFEST.MF laden
        if (args.length == 0) {
            startGui();
        } else if (args.length >= 2) {
            String baseName = args.length > 2 ? args[2] : TextListMergeService.DEFAULT_BASE_NAME;
            new TextListMergeService(baseName, System.out::println).run(Path.of(args[0]), Path.of(args[1]));
        } else {
            System.out.println("Aufruf: java -jar textlist-merger.jar [<eingabe.xml> <zielordner> [baseName]]");
            System.exit(1);
        }
    }
    
	// ###################################################################################################################################################

    private static void startGui() {
        FlatLightLaf.setup();
        SwingUtilities.invokeLater(() -> new MainWindow().setVisible(true));
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
