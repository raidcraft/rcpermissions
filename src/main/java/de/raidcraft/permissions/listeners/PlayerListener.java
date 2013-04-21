package de.raidcraft.permissions.listeners;

import de.raidcraft.permissions.PermissionsPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * @author Silthus
 */
@SuppressWarnings("unused")
public class PlayerListener implements Listener {

    private PermissionsPlugin plugin;

    public PlayerListener(PermissionsPlugin plugin) {

        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerJoin(PlayerJoinEvent event) {

        // register player for world permissions
        plugin.getPlayerManager().register(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerQuit(PlayerQuitEvent event) {

        plugin.getPlayerManager().unregister(event.getPlayer().getName());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerChangedWorld(PlayerChangedWorldEvent event) {

        plugin.getPlayerManager().register(event.getPlayer());
    }
}
