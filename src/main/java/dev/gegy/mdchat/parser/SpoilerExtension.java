package dev.gegy.mdchat.parser;

import org.commonmark.Extension;
import org.commonmark.node.Node;
import org.commonmark.node.Text;
import org.commonmark.parser.Parser;
import org.commonmark.parser.delimiter.DelimiterProcessor;
import org.commonmark.parser.delimiter.DelimiterRun;

public final class SpoilerExtension implements Parser.ParserExtension {
    public static final Extension INSTANCE = new SpoilerExtension();

    private SpoilerExtension() {
    }

    @Override
    public void extend(Parser.Builder builder) {
        builder.customDelimiterProcessor(new SpoilerDelimiterProcessor());
    }

    private static class SpoilerDelimiterProcessor implements DelimiterProcessor {
        private static final char DELIMITER = '|';

        @Override
        public char getOpeningCharacter() {
            return DELIMITER;
        }

        @Override
        public char getClosingCharacter() {
            return DELIMITER;
        }

        @Override
        public int getMinLength() {
            return 2;
        }

        @Override
        public int process(DelimiterRun openerRun, DelimiterRun closerRun) {
            if (openerRun.length() >= 2 && closerRun.length() >= 2) {
                process(openerRun.getOpener(), openerRun.getCloser());
                return 2;
            } else {
                return 0;
            }
        }

        private void process(Text opener, Text closer) {
            Node spoiler = new SpoilerNode();

            Node node = opener.getNext();
            while (node != null && node != closer) {
                Node next = node.getNext();
                spoiler.appendChild(node);
                node = next;
            }

            opener.insertAfter(spoiler);
        }
    }
}
