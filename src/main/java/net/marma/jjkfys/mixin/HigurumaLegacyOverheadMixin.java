package net.marma.jjkfys.mixin;

import net.mcreator.jujutsucraft.init.JujutsucraftModItems;
import net.mcreator.jujutsucraft.network.JujutsucraftModVariables;
import net.mcreator.jujutsucraft.procedures.AttackOverheadProcedure;
import net.mcreator.jujutsucraft.procedures.CursedTechniqueHigurumaProcedure;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CursedTechniqueHigurumaProcedure.class)
public class HigurumaLegacyOverheadMixin {
    private static final double HIGURUMA_TECHNIQUE = 27.0D;
    private static final long LEGACY_OVERHEAD_SLOT = 11L;

    @Inject(method = "execute", at = @At("HEAD"), remap = false, cancellable = true)
    private static void jjkfys$restoreLegacyOverhead(
            LevelAccessor world,
            double x,
            double y,
            double z,
            Entity entity,
            CallbackInfo ci
    ) {
        if (shouldUseLegacyOverhead(entity)) {
            AttackOverheadProcedure.execute(world, x, y, z, entity);
            ci.cancel();
        }
    }

    private static boolean shouldUseLegacyOverhead(Entity entity) {
        if (!(entity instanceof LivingEntity living) || !isHiguruma(entity)) {
            return false;
        }

        long slot = Math.round(entity.getPersistentData().getDouble("skill") - 2700.0D);
        return slot == LEGACY_OVERHEAD_SLOT
                && living.getMainHandItem().getItem() == JujutsucraftModItems.GAVEL_BIG.get();
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
}
