package net.marma.jjkfys.mixin;

import net.marma.jjkfys.GojoUvEffectLimiter;
import net.mcreator.jujutsucraft.procedures.LogicAttackDomainProcedure;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LogicAttackDomainProcedure.class)
public class UnlimitedVoidDomainMembershipMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true, remap = false)
    private static void jjkfys$ignoreReleasedUnlimitedVoidTargets(LevelAccessor world, Entity domain, Entity target, CallbackInfoReturnable<Boolean> cir) {
        if (GojoUvEffectLimiter.shouldTreatAsOutsideUnlimitedVoid(domain, target)) {
            cir.setReturnValue(false);
        }
    }
}
