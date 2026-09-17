package me.redst.worldcenter;

import me.redst.worldcenter.command.WorldCenterCommand;
import me.redst.worldcenter.command.WorldCenterTabCompleter;
import me.redst.worldcenter.config.WorldCenterConfig;
import me.redst.worldcenter.listener.EntityTrackingListener;
import me.redst.worldcenter.travel.TravelingManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class WorldCenterPlugin extends JavaPlugin {
    private WorldCenterConfig settings;
    private TravelingManager travelingManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.settings = new WorldCenterConfig(this);
        this.settings.load(false);

        this.travelingManager = new TravelingManager(this, this.settings);
        this.travelingManager.apply();
        getServer().getPluginManager().registerEvents(new EntityTrackingListener(this.travelingManager), this);

        PluginCommand command = getCommand("worldcenter");
        if (command != null) {
            command.setExecutor(new WorldCenterCommand(this));
            command.setTabCompleter(new WorldCenterTabCompleter(this));
        }
        getLogger().info("WorldCenter enabled.");
    }

    @Override
    public void onDisable() {
        if (this.travelingManager != null) {
            this.travelingManager.shutdown();
        }
        getLogger().info("WorldCenter disabled.");
    }

    public int reloadEverything() {
        int invalid = this.settings.load(true);
        this.travelingManager.apply();
        return invalid;
    }

    public void applyChanges(boolean rebuild) {
        saveConfig();
        this.settings.refresh();
        if (rebuild) {
            this.travelingManager.apply();
        }
    }

    public WorldCenterConfig getSettings() {
        return this.settings;
    }

    public TravelingManager getTravelingManager() {
        return this.travelingManager;
    }
}
