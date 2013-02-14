package de.raidcraft.permissions.players;

import de.raidcraft.permissions.PermissionsPlugin;
import org.bukkit.OfflinePlayer;

import java.util.HashMap;
import java.util.Map;

/**
 * An player manager that handles the creation and removal of player permissions within Privileges
 *
 * @author Silthus
 */
public class PlayerManager {

    private final PermissionsPlugin plugin;
    private final Map<String, Player> players = new HashMap<>();

    public PlayerManager(PermissionsPlugin plugin) {

        this.plugin = plugin;
    }

    public boolean register(String player) {

        return register(plugin.getServer().getOfflinePlayer(player));
    }

    public boolean register(OfflinePlayer offlinePlayer) {

        if (offlinePlayer == null || offlinePlayer.getPlayer() == null || !offlinePlayer.getPlayer().isOnline()) {
            plugin.getLogger().info("Attempted permission registration of a player that was offline or didn't exist!");
            return false;
        }
        Player player = players.get(offlinePlayer.getName().toLowerCase());
        if (player == null) {
            player = new PermissionsPlayer(plugin, offlinePlayer);
            players.put(offlinePlayer.getName().toLowerCase(), player);
        }
        ((PermissionsPlayer)player).registerPermissions();
        return true;
    }

    public void disable() {

        for (org.bukkit.entity.Player p : plugin.getServer().getOnlinePlayers()) {
            unregister(p.getName());
        }
    }

    public void reload() {

        for (org.bukkit.entity.Player p : plugin.getServer().getOnlinePlayers()) {
            register(p);
        }
    }

    public void unregister(String name) {

        Player player = players.remove(name.toLowerCase());
        if (player != null) {
            player.getAttachment().remove();
        }
    }

    public Player getPlayer(String name) {

        Player player = players.get(name.toLowerCase());
        if (player == null) {
            player = new PermissionsPlayer(plugin, plugin.getServer().getOfflinePlayer(name));
        }
        return player;
    }

}
