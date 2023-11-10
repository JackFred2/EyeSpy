package red.jackf.eyespy.rangefinding;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import red.jackf.eyespy.EyeSpy;

public class Rangefinder {
    public static void setup() {
        UseItemCallback.EVENT.register((player, level, hand) -> {
            if (EyeSpy.CONFIG.instance().rangefinder.enabled
                    && player instanceof ServerPlayer serverPlayer
                    && player.getItemInHand(hand).is(Items.SPYGLASS)) {
                add(serverPlayer);
            }
            return InteractionResultHolder.pass(ItemStack.EMPTY);
        });
    }

    public static void add(ServerPlayer player) {
        RangefindLie.create(player);
    }
}
