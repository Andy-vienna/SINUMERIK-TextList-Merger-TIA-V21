package code;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Liest eine XML-Datei streamend in Stücken, die jeweils mit "&gt;" enden.
 * Ein Stück besteht aus dem Text davor (z.B. Einrückung) und genau einem Tag.
 * Hängt man alle Stücke aneinander, ergibt sich wieder exakt die Originaldatei.
 */
final class XmlChunks {

    private static final Pattern CHUNK_END = Pattern.compile("(?<=>)");
    private static final Pattern TAG_AT_END = Pattern.compile("<[^<>]*>$");

    @FunctionalInterface
    interface ChunkHandler {
        void handle(String chunk) throws IOException;
    }

    private XmlChunks() {
    }

    static void forEach(Path file, ChunkHandler handler) throws IOException {
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8);
             Scanner scanner = new Scanner(reader).useDelimiter(CHUNK_END)) {
            while (scanner.hasNext()) {
                handler.handle(scanner.next());
            }
            if (scanner.ioException() != null) {
                throw scanner.ioException();
            }
        }
    }

    static String tagOf(String chunk) {
        Matcher matcher = TAG_AT_END.matcher(chunk);
        return matcher.find() ? matcher.group() : "";
    }

    static Optional<String> attribute(String tag, String name) {
        Matcher matcher = Pattern.compile("\\s" + Pattern.quote(name) + "=\"([^\"]*)\"").matcher(tag);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }
}
