package net.marma.jjkfys.mixin;

import net.mcreator.jujutsucraft.init.JujutsucraftModItems;
import net.mcreator.jujutsucraft.network.JujutsucraftModVariables;
import net.mcreator.jujutsucraft.procedures.AttackContinueProcedure;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AttackContinueProcedure.class)
public class HigurumaSmallGavelAttackContinueMixin {
    private static final double HIGURUMA_TECHNIQUE = 27.0D;
    private static final String BYPASS_KEY = "jjkfys_higuruma_small_gavel_bypass";

    @Inject(method = "execute", at = @At("HEAD"), remap = false, cancellable = true)
    private static void jjkfys$useGenericBarrageForSmallGavel(
            LevelAccessor world,
            double x,
            double y,
            double z,
            Entity entity,
            CallbackInfo ci
    ) {
        if (!shouldUseGenericPath(entity)) {
            return;
        }

        entity.getPersistentData().putBoolean(BYPASS_KEY, true);
    }

    @Inject(method = "execute", at = @At("RETURN"), remap = false)
    private static void jjkfys$clearSmallGavelBarrageMarker(
            LevelAccessor world,
            double x,
            double y,
            double z,
            Entity entity,
            CallbackInfo ci
    ) {
        if (entity != null) {
            entity.getPersistentData().remove(BYPASS_KEY);
        }
    }

    @Redirect(
            method = "execute",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;onGround()Z"
            )
    )
    private static boolean jjkfys$skipSmallGavelOverheadBranch(
            Entity checked,
            LevelAccessor world,
            double x,
            double y,
            double z,
            Entity entity
    ) {
        if (checked == entity
                && entity != null
                && entity.getPersistentData().getBoolean(BYPASS_KEY)) {
            return false;
        }

        return checked.onGround();
    }

    private static boolean shouldUseGenericPath(Entity entity) {
        if (!(entity instanceof LivingEntity living)) {
            return false;
        }

        if (entity.getPersistentData().getBoolean(BYPASS_KEY)) {
            return false;
        }

        ItemStack mainHand = living.getMainHandItem();
        ItemStack offHand = living.getOffhandItem();
        boolean hasSmallGavel = mainHand.getItem() == JujutsucraftModItems.GAVEL.get()
                || offHand.getItem() == JujutsucraftModItems.GAVEL.get();
        if (!hasSmallGavel) {
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
