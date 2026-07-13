package net.marma.jjkfys.mixin;

import net.marma.jjkfys.CursedSpiritRaceHelper;

import net.mcreator.jujutsucraft.procedures.ReverseCursedTechniqueOnEffectActiveTickProcedure;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ReverseCursedTechniqueOnEffectActiveTickProcedure.class)
public class CursedSpiritRctTickMixin {

    private static final ThreadLocal<Boolean> SHOULD_RESTORE =
            ThreadLocal.withInitial(() -> false);

    private static final ThreadLocal<Boolean> PREVIOUS_VALUE =
            ThreadLocal.withInitial(() -> false);

    @Inject(
            method = "execute",
            at = @At("HEAD"),
            remap = false
    )
    private static void jjkfys$usePlayerHealLogic(
            LevelAccessor world,
            double x,
            double y,
            double z,
            Entity entity,
            CallbackInfo ci
    ) {

        SHOULD_RESTORE.set(false);
        PREVIOUS_VALUE.set(false);

        if (!(entity instanceof Player)
                || !CursedSpiritRaceHelper.isCursedSpiritRace(entity)) {
            return;
        }

        boolean previous =
                entity.getPersistentData()
                        .getBoolean("CursedSpirit");

        PREVIOUS_VALUE.set(previous);
        SHOULD_RESTORE.set(true);

        entity.getPersistentData()
                .putBoolean(
                        "CursedSpirit",
                        false
                );
    }

    @Inject(
            method = "execute",
            at = @At("RETURN"),
            remap = false
    )
    private static void jjkfys$restoreCursedSpiritTag(
            LevelAccessor world,
            double x,
            double y,
            double z,
            Entity entity,
            CallbackInfo ci
    ) {

        if (entity != null
                && SHOULD_RESTORE.get()) {

            entity.getPersistentData()
                    .putBoolean(
                            "CursedSpirit",
                            PREVIOUS_VALUE.get()
                    );
        }

        SHOULD_RESTORE.remove();
        PREVIOUS_VALUE.remove();
    }
}
