package me.redst.worldcenter.travel;

import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.MobGoals;
import me.redst.worldcenter.config.WorldCenterConfig;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class TravelingManager {
    private static final int NORMAL_PRIORITY = 5;

    private static final int FORCE_PRIORITY = 0;

    private static final int MAX_ADDITIONS_PER_TICK = 100;

    private static final long CLEANUP_INTERVAL_TICKS = 600L;

    private final Plugin plugin;
    private final WorldCenterConfig config;
    private final MobEligibilityChecker eligibility = new MobEligibilityChecker();
    private final PathfindingManager pathfinding = new PathfindingManager();
    private final MobGoals mobGoals = Bukkit.getMobGoals();
    private final GoalKey<Mob> goalKey;

    private final Map<UUID, TravelingGoal> tracked = new HashMap<>();
    private final Queue<Mob> waiting = new ArrayDeque<>();

    private BukkitTask task;
    private long tick;

    public TravelingManager(Plugin plugin, WorldCenterConfig config) {
        this.plugin = plugin;
        this.config = config;
        this.goalKey = GoalKey.of(Mob.class, new NamespacedKey(plugin, "travel"));
    }

    public record Stats(int tracked, int eligible, int traveling, int worlds) {
    }

    public void apply() {
        removeAllGoals();
        if (!this.config.isTravelingEnabled()) {
            stopTask();
            return;
        }
        startTask();
        collectLoadedMobs();
    }

    public void shutdown() {
        stopTask();
        removeAllGoals();
    }

    public void handleAdded(Entity entity) {
        if (this.task == null || !(entity instanceof Mob mob)) {
            return;
        }
        if (!this.eligibility.canTrack(mob, this.config)) {
            return;
        }
        this.waiting.add(mob);
    }

    public void handleRemoved(Entity entity) {
        if (this.tracked.isEmpty() || !(entity instanceof Mob)) {
            return;
        }
        this.tracked.remove(entity.getUniqueId());
    }

    public Stats stats() {
        int eligibleCount = 0;
        int travelingCount = 0;
        for (TravelingGoal goal : this.tracked.values()) {
            if (goal.isEligible()) {
                eligibleCount++;
            }
            if (goal.isTraveling()) {
                travelingCount++;
            }
        }
        int worldCount = 0;
        for (World world : Bukkit.getWorlds()) {
            if (this.config.getWorldSettings(world.getName()).enabled()) {
                worldCount++;
            }
        }
        return new Stats(this.tracked.size(), eligibleCount, travelingCount, worldCount);
    }

    private void onTick() {
        this.tick++;
        addWaitingMobs();
        if (this.tick % CLEANUP_INTERVAL_TICKS == 0L) {
            forgetGoneMobs();
        }
    }

    private void addWaitingMobs() {
        for (int added = 0; added < MAX_ADDITIONS_PER_TICK; added++) {
            Mob mob = this.waiting.poll();
            if (mob == null) {
                return;
            }
            addGoal(mob);
        }
    }

    private void addGoal(Mob mob) {
        if (!mob.isValid() || !this.eligibility.canTrack(mob, this.config)) {
            return;
        }
        UUID id = mob.getUniqueId();
        if (this.tracked.containsKey(id)) {
            return;
        }
        if (this.mobGoals.hasGoal(mob, this.goalKey)) {
            this.mobGoals.removeGoal(mob, this.goalKey);
        }
        TravelingGoal goal = new TravelingGoal(this, mob);
        this.mobGoals.addGoal(mob, this.config.isForceAi() ? FORCE_PRIORITY : NORMAL_PRIORITY, goal);
        this.tracked.put(id, goal);
    }

    private void removeAllGoals() {
        for (TravelingGoal goal : this.tracked.values()) {
            this.mobGoals.removeGoal(goal.getMob(), goal);
        }
        this.tracked.clear();
        this.waiting.clear();
    }

    private void collectLoadedMobs() {
        for (World world : Bukkit.getWorlds()) {
            if (!this.config.getWorldSettings(world.getName()).enabled()) {
                continue;
            }
            this.waiting.addAll(world.getEntitiesByClass(Mob.class));
        }
    }

    private void forgetGoneMobs() {
        this.tracked.values().removeIf(goal -> !goal.getMob().isValid());
    }

    private void startTask() {
        if (this.task == null) {
            this.task = Bukkit.getScheduler().runTaskTimer(this.plugin, this::onTick, 1L, 1L);
        }
    }

    private void stopTask() {
        if (this.task != null) {
            this.task.cancel();
            this.task = null;
        }
    }

    long getTick() {
        return this.tick;
    }

    int nextStartDelay() {
        if (!this.config.isRandomDelay()) {
            return 0;
        }
        return ThreadLocalRandom.current().nextInt(this.config.getProcessingIntervalTicks());
    }

    WorldCenterConfig getConfig() {
        return this.config;
    }

    MobEligibilityChecker getEligibility() {
        return this.eligibility;
    }

    PathfindingManager getPathfinding() {
        return this.pathfinding;
    }

    GoalKey<Mob> getGoalKey() {
        return this.goalKey;
    }
}
