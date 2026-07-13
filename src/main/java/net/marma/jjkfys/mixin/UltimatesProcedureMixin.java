package net.marma.jjkfys.mixin;

import com.jujutsu.jujutsucraftaddon.network.JujutsucraftaddonModVariables;
import com.jujutsu.jujutsucraftaddon.procedures.UltimatesProcedure;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(UltimatesProcedure.class)
public class UltimatesProcedureMixin {

    @Redirect(
            method = "execute",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/jujutsu/jujutsucraftaddon/util/AdvancementSafety;isDone(Lnet/minecraft/world/entity/Entity;Ljava/lang/String;)Z"
            ),
            remap = false
    )
    private static boolean jjkfys$rebirthUnlock(
            Entity entity,
            String advancement
    ) {

        if (advancement.equals("jujutsucraftaddon:grade_ryomen_sage")) {

            return entity.getCapability(
                    JujutsucraftaddonModVariables.PLAYER_VARIABLES_CAPABILITY,
                    Direction.DOWN
            ).orElse(
                    new JujutsucraftaddonModVariables.PlayerVariables()
            ).rebirthLevel >= 3;
        }

        return false;
    }
}