package de.raidcraft.permissions.groups;

import de.raidcraft.permissions.PermissionsPlugin;
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
            Permission perm = new Permission("group." + group);

            perm.setDescription("If true, the attached player is a member of the group: " + group);
            perm.setDefault(PermissionDefault.FALSE);
            if (plugin.getServer().getPluginManager().getPermission(perm.getName()) == null) {
                plugin.getServer().getPluginManager().addPermission(perm);
            }
            group.addPermission(null, perm.getName());

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
