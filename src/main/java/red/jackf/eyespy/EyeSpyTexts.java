package red.jackf.eyespy;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class EyeSpyTexts {
    public static MutableComponent distance(ServerPlayer player, Vec3 target) {
        return Component.literal("%.2f".formatted(target.distanceTo(player.getEyePosition())) + "m");
    }

    public static Component block(BlockState state) {
        Style style = EyeSpy.CONFIG.instance().text.useColours ?
                Style.EMPTY.withColor(EyeSpyColours.getForBlock(state).toARGB()) : Style.EMPTY;

        return state.getBlock().getName().setStyle(style);
    }

    public static Component entity(Entity entity) {
        boolean useColour = EyeSpy.CONFIG.instance().text.useColours;
        Style style = useColour ? Style.EMPTY.withColor(EyeSpyColours.getForEntity(entity)) : Style.EMPTY;

        if (entity instanceof ServerPlayer player) {
            return useColour ? player.getDisplayName() : player.getName();
        } else if (entity instanceof ItemEntity item) {
            return Component.empty().append(item.getItem().getHoverName()).withStyle(item.getItem().getRarity().color);
        }

        Component name = entity.getType().getDescription().copy().setStyle(style);

        if (entity.hasCustomName()) {
            name = Component.literal("").setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE))
                            .append(entity.getCustomName())
                            .append(" (")
                            .append(name)
                            .append(")");
        }
        return name;
    }
}
