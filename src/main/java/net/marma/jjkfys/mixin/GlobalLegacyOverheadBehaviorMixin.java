package net.marma.jjkfys.mixin;

import net.mcreator.jujutsucraft.procedures.AttackOverheadProcedure;
import net.mcreator.jujutsucraft.procedures.BlockDestroyAllDirectionProcedure;
import net.mcreator.jujutsucraft.procedures.KnockbackProcedure;
import net.mcreator.jujutsucraft.procedures.RangeAttackProcedure;
import net.mcreator.jujutsucraft.procedures.ReturnEntitySizeProcedure;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AttackOverheadProcedure.class)
public class GlobalLegacyOverheadBehaviorMixin {

    private static final double CURRENT_PRIMARY_DAMAGE = 13.0D;
    private static final double CURRENT_PRIMARY_RANGE = 5.0D;
    private static final double LEGACY_PRIMARY_DAMAGE = 12.0D;
    private static final double LEGACY_PRIMARY_RANGE = 7.0D;
    private static final double LEGACY_PRIMARY_KNOCKBACK = 0.75D;
    private static final double LEGACY_SECONDARY_DAMAGE = 9.0D;
    private static final double LEGACY_SECONDARY_RANGE = 2.5D;
    private static final double LEGACY_SECONDARY_KNOCKBACK = 0.5D;
    private static final double LEGACY_KNOCKBACK_WAVE_RANGE = 1.0D;
    private static final double LEGACY_FINAL_BLOCK_DAMAGE = 0.33D;
    private static final double LEGACY_FINAL_BLOCK_RANGE = 2.5D;

