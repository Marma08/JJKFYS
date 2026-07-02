package net.marma.jjkfys.mixin;

import com.jujutsu.jujutsucraftaddon.procedures.AIClonesProcedure;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AIClonesProcedure.class)
public class DisableDagonCloneDuplicationMixin {

    @Inject(
            method = "execute",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void jjkfys$disableDagonCloneSpam(
            LevelAccessor world,
            Entity entity,
            CallbackInfo ci
    ) {

        if (entity == null) return;

        double tagged =
                entity.getPersistentData()
                        .getDouble("Tagged");
        
        if (tagged == 15.0D) {

            ci.cancel();
        }
    }
}