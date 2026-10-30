package gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import code.TextListMergeService;
import gui.iconHandler.MenuIcon;
import gui.infoDisplays.GplDisplay;
import gui.infoDisplays.InfoDisplay;
import code.FileNameFilter;

/** Fenster mit Auswahl von XML-Datei und Zielordner sowie Log-Ausgabe. */
public final class MainWindow extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final String INPUT_FILE_NAME = "PLCTextLists.xml";
    private static final int[] ICON_SIZES = {16, 32, 48, 64, 128, 256};

    private final JTextField inputField = new JTextField(40);
    private final JTextField targetField = new JTextField(40);
    private final JTextField baseNameField = new JTextField(TextListMergeService.DEFAULT_BASE_NAME, 40);
    private final JTextArea logArea = new JTextArea(15, 70);
    private final JButton startButton = new JButton("Zusammenführen");

    public MainWindow() {
        super("TextList-Merger");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setJMenuBar(setupMenu());
        add(createFormPanel(), BorderLayout.NORTH);
        add(createLogPanel(), BorderLayout.CENTER);
        startButton.addActionListener(_ -> startMerge());
        setIconImages(loadAppIcons());
        pack();
        setLocationRelativeTo(null);
    }
    
	// ###################################################################################################################################################

    private JPanel createFormPanel() {
        JButton inputButton = new JButton("Datei…");
        JButton targetButton = new JButton("Ordner…");
        inputButton.addActionListener(_ -> chooseInputFile());
        targetButton.addActionListener(_ -> chooseTargetDir());

        JPanel panel = new JPanel(new GridBagLayout());
        addRow(panel, 0, "XML-Datei:", inputField, inputButton);
        addRow(panel, 1, "Zielordner:", targetField, targetButton);
        addRow(panel, 2, "Basisname:", baseNameField, startButton);
        return panel;
    }
    
	// ###################################################################################################################################################
    
    private JMenuBar setupMenu() {
    	
    	JMenuBar menuBar = new JMenuBar();
        JMenu menuFile = new JMenu("Datei");
        JMenu menuHelp = new JMenu("Hilfe");
        
        JMenuItem itemExit = new JMenuItem("Server beenden", MenuIcon.EXIT.icon());
        JMenuItem itemInfo = new JMenuItem("Info", MenuIcon.INFO.icon());
        JMenuItem itemGpl = new JMenuItem("OpenSource Lizenz", MenuIcon.GPL.icon());
    	
    	itemExit.addActionListener(_ -> System.exit(0));
    	
    	itemInfo.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
				Component c = (Component) e.getSource();
				Window owner = SwingUtilities.getWindowAncestor(c);
				InfoDisplay infoDisplay = new InfoDisplay();
				infoDisplay.openGPL(owner, "");
			}
		});
    	itemGpl.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Component c = (Component) e.getSource();
				Window owner = SwingUtilities.getWindowAncestor(c);
				GplDisplay gplDisplay = new GplDisplay();
				gplDisplay.openGPL(owner, "");
			}
		});
        
        menuFile.add(itemExit);
        menuHelp.add(itemInfo);
        menuHelp.addSeparator();
        menuHelp.add(itemGpl);
        
        menuBar.add(menuFile);
		menuBar.add(menuHelp);
		
		return menuBar;
    }
    
	// ###################################################################################################################################################

    private static void addRow(JPanel panel, int row, String label, JTextField field, JButton button) {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.gridy = row;
        c.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel(label), c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        panel.add(field, c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0;
        panel.add(button, c);
    }
    
	// ###################################################################################################################################################

    private static List<Image> loadAppIcons() {
        return Arrays.stream(ICON_SIZES)
                .mapToObj(size -> MainWindow.class.getResource("/icon/app-" + size + ".png"))
                .filter(Objects::nonNull)
                .map(url -> new ImageIcon(url).getImage())
                .toList();
    }
    
	// ###################################################################################################################################################

    private JScrollPane createLogPanel() {
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        return new JScrollPane(logArea);
    }
    
	// ###################################################################################################################################################

    private void chooseInputFile() {
        JFileChooser chooser = new JFileChooser(currentDir(inputField));
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new FileNameFilter(INPUT_FILE_NAME));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            inputField.setText(file.getPath());
            if (targetField.getText().isBlank()) {
                targetField.setText(file.getParent());
            }
        }
    }
    
	// ###################################################################################################################################################

    private void chooseTargetDir() {
        JFileChooser chooser = new JFileChooser(currentDir(targetField));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            targetField.setText(chooser.getSelectedFile().getPath());
        }
    }
    
	// ###################################################################################################################################################

    private static File currentDir(JTextField field) {
        return field.getText().isBlank() ? null : new File(field.getText());
    }
    
	// ###################################################################################################################################################

    private void startMerge() {
        if (inputField.getText().isBlank() || targetField.getText().isBlank()) {
            log("FEHLER: Bitte XML-Datei und Zielordner wählen.");
            return;
        }
        startButton.setEnabled(false);
        new MergeWorker(Path.of(inputField.getText()), Path.of(targetField.getText()),
                baseNameField.getText().trim()).execute();
    }
    
	// ###################################################################################################################################################

    private void log(String message) {
        logArea.append(LocalTime.now().format(TIME) + "  " + message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

	// ###################################################################################################################################################
    
    /** Führt das Zusammenführen im Hintergrund aus und gibt die Log-Meldungen ans Fenster weiter. */
    private final class MergeWorker extends SwingWorker<Void, String> {

        private final Path input;
        private final Path targetDir;
        private final String baseName;

        MergeWorker(Path input, Path targetDir, String baseName) {
            this.input = input;
            this.targetDir = targetDir;
            this.baseName = baseName;
        }

        @Override
        protected Void doInBackground() {
            try {
                boolean written = new TextListMergeService(baseName, this::publish).run(input, targetDir).isPresent();
                publish(written ? "ERFOLGREICH abgeschlossen." : "Beendet ohne Änderungen.");
            } catch (Exception e) {
                publish("FEHLER: " + e);
            }
            return null;
        }

        @Override
        protected void process(List<String> messages) {
            messages.forEach(MainWindow.this::log);
        }

        @Override
        protected void done() {
            startButton.setEnabled(true);
        }
    }
}
