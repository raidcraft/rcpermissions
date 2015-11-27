package de.raidcraft.permissions.players;

import de.raidcraft.permissions.PermissionsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * An player manager that handles the creation and removal of player permissions within Privileges
 *
 * @author Silthus
 */
public class PlayerManager {

    private final PermissionsPlugin plugin;
    private final Map<UUID, Player> players = new HashMap<>();

    public PlayerManager(PermissionsPlugin plugin) {

        this.plugin = plugin;
        reload();
    }

    public boolean register(UUID playerId) {

        return register(plugin.getServer().getOfflinePlayer(playerId));
    }

    public boolean register(OfflinePlayer offlinePlayer) {

        if (offlinePlayer == null || offlinePlayer.getPlayer() == null || !offlinePlayer.getPlayer().isOnline()) {
            plugin.getLogger().info("Attempted permission registration of a player that was offline or didn't exist!");
            return false;
        }
        Player player = players.get(offlinePlayer.getUniqueId());
        if (player == null) {
            player = new PermissionsPlayer(plugin, offlinePlayer);
            players.put(offlinePlayer.getUniqueId(), player);
        }
        ((PermissionsPlayer) player).registerPermissions();
        return true;
    }

    public void disable() {

        for (org.bukkit.entity.Player p : Bukkit.getOnlinePlayers()) {
            unregister(p.getUniqueId());
        }
    }

    public void reload() {
        disable();
        Bukkit.getOnlinePlayers().forEach(this::register);
    }

    public void unregister(UUID playerId) {

        Player player = players.remove(playerId);
        if (player != null) {
            player.getAttachment().remove();
        }
    }

    public Player getPlayer(UUID playerId) {

        Player player = players.get(playerId);
        if (player == null) {
            player = new PermissionsPlayer(plugin, Bukkit.getOfflinePlayer(playerId));
        }
        return player;
    }

    public boolean hasPermission(UUID playerId, String worldName, String node) {

        Player player = getPlayer(playerId);
        return player != null && player.hasPermission(node);
    }
}
