package net.marma.jjkfys.mixin;

import net.marma.jjkfys.GojoUvEffectLimiter;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = {
        "net.mcreator.jujutsucraft.procedures.WhenEntityAttacked1Procedure",
        "net.mcreator.jujutsucraft.procedures.WhenEntityAttacked2Procedure",
        "net.mcreator.jujutsucraft.procedures.EffectAttackProcedure"
})
public class ReleasedUvNeutralizationCombatMixin {
    @Redirect(
            method = "*",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;hasEffect(Lnet/minecraft/world/effect/MobEffect;)Z"
            ),
            require = 0
    )
    private static boolean jjkfys$ignoreReleasedUvNeutralization(LivingEntity target, MobEffect effect) {
        if (GojoUvEffectLimiter.shouldIgnoreNeutralizationForCombat(target, effect)) {
            return false;
        }
        return target.hasEffect(effect);
    }
}
