package red.jackf.eyespy;

import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class Sounds {
    public static void playMiss(ServerPlayer player) {
        send(player, SoundEvents.NOTE_BLOCK_BIT, 0.7f);
    }

    public static void playBlock(ServerPlayer player, BlockHitResult blockHit) {
        send(player, SoundEvents.NOTE_BLOCK_BIT, 1f);
    }

    public static void playEntity(ServerPlayer player, EntityHitResult entityHit) {
        send(player, SoundEvents.NOTE_BLOCK_BIT, 1.1f);
    }

    private static void send(ServerPlayer player, Holder<SoundEvent> event, float pitch) {
        player.connection.send(new ClientboundSoundPacket(
                event,
                SoundSource.PLAYERS,
                player.getX(),
                player.getY(),
                player.getZ(),
                1f,
                pitch,
                player.getRandom().nextLong()
        ));
    }
}
