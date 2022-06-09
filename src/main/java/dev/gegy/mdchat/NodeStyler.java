package dev.gegy.mdchat;

import net.minecraft.text.MutableText;
import net.minecraft.text.NbtDataSource;
import net.minecraft.text.Text;
import org.commonmark.node.Node;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Styles & formats nodes into Minecraft text nodes.
 *
 * @author KJP12
 * @since 1.3.0
 **/
@FunctionalInterface
public interface NodeStyler {
    static NodeStyler compose(NodeStyler... stylers) {
        return (node, innerContent) -> {
            for (NodeStyler styler : stylers) {
                MutableText text = styler.style(node, innerContent);
                if (text != null) {
                    return text;
                }
            }
            return null;
        };
    }

    /**
     * Styles the given markdown node & formatted child text into Minecraft-formatted text.
     *
     * <h3>Helpful Methods in {@link Text}</h3>
     * <ul>
     *     <li>{@link Text#empty()} - Empty Text</li>
     *     <li>{@link Text#literal(String)} - Literal string</li>
     *     <li>{@link Text#translatable(String)} - Translatable string, automatically translates to current language.</li>
     *     <li>{@link Text#keybind(String)} - Keybind string, automatically converts to their current bound key.</li>
     *     <li>{@link Text#nbt(String, boolean, Optional, NbtDataSource)} - NBT String</li>
     *     <li>{@link Text#score(String, String)} - Score of entity or player in objective as string</li>
     *     <li>{@link Text#selector(String, Optional)} - Entity selector as an array as string</li>
     * </ul>
     *
     * @param node         The markdown node to format.
     * @param innerContent The rendered text from the child nodes.
     * @return If handled, a maybe formatted Minecraft text node, else null.
     * @implNote It is possible for this method to be called off-thread. Ensure that the
     *         implementation is thread-safe (e.g. concurrent or read-only maps &amp; lists),
     *         and won't cause issues due to 2 calls occurring at once.
     */
    @Nullable
    MutableText style(Node node, @Nullable MutableText innerContent);
}
