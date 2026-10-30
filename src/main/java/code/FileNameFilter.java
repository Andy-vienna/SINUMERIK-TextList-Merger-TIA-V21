package code;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/** Zeigt im Dateidialog nur Ordner und Dateien mit genau diesem Namen (ohne Groß-/Kleinschreibung). */
public final class FileNameFilter extends FileFilter {

    private final String fileName;

    public FileNameFilter(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public boolean accept(File file) {
        return file.isDirectory() || file.getName().equalsIgnoreCase(fileName);
    }

    @Override
    public String getDescription() {
        return fileName;
    }
}
