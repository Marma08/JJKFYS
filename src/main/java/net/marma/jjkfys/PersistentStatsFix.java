package net.marma.jjkfys;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;

import com.jujutsu.jujutsucraftaddon.network.JujutsucraftaddonModVariables;

import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class PersistentStatsFix {

    private static final String SAVE_TAG = "jjkfys_save";

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {

        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        CompoundTag data = player.getPersistentData().getCompound(SAVE_TAG);

        player.getCapability(JujutsucraftaddonModVariables.PLAYER_VARIABLES_CAPABILITY).ifPresent(vars -> {

            if (data.isEmpty()) return;

            vars.HealthAttribute = data.getDouble("hp");
            vars.sp = data.getDouble("sp");
            vars.Level = data.getDouble("level");
            vars.skillTreeNodes = data.getString("nodes");
            vars.DamageAttribute = data.getDouble("damage");
            vars.ArmorAttribute = data.getDouble("armor");
            vars.SpeedValue = data.getDouble("speed");
            vars.CE = data.getDouble("ce");
            vars.CECap = data.getDouble("cecap");
            vars.rebirthLevel = data.getInt("rebirth");

            vars.syncPlayerVariables(player);

            if (player.getAttribute(Attributes.MAX_HEALTH) != null) {
                player.getAttribute(Attributes.MAX_HEALTH)
                        .setBaseValue(vars.HealthAttribute);
            }

            if (player.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
                player.getAttribute(Attributes.ATTACK_DAMAGE)
                        .setBaseValue(vars.DamageAttribute);
            }

            if (player.getAttribute(Attributes.ARMOR) != null) {
                player.getAttribute(Attributes.ARMOR)
                        .setBaseValue(vars.ArmorAttribute);
            }
        });
    }

    @SubscribeEvent
    public static void onSave(PlayerEvent.SaveToFile event) {

        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        player.getCapability(JujutsucraftaddonModVariables.PLAYER_VARIABLES_CAPABILITY).ifPresent(vars -> {

            CompoundTag data = new CompoundTag();

            data.putDouble("hp", vars.HealthAttribute);
            data.putDouble("sp", vars.sp);
            data.putDouble("level", vars.Level);
            data.putString("nodes", vars.skillTreeNodes);
            data.putDouble("damage", vars.DamageAttribute);
            data.putDouble("armor", vars.ArmorAttribute);
            data.putDouble("speed", vars.SpeedValue);
            data.putDouble("ce", vars.CE);
            data.putDouble("cecap", vars.CECap);
            data.putInt("rebirth", vars.rebirthLevel);

            player.getPersistentData().put(SAVE_TAG, data);
        });
    }
}