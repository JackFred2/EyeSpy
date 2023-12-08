package red.jackf.eyespy.mixins;

import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import red.jackf.eyespy.networking.EyeSpyNetworking;
import red.jackf.eyespy.ping.Ping;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {

    @Shadow public ServerPlayer player;

    @Inject(method = "handlePlayerAction",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;",
                    shift = At.Shift.AFTER,
                    ordinal = 0),
            cancellable = true)
    private void eyespy$handlePlayerSwap(ServerboundPlayerActionPacket packet, CallbackInfo ci) {
        if (EyeSpyNetworking.hasClientInstalled(player.getGameProfile())) return; // don't if they have dedicated keybind

        if (Ping.canActivate(player)) {
            Ping.activate(player);
            ci.cancel();
        }
    }

}
