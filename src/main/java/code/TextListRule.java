package code;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Erkennt die TextLists "&lt;baseName&gt;_&lt;beliebiger Text&gt;", die zu einer TextList "&lt;baseName&gt;"
 * zusammengeführt werden, sowie deren Fingerprints.
 */
final class TextListRule {

    private static final String TEXT_LIST_START = "<TextList ";
    private static final String TEXT_LIST_END = "</TextList>";
    private static final String FINGERPRINT_START = "<Fingerprint ";

    private final String baseName;
    private final Pattern namePattern;

    TextListRule(String baseName) {
        this.baseName = baseName;
        this.namePattern = Pattern.compile(Pattern.quote(baseName) + "_.+");
    }

    String baseName() {
        return baseName;
    }

    boolean isMergeStart(String tag) {
        return tag.startsWith(TEXT_LIST_START)
                && XmlChunks.attribute(tag, "Name").filter(name -> namePattern.matcher(name).matches()).isPresent();
    }

    boolean isEnd(String tag) {
        return TEXT_LIST_END.equals(tag);
    }

    boolean isFingerprintOf(String tag, Set<String> ids) {
        return tag.startsWith(FINGERPRINT_START)
                && XmlChunks.attribute(tag, "Id").filter(ids::contains).isPresent();
    }

    String renamed(String chunk, String oldName) {
        return chunk.replace("Name=\"" + oldName + "\"", "Name=\"" + baseName + "\"");
    }
}
