package code;

import java.util.List;
import java.util.Set;

/** Ergebnis des ersten Durchlaufs: welche TextLists gefunden wurden und ihr gemeinsamer Inhalt. */
record MergePlan(List<String> ids, List<String> names, String mergedBody, int entryCount) {

    boolean isEmpty() {
        return ids.isEmpty();
    }

    Set<String> idSet() {
        return Set.copyOf(ids);
    }

    String firstId() {
        return ids.get(0);
    }

    String firstName() {
        return names.get(0);
    }
}
