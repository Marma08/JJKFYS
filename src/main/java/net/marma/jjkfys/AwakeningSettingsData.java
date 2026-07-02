package net.marma.jjkfys;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

public class AwakeningSettingsData extends SavedData {

    private static final String DATA_NAME =
            "jjkfys_awakening_settings";

    private static final String DURATION_TAG =
            "duration";

    private static final String POWER_TAG =
            "power";

    private static final String OUTPUT_TAG =
            "output";

    private static final String ONE_TWENTY_DURATION_TAG =
            "one_twenty_duration";

    private static final String ONE_TWENTY_POWER_TAG =
            "one_twenty_power";

    private final Map<Integer, Integer> durationByCt =
            new HashMap<>();

    private final Map<Integer, Integer> powerByCt =
            new HashMap<>();

    private final Map<Integer, Integer> outputByCt =
            new HashMap<>();

    private final Map<Integer, Integer> oneTwentyDurationByCt =
            new HashMap<>();

    private final Map<Integer, Integer> oneTwentyPowerByCt =
            new HashMap<>();

    public static AwakeningSettingsData get(
            MinecraftServer server
    ) {

        return server.overworld()
                .getDataStorage()
                .computeIfAbsent(
                        AwakeningSettingsData::load,
                        AwakeningSettingsData::new,
                        DATA_NAME
                );
    }

    public static AwakeningSettingsData load(
            CompoundTag tag
    ) {

        AwakeningSettingsData data =
                new AwakeningSettingsData();

        data.readMap(
                tag.getCompound(
                        DURATION_TAG
                ),
                data.durationByCt
        );

        data.readMap(
                tag.getCompound(
                        POWER_TAG
                ),
                data.powerByCt
        );

        data.readMap(
                tag.getCompound(
                        OUTPUT_TAG
                ),
                data.outputByCt
        );

        data.readMap(
                tag.getCompound(
                        ONE_TWENTY_DURATION_TAG
                ),
                data.oneTwentyDurationByCt
        );

        data.readMap(
                tag.getCompound(
                        ONE_TWENTY_POWER_TAG
                ),
                data.oneTwentyPowerByCt
        );

        return data;
    }

    @Override
    public CompoundTag save(
            CompoundTag tag
    ) {

        tag.put(
                DURATION_TAG,
                writeMap(
                        durationByCt
                )
        );

        tag.put(
                POWER_TAG,
                writeMap(
                        powerByCt
                )
        );

        tag.put(
                OUTPUT_TAG,
                writeMap(
                        outputByCt
                )
        );

        tag.put(
                ONE_TWENTY_DURATION_TAG,
                writeMap(
                        oneTwentyDurationByCt
                )
        );

        tag.put(
                ONE_TWENTY_POWER_TAG,
                writeMap(
                        oneTwentyPowerByCt
                )
        );

        return tag;
    }

    public int getDuration(
            int ctId,
            int fallback
    ) {

        return durationByCt.getOrDefault(
                ctId,
                fallback
        );
    }

    public int getPower(
            int ctId,
            int fallback
    ) {

        return powerByCt.getOrDefault(
                ctId,
                fallback
        );
    }

    public int getOutput(
            int ctId,
            int fallback
    ) {

        int value =
                outputByCt.getOrDefault(
                ctId,
                fallback
        );

        return value < -1
                ? fallback
                : value;
    }

    public int getOneTwentyDuration(
            int ctId,
            int fallback
    ) {

        return oneTwentyDurationByCt.getOrDefault(
                ctId,
                fallback
        );
    }

    public int getOneTwentyPower(
            int ctId,
            int fallback
    ) {

        return oneTwentyPowerByCt.getOrDefault(
                ctId,
                fallback
        );
    }

    public void setDuration(
            int ctId,
            int value
    ) {

        setValue(
                durationByCt,
                ctId,
                value
        );
    }

    public void setPower(
            int ctId,
            int value
    ) {

        setValue(
                powerByCt,
                ctId,
                value
        );
    }

    public void setOutput(
            int ctId,
            int value
    ) {

        setValue(
                outputByCt,
                ctId,
                value
        );
    }

    public void setOneTwentyDuration(
            int ctId,
            int value
    ) {

        setValue(
                oneTwentyDurationByCt,
                ctId,
                value
        );
    }

    public void setOneTwentyPower(
            int ctId,
            int value
    ) {

        setValue(
                oneTwentyPowerByCt,
                ctId,
                value
        );
    }

    private void setValue(
            Map<Integer, Integer> map,
            int ctId,
            int value
    ) {

        if (value < 0) {
            map.remove(
                    ctId
            );
        } else {
            map.put(
                    ctId,
                    value
            );
        }

        setDirty();
    }

    private void readMap(
            CompoundTag tag,
            Map<Integer, Integer> map
    ) {

        for (String key : tag.getAllKeys()) {
            try {
                map.put(
                        Integer.parseInt(
                                key
                        ),
                        tag.getInt(
                                key
                        )
                );
            } catch (NumberFormatException ignored) {
                // Ignore stale or manually edited invalid entries.
            }
        }
    }

    private CompoundTag writeMap(
            Map<Integer, Integer> map
    ) {

        CompoundTag tag =
                new CompoundTag();

        map.forEach((ctId, value) -> tag.putInt(
                Integer.toString(
                        ctId
                ),
                value
        ));

        return tag;
    }
}
