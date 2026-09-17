package me.redst.worldcenter.config;

import me.redst.worldcenter.util.Numbers;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.logging.Logger;

public final class ConfigValidator {
    private final Logger logger;

    private int invalidValues;
    private boolean fileChanged;

    public ConfigValidator(Logger logger) {
        this.logger = logger;
    }

    public int getInvalidValues() {
        return this.invalidValues;
    }

    public boolean isFileChanged() {
        return this.fileChanged;
    }

    public void markFileChanged() {
        this.fileChanged = true;
    }

    public void report(String message) {
        this.invalidValues++;
        if (this.logger != null) {
            this.logger.warning(message);
        }
    }

    public boolean bool(FileConfiguration config, String path, boolean defaultValue) {
        Object raw = config.get(path, null);
        if (raw == null) {
            restore(config, path, defaultValue);
            return defaultValue;
        }
        if (!(raw instanceof Boolean value)) {
            reset(config, path, defaultValue, "it must be true or false");
            return defaultValue;
        }
        return value;
    }

    public double number(FileConfiguration config, String path, NumericSetting setting) {
        double defaultValue = setting.defaultValue();
        double minimum = setting.minimum();
        double maximum = setting.maximum();
        Object raw = config.get(path, null);
        if (raw == null) {
            restore(config, path, defaultValue);
            return defaultValue;
        }
        if (!(raw instanceof Number number)) {
            reset(config, path, defaultValue, "it must be a number");
            return defaultValue;
        }
        double value = number.doubleValue();
        if (!Double.isFinite(value)) {
            reset(config, path, defaultValue, "it must be a number");
            return defaultValue;
        }
        if (!Numbers.hasAllowedPrecision(value)) {
            reset(config, path, defaultValue, "it may use at most " + Numbers.MAX_DECIMALS + " decimal places");
            return defaultValue;
        }
        if (value < minimum || value > maximum) {
            reset(config, path, defaultValue,
                    "it must be between " + Numbers.format(minimum) + " and " + Numbers.format(maximum));
            return defaultValue;
        }
        return value;
    }

    private void restore(FileConfiguration config, String path, Object defaultValue) {
        config.set(path, defaultValue);
        this.fileChanged = true;
    }

    private void reset(FileConfiguration config, String path, Object defaultValue, String requirement) {
        String shown = defaultValue instanceof Double value ? Numbers.format(value) : String.valueOf(defaultValue);
        report("Invalid value for " + path + ": " + requirement + ". Reset to " + shown + ".");
        config.set(path, defaultValue);
        this.fileChanged = true;
    }
}
