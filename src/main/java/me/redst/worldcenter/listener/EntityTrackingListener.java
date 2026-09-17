package me.redst.worldcenter.listener;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import me.redst.worldcenter.travel.TravelingManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class EntityTrackingListener implements Listener {
    private final TravelingManager travelingManager;

    public EntityTrackingListener(TravelingManager travelingManager) {
        this.travelingManager = travelingManager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityAddToWorld(EntityAddToWorldEvent event) {
        this.travelingManager.handleAdded(event.getEntity());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityRemoveFromWorld(EntityRemoveFromWorldEvent event) {
        this.travelingManager.handleRemoved(event.getEntity());
    }
}
