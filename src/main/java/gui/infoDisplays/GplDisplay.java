package gui.infoDisplays;

import java.awt.*;
import java.net.URL;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.html.HTMLEditorKit;

import code.StartUp;

/** Zeigt die GPL-Lizenz (help/gpl-3.0-standalone.html) im Browser an. */
public class GplDisplay {
	
	// ###################################################################################################################################################
	// Konstruktor
	// ###################################################################################################################################################
    
	public GplDisplay() {}
    
	// ###################################################################################################################################################
	// public part
	// ###################################################################################################################################################

    /** Öffnet die Lizenzseite, optional an einem Anker. */
    public void openGPL(Window parent, String anchor) {
        try {
            // get the base url from resources
            URL helpURL = GplDisplay.class.getResource(StartUp.getGplname());
            if (helpURL == null) {
            	System.out.println("[ERROR] GPL Terms and Conditions nicht gefunden: " + StartUp.getGplname());
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

            showGplDialog(parent, finalURL);

        } catch (Exception e) {
        	System.out.println("[ERROR] Fehler beim Laden der GPL Terms and Conditions: " + e.getMessage());
        }
    }
    
	// ###################################################################################################################################################
	// private part
	// ###################################################################################################################################################

    private void showGplDialog(Window parent, URL url) {
        JDialog dialog = new JDialog(parent, "GPL 3.0 Terms and Conditions", Dialog.ModalityType.MODELESS);
        dialog.setSize(1024, 640);
        dialog.setMinimumSize(new Dimension(500, 400));
        dialog.setLocationRelativeTo(parent);

        try {
            JEditorPane helpPane = new JEditorPane();
            helpPane.setEditable(false);
            
            HTMLEditorKit kit = new HTMLEditorKit();
            helpPane.setEditorKit(kit);
            helpPane.setContentType("text/html");

            helpPane.addHyperlinkListener(e -> {
                if (HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType())) {
                    String description = e.getDescription();
                    // rename 'url' to 'targetUrl'
                    URL targetUrl = e.getURL(); 

                    try {
                        if (description != null && description.startsWith("#")) {
                            helpPane.scrollToReference(description.substring(1));
                        } 
                        else if (targetUrl != null && (targetUrl.getProtocol().equals("http") || targetUrl.getProtocol().equals("https"))) {
                            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                                Desktop.getDesktop().browse(targetUrl.toURI());
                                System.out.println("[INFO] Externer Browser geöffnet.");
                            }
                        } 
                        else {
                            helpPane.setPage(targetUrl);
                        }
                    } catch (Exception ex) {
                        System.out.println("[ERROR] Link konnte nicht gefolgt werden: " + ex.getMessage());
                    }
                }
            });

            // load the help page
            helpPane.setPage(url);

            // jump to anchor after start
            String anchorRef = url.getRef();
            if (anchorRef != null) {
                // do a short delay to render html in swing
                SwingUtilities.invokeLater(() -> {
                    Timer timer = new Timer(300, _ -> {
                        helpPane.scrollToReference(anchorRef);
                    });
                    timer.setRepeats(false);
                    timer.start();
                });
            }

            dialog.add(new JScrollPane(helpPane));
        } catch (Exception e) {
            dialog.add(new JLabel("Inhalt fehlerhaft: " + e.getMessage()));
        }

        dialog.setVisible(true);
    }
}