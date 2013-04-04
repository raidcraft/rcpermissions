package de.raidcraft.permissions.groups;

import de.raidcraft.permissions.PermissionsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A group manager that handles the creation and removal of group permissions in Privileges
 *
 * @author Silthus
 */
public class GroupManager {

    private PermissionsPlugin plugin;
    private String DEFAULT;
    private Map<String, Group> groupList = new HashMap<>();

    public GroupManager(PermissionsPlugin plugin) {

        this.plugin = plugin;
        Group defaultGroup = plugin.getProvider().getDefaultGroup();
        if (defaultGroup != null) {
            this.DEFAULT = defaultGroup.getName();
        }
    }

    private void load() {

        for (Group group : plugin.getProvider().getGroups()) {
            // we need to create a permission with the group name for group perm lookups
            Permission perm = plugin.getServer().getPluginManager().getPermission(group.getGlobalMasterPermission());
            if (perm == null) {
                perm = new Permission(group.getGlobalMasterPermission());
            }

            perm.setDescription("If true, the attached player is a member of the group: " + group);
            perm.setDefault(PermissionDefault.FALSE);
            perm.getChildren().clear();
            // add the global master group permission
            if (plugin.getServer().getPluginManager().getPermission(perm.getName()) == null) {
                plugin.getServer().getPluginManager().addPermission(perm);
            }
            // now register a world permission for the group
            for (World world : Bukkit.getWorlds()) {
                Permission worldPerm = plugin.getServer().getPluginManager().getPermission(group.getMasterPermission(world.getName()));
                if (worldPerm == null) {
                    worldPerm = new Permission(group.getMasterPermission(world.getName()));
                }
                worldPerm.setDefault(PermissionDefault.FALSE);
                worldPerm.getChildren().clear();
                perm.getChildren().put(worldPerm.getName(), true);
                if (plugin.getServer().getPluginManager().getPermission(worldPerm.getName()) == null) {
                    plugin.getServer().getPluginManager().addPermission(worldPerm);
                }
            }
            // now register all child permissions of the group
            ((SimpleGroup) group).registerPermissions();
            groupList.put(group.getName(), group);
            plugin.getLogger().info("Permission Group loaded: " + group.getName());
        }
    }

    public void clean() {

        groupList.clear();
    }

    public void reload() {

        clean();
        load();
    }

    public Group getDefaultGroup() {

        Group group = groupList.get(DEFAULT);
        if (group == null) {
            throw new NullPointerException("An invalid default group is defined.");
        }
        return group;
    }

    /**
     * Adds the specified player to the specified group
     *
     * @param player The player to change
     * @param group  The group to add
     *
     * @return The new group for the player
     */
    public Group addPlayerToGroup(String player, String group) {

        return plugin.getPlayerManager().getPlayer(player).addGroup(group);
    }

    /**
     * Removes the specified player from the specified group.
     *
     * @param player The player to change
     * @param group  The group to remove
     *
     * @return The old group of the player
     */
    public Group removePlayerFromGroup(String player, String group) {

        return plugin.getPlayerManager().getPlayer(player).removeGroup(group);
    }

    /**
     * Checks if the given player is in the specified group.
     *
     * @param world to check
     * @param player to check
     * @param group to check for
     * @return true if player is in group
     */
    public boolean isPlayerInGroup(String world, String player, String group) {

        return getGroup(group).isPlayerInGroup(world, player);
    }

    /**
     * Checks if the given player is in the specified group.
     *
     * @param player to check
     * @param group to check for
     * @return true if player is in group
     */
    public boolean isPlayerInGroup(String player, String group) {

        return getGroup(group).isPlayerInGroup(player);
    }

    /**
     * Gets the specified group by name (case-insensitive)
     *
     * @param group The group's name.
     *
     * @return the group instance, or null
     */
    public Group getGroup(String group) {

        if (!groupList.containsKey(group)) {
            plugin.getLogger().warning("The specified group " + group + " was not found. Using the default group instead...");
            return getDefaultGroup();
        }
        return groupList.get(group.toLowerCase());
    }

    /**
     * Returns a set of groups of which the plugin is currently aware
     *
     * @return The current groups, as a Set
     */
    public Set<Group> getGroups() {

        return new HashSet<>(groupList.values());
    }

}
