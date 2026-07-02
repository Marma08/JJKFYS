package net.marma.jjkfys.mixin;

import com.jujutsu.jujutsucraftaddon.procedures.BrainEffectTwoProcedure;
import net.marma.jjkfys.init.JJKFysGameRules;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BrainEffectTwoProcedure.class)
public class JjkuBrainDamageTimingMixin {
    private static final ThreadLocal<LevelAccessor> JJKFYS_WORLD = new ThreadLocal<>();

    @Inject(method = "execute", at = @At("HEAD"), remap = false)
    private static void jjkfys$captureWorld(LevelAccessor world, Entity entity, CallbackInfo ci) {
        JJKFYS_WORLD.set(world);
    }

    @Inject(method = "execute", at = @At("RETURN"), remap = false)
    private static void jjkfys$clearWorld(LevelAccessor world, Entity entity, CallbackInfo ci) {
        JJKFYS_WORLD.remove();
    }

    @ModifyConstant(method = "execute", constant = @Constant(intValue = 6000), remap = false)
    private static int jjkfys$brainDamageDuration(int original) {
        LevelAccessor world = JJKFYS_WORLD.get();
        if (world == null) {
            return original;
        }
        return Math.max(0, world.getLevelData().getGameRules().getInt(JJKFysGameRules.GOJO_BRAIN_DAMAGE_DURATION_TICKS));
    }
}
