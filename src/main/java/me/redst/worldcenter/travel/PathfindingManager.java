package me.redst.worldcenter.travel;

import me.redst.worldcenter.config.WorldSettings;
import org.bukkit.Location;
import org.bukkit.entity.Mob;

public final class PathfindingManager {
    private static final double SPEED = 1.0D;

    private static final double LEG_DISTANCE = 20.0D;

    private final Location target = new Location(null, 0.0D, 0.0D, 0.0D);

    public boolean move(Mob mob, WorldSettings settings) {
        double x = mob.getX();
        double y = mob.getY();
        double z = mob.getZ();
        double dx = settings.destinationX() - x;
        double dz = settings.destinationZ() - z;
        double distance = Math.sqrt(dx * dx + dz * dz);
        if (distance <= 0.0D) {
            return false;
        }
        double step = Math.min(distance, LEG_DISTANCE) / distance;

        this.target.setWorld(mob.getWorld());
        this.target.setX(x + dx * step);
        this.target.setY(y);
        this.target.setZ(z + dz * step);
        return mob.getPathfinder().moveTo(this.target, SPEED);
    }

    public double distanceTo(Mob mob, WorldSettings settings) {
        double dx = settings.destinationX() - mob.getX();
        double dz = settings.destinationZ() - mob.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }
}
