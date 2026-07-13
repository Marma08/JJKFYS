package net.marma.jjkfys.mixin;

import net.mcreator.jujutsucraft.procedures.AttackWeakPunchProcedure;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AttackWeakPunchProcedure.class)
public class HigurumaSmallGavelBarrageKnockbackMixin {
    private static final String BYPASS_KEY = "jjkfys_higuruma_small_gavel_bypass";
    private static final String KNOCKBACK_KEY = "knockback";
    private static final double NORMAL_BARRAGE_KNOCKBACK_CAP = 0.25D;

    @Redirect(
            method = "execute",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/nbt/CompoundTag;putDouble(Ljava/lang/String;D)V"
            )
    )
    private static void jjkfys$limitSmallGavelBarrageKnockback(
            CompoundTag tag,
            String key,
            double value,
            LevelAccessor world,
            double x,
            double y,
            double z,
            Entity entity
    ) {
        if (KNOCKBACK_KEY.equals(key)
                && entity != null
                && entity.getPersistentData().getBoolean(BYPASS_KEY)) {
            tag.putDouble(key, Math.min(value, NORMAL_BARRAGE_KNOCKBACK_CAP));
            return;
        }

        tag.putDouble(key, value);
    }
}
