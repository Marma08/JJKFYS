package net.marma.jjkfys.mixin;

import net.mcreator.jujutsucraft.init.JujutsucraftModItems;
import net.mcreator.jujutsucraft.network.JujutsucraftModVariables;
import net.mcreator.jujutsucraft.procedures.RangeAttackProcedure;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;


@Mixin(RangeAttackProcedure.class)
public class HigurumaBlackFlashEligibilityMixin {
    private static final double HIGURUMA_TECHNIQUE = 27.0D;
    private static final double BLACK_FLASH_DAMAGE_FLOOR = 9.0D;
    private static final double GAVEL_NEGATIVE_CNT6_DAMAGE = 8.0D;
    private static final double BLACK_FLASH_FORCE_THRESHOLD = 0.001D;
    private static final double BLACK_FLASH_DISABLED_THRESHOLD = 1.0D;

    @ModifyConstant(
            method = "execute",
            constant = @Constant(doubleValue = 0.998D),
            remap = false,
            require = 0
    )
    private static double jjkfys$disableHigurumaVanillaBlackFlashFallback(
            double constant,
            LevelAccessor world,
            double x,
            double y,
            double z,
            Entity entity
    ) {

        if (!isHiguruma(entity) || constant == BLACK_FLASH_FORCE_THRESHOLD) {
            return constant;
        }

        return BLACK_FLASH_DISABLED_THRESHOLD;
    }

    @Redirect(
            method = "execute",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/nbt/CompoundTag;getDouble(Ljava/lang/String;)D",
                    ordinal = 0
            )
    )
    private static double jjkfys$allowHigurumaGavelDamageThroughBlackFlashGate(
            CompoundTag data,
            String key,
            LevelAccessor world,
            double x,
            double y,
            double z,
            Entity entity
    ) {

        double value = data.getDouble(key);

        if (!"Damage".equals(key) || value >= BLACK_FLASH_DAMAGE_FLOOR) {
            return value;
        }

        if (value >= GAVEL_NEGATIVE_CNT6_DAMAGE && isHiguruma(entity) && hasGavel(entity)) {
            return BLACK_FLASH_DAMAGE_FLOOR;
        }

        return value;
    }

    @Redirect(
            method = "execute",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/nbt/CompoundTag;getDouble(Ljava/lang/String;)D",
                    ordinal = 10
            )
    )
    private static double jjkfys$allowHigurumaNegativeCnt6ThroughBlackFlashGate(
            CompoundTag data,
            String key,
            LevelAccessor world,
            double x,
            double y,
            double z,
            Entity entity
    ) {

        return normalizeHigurumaBlackFlashCnt6(data, key, entity);
    }

    @Redirect(
            method = "execute",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/nbt/CompoundTag;getDouble(Ljava/lang/String;)D",
                    ordinal = 11
            )
    )
    private static double jjkfys$useNeutralHigurumaCnt6ForBlackFlashRolls(
            CompoundTag data,
            String key,
            LevelAccessor world,
            double x,
            double y,
            double z,
            Entity entity
    ) {

        return normalizeHigurumaBlackFlashCnt6(data, key, entity);
    }

    private static double normalizeHigurumaBlackFlashCnt6(CompoundTag data, String key, Entity entity) {
        double value = data.getDouble(key);

        if (!"cnt6".equals(key) || value >= 0.0D || !isHiguruma(entity)) {
            return value;
        }

        return 0.0D;
    }

    private static boolean isHiguruma(Entity entity) {
        if (entity == null) {
            return false;
        }

        JujutsucraftModVariables.PlayerVariables vars =
                entity.getCapability(
                        JujutsucraftModVariables.PLAYER_VARIABLES_CAPABILITY,
                        null
                ).orElse(new JujutsucraftModVariables.PlayerVariables());

        return vars.PlayerCurseTechnique == HIGURUMA_TECHNIQUE
                || vars.PlayerCurseTechnique2 == HIGURUMA_TECHNIQUE;
    }

    private static boolean hasGavel(Entity entity) {
        if (!(entity instanceof LivingEntity living)) {
            return false;
        }

        return isGavel(living.getMainHandItem().getItem())
                || isGavel(living.getOffhandItem().getItem());
    }

    private static boolean isGavel(net.minecraft.world.item.Item item) {
        return item == JujutsucraftModItems.GAVEL.get()
                || item == JujutsucraftModItems.GAVEL_LONG.get()
                || item == JujutsucraftModItems.GAVEL_BIG.get();
    }
}
