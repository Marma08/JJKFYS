package net.marma.jjkfys.mixin;

import com.jujutsu.jujutsucraftaddon.network.JujutsucraftaddonModVariables;
import com.jujutsu.jujutsucraftaddon.procedures.WorldSlashKeyOnKeyPressedProcedure;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldSlashKeyOnKeyPressedProcedure.class)
public class UltimateUnlockMixin {

    @Inject(
            method = "execute",
            at = @At("HEAD"),
            remap = false
    )
    private static void jjkfys$unlockUltimate(
            LevelAccessor world,
            double x,
            double y,
            double z,
            Entity entity,
            CallbackInfo ci
    ) {

        if (entity == null) return;

        boolean unlocked = entity.getCapability(
                JujutsucraftaddonModVariables.PLAYER_VARIABLES_CAPABILITY,
                Direction.DOWN
        ).orElse(
                new JujutsucraftaddonModVariables.PlayerVariables()
        ).rebirthLevel >= 3;

        if (unlocked) {

            entity.getPersistentData()
                    .putBoolean("PRESS_ULT", true);
        }
    }
}