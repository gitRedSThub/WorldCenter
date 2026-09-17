package me.redst.worldcenter.command;

import me.redst.worldcenter.WorldCenterPlugin;
import me.redst.worldcenter.config.BlacklistManager;
import me.redst.worldcenter.config.BooleanSetting;
import me.redst.worldcenter.config.NumericSetting;
import me.redst.worldcenter.config.Setting;
import me.redst.worldcenter.config.WorldCenterConfig;
import me.redst.worldcenter.config.WorldSettings;
import me.redst.worldcenter.travel.TravelingManager;
import me.redst.worldcenter.util.Numbers;
import me.redst.worldcenter.util.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

public final class WorldCenterCommand implements CommandExecutor {
    public static final String PERMISSION = "worldcenter.admin";

    private static final String TRAVELING_SYSTEM = "traveling";

    private final WorldCenterPlugin plugin;

    public WorldCenterCommand(WorldCenterPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(Text.problem("You do not have permission to use that command."));
            return true;
        }
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "help" -> sendHelp(sender);
            case "info" -> sendInfo(sender);
            case "reload" -> reload(sender);
            case "toggle" -> toggle(sender, args);
            case "set" -> set(sender, args);
            case "blacklist" -> blacklist(sender, args);
            default -> {
                sender.sendMessage(Text.problem("Unknown command: " + args[0]));
                sender.sendMessage(Text.detail("Use /worldcenter help to see everything."));
            }
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Text.header("WorldCenter Commands"));
        sender.sendMessage(helpLine(false, "/worldcenter set", "Change any setting."));
        sender.sendMessage(helpLine(false, "/worldcenter toggle traveling",
                "Turn the traveling system on or off."));
        sender.sendMessage(helpLine(false, "/worldcenter blacklist add <entity>",
                "Stop an entity type from traveling."));
        sender.sendMessage(helpLine(false, "/worldcenter blacklist remove <entity>",
                "Let an entity type travel again."));
        sender.sendMessage(helpLine(false, "/worldcenter blacklist get", "Show the blacklist."));
        sender.sendMessage(helpLine(false, "/worldcenter info", "Show the settings and what is happening now."));
        sender.sendMessage(helpLine(false, "/worldcenter reload", "Read config.yml again."));
        sender.sendMessage(helpLine(true, "/worldcenter help", "Show this list."));
    }

    private Component helpLine(boolean last, String usage, String description) {
        return Component.text(last ? Text.END : Text.TEE, Text.BRANCH)
                .append(Component.text(usage, Text.ACCENT))
                .append(Component.text(" - " + description, Text.LABEL));
    }

    private void sendInfo(CommandSender sender) {
        WorldCenterConfig settings = this.plugin.getSettings();
        sender.sendMessage(Text.header("WorldCenter"));

        sender.sendMessage(Text.branch("", false, "Traveling", Text.onOff(settings.isTravelingEnabled())));
        sender.sendMessage(Text.branch(Text.BAR, false, "Force AI", Text.onOff(settings.isForceAi())));
        sender.sendMessage(Text.branch(Text.BAR, false, "Stop Distance",
                Text.value(NumericSetting.STOP_DISTANCE_CHUNKS.describe(settings.getStopDistanceChunks()))));
        sender.sendMessage(Text.branch(Text.BAR, false, "Minimum Y",
                Text.value(Numbers.format(settings.getMinimumY()))));
        sender.sendMessage(Text.branch(Text.BAR, false, "Ignore Teamed", Text.onOff(settings.isIgnoreTeamed())));
        sender.sendMessage(Text.branch(Text.BAR, false, "Ignore Owned", Text.onOff(settings.isIgnoreOwned())));
        sender.sendMessage(Text.branch(Text.BAR, false, "Processing Interval",
                Text.value(NumericSetting.PROCESSING_INTERVAL.describe(settings.getProcessingInterval()))));
        sender.sendMessage(Text.branch(Text.BAR, false, "Random Delay", Text.onOff(settings.isRandomDelay())));
        sender.sendMessage(Text.branch(Text.BAR, true, "Pathfinding", null));
        String deep = Text.BAR + Text.GAP;
        sender.sendMessage(Text.branch(deep, false, "Update Interval",
                Text.value(NumericSetting.UPDATE_INTERVAL.describe(settings.getUpdateInterval()))));
        sender.sendMessage(Text.branch(deep, true, "Retry Delay",
                Text.value(NumericSetting.RETRY_DELAY.describe(settings.getRetryDelay()))));

        sender.sendMessage(Component.text(Text.BAR.stripTrailing(), Text.BRANCH));
        sender.sendMessage(Text.branch("", false, "Blacklist", null));
        List<EntityType> blacklisted = settings.getBlacklist().sorted();
        if (blacklisted.isEmpty()) {
            sender.sendMessage(Text.branch(Text.BAR, true, "Empty", null));
        } else {
            for (int i = 0; i < blacklisted.size(); i++) {
                sender.sendMessage(Text.branch(Text.BAR, i == blacklisted.size() - 1,
                        Text.pretty(blacklisted.get(i)), null));
            }
        }

        sender.sendMessage(Component.text(Text.BAR.stripTrailing(), Text.BRANCH));
        sender.sendMessage(Text.branch("", false, "Worlds", null));
        Set<String> worldNames = new TreeSet<>(settings.getWorlds().keySet());
        for (World world : Bukkit.getWorlds()) {
            worldNames.add(world.getName());
        }
        if (worldNames.isEmpty()) {
            sender.sendMessage(Text.branch(Text.BAR, true, "None", null));
        } else {
            int index = 0;
            for (String name : worldNames) {
                WorldSettings world = settings.getWorldSettings(name);
                Component value = Text.onOff(world.enabled())
                        .append(Component.text(Text.ARROW, Text.BRANCH))
                        .append(Text.value(Numbers.format(world.destinationX())
                                + ", " + Numbers.format(world.destinationZ())));
                sender.sendMessage(Text.branch(Text.BAR, ++index == worldNames.size(), name, value));
            }
        }

        sender.sendMessage(Component.text(Text.BAR.stripTrailing(), Text.BRANCH));
        TravelingManager.Stats stats = this.plugin.getTravelingManager().stats();
        sender.sendMessage(Text.branch("", true, "Status", null));
        sender.sendMessage(Text.branch(Text.GAP, false, "Active Worlds",
                Text.value(Integer.toString(stats.worlds()))));
        sender.sendMessage(Text.branch(Text.GAP, false, "Tracked Mobs",
                Text.value(Integer.toString(stats.tracked()))));
        sender.sendMessage(Text.branch(Text.GAP, false, "Eligible Mobs",
                Text.value(Integer.toString(stats.eligible()))));
        sender.sendMessage(Text.branch(Text.GAP, true, "Traveling Mobs",
                Text.value(Integer.toString(stats.traveling()))));
    }

    private void reload(CommandSender sender) {
        int invalid = this.plugin.reloadEverything();
        sender.sendMessage(Text.info("Configuration reloaded."));
        if (invalid > 0) {
            sender.sendMessage(Text.detail("Invalid values were reset to their defaults: " + invalid));
        }
    }

    private void toggle(CommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(Text.problem("Usage: /worldcenter toggle " + TRAVELING_SYSTEM));
            return;
        }
        if (!args[1].equalsIgnoreCase(TRAVELING_SYSTEM)) {
            sender.sendMessage(Text.problem("Unknown system: " + args[1]));
            sender.sendMessage(Text.detail("The only system is " + TRAVELING_SYSTEM + "."));
            return;
        }
        boolean value = !this.plugin.getSettings().isTravelingEnabled();
        this.plugin.getConfig().set(WorldCenterConfig.PATH_TRAVELING_ENABLED, value);
        this.plugin.applyChanges(true);
        sender.sendMessage(Text.info("Traveling is now " + onOff(value) + "."));
    }

    private void set(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sendSetUsage(sender);
            return;
        }
        if (args[1].equalsIgnoreCase(SettingRegistry.WORLDS)) {
            setWorld(sender, args);
            return;
        }
        String key = joinLower(args, 1, args.length - 1);
        if (key.equals(SettingRegistry.MAIN_SWITCH)) {
            sender.sendMessage(Text.problem("Traveling is a main system, so it is not changed here."));
            sender.sendMessage(Text.detail("Use /worldcenter toggle traveling instead."));
            return;
        }
        Setting setting = SettingRegistry.get(key);
        if (setting == null) {
            sender.sendMessage(Text.problem("Unknown setting: " + key));
            sender.sendMessage(Text.detail("Use tab completion to see what can be changed."));
            return;
        }
        String raw = args[args.length - 1];
        if (setting instanceof BooleanSetting booleanSetting) {
            applyBoolean(sender, booleanSetting.path(), booleanSetting.display(), raw,
                    booleanSetting == BooleanSetting.FORCE_AI);
        } else {
            applyNumber(sender, (NumericSetting) setting, setting.path(), setting.display(), raw);
        }
    }

    private void sendSetUsage(CommandSender sender) {
        sender.sendMessage(Text.problem("Usage: /worldcenter set <setting> <value>"));
        sender.sendMessage(Text.detail("Use tab completion to see what can be changed."));
    }

    private void setWorld(CommandSender sender, String[] args) {
        if (args.length < 5) {
            sender.sendMessage(Text.problem("Usage: /worldcenter set worlds <world> enabled <true|false>"));
            sender.sendMessage(Text.detail("Or: /worldcenter set worlds <world> destination <x|z> <value>"));
            return;
        }
        String worldName = resolveWorldName(args[2]);
        String base = WorldCenterConfig.PATH_WORLDS + "." + worldName;
        String option = args[3].toLowerCase(Locale.ROOT);

        if (option.equals(SettingRegistry.WORLD_ENABLED)) {
            if (args.length != 5) {
                sender.sendMessage(Text.problem("Usage: /worldcenter set worlds <world> enabled <true|false>"));
                return;
            }
            Boolean value = parseBoolean(args[4]);
            if (value == null) {
                sendInvalidBoolean(sender, args[4]);
                return;
            }
            createWorldSection(worldName);
            this.plugin.getConfig().set(base + "." + SettingRegistry.WORLD_ENABLED, value);
            this.plugin.applyChanges(true);
            sender.sendMessage(Text.info("Traveling in " + worldName + " is now " + onOff(value) + "."));
            warnIfNotLoaded(sender, worldName);
            return;
        }

        if (option.equals(SettingRegistry.WORLD_DESTINATION)) {
            if (args.length != 6) {
                sender.sendMessage(Text.problem("Usage: /worldcenter set worlds <world> destination <x|z> <value>"));
                return;
            }
            String axis = args[4].toLowerCase(Locale.ROOT);
            NumericSetting setting = switch (axis) {
                case "x" -> NumericSetting.DESTINATION_X;
                case "z" -> NumericSetting.DESTINATION_Z;
                default -> null;
            };
            if (setting == null) {
                sender.sendMessage(Text.problem("Unknown part of the destination: " + args[4]));
                sender.sendMessage(Text.detail("A destination has an x and a z."));
                return;
            }
            createWorldSection(worldName);
            boolean applied = applyNumber(sender, setting,
                    base + "." + SettingRegistry.WORLD_DESTINATION + "." + axis,
                    worldName + " destination " + axis.toUpperCase(Locale.ROOT), args[5]);
            if (applied) {
                warnIfNotLoaded(sender, worldName);
            }
            return;
        }

        sender.sendMessage(Text.problem("Unknown world setting: " + args[3]));
        sender.sendMessage(Text.detail("A world has enabled and destination."));
    }

    private void applyBoolean(CommandSender sender, String path, String display, String raw, boolean rebuild) {
        Boolean value = parseBoolean(raw);
        if (value == null) {
            sendInvalidBoolean(sender, raw);
            return;
        }
        this.plugin.getConfig().set(path, value);
        this.plugin.applyChanges(rebuild);
        sender.sendMessage(Text.info(display + " is now " + onOff(value) + "."));
    }

    private boolean applyNumber(CommandSender sender, NumericSetting setting, String path, String display,
                                String raw) {
        Double parsed = Numbers.parse(raw);
        if (parsed == null) {
            if (Numbers.isTooPrecise(raw)) {
                sender.sendMessage(Text.problem("Use at most " + Numbers.MAX_DECIMALS + " decimal places."));
                sender.sendMessage(Text.detail("For example: " + Numbers.format(setting.defaultValue())));
            } else {
                sender.sendMessage(Text.problem("Invalid number."));
                sender.sendMessage(Text.detail("Use a value between " + setting.range().replace(" - ", " and ") + "."));
            }
            return false;
        }
        if (!setting.isInRange(parsed)) {
            sender.sendMessage(Text.problem("That value is outside the safe limit."));
            sender.sendMessage(Text.detail("Allowed range: " + setting.range()));
            return false;
        }
        double value = Numbers.round(parsed);
        this.plugin.getConfig().set(path, value);
        this.plugin.applyChanges(false);
        sender.sendMessage(Text.info(display + " is now " + setting.describe(value) + "."));
        return true;
    }

    private void blacklist(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sendBlacklistUsage(sender);
            return;
        }
        switch (args[1].toLowerCase(Locale.ROOT)) {
            case "get" -> sendBlacklist(sender);
            case "add" -> changeBlacklist(sender, args, true);
            case "remove" -> changeBlacklist(sender, args, false);
            default -> sendBlacklistUsage(sender);
        }
    }

    private void sendBlacklistUsage(CommandSender sender) {
        sender.sendMessage(Text.problem("Usage: /worldcenter blacklist <add|remove|get> [entity]"));
    }

    private void changeBlacklist(CommandSender sender, String[] args, boolean adding) {
        if (args.length != 3) {
            sender.sendMessage(Text.problem("Usage: /worldcenter blacklist "
                    + (adding ? "add" : "remove") + " <entity>"));
            return;
        }
        EntityType type = BlacklistManager.match(args[2]);
        if (type == null) {
            sender.sendMessage(Text.problem("Unknown entity type: " + args[2]));
            sender.sendMessage(Text.detail("Use the suggested entity types shown by tab completion."));
            return;
        }
        if (!BlacklistManager.isMobType(type)) {
            sender.sendMessage(Text.problem(Text.pretty(type) + " is not a mob, so it never travels anyway."));
            sender.sendMessage(Text.detail("Use the suggested entity types shown by tab completion."));
            return;
        }
        BlacklistManager blacklist = this.plugin.getSettings().getBlacklist();
        String name = Text.pretty(type);
        if (adding) {
            if (!blacklist.add(type)) {
                sender.sendMessage(Text.info(name + " is already on the WorldCenter blacklist."));
                return;
            }
        } else if (!blacklist.remove(type)) {
            sender.sendMessage(Text.info(name + " is not on the WorldCenter blacklist."));
            return;
        }
        blacklist.write(this.plugin.getConfig());
        this.plugin.applyChanges(true);
        sender.sendMessage(Text.info(adding
                ? "Added " + name + " to the WorldCenter blacklist."
                : "Removed " + name + " from the WorldCenter blacklist."));
    }

    private void sendBlacklist(CommandSender sender) {
        List<EntityType> types = this.plugin.getSettings().getBlacklist().sorted();
        sender.sendMessage(Text.header("WorldCenter Blacklist"));
        if (types.isEmpty()) {
            sender.sendMessage(Text.branch("", true, "Empty", null));
            return;
        }
        for (int i = 0; i < types.size(); i++) {
            sender.sendMessage(Text.branch("", i == types.size() - 1, Text.pretty(types.get(i)), null));
        }
    }

    private void createWorldSection(String worldName) {
        FileConfiguration config = this.plugin.getConfig();
        String base = WorldCenterConfig.PATH_WORLDS + "." + worldName;
        if (config.get(base, null) instanceof ConfigurationSection) {
            return;
        }
        config.set(base + "." + SettingRegistry.WORLD_ENABLED, BooleanSetting.WORLD_ENABLED.defaultValue());
        config.set(base + "." + SettingRegistry.WORLD_DESTINATION + ".x",
                NumericSetting.DESTINATION_X.defaultValue());
        config.set(base + "." + SettingRegistry.WORLD_DESTINATION + ".z",
                NumericSetting.DESTINATION_Z.defaultValue());
    }

    private String resolveWorldName(String input) {
        World loaded = Bukkit.getWorld(input);
        if (loaded != null) {
            return loaded.getName();
        }
        for (World world : Bukkit.getWorlds()) {
            if (world.getName().equalsIgnoreCase(input)) {
                return world.getName();
            }
        }
        for (String name : this.plugin.getSettings().getWorlds().keySet()) {
            if (name.equalsIgnoreCase(input)) {
                return name;
            }
        }
        return input;
    }

    private void warnIfNotLoaded(CommandSender sender, String worldName) {
        if (Bukkit.getWorld(worldName) == null) {
            sender.sendMessage(Text.detail("That world is not loaded right now, but the setting is saved."));
        }
    }

    private void sendInvalidBoolean(CommandSender sender, String raw) {
        sender.sendMessage(Text.problem("Invalid value: " + raw));
        sender.sendMessage(Text.detail("Use true or false."));
    }

    private static Boolean parseBoolean(String raw) {
        if (raw.equalsIgnoreCase("true")) {
            return Boolean.TRUE;
        }
        if (raw.equalsIgnoreCase("false")) {
            return Boolean.FALSE;
        }
        return null;
    }

    private static String onOff(boolean value) {
        return value ? "ON" : "OFF";
    }

    private static String joinLower(String[] args, int from, int toExclusive) {
        StringBuilder builder = new StringBuilder();
        for (int i = from; i < toExclusive; i++) {
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(args[i].toLowerCase(Locale.ROOT));
        }
        return builder.toString();
    }
}
