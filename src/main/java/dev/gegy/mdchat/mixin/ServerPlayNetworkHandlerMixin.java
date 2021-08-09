package dev.gegy.mdchat.mixin;

import dev.gegy.mdchat.TextStyler;
import net.minecraft.server.filter.TextStream;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = ServerPlayNetworkHandler.class, priority = 999)
public class ServerPlayNetworkHandlerMixin {
    @Shadow
    public ServerPlayerEntity player;

    @ModifyVariable(
            method = "handleMessage",
            ordinal = 1,
            at = @At(value = "STORE", ordinal = 0)
    )
    private Text formatChat(Text text, TextStream.Message message) {
        Text styled = TextStyler.INSTANCE.apply(message.getFiltered());
        if (styled != null) {
            return new TranslatableText("chat.type.text", this.player.getDisplayName(), styled);
        }
        return text;
    }
}
