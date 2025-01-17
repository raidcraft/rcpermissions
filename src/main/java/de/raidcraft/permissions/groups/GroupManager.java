package de.raidcraft.permissions.groups;

import de.raidcraft.api.BasePlugin;
import de.raidcraft.api.RaidCraftException;
import de.raidcraft.api.permissions.Group;
import de.raidcraft.api.permissions.RCPermissionsProvider;
import de.raidcraft.permissions.PermissionsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.*;

/**
 * A group manager that handles the creation and removal of group permissions in Privileges
 *
 * @author Silthus
 */
public class GroupManager implements de.raidcraft.api.permissions.GroupManager {

    private PermissionsPlugin plugin;
    private String DEFAULT;
    private Map<String, Group> groupList = new HashMap<>();

    public GroupManager(PermissionsPlugin plugin) {

        this.plugin = plugin;
    }

    private void load() throws RaidCraftException {

        RCPermissionsProvider<? extends BasePlugin> permissionsProvider = plugin.getProvider();

        if (permissionsProvider == null) {
            throw new RaidCraftException("Failed to load Permissions plugin! Permission Provider is NULL!");
        }

        permissionsProvider.getGroups().forEach(this::updateGroupPermissions);
        Group defaultGroup = permissionsProvider.getDefaultGroup();
        if (defaultGroup != null) {
            this.DEFAULT = defaultGroup.getName();
        }
    }

    @Override
    public Group createGroup(RCPermissionsProvider provider,
                             String name,
                             Map<String,
                                     Set<String>> permissions,
                             String... globalPermissions) {
        return new SimpleGroup(provider, name, permissions, globalPermissions);
    }

    @Override
    public void updateGroupPermissions(Group group) {

        // we need to create a permission with the group displayName for group perm lookups
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
        plugin.info("Permission Group loaded: " + group.getName(), "groups");
    }

    @Override
    public void clean() {

        groupList.clear();
    }

    @Override
    public void reload() throws RaidCraftException {

        clean();
        load();
    }

    @Override
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
     * @param playerId The player to change
     * @param group  The group to add
     *
     * @return The new group for the player
     */
    @Override
    public Group addPlayerToGroup(UUID playerId, String group) {

        return plugin.getPlayerManager().getPlayer(playerId).addGroup(group);
    }

    /**
     * Removes the specified player from the specified group.
     *
     * @param playerId The player to change
     * @param group  The group to remove
     *
     * @return The old group of the player
     */
    @Override
    public Group removePlayerFromGroup(UUID playerId, String group) {

        return plugin.getPlayerManager().getPlayer(playerId).removeGroup(group);
    }

    /**
     * Checks if the given player is in the specified group.
     *
     * @param world to check
     * @param playerId to check
     * @param group to check for
     * @return true if player is in group
     */
    @Override
    public boolean isPlayerInGroup(String world, UUID playerId, String group) {

        return getGroup(group).isPlayerInGroup(world, playerId);
    }

    /**
     * Checks if the given player is in the specified group.
     *
     * @param playerId to check
     * @param group to check for
     * @return true if player is in group
     */
    @Override
    public boolean isPlayerInGroup(UUID playerId, String group) {

        return getGroup(group).isPlayerInGroup(playerId);
    }

    /**
     * Gets the specified group by displayName (case-insensitive)
     *
     * @param group The group's displayName.
     *
     * @return the group instance, or null
     */
    @Override
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
    @Override
    public Set<Group> getGroups() {

        return new HashSet<>(groupList.values());
    }

}
