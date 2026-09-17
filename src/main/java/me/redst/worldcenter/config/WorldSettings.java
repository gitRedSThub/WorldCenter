package me.redst.worldcenter.config;

public record WorldSettings(boolean enabled, double destinationX, double destinationZ) {
    public static final WorldSettings UNKNOWN = new WorldSettings(false, 0.0D, 0.0D);
}
