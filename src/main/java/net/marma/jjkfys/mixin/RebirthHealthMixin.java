package net.marma.jjkfys.mixin;

import com.jujutsu.jujutsucraftaddon.util.RebirthManager;

import net.minecraft.world.entity.LivingEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RebirthManager.class)
public class RebirthHealthMixin {

    @Inject(
            method = "applyHealthBonus",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void jjkfys$removeRebirthHP(
            LivingEntity entity,
            int level,
            CallbackInfo ci
    ) {
        
        ci.cancel();
    }
}