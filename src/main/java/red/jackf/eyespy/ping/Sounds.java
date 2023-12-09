package red.jackf.eyespy.ping;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;

public class Sounds {
    private static final int SOUND_OFFSET = 2;
    public static void playMiss(ServerPlayer player) {
        send(player, SoundEvents.NOTE_BLOCK_BELL, player.getEyePosition(), 0.5f);
    }

    public static void playBlock(Collection<ServerPlayer> players, BlockHitResult blockHit, BlockState blockState) {
        for (ServerPlayer player : players) {
            send(player, SoundEvents.NOTE_BLOCK_BELL, towards(player.getEyePosition(), blockHit.getBlockPos().getCenter()), getBlockPitch(blockState));
        }
    }

    public static void playEntity(Collection<ServerPlayer> players, EntityHitResult entityHit) {
        var sound = getEntitySound(entityHit.getEntity());
        for (ServerPlayer player : players) {
            send(player, sound.getFirst(), towards(player.getEyePosition(), entityHit.getEntity().position()), sound.getSecond());
        }
    }

    private static Pair<Holder<SoundEvent>, Float> getEntitySound(Entity entity) {
        if (!(entity instanceof LivingEntity living)) return Pair.of(SoundEvents.NOTE_BLOCK_BELL, 1f);

        float pitch = (float) Mth.clamp(Math.exp(-living.getHealth() / 18) * 1.5 + 0.5, 0.5, 2);

        return switch (entity.getType().getCategory()) {
            case MONSTER -> Pair.of(SoundEvents.NOTE_BLOCK_PLING, pitch / 2 + 0.25f);
            case WATER_CREATURE, WATER_AMBIENT, AXOLOTLS, UNDERGROUND_WATER_CREATURE -> Pair.of(Holder.direct(SoundEvents.PLAYER_SPLASH), pitch);
            default -> Pair.of(SoundEvents.NOTE_BLOCK_BELL, pitch);
        };
    }

    public static void playWarn(Collection<ServerPlayer> players, Vec3 pos) {
        for (ServerPlayer player : players) {
            send(player, SoundEvents.NOTE_BLOCK_PLING, towards(player.getEyePosition(), pos), 0.4f);
        }
    }

    //--------------------------------------------------------------------

    private static float getBlockPitch(BlockState state) {
        return 2f - Mth.clamp(state.getBlock().defaultDestroyTime() / 5f, 0, 1.25f);
    }

    private static float jitterPitch(ServerPlayer player) {
        return (player.getRandom().nextFloat() - 0.5f) * 0.1f;
    }

    // Move a source vector to a target, up to a maximum distance (SOUND_OFFSET)
    private static Vec3 towards(Vec3 source, Vec3 target) {
        if (source.distanceTo(target) < SOUND_OFFSET) return target;
        Vec3 offset = target.subtract(source).normalize().scale(SOUND_OFFSET);
        return source.add(offset);
    }

    private static void send(ServerPlayer player, Holder<SoundEvent> event, Vec3 pos, float pitch) {
        player.connection.send(new ClientboundSoundPacket(
                event,
                SoundSource.PLAYERS,
                pos.x,
                pos.y,
                pos.z,
                1f,
                pitch + jitterPitch(player),
                player.getRandom().nextLong()
        ));
    }
}
