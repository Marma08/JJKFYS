package net.marma.jjkfys.mixin;

import net.marma.jjkfys.GojoUvEffectLimiter;
import net.marma.jjkfys.init.JJKFysGameRules;
import net.mcreator.jujutsucraft.procedures.UnlimitedVoidActiveProcedure;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(UnlimitedVoidActiveProcedure.class)
public class GojoDomainTimingMixin {
    private static final ThreadLocal<LevelAccessor> JJKFYS_WORLD = new ThreadLocal<>();
    private static final ThreadLocal<Entity> JJKFYS_DOMAIN = new ThreadLocal<>();

    @Inject(method = "execute", at = @At("HEAD"), remap = false)
    private static void jjkfys$captureWorld(LevelAccessor world, Entity entity, CallbackInfo ci) {
        JJKFYS_WORLD.set(world);
        JJKFYS_DOMAIN.set(entity);
        GojoUvEffectLimiter.beforeUnlimitedVoid(world, entity);
        GojoUvEffectLimiter.beginUnlimitedVoidPass();
    }

    @Inject(method = "execute", at = @At("RETURN"), remap = false)
    private static void jjkfys$clearWorld(LevelAccessor world, Entity entity, CallbackInfo ci) {
        GojoUvEffectLimiter.endUnlimitedVoidPass();
        GojoUvEffectLimiter.markFromUnlimitedVoid(world, entity);
        JJKFYS_WORLD.remove();
        JJKFYS_DOMAIN.remove();
    }

    @ModifyConstant(method = "execute", constant = @Constant(intValue = 6000), remap = false)
    private static int jjkfys$brainDamageDuration(int original) {
        LevelAccessor world = JJKFYS_WORLD.get();
        if (world == null) {
            return original;
        }
        int duration = world.getLevelData().getGameRules().getInt(JJKFysGameRules.GOJO_BRAIN_DAMAGE_DURATION_TICKS);
        return duration < 0 ? original : duration;
    }

    @Redirect(
            method = "execute",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/nbt/CompoundTag;putDouble(Ljava/lang/String;D)V"
            ),
            require = 0,
            remap = false
    )
    private static void jjkfys$skipReleasedUvSkillReset(CompoundTag data, String key, double value) {
        if (!GojoUvEffectLimiter.isUnlimitedVoidTimingLimited(JJKFYS_WORLD.get())
                || !GojoUvEffectLimiter.shouldSuppressUnlimitedVoidSkillWrite(data, key, value)) {
            data.putDouble(key, value);
        }
    }

    @Redirect(
            method = "execute",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/mcreator/jujutsucraft/procedures/LogicAttackDomainProcedure;execute(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)Z"
            ),
            require = 0,
            remap = false
    )
    private static boolean jjkfys$skipReleasedUvTarget(LevelAccessor world, Entity domain, Entity target) {
        if (!GojoUvEffectLimiter.isUnlimitedVoidTimingLimited(world)) {
            return net.mcreator.jujutsucraft.procedures.LogicAttackDomainProcedure.execute(world, domain, target);
        }

        return !GojoUvEffectLimiter.shouldSkipUnlimitedVoidTarget(domain, target)
                && net.mcreator.jujutsucraft.procedures.LogicAttackDomainProcedure.execute(world, domain, target);
    }

    @Redirect(
            method = "execute",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z"
            ),
            require = 0,
            remap = false
    )
    private static boolean jjkfys$skipReleasedUvEffect(LivingEntity target, MobEffectInstance effect) {
        if (!GojoUvEffectLimiter.isUnlimitedVoidTimingLimited(JJKFYS_WORLD.get())) {
            return target.addEffect(effect);
        }

        GojoUvEffectLimiter.noteUnlimitedVoidEffect(JJKFYS_WORLD.get(), JJKFYS_DOMAIN.get(), target);
        if (GojoUvEffectLimiter.shouldSuppressUnlimitedVoidEffect(target, effect)) {
            return false;
        }
        return target.addEffect(effect);
    }

    @Redirect(
            method = "execute",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;removeEffect(Lnet/minecraft/world/effect/MobEffect;)Z"
            ),
            require = 0,
            remap = false
    )
    private static boolean jjkfys$skipReleasedUvEffectRemoval(LivingEntity target, MobEffect effect) {
        if (GojoUvEffectLimiter.isUnlimitedVoidTimingLimited(JJKFYS_WORLD.get())
                && GojoUvEffectLimiter.shouldSuppressUnlimitedVoidEffectRemoval(target, effect)) {
            return false;
        }
        return target.removeEffect(effect);
    }
}
