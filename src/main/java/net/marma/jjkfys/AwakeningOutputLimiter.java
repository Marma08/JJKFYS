package net.marma.jjkfys;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public final class AwakeningOutputLimiter {

    private AwakeningOutputLimiter() {
    }

    public static void capAttackEntity(
            Entity entity,
            Level level
    ) {

        // The awakening output cap is now applied at the source:
        // GojoAwakeningOnEffectActiveTickProcedure. Capping attack entities
        // here would also cap cnt6 values explicitly set by attacks.
    }

    public static boolean capAttackEntityForOwner(
            Entity entity,
            ServerPlayer player
    ) {

        return false;
    }

    public static double capCnt6ForPlayer(
            ServerPlayer player,
            double value
    ) {

        return value;
    }
}
