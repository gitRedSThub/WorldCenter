package me.redst.worldcenter.config;

public enum BooleanSetting implements Setting {
    FORCE_AI("traveling.force-ai.enabled", "Force AI", false),
    IGNORE_TEAMED("traveling.ignore-teamed", "Ignore Teamed", true),
    IGNORE_OWNED("traveling.ignore-owned", "Ignore Owned", true),
    RANDOM_DELAY("traveling.processing.random-delay", "Random Delay", true),

    WORLD_ENABLED(null, "Enabled", false);

    private final String path;
    private final String display;
    private final boolean defaultValue;

    BooleanSetting(String path, String display, boolean defaultValue) {
        this.path = path;
        this.display = display;
        this.defaultValue = defaultValue;
    }

    @Override
    public String path() {
        return this.path;
    }

    @Override
    public String display() {
        return this.display;
    }

    public boolean defaultValue() {
        return this.defaultValue;
    }
}
