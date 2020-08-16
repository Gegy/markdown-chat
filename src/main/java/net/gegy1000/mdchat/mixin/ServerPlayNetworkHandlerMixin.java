package net.gegy1000.mdchat.mixin;

import net.gegy1000.mdchat.TextStyler;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @Shadow
    public ServerPlayerEntity player;

    @ModifyVariable(
            method = "onGameMessage",
            at = @At(value = "STORE", ordinal = 0),
            ordinal = 0
    )
    private Text modifyGameMessage(Text text, ChatMessageC2SPacket packet) {
        String rawMessage = packet.getChatMessage();

        Text styled = TextStyler.INSTANCE.apply(rawMessage);
        if (styled != null) {
            return new TranslatableText("chat.type.text", this.player.getDisplayName(), styled);
        }

        return text;
    }
}
