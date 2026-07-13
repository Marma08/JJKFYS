package net.marma.jjkfys.mixin;

import net.mcreator.jujutsucraft.procedures.AITrueSphereProcedure;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AITrueSphereProcedure.class)
public class AITrueSphereMixin {

    @Inject(
            method = "execute",
            at = @At("HEAD"),
            remap = false
    )
    private static void jjkfys$instantAttack(
            LevelAccessor world,
            double x,
            double y,
            double z,
            Entity entity,
            CallbackInfo ci
    ) {

        if (entity == null) return;

        double ranged =
                entity.getPersistentData()
                        .getDouble("NameRanged_ranged");

        if (ranged == 0.0D) {

            if (world instanceof Level level) {

                for (Object obj : level.players()) {

                    if (!(obj instanceof Player)) continue;

                    Player p = (Player)obj;

                    double playerRanged =
                            p.getPersistentData()
                                    .getDouble("NameRanged");

                    if (playerRanged != 0.0D) {

                        entity.getPersistentData()
                                .putDouble(
                                        "NameRanged_ranged",
                                        playerRanged
                                );

                        ranged = playerRanged;

                        break;
                    }
                }
            }
        }

        if (world instanceof Level level) {

            for (Object obj : level.players()) {

                if (!(obj instanceof Player)) continue;

                Player p = (Player)obj;

                double playerRanged =
                        p.getPersistentData()
                                .getDouble("NameRanged");

                if (playerRanged != ranged) continue;

                boolean attack =
                        p.getPersistentData()
                                .getBoolean("attack");

                boolean wasAttacking =
                        entity.getPersistentData()
                                .getBoolean("jjkfys_attacking");

                if (attack && !wasAttacking) {

                    entity.getPersistentData()
                            .putDouble("cnt_x", 199.0D);

                    entity.getPersistentData()
                            .putBoolean(
                                    "jjkfys_attacking",
                                    true
                            );
                }

                if (!attack && wasAttacking) {

                    entity.getPersistentData()
                            .putBoolean(
                                    "jjkfys_attacking",
                                    false
                            );
                }

                break;
            }
        }
    }
}