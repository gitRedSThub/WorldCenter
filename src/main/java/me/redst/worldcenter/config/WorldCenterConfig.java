package me.redst.worldcenter.config;

import me.redst.worldcenter.util.Numbers;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class WorldCenterConfig {
    public static final String PATH_TRAVELING_ENABLED = "traveling.enabled";
    public static final String PATH_WORLDS = "worlds";

    public static final boolean DEFAULT_TRAVELING_ENABLED = true;

    private static final double BLOCKS_PER_CHUNK = 16.0D;

    private final Plugin plugin;
    private final BlacklistManager blacklist = new BlacklistManager();

    private boolean travelingEnabled = DEFAULT_TRAVELING_ENABLED;
    private boolean forceAi = BooleanSetting.FORCE_AI.defaultValue();
    private boolean ignoreTeamed = BooleanSetting.IGNORE_TEAMED.defaultValue();
    private boolean ignoreOwned = BooleanSetting.IGNORE_OWNED.defaultValue();
    private boolean randomDelay = BooleanSetting.RANDOM_DELAY.defaultValue();

    private double stopDistanceChunks = NumericSetting.STOP_DISTANCE_CHUNKS.defaultValue();
    private double stopDistanceSquared;
    private double minimumY = NumericSetting.MINIMUM_Y.defaultValue();

    private double processingInterval = NumericSetting.PROCESSING_INTERVAL.defaultValue();
    private int processingIntervalTicks;
    private double updateInterval = NumericSetting.UPDATE_INTERVAL.defaultValue();
    private int updateIntervalTicks;
    private double retryDelay = NumericSetting.RETRY_DELAY.defaultValue();
    private int retryDelayTicks;

    private Map<String, WorldSettings> worlds = Map.of();

    public WorldCenterConfig(Plugin plugin) {
        this.plugin = plugin;
    }

    public int load(boolean fromDisk) {
        return load(fromDisk, false);
    }

    public void refresh() {
        load(false, true);
    }

    private int load(boolean fromDisk, boolean quiet) {
        if (fromDisk) {
            this.plugin.reloadConfig();
        }
        FileConfiguration config = this.plugin.getConfig();
        ConfigValidator validator = new ConfigValidator(quiet ? null : this.plugin.getLogger());

        this.travelingEnabled = validator.bool(config, PATH_TRAVELING_ENABLED, DEFAULT_TRAVELING_ENABLED);
        this.forceAi = validator.bool(config, BooleanSetting.FORCE_AI.path(), BooleanSetting.FORCE_AI.defaultValue());
        this.ignoreTeamed = validator.bool(config, BooleanSetting.IGNORE_TEAMED.path(),
                BooleanSetting.IGNORE_TEAMED.defaultValue());
        this.ignoreOwned = validator.bool(config, BooleanSetting.IGNORE_OWNED.path(),
                BooleanSetting.IGNORE_OWNED.defaultValue());
        this.randomDelay = validator.bool(config, BooleanSetting.RANDOM_DELAY.path(),
                BooleanSetting.RANDOM_DELAY.defaultValue());

        this.stopDistanceChunks = validator.number(config, NumericSetting.STOP_DISTANCE_CHUNKS.path(),
                NumericSetting.STOP_DISTANCE_CHUNKS);
        double stopDistanceBlocks = this.stopDistanceChunks * BLOCKS_PER_CHUNK;
        this.stopDistanceSquared = stopDistanceBlocks * stopDistanceBlocks;

        this.minimumY = validator.number(config, NumericSetting.MINIMUM_Y.path(), NumericSetting.MINIMUM_Y);

        this.processingInterval = validator.number(config, NumericSetting.PROCESSING_INTERVAL.path(),
                NumericSetting.PROCESSING_INTERVAL);
        this.processingIntervalTicks = Numbers.toTicks(this.processingInterval);

        this.updateInterval = validator.number(config, NumericSetting.UPDATE_INTERVAL.path(),
                NumericSetting.UPDATE_INTERVAL);
        this.updateIntervalTicks = Numbers.toTicks(this.updateInterval);

        this.retryDelay = validator.number(config, NumericSetting.RETRY_DELAY.path(), NumericSetting.RETRY_DELAY);
        this.retryDelayTicks = Numbers.toTicks(this.retryDelay);

        this.blacklist.load(config, validator);
        this.worlds = loadWorlds(config, validator);

        if (validator.isFileChanged()) {
            this.plugin.saveConfig();
        }
        return validator.getInvalidValues();
    }

    private Map<String, WorldSettings> loadWorlds(FileConfiguration config, ConfigValidator validator) {
        Object raw = config.get(PATH_WORLDS, null);
        if (!(raw instanceof ConfigurationSection)) {
            if (raw != null) {
                validator.report("Invalid value for " + PATH_WORLDS
                        + ": it must be a list of worlds. Reset to the defaults.");
            }
            writeDefaultWorlds(config);
            validator.markFileChanged();
            raw = config.get(PATH_WORLDS, null);
        }
        ConfigurationSection section = (ConfigurationSection) raw;
        Set<String> names = section.getKeys(false);
        Map<String, WorldSettings> loaded = new HashMap<>(Math.max(4, names.size() * 2));
        for (String name : names) {
            String base = PATH_WORLDS + "." + name;
            if (!(config.get(base, null) instanceof ConfigurationSection)) {
                validator.report("Invalid settings for world " + name + ": reset to the defaults.");
                config.set(base + ".enabled", BooleanSetting.WORLD_ENABLED.defaultValue());
                config.set(base + ".destination.x", NumericSetting.DESTINATION_X.defaultValue());
                config.set(base + ".destination.z", NumericSetting.DESTINATION_Z.defaultValue());
                validator.markFileChanged();
            }
            boolean enabled = validator.bool(config, base + ".enabled",
                    BooleanSetting.WORLD_ENABLED.defaultValue());
            double x = validator.number(config, base + ".destination.x", NumericSetting.DESTINATION_X);
            double z = validator.number(config, base + ".destination.z", NumericSetting.DESTINATION_Z);
            loaded.put(name, new WorldSettings(enabled, x, z));
        }
        return Map.copyOf(loaded);
    }

    private void writeDefaultWorlds(FileConfiguration config) {
        config.set(PATH_WORLDS, null);
        writeDefaultWorld(config, "world", true);
        writeDefaultWorld(config, "world_nether", true);
        writeDefaultWorld(config, "world_the_end", false);
    }

    private void writeDefaultWorld(FileConfiguration config, String name, boolean enabled) {
        String base = PATH_WORLDS + "." + name;
        config.set(base + ".enabled", enabled);
        config.set(base + ".destination.x", NumericSetting.DESTINATION_X.defaultValue());
        config.set(base + ".destination.z", NumericSetting.DESTINATION_Z.defaultValue());
    }

    public WorldSettings getWorldSettings(String worldName) {
        WorldSettings settings = this.worlds.get(worldName);
        return settings == null ? WorldSettings.UNKNOWN : settings;
    }

    public Map<String, WorldSettings> getWorlds() {
        Map<String, WorldSettings> ordered = new LinkedHashMap<>();
        this.worlds.keySet().stream().sorted().forEach(name -> ordered.put(name, this.worlds.get(name)));
        return ordered;
    }

    public BlacklistManager getBlacklist() {
        return this.blacklist;
    }

    public boolean isTravelingEnabled() {
        return this.travelingEnabled;
    }

    public boolean isForceAi() {
        return this.forceAi;
    }

    public boolean isIgnoreTeamed() {
        return this.ignoreTeamed;
    }

    public boolean isIgnoreOwned() {
        return this.ignoreOwned;
    }

    public boolean isRandomDelay() {
        return this.randomDelay;
    }

    public double getStopDistanceChunks() {
        return this.stopDistanceChunks;
    }

    public double getStopDistanceSquared() {
        return this.stopDistanceSquared;
    }

    public double getMinimumY() {
        return this.minimumY;
    }

    public double getProcessingInterval() {
        return this.processingInterval;
    }

    public int getProcessingIntervalTicks() {
        return this.processingIntervalTicks;
    }

    public double getUpdateInterval() {
        return this.updateInterval;
    }

    public int getUpdateIntervalTicks() {
        return this.updateIntervalTicks;
    }

    public double getRetryDelay() {
        return this.retryDelay;
    }

    public int getRetryDelayTicks() {
        return this.retryDelayTicks;
    }
}
