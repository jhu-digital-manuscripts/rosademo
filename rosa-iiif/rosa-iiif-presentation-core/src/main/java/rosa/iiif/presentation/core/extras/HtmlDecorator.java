package rosa.iiif.presentation.core.extras;

import java.net.URI;

import org.apache.commons.lang3.StringEscapeUtils;

public class HtmlDecorator {
    private static final int MIN_TERM_LENGTH = 3;

    // Term starts with upper case letter
    private int find_term_start(String s, int offset) {
        for (int i = offset; i < s.length(); i++) {
            char c = s.charAt(i);

            if (Character.isUpperCase(c)) {
                return i;
            }
        }

        return -1;
    }

    // Term ends with non-letter character unless there are space characters followed by an upper case letter.
    private int find_term_end(String s, int offset) {
        for (int i = offset; i < s.length(); i++) {
            char c = s.charAt(i);

            if (Character.isSpaceChar(c)) {
                int space_end = i + 1;

                for (; space_end < s.length() && Character.isSpaceChar(s.charAt(space_end)); space_end++)
                    ;

                if (space_end < s.length() && Character.isUpperCase(s.charAt(space_end))) {
                    i = space_end;
                    continue;
                } else {
                    return i;
                }
            } else if (!Character.isLetter(c)) {
                return i;
            }
        }

        return s.length();
    }

    /**
     * @param text
     * @param dbs
     * @return properly escaped HTML text with links for terms.
     */
    // TODO Does not handle or multi-word terms
    // TODO Assume term starts with uppercase letter.
    public String decorate(String text, ExternalResourceDb... dbs) {
        StringBuilder result = new StringBuilder();

        for (int offset = 0;;) {
            int term_start = find_term_start(text, offset);

            if (term_start == -1) {
                result.append(escape_html(text.substring(offset, text.length())));
                break;
            } else {
                result.append(escape_html(text.substring(offset, term_start)));

                int term_end = find_term_end(text, term_start);

                String term = text.substring(term_start, term_end);

                URI uri = null;

                for (ExternalResourceDb db : dbs) {
                    URI test = db.lookup(term);

                    if (test != null) {
                        uri = test;
                        break;
                    }
                }

                if (uri == null || term.length() < MIN_TERM_LENGTH) {
                    result.append(escape_html(term));
                } else {
                    result.append(create_link(term, uri.toString()));
                }

                offset = term_end;
            }
        }

        return result.toString();
    }

    private String escape_html(String text) {
        return StringEscapeUtils.escapeHtml4(text);
    }

    private String create_link(String text, String url) {
        return "<a class='external-link' target='_blank' href='" + url + "'>" + escape_html(text) + "</a>";
    }
}