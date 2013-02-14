package de.raidcraft.permissions.listeners;

import de.raidcraft.permissions.PermissionsPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionAttachmentInfo;

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
    public void playerJoin(PlayerJoinEvent event) {

        // re-register player for world permissions
        plugin.getPlayerManager().register(event.getPlayer());
        for (PermissionAttachmentInfo node : event.getPlayer().getEffectivePermissions()) {
            event.getPlayer().sendMessage(node.getPermission() + ": " + node.getValue());
        }
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
