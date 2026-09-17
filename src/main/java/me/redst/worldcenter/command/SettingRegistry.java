package me.redst.worldcenter.command;

import me.redst.worldcenter.config.BooleanSetting;
import me.redst.worldcenter.config.NumericSetting;
import me.redst.worldcenter.config.Setting;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SettingRegistry {
    public static final String WORLDS = "worlds";
    public static final String WORLD_ENABLED = "enabled";
    public static final String WORLD_DESTINATION = "destination";

    public static final String MAIN_SWITCH = "traveling enabled";

    private static final Map<String, Setting> SETTINGS = new LinkedHashMap<>();

    static {
        SETTINGS.put("traveling force-ai enabled", BooleanSetting.FORCE_AI);
        SETTINGS.put("traveling stop-distance-chunks", NumericSetting.STOP_DISTANCE_CHUNKS);
        SETTINGS.put("traveling minimum-y", NumericSetting.MINIMUM_Y);
        SETTINGS.put("traveling ignore-teamed", BooleanSetting.IGNORE_TEAMED);
        SETTINGS.put("traveling ignore-owned", BooleanSetting.IGNORE_OWNED);
        SETTINGS.put("traveling processing interval", NumericSetting.PROCESSING_INTERVAL);
        SETTINGS.put("traveling processing random-delay", BooleanSetting.RANDOM_DELAY);
        SETTINGS.put("traveling pathfinding update-interval", NumericSetting.UPDATE_INTERVAL);
        SETTINGS.put("traveling pathfinding retry-delay", NumericSetting.RETRY_DELAY);
    }

    private SettingRegistry() {
    }

    public static Setting get(String key) {
        return SETTINGS.get(key);
    }

    public static List<String> nextWords(String prefix) {
        String search = prefix.isEmpty() ? "" : prefix + " ";
        List<String> words = new ArrayList<>();
        for (String key : SETTINGS.keySet()) {
            if (!key.startsWith(search)) {
                continue;
            }
            String rest = key.substring(search.length());
            int space = rest.indexOf(' ');
            String word = space < 0 ? rest : rest.substring(0, space);
            if (!word.isEmpty() && !words.contains(word)) {
                words.add(word);
            }
        }
        return words;
    }
}
