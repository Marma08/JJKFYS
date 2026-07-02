package net.marma.jjkfys;

import net.marma.jjkfys.init.JJKFysGameRules;
import net.mcreator.jujutsucraft.init.JujutsucraftModMobEffects;
import net.mcreator.jujutsucraft.network.JujutsucraftModVariables;
import net.mcreator.jujutsucraft.procedures.LogicAttackDomainProcedure;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = JJKFYS.MODID)
public class GojoUvEffectLimiter {
    private static final String UV_SOURCE_KEY = "jjkfysGojoUvSource";
    private static final String UV_UNTIL_KEY = "jjkfysGojoUvUntil";
    private static final String BRAIN_UNTIL_KEY = "jjkfysGojoBrainUntil";

    public static void clearClosedDomainSession(LevelAccessor world, Entity domainOwner) {
        if (!(world instanceof ServerLevel level) || domainOwner == null) {
            return;
        }

        String source = domainOwner.getStringUUID();
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof LivingEntity living) {
                CompoundTag data = living.getPersistentData();
                if (source.equals(data.getString(UV_SOURCE_KEY))) {
                    clearSession(data);
                }
            }
        }
    }

    public static void markFromUnlimitedVoid(LevelAccessor world, Entity domain) {
        if (!(world instanceof Level level) || domain == null || level.isClientSide()) {
            return;
        }

        double radius = Math.max(1.0D, JujutsucraftModVariables.MapVariables.get(world).DomainExpansionRadius);
        Vec3 center = domainCenter(domain);
        AABB area = new AABB(center, center).inflate(Math.max(radius, 64.0D));

        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area)) {
            if (target == domain) {
                continue;
            }
            if (LogicAttackDomainProcedure.execute(world, domain, target) || hasUnlimitedVoidEffect(target)) {
                mark(target, domain.getStringUUID(), world);
                enforce(target);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            enforce(event.player);
        }
    }

    private static void mark(LivingEntity target, String source, LevelAccessor world) {
        int duration = Math.max(0, world.getLevelData().getGameRules().getInt(JJKFysGameRules.GOJO_UV_DURATION_TICKS));
        int brainDuration = Math.max(0, world.getLevelData().getGameRules().getInt(JJKFysGameRules.GOJO_BRAIN_DAMAGE_DURATION_TICKS));
        long now = target.level().getGameTime();
        CompoundTag data = target.getPersistentData();
        if (domainSessionClosed(target)) {
            clearSession(data);
        }

        if (!source.equals(data.getString(UV_SOURCE_KEY)) || !data.contains(UV_UNTIL_KEY)) {
            data.putString(UV_SOURCE_KEY, source);
            data.putLong(UV_UNTIL_KEY, now + duration);
            data.putLong(BRAIN_UNTIL_KEY, now + brainDuration);
        }
    }

    private static void enforce(Player player) {
        enforce((LivingEntity) player);
    }

    private static void enforce(LivingEntity target) {
        if (target.level().isClientSide()) {
            return;
        }

        CompoundTag data = target.getPersistentData();
        enforceBrainDamageDuration(target, data);

        if (!data.contains(UV_UNTIL_KEY)) {
            return;
        }

        if (domainSessionClosed(target)) {
            clearSession(data);
            return;
        }

        long now = target.level().getGameTime();
        if (now >= data.getLong(UV_UNTIL_KEY)) {
            removeUnlimitedVoidEffects(target);
        }
    }

    private static boolean domainSessionClosed(LivingEntity target) {
        Level level = target.level();

        CompoundTag data = target.getPersistentData();
        if (!data.contains(UV_SOURCE_KEY)) {
            return true;
        }

        Entity source = findEntity(level, data.getString(UV_SOURCE_KEY));
        if (source == null) {
            return true;
        }
        if (!(source instanceof LivingEntity livingSource)) {
            return !source.isAlive();
        }

        return !livingSource.hasEffect(JujutsucraftModMobEffects.DOMAIN_EXPANSION.get())
                || livingSource.getPersistentData().getBoolean("DomainDefeated")
                || livingSource.getPersistentData().getBoolean("Failed") && !livingSource.getPersistentData().getBoolean("Cover");
    }

    private static Entity findEntity(Level level, String uuidText) {
        try {
            return level instanceof net.minecraft.server.level.ServerLevel serverLevel
                    ? serverLevel.getEntity(UUID.fromString(uuidText))
                    : null;
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static void clearSession(CompoundTag data) {
        data.remove(UV_SOURCE_KEY);
        data.remove(UV_UNTIL_KEY);
    }

    private static void enforceBrainDamageDuration(LivingEntity target, CompoundTag data) {
        if (!data.contains(BRAIN_UNTIL_KEY)) {
            return;
        }

        MobEffect brainDamage = JujutsucraftModMobEffects.BRAIN_DAMAGE.get();
        if (!target.hasEffect(brainDamage)) {
            data.remove(BRAIN_UNTIL_KEY);
            return;
        }

        long now = target.level().getGameTime();
        long remaining = data.getLong(BRAIN_UNTIL_KEY) - now;
        if (remaining <= 0L) {
            target.removeEffect(brainDamage);
            data.remove(BRAIN_UNTIL_KEY);
            return;
        }

        int clamped = remaining > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) remaining;
        var current = target.getEffect(brainDamage);
        if (current != null && current.getDuration() > clamped) {
            target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    brainDamage,
                    clamped,
                    current.getAmplifier(),
                    false,
                    false
            ));
        }
    }

    private static Vec3 domainCenter(Entity domain) {
        CompoundTag data = domain.getPersistentData();
        return new Vec3(
                data.getDouble("x_pos_doma") != 0.0D ? data.getDouble("x_pos_doma") : domain.getX(),
                data.getDouble("y_pos_doma") != 0.0D ? data.getDouble("y_pos_doma") : domain.getY(),
                data.getDouble("z_pos_doma") != 0.0D ? data.getDouble("z_pos_doma") : domain.getZ()
        );
    }

    private static boolean hasUnlimitedVoidEffect(LivingEntity target) {
        return target.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)
                || target.hasEffect(MobEffects.CONFUSION)
                || target.hasEffect(MobEffects.DIG_SLOWDOWN)
                || target.hasEffect(MobEffects.BLINDNESS)
                || target.hasEffect(MobEffects.WEAKNESS)
                || target.hasEffect(JujutsucraftModMobEffects.NEUTRALIZATION.get())
                || target.hasEffect(JujutsucraftModMobEffects.CURSED_TECHNIQUE.get())
                || target.hasEffect(JujutsucraftModMobEffects.COOLDOWN_TIME_COMBAT.get());
    }

    private static void removeUnlimitedVoidEffects(LivingEntity target) {
        removeIfPresent(target, MobEffects.MOVEMENT_SLOWDOWN);
        removeIfPresent(target, MobEffects.CONFUSION);
        removeIfPresent(target, MobEffects.DIG_SLOWDOWN);
        removeIfPresent(target, MobEffects.BLINDNESS);
        removeIfPresent(target, MobEffects.WEAKNESS);
        removeIfPresent(target, JujutsucraftModMobEffects.NEUTRALIZATION.get());
        removeIfPresent(target, JujutsucraftModMobEffects.CURSED_TECHNIQUE.get());
        removeIfPresent(target, JujutsucraftModMobEffects.COOLDOWN_TIME_COMBAT.get());
    }

    private static void removeIfPresent(LivingEntity target, MobEffect effect) {
        if (target.hasEffect(effect)) {
            target.removeEffect(effect);
        }
    }
}
