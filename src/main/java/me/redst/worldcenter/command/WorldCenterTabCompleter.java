package me.redst.worldcenter.command;

import me.redst.worldcenter.WorldCenterPlugin;
import me.redst.worldcenter.config.BlacklistManager;
import me.redst.worldcenter.config.BooleanSetting;
import me.redst.worldcenter.config.NumericSetting;
import me.redst.worldcenter.config.Setting;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

public final class WorldCenterTabCompleter implements TabCompleter {
    private static final List<String> ROOT = List.of("set", "toggle", "blacklist", "info", "reload", "help");
    private static final List<String> SYSTEMS = List.of("traveling");
    private static final List<String> BOOLEANS = List.of("true", "false");
    private static final List<String> BLACKLIST_ACTIONS = List.of("add", "remove", "get");
    private static final List<String> WORLD_OPTIONS =
            List.of(SettingRegistry.WORLD_ENABLED, SettingRegistry.WORLD_DESTINATION);
    private static final List<String> AXES = List.of("x", "z");

    private final WorldCenterPlugin plugin;

    public WorldCenterTabCompleter(WorldCenterPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission(WorldCenterCommand.PERMISSION) || args.length == 0) {
            return List.of();
        }
        String current = args[args.length - 1];
        if (args.length == 1) {
            return matching(ROOT, current);
        }
        return switch (args[0].toLowerCase(Locale.ROOT)) {
            case "toggle" -> args.length == 2 ? matching(SYSTEMS, current) : List.of();
            case "blacklist" -> completeBlacklist(args, current);
            case "set" -> completeSet(args, current);
            default -> List.of();
        };
    }

    private List<String> completeBlacklist(String[] args, String current) {
        if (args.length == 2) {
            return matching(BLACKLIST_ACTIONS, current);
        }
        if (args.length != 3) {
            return List.of();
        }
        BlacklistManager blacklist = this.plugin.getSettings().getBlacklist();
        List<String> names = new ArrayList<>();
        if (args[1].equalsIgnoreCase("add")) {
            for (EntityType type : BlacklistManager.mobTypes()) {
                if (!blacklist.isBlacklisted(type)) {
                    names.add(type.name());
                }
            }
        } else if (args[1].equalsIgnoreCase("remove")) {
            for (EntityType type : blacklist.sorted()) {
                names.add(type.name());
            }
        }
        return matching(names, current);
    }

    private List<String> completeSet(String[] args, String current) {
        if (args.length >= 3 && args[1].equalsIgnoreCase(SettingRegistry.WORLDS)) {
            return completeWorlds(args, current);
        }
        String typed = joinLower(args, 1, args.length - 1);
        if (typed.isEmpty()) {
            List<String> words = new ArrayList<>(SettingRegistry.nextWords(""));
            words.add(SettingRegistry.WORLDS);
            return matching(words, current);
        }
        Setting setting = SettingRegistry.get(typed);
        if (setting != null) {
            return matching(valuesFor(setting), current);
        }
        return matching(SettingRegistry.nextWords(typed), current);
    }

    private List<String> completeWorlds(String[] args, String current) {
        if (args.length == 3) {
            return matching(worldNames(), current);
        }
        if (args.length == 4) {
            return matching(WORLD_OPTIONS, current);
        }
        String option = args[3].toLowerCase(Locale.ROOT);
        if (args.length == 5) {
            if (option.equals(SettingRegistry.WORLD_ENABLED)) {
                return matching(BOOLEANS, current);
            }
            if (option.equals(SettingRegistry.WORLD_DESTINATION)) {
                return matching(AXES, current);
            }
            return List.of();
        }
        if (args.length == 6 && option.equals(SettingRegistry.WORLD_DESTINATION)) {
            return switch (args[4].toLowerCase(Locale.ROOT)) {
                case "x" -> matching(NumericSetting.DESTINATION_X.suggestions(), current);
                case "z" -> matching(NumericSetting.DESTINATION_Z.suggestions(), current);
                default -> List.of();
            };
        }
        return List.of();
    }

    private List<String> valuesFor(Setting setting) {
        if (setting instanceof BooleanSetting) {
            return BOOLEANS;
        }
        return ((NumericSetting) setting).suggestions();
    }

    private List<String> worldNames() {
        Set<String> names = new TreeSet<>(this.plugin.getSettings().getWorlds().keySet());
        for (World world : Bukkit.getWorlds()) {
            names.add(world.getName());
        }
        return new ArrayList<>(names);
    }

    private static List<String> matching(List<String> options, String current) {
        if (current.isEmpty()) {
            return List.copyOf(options);
        }
        String prefix = current.toLowerCase(Locale.ROOT);
        List<String> matches = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase(Locale.ROOT).startsWith(prefix)) {
                matches.add(option);
            }
        }
        return matches;
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
