package me.redst.worldcenter.travel;

import me.redst.worldcenter.config.WorldCenterConfig;
import me.redst.worldcenter.config.WorldSettings;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Tameable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.EnumSet;
import java.util.Set;

public final class MobEligibilityChecker {
    private static final Set<EntityType> NO_NAVIGATION = EnumSet.of(
            EntityType.BAT,
            EntityType.GHAST,
            EntityType.HAPPY_GHAST,
            EntityType.PHANTOM,
            EntityType.ENDER_DRAGON);

    private Scoreboard mainScoreboard;

    public boolean canTrack(Entity entity, WorldCenterConfig config) {
        if (!(entity instanceof Mob)) {
            return false;
        }
        if (!config.isTravelingEnabled()) {
            return false;
        }
        EntityType type = entity.getType();
        if (NO_NAVIGATION.contains(type)) {
            return false;
        }
        if (config.getBlacklist().isBlacklisted(type)) {
            return false;
        }
        return config.getWorldSettings(entity.getWorld().getName()).enabled();
    }

    public boolean canTravel(Mob mob, WorldSettings settings, WorldCenterConfig config) {
        if (!mob.isValid()) {
            return false;
        }
        if (!config.isTravelingEnabled() || !settings.enabled()) {
            return false;
        }
        if (config.getBlacklist().isBlacklisted(mob.getType())) {
            return false;
        }
        if (mob.getY() < config.getMinimumY()) {
            return false;
        }
        if (config.isIgnoreOwned() && isOwned(mob)) {
            return false;
        }
        if (config.isIgnoreTeamed() && isTeamed(mob)) {
            return false;
        }
        if (isWithinStopDistance(mob, settings, config)) {
            return false;
        }
        return config.isForceAi() || mob.getTarget() == null;
    }

    private boolean isWithinStopDistance(Mob mob, WorldSettings settings, WorldCenterConfig config) {
        double dx = mob.getX() - settings.destinationX();
        double dz = mob.getZ() - settings.destinationZ();
        return dx * dx + dz * dz <= config.getStopDistanceSquared();
    }

    private boolean isOwned(Mob mob) {
        return mob instanceof Tameable tameable && tameable.isTamed();
    }

    private boolean isTeamed(Mob mob) {
        Scoreboard scoreboard = mainScoreboard();
        return scoreboard != null && scoreboard.getEntityTeam(mob) != null;
    }

    private Scoreboard mainScoreboard() {
        if (this.mainScoreboard == null) {
            ScoreboardManager manager = Bukkit.getScoreboardManager();
            if (manager != null) {
                this.mainScoreboard = manager.getMainScoreboard();
            }
        }
        return this.mainScoreboard;
    }
}
