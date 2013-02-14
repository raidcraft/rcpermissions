package de.raidcraft.permissions.players;

import de.raidcraft.permissions.PermissionsPlugin;
import de.raidcraft.permissions.groups.Group;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionDefault;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Silthus
 */
public class PermissionsPlayer implements Player {

    private final PermissionsPlugin plugin;
    private final String name;
    private final Map<String, Group> groups = new HashMap<>();
    private final PermissionAttachment attachment;

    public PermissionsPlayer(PermissionsPlugin plugin, OfflinePlayer player) {

        this.plugin = plugin;
        this.name = player.getName().toLowerCase();
        this.attachment = player.getPlayer().addAttachment(plugin);

        for (World world : plugin.getServer().getWorlds()) {

            Permission perm = new Permission("player." + this.name + "." + world.getName(), PermissionDefault.FALSE);
            plugin.getServer().getPluginManager().removePermission(perm);
            perm.getChildren().clear();
            plugin.getServer().getPluginManager().addPermission(perm);
            perm.recalculatePermissibles();
        }
    }

    @Override
    public String getName() {

        return name;
    }

    @Override
    public PermissionAttachment getAttachment() {

        return attachment;
    }

    @Override
    public void addGroup(Group group) {

        org.bukkit.entity.Player player = Bukkit.getPlayer(getName());
        if (player == null) {
            return;
        }
        groups.put(group.getName(), group);
        attachment.setPermission(group.getMasterPermission(player.getWorld().getName()), true);
    }

    @Override
    public Group addGroup(String group) {

        Group g = plugin.getGroupManager().getGroup(group);
        addGroup(g);
        return g;
    }

    @Override
    public void removeGroup(Group group) {

        org.bukkit.entity.Player player = Bukkit.getPlayer(getName());
        if (player == null) {
            return;
        }
        attachment.unsetPermission(group.getMasterPermission(player.getWorld().getName()));
        groups.remove(group.getName());
    }

    @Override
    public Group removeGroup(String group) {

        Group g = plugin.getGroupManager().getGroup(group);
        removeGroup(g);
        return g;
    }

    @Override
    public Set<Group> getGroups() {

        return new HashSet<>(this.groups.values());
    }

    public String getMasterPermission(String world) {

        return "player." + this.name + "." + world;
    }

}
