package red.jackf.eyespy;

import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class Sounds {
    private static final int SOUND_OFFSET = 2;
    public static void playMiss(ServerPlayer player) {
        send(player, SoundEvents.NOTE_BLOCK_BIT, player.getEyePosition(), 0.7f);
    }

    public static void playBlock(ServerPlayer player, BlockHitResult blockHit) {
        send(player, SoundEvents.NOTE_BLOCK_BIT, towards(player.getEyePosition(), blockHit.getBlockPos().getCenter()), 1f);
    }

    public static void playEntity(ServerPlayer player, EntityHitResult entityHit) {
        send(player, SoundEvents.NOTE_BLOCK_BIT, towards(player.getEyePosition(), entityHit.getEntity().position()), 1.1f);
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
                pitch,
                player.getRandom().nextLong()
        ));
    }
}
