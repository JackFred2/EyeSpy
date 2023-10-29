package red.jackf.eyespy;

import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.Saddleable;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.level.block.state.BlockState;
import red.jackf.jackfredlib.api.colour.Colour;

import java.util.Set;

public class Colours {
    private static final ChatFormatting HOSTILE = ChatFormatting.RED;
    private static final ChatFormatting VILLAGE = ChatFormatting.LIGHT_PURPLE;
    private static final ChatFormatting FRIENDLY = ChatFormatting.GREEN;
    private static final ChatFormatting VEHICLE = ChatFormatting.AQUA;
    private static final ChatFormatting AMBIENT = ChatFormatting.YELLOW;
    private static final ChatFormatting DEFAULT = ChatFormatting.WHITE;

    private static final Set<EntityType<?>> VEHICLES = Set.of(
            EntityType.BOAT,
            EntityType.CHEST_BOAT,
            EntityType.MINECART,
            EntityType.CHEST_MINECART,
            EntityType.FURNACE_MINECART,
            EntityType.HOPPER_MINECART,
            EntityType.TNT_MINECART
    );

    public static ChatFormatting getForEntity(Entity entity) {
        if (isVehicle(entity)) return VEHICLE;

        MobCategory category = entity.getType().getCategory();
        if (category == MobCategory.MONSTER) return HOSTILE;

        if (entity instanceof Npc) return VILLAGE;
        if (category == MobCategory.CREATURE
                || category == MobCategory.WATER_CREATURE
                || category == MobCategory.UNDERGROUND_WATER_CREATURE
                || category == MobCategory.AXOLOTLS) return FRIENDLY;
        if (category == MobCategory.AMBIENT || category == MobCategory.WATER_AMBIENT) return AMBIENT;

        if (entity.getType() == EntityType.IRON_GOLEM) return VILLAGE;

        return DEFAULT;
    }

    private static boolean isVehicle(Entity entity) {
        return VEHICLES.contains(entity.getType()) || entity instanceof Saddleable saddleable && saddleable.isSaddled();
    }

    public static Colour getForBlock(BlockState state) {
        return Colour.fromInt(state.getBlock().defaultMapColor().col).scaleBrightness(1.5f);
    }
}
