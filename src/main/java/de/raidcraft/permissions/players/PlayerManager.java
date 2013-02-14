package de.raidcraft.permissions.players;

import de.raidcraft.permissions.PermissionsPlugin;
import de.raidcraft.permissions.groups.Group;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

    public boolean register(OfflinePlayer ply) {

        if (ply == null || ply.getPlayer() == null) {
            plugin.getLogger().info("Attempted permission registration of a player that was offline or didn't exist!");
            return false;
        }
        Player priv = players.get(ply.getName().toLowerCase());
        if (priv == null) {
            priv = new PermissionsPlayer(plugin.getProvider(), ply);
            players.put(ply.getName().toLowerCase(), priv);
        }
        // lets add the player to all specified groups
        Set<Group> groups = new HashSet<>();
        for (String grp : priv.getGroups()) {
            groups.add(plugin.getGroupManager().addPlayerToGroup(priv.getName(), grp));
        }
        org.bukkit.entity.Player player = ply.getPlayer();
        // clear the player's permissions
        for (PermissionAttachmentInfo info : player.getEffectivePermissions()) {
            PermissionAttachment att = info.getAttachment();
            if (att == null) {
                continue;
            }
            att.unsetPermission(info.getPermission());
        }
        // build the attachment
        PermissionAttachment attachment = player.addAttachment(plugin);
        for (Group group : groups) {
            attachment.setPermission(group.getMasterPermission(player.getWorld().getName()), true);
        }
        attachment.setPermission(priv.getMasterPermission(player.getWorld().getName()), true);
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
            for (World world : plugin.getServer().getWorlds()) {
                String node = player.getMasterPermission(world.getName());
                plugin.getServer().getPluginManager().removePermission(node);
            }
            plugin.getLogger().info(name + " was successfully unregistered.");
        } else {
            plugin.getLogger().info(name + " was already unregistered!");
        }
    }

    public Player getPlayer(String name) {

        Player player = players.get(name.toLowerCase());
        if (player == null) {
            player = new PermissionsPlayer(plugin.getProvider(), plugin.getServer().getOfflinePlayer(name));
        }
        return player;
    }

}
