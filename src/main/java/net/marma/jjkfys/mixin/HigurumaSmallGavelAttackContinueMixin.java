package net.marma.jjkfys.mixin;

import net.mcreator.jujutsucraft.init.JujutsucraftModItems;
import net.mcreator.jujutsucraft.network.JujutsucraftModVariables;
import net.mcreator.jujutsucraft.procedures.AttackContinueProcedure;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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

        LivingEntity living = (LivingEntity) entity;
        ItemStack mainHand = living.getMainHandItem().copy();
        ItemStack offHand = living.getOffhandItem().copy();
        entity.getPersistentData().putBoolean(BYPASS_KEY, true);

        try {
            living.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            living.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
            AttackContinueProcedure.execute(world, x, y, z, entity);
        } finally {
            living.setItemInHand(InteractionHand.MAIN_HAND, mainHand);
            living.setItemInHand(InteractionHand.OFF_HAND, offHand);

            if (living instanceof Player player) {
                player.getInventory().setChanged();
            }

            entity.getPersistentData().remove(BYPASS_KEY);
        }

        ci.cancel();
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
