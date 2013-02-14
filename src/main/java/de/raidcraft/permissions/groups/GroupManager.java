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
    // maps the players and all their groups they are in
    private Map<String, Set<String>> players = new HashMap<>();

    public GroupManager(PermissionsPlugin plugin) {

        this.plugin = plugin;
        this.DEFAULT = plugin.getProvider().getDefaultGroup().getName();
        load();
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
        }
    }

    public void clean() {

        groupList.clear();
        players.clear();
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
     * @param group  The group to set
     *
     * @return The new group for the player
     */
    public Group addPlayerToGroup(String player, String group) {

        Group g = getGroup(group);
        if (!players.containsKey(player)) {
            players.put(player.toLowerCase(), new HashSet<String>());
        }
        if (g != null) players.get(player).add(g.getName());
        return g;
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

    public Set<Group> getPlayerGroups(String player) {

        HashSet<Group> groups = new HashSet<>();
        for (String group : players.get(player)) {
            groups.add(getGroup(group));
        }
        return groups;
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
