package net.marma.jjkfys.mixin;

import net.mcreator.jujutsucraft.init.JujutsucraftModItems;
import net.mcreator.jujutsucraft.network.JujutsucraftModVariables;
import net.mcreator.jujutsucraft.procedures.AttackContinueProcedure;
import net.mcreator.jujutsucraft.procedures.CursedTechniqueHigurumaProcedure;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CursedTechniqueHigurumaProcedure.class)
public class HigurumaSmallGavelBarrageMixin {
    private static final long SMALL_GAVEL_BARRAGE_SKILL_1 = 2708L;
    private static final long SMALL_GAVEL_BARRAGE_SKILL_2 = 2709L;
    private static final double HIGURUMA_TECHNIQUE = 27.0D;

    @Inject(method = "execute", at = @At("HEAD"), remap = false, cancellable = true)
    private static void jjkfys$restoreLegacySmallGavelBarrage(
            LevelAccessor world,
            double x,
            double y,
            double z,
            Entity entity,
            CallbackInfo ci
    ) {
        if (!shouldUseLegacyBarrage(entity)) {
            return;
        }

        AttackContinueProcedure.execute(world, x, y, z, entity);
        ci.cancel();
    }

    private static boolean shouldUseLegacyBarrage(Entity entity) {
        if (!(entity instanceof LivingEntity living)) {
            return false;
        }

        long skill = Math.round(entity.getPersistentData().getDouble("skill"));
        if (skill != SMALL_GAVEL_BARRAGE_SKILL_1 && skill != SMALL_GAVEL_BARRAGE_SKILL_2) {
            return false;
        }

        ItemStack mainHand = living.getMainHandItem();
        if (mainHand.getItem() != JujutsucraftModItems.GAVEL.get()) {
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
