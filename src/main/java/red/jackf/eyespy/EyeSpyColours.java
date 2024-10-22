package red.jackf.eyespy;

import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.Saddleable;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import red.jackf.jackfredlib.api.colour.Colour;
import red.jackf.jackfredlib.api.colour.Colours;

import java.util.Set;

public class EyeSpyColours {
    private static final ChatFormatting HOSTILE = ChatFormatting.RED;
    private static final ChatFormatting VILLAGE = ChatFormatting.LIGHT_PURPLE;
    private static final ChatFormatting FRIENDLY = ChatFormatting.GREEN;
    private static final ChatFormatting VEHICLE = ChatFormatting.AQUA;
    private static final ChatFormatting AMBIENT = ChatFormatting.YELLOW;
    private static final ChatFormatting DEFAULT = ChatFormatting.WHITE;

    private static final Set<EntityType<?>> VEHICLES = Set.of(
            // TODO make this auto detect boats and minecraft
            EntityType.OAK_BOAT,
            EntityType.OAK_CHEST_BOAT,
            EntityType.SPRUCE_BOAT,
            EntityType.SPRUCE_CHEST_BOAT,
            EntityType.BIRCH_BOAT,
            EntityType.BIRCH_CHEST_BOAT,
            EntityType.JUNGLE_BOAT,
            EntityType.JUNGLE_CHEST_BOAT,
            EntityType.ACACIA_BOAT,
            EntityType.ACACIA_CHEST_BOAT,
            EntityType.DARK_OAK_BOAT,
            EntityType.DARK_OAK_CHEST_BOAT,
            EntityType.CHERRY_BOAT,
            EntityType.CHERRY_CHEST_BOAT,
            EntityType.MANGROVE_BOAT,
            EntityType.MANGROVE_CHEST_BOAT,
            EntityType.PALE_OAK_BOAT,
            EntityType.PALE_OAK_CHEST_BOAT,
            EntityType.BAMBOO_RAFT,
            EntityType.BAMBOO_CHEST_RAFT,
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
        MapColor mapColor = state.getBlock().defaultMapColor();
        Colour colour = mapColor == MapColor.NONE ? Colours.WHITE : Colour.fromInt(mapColor.col);
        return colour.lerp(Colours.WHITE, .25f);
    }
}
