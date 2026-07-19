package net.marma.jjkfys.mixin;

import net.marma.jjkfys.init.JJKFysGameRules;

import com.jujutsu.jujutsucraftaddon.procedures.WorldSlashProcedure;
import com.jujutsu.jujutsucraftaddon.procedures.WorldSlashVariantsProcedure;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = {
        WorldSlashProcedure.class,
        WorldSlashVariantsProcedure.class
})
public class WorldSlashResistanceGuardMixin {

    @Redirect(
            method = "execute",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;removeEffect(Lnet/minecraft/world/effect/MobEffect;)Z"
            ),
            require = 1
    )
    private static boolean jjkfys$keepResistance(LivingEntity target, MobEffect effect) {
        if (effect == MobEffects.DAMAGE_RESISTANCE
                && target.level()
                        .getGameRules()
                        .getBoolean(JJKFysGameRules.PREVENT_WORLD_CUT_RESISTANCE_REMOVAL)) {
            return false;
        }

        return target.removeEffect(effect);
    }
}
