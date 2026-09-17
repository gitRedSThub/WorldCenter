package me.redst.worldcenter.config;

import me.redst.worldcenter.util.Text;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class BlacklistManager {
    private static final String PATH = "blacklist";

    private static final Set<EntityType> DEFAULTS = EnumSet.of(
            EntityType.VILLAGER,
            EntityType.BEE,
            EntityType.IRON_GOLEM,
            EntityType.SHULKER,
            EntityType.CAT);

    private static final List<EntityType> MOB_TYPES;

    static {
        List<EntityType> types = new ArrayList<>();
        for (EntityType type : EntityType.values()) {
            if (isMobType(type)) {
                types.add(type);
            }
        }
        types.sort(Comparator.comparing(Enum::name));
        MOB_TYPES = List.copyOf(types);
    }

    private final Set<EntityType> blacklisted = EnumSet.noneOf(EntityType.class);

    public static List<EntityType> mobTypes() {
        return MOB_TYPES;
    }

    public static boolean isMobType(EntityType type) {
        Class<?> entityClass = type.getEntityClass();
        return entityClass != null && Mob.class.isAssignableFrom(entityClass);
    }

    public static EntityType match(String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }
        String name = input.trim().toLowerCase(Locale.ROOT);
        int colon = name.indexOf(':');
        if (colon >= 0) {
            name = name.substring(colon + 1);
        }
        name = name.replace(' ', '_').toUpperCase(Locale.ROOT);
        try {
            return EntityType.valueOf(name);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    public boolean isBlacklisted(EntityType type) {
        return this.blacklisted.contains(type);
    }

    public boolean add(EntityType type) {
        return this.blacklisted.add(type);
    }

    public boolean remove(EntityType type) {
        return this.blacklisted.remove(type);
    }

    public List<EntityType> sorted() {
        List<EntityType> types = new ArrayList<>(this.blacklisted);
        types.sort(Comparator.comparing(Text::pretty));
        return types;
    }

    public void load(FileConfiguration config, ConfigValidator validator) {
        this.blacklisted.clear();
        Object raw = config.get(PATH, null);
        if (raw == null) {
            this.blacklisted.addAll(DEFAULTS);
            write(config);
            validator.markFileChanged();
            return;
        }
        if (!(raw instanceof List<?> entries)) {
            validator.report("Invalid value for " + PATH
                    + ": it must be a list of entity types. Reset to the defaults.");
            this.blacklisted.addAll(DEFAULTS);
            write(config);
            return;
        }
        for (Object entry : entries) {
            if (!(entry instanceof String name)) {
                validator.report("Ignored a blacklist entry that is not an entity type name.");
                continue;
            }
            EntityType type = match(name);
            if (type == null) {
                validator.report("Ignored unknown entity type in the blacklist: " + name);
                continue;
            }
            if (!isMobType(type)) {
                validator.report("Ignored blacklist entry " + name + ": it is not a mob.");
                continue;
            }
            this.blacklisted.add(type);
        }
    }

    public void write(FileConfiguration config) {
        List<String> names = new ArrayList<>(this.blacklisted.size());
        for (EntityType type : this.blacklisted) {
            names.add(type.name());
        }
        config.set(PATH, names);
    }
}
