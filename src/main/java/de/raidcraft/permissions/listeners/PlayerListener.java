package de.raidcraft.permissions.listeners;

import de.raidcraft.permissions.PermissionsPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
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

    @EventHandler(priority = EventPriority.LOWEST)
    public void playerLogin(PlayerLoginEvent event) {

        // register player for early perm checks
        plugin.getPlayerManager().register(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerFailLogin(PlayerLoginEvent event) {

        // unregister if player is prevented from joining
        if (event.getResult() != PlayerLoginEvent.Result.ALLOWED) {
            plugin.getPlayerManager().unregister(event.getPlayer().getName());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void playerJoin(PlayerJoinEvent event) {

        // re-register player for world permissions
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
