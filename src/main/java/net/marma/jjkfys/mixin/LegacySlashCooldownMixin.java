package net.marma.jjkfys.mixin;

import net.mcreator.jujutsucraft.init.JujutsucraftModMobEffects;
import net.mcreator.jujutsucraft.procedures.AISlashProcedure;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AISlashProcedure.class)
public class LegacySlashCooldownMixin {

    private static final double LEGACY_SLASH_COOLDOWN = 10.0D;

    @Inject(method = "execute", at = @At("HEAD"), remap = false)
    private static void jjkfys$restoreLegacySlashCooldown(
            LevelAccessor world,
            Entity entity,
            CallbackInfo ci
    ) {

        if (!(entity instanceof LivingEntity living)) {
            return;
        }

        if (entity.getPersistentData().getDouble("cnt1") > 1.0D) {
            return;
        }

        if (entity.getPersistentData().getDouble("COOLDOWN_TICKS") > LEGACY_SLASH_COOLDOWN) {
            entity.getPersistentData().putDouble("COOLDOWN_TICKS", LEGACY_SLASH_COOLDOWN);
        }

        if (living.level().isClientSide()) {
            return;
        }

        MobEffect cooldown = JujutsucraftModMobEffects.COOLDOWN_TIME_COMBAT.get();
        MobEffectInstance current = living.getEffect(cooldown);
        if (current == null || current.getDuration() > (int) LEGACY_SLASH_COOLDOWN) {
            living.addEffect(new MobEffectInstance(cooldown, (int) LEGACY_SLASH_COOLDOWN, 1, false, false));
        }
    }
}
