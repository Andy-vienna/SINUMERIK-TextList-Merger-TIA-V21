package code;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

/**
 * Zweiter Durchlauf: schreibt die Datei 1:1 weiter. An der Stelle der ersten passenden TextList
 * steht die zusammengeführte Liste, die übrigen Listen und alle betroffenen Fingerprints entfallen.
 */
final class TextListMerger {

    private final TextListRule rule;
    private final MergePlan plan;
    private final Set<String> mergedIds;

    private Writer out;
    private boolean insideList;
    private boolean insideFirstList;
    private boolean mergedListWritten;

    TextListMerger(TextListRule rule, MergePlan plan) {
        this.rule = rule;
        this.plan = plan;
        this.mergedIds = plan.idSet();
    }

    void write(Path input, Path output) throws IOException {
        try (Writer writer = Files.newBufferedWriter(output, StandardCharsets.UTF_8)) {
            out = writer;
            XmlChunks.forEach(input, this::handle);
        }
    }

    private void handle(String chunk) throws IOException {
        String tag = XmlChunks.tagOf(chunk);
        if (insideList) {
            handleInsideList(chunk, tag);
        } else if (rule.isMergeStart(tag)) {
            startList(chunk);
        } else if (!rule.isFingerprintOf(tag, mergedIds)) {
            out.write(chunk);
        }
    }

    private void startList(String chunk) throws IOException {
        insideList = true;
        insideFirstList = !mergedListWritten;
        if (insideFirstList) {
            out.write(rule.renamed(chunk, plan.firstName()));
            out.write(plan.mergedBody());
            mergedListWritten = true;
        }
    }

    private void handleInsideList(String chunk, String tag) throws IOException {
        if (!rule.isEnd(tag)) {
            return;
        }
        insideList = false;
        if (insideFirstList) {
            out.write(chunk);
        }
    }
}
