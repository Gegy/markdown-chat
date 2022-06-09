package dev.gegy.mdchat;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.network.message.MessageDecorator;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.commonmark.Extension;
import org.commonmark.node.Block;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.parser.PostProcessor;
import org.commonmark.parser.block.BlockParserFactory;
import org.commonmark.parser.delimiter.DelimiterProcessor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public final class TextStyler implements MessageDecorator {
    public static final TextStyler INSTANCE = builderWithGlobal().build();

    private final NodeStyler styler;
    private final Parser parser;

    private TextStyler(NodeStyler styler, Parser parser) {
        this.styler = styler;
        this.parser = parser;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a new TextStyler based off of globally-invoked bootstraps.
     *
     * @return A global-like TextStyler.
     * @author KJP12
     * @since 1.3.0
     */
    public static TextStyler.Builder builderWithGlobal() {
        TextStyler.Builder stylerBuilder = new TextStyler.Builder();
        StylerBootstrap.global().bootstrap(stylerBuilder);

        return stylerBuilder;
    }

    /**
     * Converts the input string into formatted Minecraft text elements.
     *
     * @param string The input markdown string.
     * @return Formatted Minecraft text.
     */
    @Nullable
    public net.minecraft.text.Text apply(String string) {
        Node node = this.parser.parse(string);
        return this.render(node);
    }

    /**
     * Converts the input text into formatted Minecraft text elements.
     *
     * @param text The input markdown text.
     * @return Formatted Minecraft text.
     * @author KJP12
     * @since 1.3.0
     */
    @Nullable
    public net.minecraft.text.Text apply(net.minecraft.text.Text text) {
        return this.apply(text.getString());
    }

    /**
     * Converts the input Text into formatted Minecraft text elements.
     *
     * @param sender  The sender of the message. Ignored.
     * @param message The message to get the contents of.
     * @return {@link #apply(String)} wrapped in a CompletableFuture.
     * @author KJP12
     * @see Main
     * @since 1.3.0
     */
    @Override
    public CompletableFuture<net.minecraft.text.Text> decorate(@Nullable ServerPlayerEntity sender, net.minecraft.text.Text message) {
        return CompletableFuture.completedFuture(apply(message));
    }

    @Nullable
    private MutableText render(Node node) {
        MutableText innerContent = this.renderInnerContent(node);
        MutableText styledContent = this.styler.style(node, innerContent);
        return styledContent != null ? styledContent : innerContent;
    }

    @Nullable
    private MutableText renderInnerContent(Node parent) {
        MutableText result = null;

        Node child = parent.getFirstChild();
        while (child != null) {
            Node next = child.getNext();

            MutableText text = this.render(child);
            if (text != null) {
                if (result == null) {
                    result = Text.empty();
                }
                result = result.append(text);
            }

            child = next;
        }

        return result;
    }

    public static final class Builder {
        private final List<NodeStyler> nodeStylers = new ArrayList<>();
        private final Parser.Builder parser = Parser.builder();
        private final Set<Class<? extends Block>> enabledBlockTypes = new ReferenceOpenHashSet<>();

        private Builder() {
        }

        public void addNodeStyler(NodeStyler nodeStyler) {
            this.nodeStylers.add(nodeStyler);
        }

        public Builder addExtension(Extension extension) {
            return this.addExtensions(List.of(extension));
        }

        public Builder addExtensions(Iterable<? extends Extension> extensions) {
            this.parser.extensions(extensions);
            return this;
        }

        public Builder enableBlockType(Class<? extends Block> blockType) {
            this.enabledBlockTypes.add(blockType);
            return this;
        }

        public Builder addBlockParserFactory(BlockParserFactory blockParserFactory) {
            this.parser.customBlockParserFactory(blockParserFactory);
            return this;
        }

        public Builder addDelimiterProcessor(DelimiterProcessor delimiterProcessor) {
            this.parser.customDelimiterProcessor(delimiterProcessor);
            return this;
        }

        public Builder addPostProcessor(PostProcessor postProcessor) {
            this.parser.postProcessor(postProcessor);
            return this;
        }

        public TextStyler build() {
            NodeStyler nodeStyler = NodeStyler.compose(this.nodeStylers.toArray(NodeStyler[]::new));
            Parser parser = this.parser
                    .enabledBlockTypes(this.enabledBlockTypes)
                    .build();

            return new TextStyler(nodeStyler, parser);
        }
    }
}
