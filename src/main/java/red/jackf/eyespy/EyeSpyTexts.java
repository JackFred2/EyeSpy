package red.jackf.eyespy;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import red.jackf.eyespy.mixins.EyeSpyEntityInvoker;

public class EyeSpyTexts {
    public static MutableComponent distance(ServerPlayer player, Vec3 target) {
        return Component.literal("%.2f".formatted(target.distanceTo(player.getEyePosition())) + "m");
    }

    public static Component block(ServerPlayer player, Vec3 target, BlockState state) {
        Style style = EyeSpy.CONFIG.instance().rangefinder.useColours ?
                Style.EMPTY.withColor(EyeSpyColours.getForBlock(state).toARGB()) : Style.EMPTY;

        return distance(player, target)
                .append(CommonComponents.NEW_LINE)
                .append(state.getBlock().getName().setStyle(style));
    }

    public static Component entity(ServerPlayer player, Vec3 target, Entity entity) {
        Style style = EyeSpy.CONFIG.instance().rangefinder.useColours ?
                Style.EMPTY.withColor(EyeSpyColours.getForEntity(entity)) : Style.EMPTY;
        Component name = ((EyeSpyEntityInvoker) entity).eyespy$getTypeName().copy().setStyle(style);

        if (entity.hasCustomName()) {
            name = Component.literal("").setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE))
                            .append(entity.getCustomName())
                            .append(" (")
                            .append(name)
                            .append(")");
        }
        return distance(player, target)
                .append(CommonComponents.NEW_LINE)
                .append(name);
    }
}
