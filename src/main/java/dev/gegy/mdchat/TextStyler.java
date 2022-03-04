package dev.gegy.mdchat;

import com.google.common.collect.Lists;
import dev.gegy.mdchat.parser.ColoredChatExtension;
import dev.gegy.mdchat.parser.FormattedNode;
import dev.gegy.mdchat.parser.SpoilerExtension;
import dev.gegy.mdchat.parser.SpoilerNode;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.ext.gfm.strikethrough.Strikethrough;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.node.Text;
import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

public final class TextStyler {
    public static final TextStyler INSTANCE = new TextStyler();

    private static final Style SPOILER = Style.EMPTY.withFormatting(Formatting.DARK_GRAY, Formatting.OBFUSCATED);

    private static final Parser PARSER = Parser.builder()
            .enabledBlockTypes(Collections.emptySet())
            .extensions(Lists.newArrayList(
                    ColoredChatExtension.INSTANCE,
                    SpoilerExtension.INSTANCE,
                    AutolinkExtension.create(),
                    StrikethroughExtension.create()
            ))
            .build();

    private TextStyler() {
    }

    @Nullable
    public net.minecraft.text.Text apply(String string) {
        Node node = PARSER.parse(string);
        return this.renderAsText(node);
    }

    @Nullable
    private MutableText renderAsText(Node node) {
        if (node instanceof Text) {
            return this.renderLiteral((Text) node);
        } else if (node instanceof Code) {
            return this.renderCode((Code) node);
        } else if (node instanceof StrongEmphasis) {
            return this.renderStrongEmphasis((StrongEmphasis) node);
        } else if (node instanceof Emphasis) {
            return this.renderEmphasis(node, Formatting.ITALIC);
        } else if (node instanceof Strikethrough) {
            return this.renderEmphasis(node, Formatting.STRIKETHROUGH);
        } else if (node instanceof Link) {
            return this.renderLink((Link) node);
        } else if (node instanceof FormattedNode) {
            return this.renderFormattedText((FormattedNode) node);
        } else if (node instanceof SpoilerNode) {
            return this.renderSpoiler((SpoilerNode) node);
        }

        return this.renderChildren(node);
    }

    private MutableText renderLiteral(Text text) {
        return new LiteralText(text.getLiteral());
    }

    private MutableText renderCode(Code code) {
        return new LiteralText(code.getLiteral()).formatted(Formatting.GRAY);
    }

    private MutableText renderStrongEmphasis(StrongEmphasis emphasis) {
        String delimiter = emphasis.getOpeningDelimiter();
        if (delimiter.equals("__")) {
            return this.renderEmphasis(emphasis, Formatting.UNDERLINE);
        } else {
            return this.renderEmphasis(emphasis, Formatting.BOLD);
        }
    }

    @Nullable
    private MutableText renderFormattedText(FormattedNode formatted) {
        MutableText text = this.renderChildren(formatted);
        if (text != null) {
            return text.formatted(formatted.getFormatting());
        }
        return null;
    }

    @Nullable
    private MutableText renderSpoiler(SpoilerNode spoiler) {
        MutableText text = this.renderChildren(spoiler);
        if (text != null) {
            return text.shallowCopy()
                    .setStyle(SPOILER.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, text)));
        }
        return null;
    }

    @Nullable
    private MutableText renderEmphasis(Node node, Formatting formatting) {
        MutableText text = this.renderChildren(node);
        if (text != null) {
            return text.formatted(formatting);
        }
        return null;
    }

    @Nullable
    private MutableText renderLink(Link link) {
        MutableText title;
        if (link.getTitle() != null) {
            title = new LiteralText(link.getTitle());
        } else {
            title = this.renderChildren(link);
        }

        if (title == null) {
            title = new LiteralText(link.getDestination());
        }

        return title.setStyle(this.buildLinkStyle(link.getDestination()));
    }

    private Style buildLinkStyle(String url) {
        return Style.EMPTY
                .withFormatting(Formatting.AQUA, Formatting.UNDERLINE)
                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText(url)));
    }

    @Nullable
    private MutableText renderChildren(Node parent) {
        MutableText result = null;

        Node child = parent.getFirstChild();
        while (child != null) {
            Node next = child.getNext();

            MutableText text = this.renderAsText(child);
            if (text != null) {
                if (result == null) {
                    result = new LiteralText("");
                }
                result = result.append(text);
            }

            child = next;
        }

        return result;
    }
}
