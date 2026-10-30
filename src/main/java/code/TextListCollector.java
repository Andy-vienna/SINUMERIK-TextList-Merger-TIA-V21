package code;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/** Erster Durchlauf: sammelt die Einträge aller zusammenzuführenden TextLists. */
final class TextListCollector {

    private static final String ENTRY_START = "<Entry ";

    private final TextListRule rule;
    private final Consumer<String> log;

    private final List<String> ids = new ArrayList<>();
    private final List<String> names = new ArrayList<>();
    private final StringBuilder body = new StringBuilder();
    private final Set<String> indices = new HashSet<>();
    private boolean insideList;

    TextListCollector(TextListRule rule, Consumer<String> log) {
        this.rule = rule;
        this.log = log;
    }

    MergePlan collect(Path input) throws IOException {
        XmlChunks.forEach(input, this::handle);
        return new MergePlan(List.copyOf(ids), List.copyOf(names), body.toString(), indices.size());
    }

    private void handle(String chunk) {
        String tag = XmlChunks.tagOf(chunk);
        if (!insideList) {
            startListIfMatching(tag);
        } else if (rule.isEnd(tag)) {
            insideList = false;
        } else {
            body.append(chunk);
            checkDuplicateIndex(tag);
        }
    }

    private void startListIfMatching(String tag) {
        if (!rule.isMergeStart(tag)) {
            return;
        }
        insideList = true;
        String id = XmlChunks.attribute(tag, "Id").orElse("?");
        String name = XmlChunks.attribute(tag, "Name").orElse("?");
        ids.add(id);
        names.add(name);
        log.accept("Gefunden: TextList Id=" + id + " Name=" + name);
    }

    private void checkDuplicateIndex(String tag) {
        if (!tag.startsWith(ENTRY_START)) {
            return;
        }
        XmlChunks.attribute(tag, "Index")
                .filter(index -> !indices.add(index))
                .ifPresent(index -> log.accept("WARNUNG: Index " + index + " kommt mehrfach vor"));
    }
}
