package net.marma.jjkfys.mixin;

import net.marma.jjkfys.GojoUvEffectLimiter;
import net.mcreator.jujutsucraft.procedures.DomainExpansionEffectExpiresProcedure;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DomainExpansionEffectExpiresProcedure.class)
public class GojoDomainCloseTimingMixin {
    @Inject(method = "execute", at = @At("HEAD"), remap = false)
    private static void jjkfys$clearUvSession(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        GojoUvEffectLimiter.clearClosedDomainSession(world, entity);
    }
}
