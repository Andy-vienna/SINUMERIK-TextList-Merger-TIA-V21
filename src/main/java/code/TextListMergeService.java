package code;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;

/** Führt alle TextLists "&lt;baseName&gt;_&lt;beliebiger Text&gt;" einer Datei zu einer TextList "&lt;baseName&gt;" zusammen. */
public final class TextListMergeService {

    public static final String DEFAULT_BASE_NAME = "SINUMERIK_UserAlarmsTextLib";
    private static final String SAME_DIR_SUBFOLDER = "merged";

    private final TextListRule rule;
    private final Consumer<String> log;

    public TextListMergeService(String baseName, Consumer<String> log) {
        this.rule = new TextListRule(baseName);
        this.log = log;
    }

    /** Liefert die geschriebene Ausgabedatei oder leer, wenn nichts zusammenzuführen war. */
    public Optional<Path> run(Path input, Path targetDir) throws IOException {
        log.accept("Lese " + input);
        MergePlan plan = new TextListCollector(rule, log).collect(input);
        if (plan.isEmpty()) {
            log.accept("Keine TextList " + rule.baseName() + "_<Suffix> gefunden. Nichts geschrieben.");
            return Optional.empty();
        }
        Path output = outputDir(input, targetDir).resolve(input.getFileName());
        new TextListMerger(rule, plan).write(input, output);
        log.accept(plan.ids().size() + " TextList(s) mit " + plan.entryCount() + " Einträgen zu '"
                + rule.baseName() + "' (Id " + plan.firstId() + ") zusammengeführt.");
        log.accept("Geschrieben: " + output);
        return Optional.of(output);
    }

    /** Liegt das Ziel im Quellordner, wird in den Unterordner "merged" geschrieben, damit die Quelle erhalten bleibt. */
    private Path outputDir(Path input, Path targetDir) throws IOException {
        Files.createDirectories(targetDir);
        Path sourceDir = input.toAbsolutePath().getParent();
        if (!Files.isSameFile(sourceDir, targetDir)) {
            return targetDir;
        }
        Path subDir = targetDir.resolve(SAME_DIR_SUBFOLDER);
        if (Files.notExists(subDir)) {
            Files.createDirectory(subDir);
            log.accept("Unterordner angelegt: " + subDir);
        }
        return subDir;
    }
}
