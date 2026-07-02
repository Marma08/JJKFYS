package net.marma.jjkfys.mixin;

import net.minecraft.world.entity.Entity;

import com.jujutsu.jujutsucraftaddon.procedures.WorldSlashKeyOnKeyReleasedProcedure;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldSlashKeyOnKeyReleasedProcedure.class)
public class UltimateReleaseMixin {

    @Inject(
            method = "execute",
            at = @At("HEAD"),
            remap = false
    )
    private static void jjkfys$fixUltimateRelease(
            Entity entity,
            CallbackInfo ci
    ) {

        if (entity == null) return;

        double cnt = entity.getPersistentData()
                .getDouble("cnt_ult");

        entity.getPersistentData()
                .putDouble("jjkfys_saved_cnt_ult", cnt);
    }

    @Inject(
            method = "execute",
            at = @At("TAIL"),
            remap = false
    )
    private static void jjkfys$restoreUltimateRelease(
            Entity entity,
            CallbackInfo ci
    ) {

        if (entity == null) return;

        double cnt = entity.getPersistentData()
                .getDouble("jjkfys_saved_cnt_ult");

        if (cnt > 0) {

            entity.getPersistentData()
                    .putDouble("cnt_ult", cnt);
        }
    }
}