    @Redirect(
            method = "execute",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/mcreator/jujutsucraft/procedures/RangeAttackProcedure;execute(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;)V",
                    ordinal = 1
            ),
            remap = false
    )
    private static void jjkfys$restoreLegacyMainOverheadWave(
            LevelAccessor world,
            double x,
            double y,
            double z,
            Entity entity
    ) {

        CompoundTag data = entity.getPersistentData();
        double baseRange = data.getDouble("Range");
        double baseDamage = data.getDouble("Damage");
        double baseKnockback = data.getDouble("knockback");
        double baseEffect = data.getDouble("effect");
        boolean baseAttack = data.getBoolean("attack");
        double baseCnt6 = data.getDouble("cnt6");
        double scale = resolveScale(baseRange, baseDamage);
        double shockScale = resolveShockScale(data, scale);

        data.putDouble("cnt6", -1.0D);
        data.putDouble("Damage", LEGACY_PRIMARY_DAMAGE * scale);
        data.putDouble("knockback", LEGACY_PRIMARY_KNOCKBACK);
        data.putDouble("Range", LEGACY_PRIMARY_RANGE * scale);
        data.putDouble("effect", 5.0D);
        data.putBoolean("attack", true);
        RangeAttackProcedure.execute(world, x, y, z, entity);

        data.putDouble("Damage", LEGACY_SECONDARY_DAMAGE * scale);
        data.putDouble("knockback", LEGACY_SECONDARY_KNOCKBACK);
        data.putDouble("Range", LEGACY_SECONDARY_RANGE * shockScale);
        data.putBoolean("attack", true);
        RangeAttackProcedure.execute(world, x, y, z, entity);

        data.putDouble("cnt6", baseCnt6);
        data.putDouble("Damage", baseDamage);
        data.putDouble("Range", baseRange);
        data.putDouble("knockback", baseKnockback);
        data.putDouble("effect", baseEffect);
        data.putBoolean("attack", baseAttack);
    }

    @Redirect(
            method = "execute",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/mcreator/jujutsucraft/procedures/BlockDestroyAllDirectionProcedure;execute(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;)V",
                    ordinal = 0
            ),
            remap = false
    )
    private static void jjkfys$restoreLegacyMainOverheadBlockRange(
            LevelAccessor world,
            double x,
            double y,
            double z,
            Entity entity
    ) {

        CompoundTag data = entity.getPersistentData();
        double baseBlockDamage = data.getDouble("BlockDamage");
        double baseBlockRange = data.getDouble("BlockRange");
        double baseRange = data.getDouble("Range");
        double baseKnockback = data.getDouble("knockback");
        boolean baseNoParticle = data.getBoolean("noParticle");
        double scale = resolveScale(baseRange, data.getDouble("Damage"));
        double shockScale = resolveShockScale(data, scale);

        data.putDouble("BlockDamage", 1.0D + shockScale);
        data.putDouble("BlockRange", shockScale);
        data.putBoolean("noParticle", true);
        BlockDestroyAllDirectionProcedure.execute(world, x, y, z, entity);

        data.putDouble("knockback", LEGACY_SECONDARY_KNOCKBACK);
        data.putDouble("Range", LEGACY_KNOCKBACK_WAVE_RANGE * shockScale);
        KnockbackProcedure.execute(world, x, y, z, entity);

        data.putDouble("BlockDamage", baseBlockDamage);
        data.putDouble("BlockRange", baseBlockRange);
        data.putDouble("Range", baseRange);
        data.putDouble("knockback", baseKnockback);
        data.putBoolean("noParticle", baseNoParticle);
    }

    @Redirect(
            method = "execute",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/mcreator/jujutsucraft/procedures/BlockDestroyAllDirectionProcedure;execute(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;)V",
                    ordinal = 1
            ),
            remap = false
    )
    private static void jjkfys$restoreLegacyFinalOverheadBlockWave(
            LevelAccessor world,
            double x,
            double y,
            double z,
            Entity entity
    ) {

        // JJC 50 moved this block hit to cnt1 == 5. The legacy hit is applied at cnt1 == 9 below.
    }

    @Inject(method = "execute", at = @At("TAIL"), remap = false)
    private static void jjkfys$applyLegacyFinalOverheadBlockWaveAtLegacyTiming(
            LevelAccessor world,
            double x,
            double y,
            double z,
            Entity entity,
            CallbackInfo ci
    ) {

        if (!isLegacyFinalOverheadTick(entity)) {
            return;
        }
        applyLegacyFinalOverheadBlockWave(world, x, y, z, entity);
    }

    private static void applyLegacyFinalOverheadBlockWave(
            LevelAccessor world,
            double x,
            double y,
            double z,
            Entity entity
    ) {

        CompoundTag data = entity.getPersistentData();
        double baseBlockDamage = data.getDouble("BlockDamage");
        double baseBlockRange = data.getDouble("BlockRange");
        boolean baseNoParticle = data.getBoolean("noParticle");
        double shockScale = resolveShockScaleFromEntity(entity);

        data.putDouble("BlockDamage", LEGACY_FINAL_BLOCK_DAMAGE);
        data.putDouble("BlockRange", LEGACY_FINAL_BLOCK_RANGE * shockScale);
        data.putBoolean("noParticle", true);
        BlockDestroyAllDirectionProcedure.execute(world, x, y, z, entity);

        data.putDouble("BlockDamage", baseBlockDamage);
        data.putDouble("BlockRange", baseBlockRange);
        data.putBoolean("noParticle", baseNoParticle);
    }

    private static boolean isLegacyFinalOverheadTick(Entity entity) {

        return Math.abs(entity.getPersistentData().getDouble("cnt1") - 9.0D) < 0.001D;
    }

    private static double resolveScale(double range, double damage) {

        if (range > 0.0D) {
            return range / CURRENT_PRIMARY_RANGE;
        }

        if (damage > 0.0D) {
            return damage / CURRENT_PRIMARY_DAMAGE;
        }

        return 1.0D;
    }

    private static double resolveShockScale(CompoundTag data, double scale) {

        return Math.max(1.0D, Math.sqrt(Math.max(0.0D, data.getDouble("cnt8"))))
                * Math.max(scale, 0.0D);
    }

    private static double resolveShockScaleFromEntity(Entity entity) {

        return Math.max(1.0D, Math.sqrt(Math.max(0.0D, entity.getPersistentData().getDouble("cnt8"))))
                * ReturnEntitySizeProcedure.execute(entity);
    }
}
