package net.marma.jjkfys.mixin;

import net.marma.jjkfys.init.JJKFysGameRules;

import net.mcreator.jujutsucraft.procedures.EffectConfirm3Procedure;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EffectConfirm3Procedure.class)
public class EffectConfirm3ResistanceGuardMixin {

    @Redirect(
            method = "execute",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;removeEffect(Lnet/minecraft/world/effect/MobEffect;)Z"
            ),
            require = 1
    )
    private static boolean jjkfys$keepPlayerResistance(LivingEntity target, MobEffect effect) {
        if (target instanceof Player
                && effect == MobEffects.DAMAGE_RESISTANCE
                && target.level()
                        .getGameRules()
                        .getBoolean(JJKFysGameRules.PREVENT_WORLD_CUT_RESISTANCE_REMOVAL)) {
            return false;
        }

        return target.removeEffect(effect);
    }
}
