package dev.gegy.mdchat.parser;

import net.minecraft.util.Formatting;
import org.commonmark.node.CustomNode;

public final class FormattedNode extends CustomNode {
    private final Formatting formatting;

    public FormattedNode(Formatting formatting) {
        this.formatting = formatting;
    }

    public Formatting getFormatting() {
        return this.formatting;
    }
}
