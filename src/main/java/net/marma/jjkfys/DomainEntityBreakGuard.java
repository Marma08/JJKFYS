package net.marma.jjkfys;

import net.mcreator.jujutsucraft.entity.DomainExpansionEntityEntity;
import net.mcreator.jujutsucraft.init.JujutsucraftModMobEffects;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class DomainEntityBreakGuard {

    private static final double CENTER_TOLERANCE = 2.0D;
    private static final double VERTICAL_TOLERANCE = 8.0D;
    private static final double OWNER_SEARCH_RADIUS = 192.0D;

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {

        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        if (!(event.level instanceof ServerLevel level)) {
            return;
        }

        for (Entity entity : level.getAllEntities()) {
            if (!(entity instanceof DomainExpansionEntityEntity domainEntity)) {
                continue;
            }

            keepActiveDomainEntityAlive(level, domainEntity);
        }
    }

    private static void keepActiveDomainEntityAlive(
            ServerLevel level,
            DomainExpansionEntityEntity domainEntity
    ) {

        CompoundTag domainData = domainEntity.getPersistentData();

        Vec3 center = new Vec3(
                domainData.contains("x_pos")
                        ? domainData.getDouble("x_pos")
                        : domainEntity.getX(),
                domainData.contains("y_pos")
                        ? domainData.getDouble("y_pos")
                        : domainEntity.getY(),
                domainData.contains("z_pos")
                        ? domainData.getDouble("z_pos")
                        : domainEntity.getZ()
        );

        if (!hasActiveOwner(level, center)) {
            return;
        }

        domainData.putBoolean("Break", false);
        domainData.putDouble("cnt_break", 0);
        domainData.putDouble("cnt_life2", 0);
    }

    private static boolean hasActiveOwner(
            ServerLevel level,
            Vec3 center
    ) {

        AABB searchArea =
                new AABB(center, center)
                        .inflate(OWNER_SEARCH_RADIUS);

        return !level.getEntitiesOfClass(
                LivingEntity.class,
                searchArea,
                living -> isActiveDomainOwner(living, center)
        ).isEmpty();
    }

    private static boolean isActiveDomainOwner(
            LivingEntity living,
            Vec3 center
    ) {

        if (!living.hasEffect(
                JujutsucraftModMobEffects
                        .DOMAIN_EXPANSION
                        .get()
        )) {
            return false;
        }

        CompoundTag data = living.getPersistentData();

        if (data.getBoolean("DomainDefeated")) {
            return false;
        }

        if (data.getBoolean("Failed")
                && !data.getBoolean("Cover")) {
            return false;
        }

        if (!data.contains("x_pos_doma")
                || !data.contains("y_pos_doma")
                || !data.contains("z_pos_doma")) {
            return false;
        }

        return closeEnough(
                data.getDouble("x_pos_doma"),
                center.x,
                CENTER_TOLERANCE
        ) && closeEnough(
                data.getDouble("y_pos_doma"),
                center.y,
                VERTICAL_TOLERANCE
        ) && closeEnough(
                data.getDouble("z_pos_doma"),
                center.z,
                CENTER_TOLERANCE
        );
    }

    private static boolean closeEnough(
            double first,
            double second,
            double tolerance
    ) {

        return Math.abs(first - second)
                <= tolerance;
    }
}
