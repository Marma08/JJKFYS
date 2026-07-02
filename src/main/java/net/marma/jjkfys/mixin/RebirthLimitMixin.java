package net.marma.jjkfys.mixin;

import com.jujutsu.jujutsucraftaddon.network.JujutsucraftaddonModVariables;
import com.jujutsu.jujutsucraftaddon.util.RebirthManager;

import net.marma.jjkfys.init.JJKFysGameRules;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RebirthManager.class)
public class RebirthLimitMixin {

    @Inject(
            method = "canRebirth",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void jjkfys$rebirthLimit(
            ServerPlayer player,
            CallbackInfoReturnable<Boolean> cir
    ) {

        if (player == null) return;

        int limit = player.level()
                .getGameRules()
                .getInt(JJKFysGameRules.MAX_REBIRTHS);

        int rebirths = player.getCapability(
                JujutsucraftaddonModVariables.PLAYER_VARIABLES_CAPABILITY,
                Direction.DOWN
        ).orElse(
                new JujutsucraftaddonModVariables.PlayerVariables()
        ).rebirthLevel;
        
        if (rebirths >= limit) {

            cir.setReturnValue(false);
        }
    }
}