package dev.gegy.mdchat.parser;

import net.minecraft.util.Formatting;
import org.commonmark.Extension;
import org.commonmark.node.Node;
import org.commonmark.node.Text;
import org.commonmark.parser.Parser;
import org.commonmark.parser.delimiter.DelimiterProcessor;
import org.commonmark.parser.delimiter.DelimiterRun;

public final class ColoredChatExtension implements Parser.ParserExtension {
    public static final Extension INSTANCE = new ColoredChatExtension();

    private ColoredChatExtension() {
    }

    @Override
    public void extend(Parser.Builder builder) {
        builder.customDelimiterProcessor(new FormatDelimiterProcessor());
    }

    private static class FormatDelimiterProcessor implements DelimiterProcessor {
        private static final char DELIMITER = '^';

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
            return 1;
        }

        @Override
        public int process(DelimiterRun openerRun, DelimiterRun closerRun) {
            if (openerRun.length() >= 1 && closerRun.length() >= 1) {
                process(openerRun.getOpener(), openerRun.getCloser());
                return 1;
            } else {
                return 0;
            }
        }

        private void process(Text opener, Text closer) {
            Formatting targetFormat = null;

            Node first = opener.getNext();
            if (first instanceof Text firstText) {
                String literal = firstText.getLiteral();
                firstText.setLiteral(literal.substring(1));

                char code = Character.toLowerCase(literal.charAt(0));
                for (Formatting format : Formatting.values()) {
                    if (format.code == code) {
                        targetFormat = format;
                        break;
                    }
                }
            }

            if (targetFormat != null) {
                Node formatted = new FormattedNode(targetFormat);
                this.insertBetween(opener, closer, formatted);
            }
        }

        private void insertBetween(Text opener, Text closer, Node node) {
            Node sibling = opener.getNext();
            while (sibling != null && sibling != closer) {
                Node next = sibling.getNext();
                node.appendChild(sibling);
                sibling = next;
            }

            opener.insertAfter(node);
        }
    }
}
