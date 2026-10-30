package gui.infoDisplays;

import java.awt.*;
import java.io.InputStream;
import java.net.URL;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.html.HTMLEditorKit;

import code.StartUp;

/** Zeigt die Programminfo (help/info.html) im Browser an. */
public class InfoDisplay {
	
	// ###################################################################################################################################################
	// Konstruktor
	// ###################################################################################################################################################
    
	public InfoDisplay() {}
    
	// ###################################################################################################################################################
	// public part
	// ###################################################################################################################################################

    /** Öffnet die Infoseite, optional an einem Anker. */
    public void openGPL(Window parent, String anchor) {
        try {
            // get the base url from resources
            URL helpURL = InfoDisplay.class.getResource(StartUp.getInfoname());
            if (helpURL == null) {
            	System.out.println("[ERROR] Info-Screen nicht gefunden: " + StartUp.getInfoname());
                return;
            }

            URL finalURL;
            if (anchor != null && !anchor.isEmpty()) {
                // clean the anchor if there is an #
                String cleanAnchor = anchor.startsWith("#") ? anchor.substring(1) : anchor;
                
                // string based mounting to avoid deprecated methods
                String urlWithAnchor = helpURL.toString() + "#" + cleanAnchor;
                finalURL = java.net.URI.create(urlWithAnchor).toURL();
            } else {
                finalURL = helpURL;
            }

            showInfoDialog(parent, finalURL);

        } catch (Exception e) {
        	System.out.println("[ERROR] Fehler beim Laden des Info-Screens: " + e.getMessage());
        }
    }
    
	// ###################################################################################################################################################
	// private part
	// ###################################################################################################################################################

    private void showInfoDialog(Window parent, URL url) {
        JDialog dialog = new JDialog(parent, "Info Display", Dialog.ModalityType.MODELESS);
        dialog.setSize(400, 285);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(parent);

        try {
            // 1. HTML-Datei aus der URL als String einlesen
            String rawHtml;
            try (InputStream is = url.openStream()) {
                rawHtml = new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            }

            // 2. SWING-FIXES:
            // - DOCTYPE & XML-Header entfernen
            // - Meta-Charset entfernen (behebt die ChangedCharSetException!)
            rawHtml = rawHtml.replaceAll("(?s)<!DOCTYPE.*?>", "")
                             .replaceAll("(?s)<html.*?>", "<html>")
                             .replaceAll("(?i)<meta[^>]*charset[^>]*>", "");

            // 3. Daten für Platzhalter ermitteln
            String name = StartUp.getAppname();
            String javaVersion = System.getProperty("java.version");
            String[] info = StartUp.getManifest();

            String formattedHtml = rawHtml
                    .replaceFirst("%s", name != null ? name : "Unbekannt")
                    .replaceFirst("%s", info[1] != null ? info[1] : "-")
                    .replaceFirst("%s", info[0] != null ? info[0] : "-")
                    .replaceFirst("%s", javaVersion != null ? javaVersion : "-");

            // 4. HTMLEditorKit und HTMLDocument manuell aufsetzen
            HTMLEditorKit kit = new HTMLEditorKit();
            javax.swing.text.html.HTMLDocument doc = (javax.swing.text.html.HTMLDocument) kit.createDefaultDocument();

            JEditorPane helpPane = new JEditorPane();
            helpPane.setEditorKit(kit);
            helpPane.setDocument(doc);
            helpPane.setEditable(false);

            // HTML sauber in das Document parsen
            try (java.io.StringReader reader = new java.io.StringReader(formattedHtml)) {
                kit.read(reader, doc, 0);
            }

            helpPane.setCaretPosition(0);

            // Hyperlink-Listener für externe Links
            helpPane.addHyperlinkListener(e -> {
                if (HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType())) {
                    String description = e.getDescription();
                    URL targetUrl = e.getURL();

                    try {
                        if (description != null && description.startsWith("#")) {
                            helpPane.scrollToReference(description.substring(1));
                        } else if (targetUrl != null && (targetUrl.getProtocol().equals("http") || targetUrl.getProtocol().equals("https"))) {
                            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                                Desktop.getDesktop().browse(targetUrl.toURI());
                            }
                        }
                    } catch (Exception ex) {
                        System.out.println("[ERROR] Link konnte nicht gefolgt werden: " + ex.getMessage());
                    }
                }
            });

            // 5. In Dialog einbetten
            JScrollPane scrollPane = new JScrollPane(helpPane);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            dialog.add(scrollPane);

        } catch (Exception e) {
            e.printStackTrace();
            dialog.add(new JLabel("Fehler beim Laden: " + e.getMessage()));
        }

        dialog.setVisible(true);
    }
}