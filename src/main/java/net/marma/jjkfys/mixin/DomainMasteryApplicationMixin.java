package net.marma.jjkfys.mixin;

import com.jujutsu.jujutsucraftaddon.procedures.BarrierlessAndCompressedProcedure;

import net.marma.jjkfys.DomainMasteryDisabler;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BarrierlessAndCompressedProcedure.class, remap = false)
public abstract class DomainMasteryApplicationMixin {

    @Inject(
            method = "execute",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void jjkfys$blockDomainMasteryApplication(
            LevelAccessor world,
            Entity entity,
            CallbackInfo ci
    ) {

        if (!DomainMasteryDisabler.isDisabled(
                entity
        )) {
            return;
        }

        DomainMasteryDisabler.forceNormalDomain(
                entity
        );
        ci.cancel();
    }
}
