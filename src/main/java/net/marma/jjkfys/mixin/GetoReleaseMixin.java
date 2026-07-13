package net.marma.jjkfys.mixin;

import com.jujutsu.jujutsucraftaddon.procedures.GetoReleaseProcedure;

import net.mcreator.jujutsucraft.entity.DagonEntity;
import net.mcreator.jujutsucraft.entity.HanamiEntity;
import net.mcreator.jujutsucraft.entity.JogoEntity;
import net.mcreator.jujutsucraft.entity.MahitoEntity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.LevelAccessor;

import net.minecraft.server.level.ServerLevel;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GetoReleaseProcedure.class)
public class GetoReleaseMixin {

    @Inject(
            method = "execute",
            at = @At("TAIL"),
            remap = false
    )
    private static void jjkfys$fixUltimateHP(
            LevelAccessor world,
            double x,
            double y,
            double z,
            net.minecraft.world.entity.Entity entity,
            CallbackInfo ci
    ) {

        if (!(world instanceof ServerLevel level)) return;

        for (LivingEntity mob : level.getEntitiesOfClass(
                LivingEntity.class,
                new net.minecraft.world.phys.AABB(
                        x - 20, y - 20, z - 20,
                        x + 20, y + 20, z + 20
                )
        )) {

            boolean valid =
                    mob instanceof JogoEntity
                            || mob instanceof MahitoEntity
                            || mob instanceof DagonEntity
                            || mob instanceof HanamiEntity;

            if (!valid) continue;

            if (mob.getAttribute(Attributes.MAX_HEALTH) == null) continue;

            if (mob.getMaxHealth() < 2000.0F) continue;

            mob.getAttribute(Attributes.MAX_HEALTH)
                    .setBaseValue(750.0D);

            mob.setHealth(750.0F);
        }
    }
}