package dev.gegy.mdchat;

import dev.gegy.mdchat.parser.ColoredChatExtension;
import dev.gegy.mdchat.parser.FormattedNode;
import dev.gegy.mdchat.parser.SpoilerExtension;
import dev.gegy.mdchat.parser.SpoilerNode;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.ext.gfm.strikethrough.Strikethrough;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.node.*;

import java.util.List;
import java.util.Objects;

import static net.minecraft.text.Text.literal;
import static net.minecraft.text.Text.translatable;

public final class BuiltinStylers {
	public static final NodeStyler LITERAL = (node, innerContent) -> {
		if (node instanceof Text text) {
			return literal(text.getLiteral());
		}
		return null;
	};

	public static final NodeStyler CODE = (node, innerContent) -> {
		if (node instanceof Code code) {
			String literal = code.getLiteral();
			MutableText text = literal(literal).formatted(Formatting.GRAY);
			if (literal.startsWith("/")) {
				return text.styled(style -> style
						.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, literal("Click to Copy to Console")))
						.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, literal))
				);
			} else {
				return text.styled(style -> style
						.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, translatable("chat.copy.click")))
						.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, literal))
				);
			}
		}
		return null;
	};

	public static final NodeStyler BOLD = (node, innerContent) -> {
		if (node instanceof StrongEmphasis emphasis && innerContent != null) {
			String delimiter = emphasis.getOpeningDelimiter();
			if (delimiter.equals("**")) {
				return innerContent.formatted(Formatting.BOLD);
			}
		}
		return null;
	};

	public static final NodeStyler UNDERLINE = (node, innerContent) -> {
		if (node instanceof StrongEmphasis emphasis && innerContent != null) {
			String delimiter = emphasis.getOpeningDelimiter();
			if (delimiter.equals("__")) {
				return innerContent.formatted(Formatting.UNDERLINE);
			}
		}
		return null;
	};

	public static final NodeStyler ITALIC = (node, innerContent) -> {
		if (node instanceof Emphasis && innerContent != null) {
			return innerContent.formatted(Formatting.ITALIC);
		}
		return null;
	};

	public static final NodeStyler STRIKETHROUGH = (node, innerContent) -> {
		if (node instanceof Strikethrough && innerContent != null) {
			return innerContent.formatted(Formatting.STRIKETHROUGH);
		}
		return null;
	};

	public static final NodeStyler LINK = (node, innerContent) -> {
		if (node instanceof Link link) {
			MutableText title = Objects.requireNonNullElseGet(innerContent, () -> literal(link.getDestination()));

			MutableText redirectsTo = literal("Goes to ")
					.append(literal(link.getDestination()).formatted(Formatting.AQUA, Formatting.UNDERLINE))
					.formatted(Formatting.GRAY, Formatting.ITALIC);

			String hoverText = link.getTitle();
			MutableText hover;
			if (hoverText != null) {
				hover = literal(hoverText).append("\n\n").append(redirectsTo);
			} else {
				hover = redirectsTo;
			}

			return title.setStyle(buildLinkStyle(link.getDestination(), hover));
		}

		return null;
	};

	public static final NodeStyler FORMATTED = (node, innerContent) -> {
		if (node instanceof FormattedNode formatted && innerContent != null) {
			return innerContent.formatted(formatted.getFormatting());
		}
		return null;
	};

	private static final Style SPOILER_STYLE = Style.EMPTY.withFormatting(Formatting.DARK_GRAY, Formatting.OBFUSCATED);

	public static final NodeStyler SPOILER = (node, innerContent) -> {
		if (node instanceof SpoilerNode && innerContent != null) {
			return innerContent.copyContentOnly()
					.setStyle(SPOILER_STYLE.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, innerContent)));
		}
		return null;
	};

	private static Style buildLinkStyle(String url, MutableText hover) {
		return Style.EMPTY
				.withFormatting(Formatting.AQUA, Formatting.UNDERLINE)
				.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url))
				.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover));
	}

	public static void addTo(TextStyler.Builder builder) {
		NodeStyler styler = NodeStyler.compose(LITERAL, CODE, BOLD, UNDERLINE, ITALIC, STRIKETHROUGH, LINK, FORMATTED, SPOILER);
		builder.addNodeStyler(styler);

		builder.addExtensions(List.of(
				ColoredChatExtension.INSTANCE,
				SpoilerExtension.INSTANCE,
				AutolinkExtension.create(),
				StrikethroughExtension.create()
		));
	}
}
