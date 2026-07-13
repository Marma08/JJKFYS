package net.marma.jjkfys;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;

import com.jujutsu.jujutsucraftaddon.init.JujutsucraftaddonModGameRules;
import com.jujutsu.jujutsucraftaddon.network.JujutsucraftaddonModVariables;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.UUID;

@Mod.EventBusSubscriber
public class HpCapHandler {

    private static final HashMap<UUID, Integer> tickDelay = new HashMap<>();
    private static final String SAVE_TAG = "jjkfys_save";

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {

        if (event.phase != TickEvent.Phase.END) return;

        Player player = event.player;

        if (!(player.level() instanceof ServerLevel level)) return;

        UUID id = player.getUUID();
        int ticks = tickDelay.getOrDefault(id, 0);

        if (ticks < 40) {
            tickDelay.put(id, ticks + 1);
            return;
        }

        player.getCapability(JujutsucraftaddonModVariables.PLAYER_VARIABLES_CAPABILITY).ifPresent(vars -> {

            vars.HPCap = level.getGameRules()
                    .getInt(JujutsucraftaddonModGameRules.JJKU_HP_CAP);

            CompoundTag data = player.getPersistentData().getCompound(SAVE_TAG);

            data.putDouble("hp", vars.HealthAttribute);
            data.putDouble("sp", vars.sp);
            data.putDouble("level", vars.Level);
            data.putDouble("rebirth", vars.rebirthLevel);
            data.putString("nodes", vars.skillTreeNodes);

            data.putDouble("damage", vars.DamageAttribute);
            data.putDouble("armor", vars.ArmorAttribute);
            data.putDouble("speed", vars.SpeedValue);
            data.putDouble("ce", vars.CE);
            data.putDouble("cecap", vars.CECap);
            data.putDouble("cursedlevel", vars.CursedLevel);

            player.getPersistentData().put(SAVE_TAG, data);

            vars.syncPlayerVariables(player);
        });
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {

        if (!event.isWasDeath()) return;

        Player newPlayer = event.getEntity();
        Player oldPlayer = event.getOriginal();

        CompoundTag oldPersist = oldPlayer.getPersistentData();
        newPlayer.getPersistentData().merge(oldPersist.copy());

        CompoundTag data = oldPersist.getCompound(SAVE_TAG);

        newPlayer.getCapability(JujutsucraftaddonModVariables.PLAYER_VARIABLES_CAPABILITY).ifPresent(vars -> {

            vars.HealthAttribute = data.getDouble("hp");
            vars.sp = data.getDouble("sp");
            vars.Level = data.getDouble("level");
            vars.rebirthLevel = data.getInt("rebirth");
            vars.skillTreeNodes = data.getString("nodes");

            vars.DamageAttribute = data.getDouble("damage");
            vars.ArmorAttribute = data.getDouble("armor");
            vars.SpeedValue = data.getDouble("speed");
            vars.CE = data.getDouble("ce");
            vars.CECap = data.getDouble("cecap");
            vars.CursedLevel = data.getDouble("cursedlevel");

            vars.syncPlayerVariables(newPlayer);
        });
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {

        Player player = event.getEntity();

        CompoundTag data = player.getPersistentData().getCompound(SAVE_TAG);

        player.getCapability(JujutsucraftaddonModVariables.PLAYER_VARIABLES_CAPABILITY).ifPresent(vars -> {

            vars.DamageAttribute = data.getDouble("damage");
            vars.ArmorAttribute = data.getDouble("armor");
            vars.CursedLevel = data.getDouble("cursedlevel");

            vars.syncPlayerVariables(player);
        });
    }
}