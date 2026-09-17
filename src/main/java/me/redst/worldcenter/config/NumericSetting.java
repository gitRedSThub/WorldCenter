package me.redst.worldcenter.config;

import me.redst.worldcenter.util.Numbers;

import java.util.List;

public enum NumericSetting implements Setting {
    STOP_DISTANCE_CHUNKS("traveling.stop-distance-chunks", "Stop Distance",
            2.00D, 0.25D, 32.00D, " chunks", List.of("1", "2", "4", "8")),

    MINIMUM_Y("traveling.minimum-y", "Minimum Y",
            63.00D, -2048.00D, 2048.00D, "", List.of("0", "32", "63", "100")),

    PROCESSING_INTERVAL("traveling.processing.interval", "Processing Interval",
            1.00D, 0.25D, 60.00D, "s", List.of("0.5", "1", "2", "5")),

    UPDATE_INTERVAL("traveling.pathfinding.update-interval", "Update Interval",
            2.00D, 0.50D, 60.00D, "s", List.of("1", "2", "3", "5")),

    RETRY_DELAY("traveling.pathfinding.retry-delay", "Retry Delay",
            5.00D, 0.50D, 300.00D, "s", List.of("3", "5", "10", "30")),

    DESTINATION_X(null, "Destination X",
            0.00D, -30000000.00D, 30000000.00D, "", List.of("0", "100", "-100", "1000")),

    DESTINATION_Z(null, "Destination Z",
            0.00D, -30000000.00D, 30000000.00D, "", List.of("0", "100", "-100", "1000"));

    private final String path;
    private final String display;
    private final double defaultValue;
    private final double minimum;
    private final double maximum;
    private final String suffix;
    private final List<String> suggestions;

    NumericSetting(String path, String display, double defaultValue, double minimum, double maximum,
                   String suffix, List<String> suggestions) {
        this.path = path;
        this.display = display;
        this.defaultValue = defaultValue;
        this.minimum = minimum;
        this.maximum = maximum;
        this.suffix = suffix;
        this.suggestions = suggestions;
    }

    @Override
    public String path() {
        return this.path;
    }

    @Override
    public String display() {
        return this.display;
    }

    public double defaultValue() {
        return this.defaultValue;
    }

    public double minimum() {
        return this.minimum;
    }

    public double maximum() {
        return this.maximum;
    }

    public List<String> suggestions() {
        return this.suggestions;
    }

    public boolean isInRange(double value) {
        return value >= this.minimum && value <= this.maximum;
    }

    public String range() {
        return Numbers.format(this.minimum) + " - " + Numbers.format(this.maximum);
    }

    public String describe(double value) {
        return Numbers.format(value) + this.suffix;
    }
}
