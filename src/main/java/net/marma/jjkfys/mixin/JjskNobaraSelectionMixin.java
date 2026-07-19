package net.marma.jjkfys.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "net.mcreator.jujutsucrafts.procedures.NobaraProcedure", remap = false)
public class JjskNobaraSelectionMixin {

    private static final int RESONANCE_SLOT = 15;
    private static final double RESONANCE_COST = 50.0;
    private static final String GRADE_PROCEDURE = "net.mcreator.jujutsucrafts.procedures.GradeProcedure";
    private static final String TECHNIQUE_PROCEDURE = "net.mcreator.jujutsucrafts.procedures.TechniqueProcedure";

    @Inject(
            method = "execute",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void jjkfys$restoreResonanceSelection(
            Player player,
            int selected,
            CallbackInfoReturnable<Boolean> cir
    ) {

        if (player == null || selected != RESONANCE_SLOT) {
            return;
        }

        if (grade(player) >= 2) {
            cir.setReturnValue(setInfo(player, selected));
        }
    }

    private static int grade(Player player) {

        try {
            Object value = Class.forName(GRADE_PROCEDURE)
                    .getMethod("execute", Entity.class)
                    .invoke(null, player);
            return value instanceof Number number ? number.intValue() : 0;
        } catch (ReflectiveOperationException ignored) {
            return 0;
        }
    }

    private static boolean setInfo(Player player, int selected) {

        try {
            Object value = Class.forName(TECHNIQUE_PROCEDURE)
                    .getMethod("setInfo", Player.class, int.class, String.class, double.class, boolean.class, boolean.class)
                    .invoke(
                            null,
                            player,
                            selected,
                            Component.translatable("jujutsu.technique.kugisaki3").getString(),
                            RESONANCE_COST,
                            false,
                            false
                    );
            return value instanceof Boolean result && result;
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }
}
