package me.redst.worldcenter.travel;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import me.redst.worldcenter.config.WorldCenterConfig;
import me.redst.worldcenter.config.WorldSettings;
import org.bukkit.entity.Mob;

import java.util.EnumSet;

public final class TravelingGoal implements Goal<Mob> {
    private static final EnumSet<GoalType> TYPES = EnumSet.of(GoalType.MOVE);

    private static final double MIN_PROGRESS = 0.5D;

    private final TravelingManager manager;
    private final Mob mob;

    private WorldSettings settings = WorldSettings.UNKNOWN;

    private long nextEvaluationTick;
    private long nextAttemptTick;
    private long nextRenewalTick;
    private double lastDistance = Double.MAX_VALUE;

    private boolean eligible;
    private boolean traveling;

    TravelingGoal(TravelingManager manager, Mob mob) {
        this.manager = manager;
        this.mob = mob;
        this.nextEvaluationTick = manager.getTick() + manager.nextStartDelay();
    }

    @Override
    public boolean shouldActivate() {
        long now = this.manager.getTick();
        if (now < this.nextAttemptTick) {
            return false;
        }
        if (now >= this.nextEvaluationTick) {
            evaluate(now);
        }
        return this.eligible;
    }

    @Override
    public void start() {
        this.traveling = true;
        this.lastDistance = this.manager.getPathfinding().distanceTo(this.mob, this.settings);
        this.nextRenewalTick = this.manager.getTick() + this.manager.getConfig().getUpdateIntervalTicks();
        if (!this.manager.getPathfinding().move(this.mob, this.settings)) {
            waitBeforeRetrying();
        }
    }

    @Override
    public void tick() {
        long now = this.manager.getTick();
        if (now < this.nextRenewalTick) {
            return;
        }
        this.nextRenewalTick = now + this.manager.getConfig().getUpdateIntervalTicks();
        if (this.mob.getPathfinder().hasPath()) {
            return;
        }
        double distance = this.manager.getPathfinding().distanceTo(this.mob, this.settings);
        if (this.lastDistance - distance < MIN_PROGRESS) {
            waitBeforeRetrying();
            return;
        }
        this.lastDistance = distance;
        if (!this.manager.getPathfinding().move(this.mob, this.settings)) {
            waitBeforeRetrying();
        }
    }

    @Override
    public void stop() {
        this.traveling = false;
        this.mob.getPathfinder().stopPathfinding();
    }

    @Override
    public GoalKey<Mob> getKey() {
        return this.manager.getGoalKey();
    }

    @Override
    public EnumSet<GoalType> getTypes() {
        return TYPES;
    }

    private void evaluate(long now) {
        WorldCenterConfig config = this.manager.getConfig();
        this.nextEvaluationTick = now + config.getProcessingIntervalTicks();
        this.settings = config.getWorldSettings(this.mob.getWorld().getName());
        this.eligible = this.manager.getEligibility().canTravel(this.mob, this.settings, config);
    }

    private void waitBeforeRetrying() {
        this.eligible = false;
        this.nextAttemptTick = this.manager.getTick() + this.manager.getConfig().getRetryDelayTicks();
    }

    Mob getMob() {
        return this.mob;
    }

    boolean isEligible() {
        return this.eligible;
    }

    boolean isTraveling() {
        return this.traveling;
    }
}
