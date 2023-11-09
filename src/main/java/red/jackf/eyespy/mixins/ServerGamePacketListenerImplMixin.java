package red.jackf.eyespy.mixins;

import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import red.jackf.eyespy.EyeSpy;
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
        if (!EyeSpy.CONFIG.instance().ping.enabled) return;
        if (EyeSpy.CONFIG.instance().ping.requiresZoomIn) {
            if (this.player.getUseItem().is(Items.SPYGLASS)) {
                ci.cancel();
                Ping.activate(this.player);
            }
        } else {
            if (this.player.getMainHandItem().is(Items.SPYGLASS) || this.player.getOffhandItem().is(Items.SPYGLASS)) {
                ci.cancel();
                Ping.activate(this.player);
            }
        }
    }

}
