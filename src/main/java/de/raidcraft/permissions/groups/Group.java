package de.raidcraft.permissions.groups;

import java.util.Set;
import java.util.UUID;

/**
 * @author Silthus
 */
@SuppressWarnings("unused")
public interface Group {

    // Gets this group's actual name
    public String getName();

    public String getGlobalMasterPermission();

    /**
     * Returns the master permission string for this group on the specified world
     *
     * @param world The name of the world we're fetching the permission master for
     *
     * @return A string consisting of this group's name and the specified world, prefixed by master "master.[group name].[world]"
     */
    public String getMasterPermission(String world);

    /**
     * Gets all permissions that are attached to this group.
     *
     * @param world The name of the world to fetch the permissions for
     *
     * @return A set of unique permission nodes attached to this group.
     */
    public Set<String> getPermissions(String world);

    /**
     * Checks whether this group has the specified permission node on the given world
     *
     * @param node  The permission node
     * @param world The name of the world on which we're checking the permission
     *
     * @return true if the group has the permission, otherwise false
     */
    public boolean hasPermission(String node, String world);

    /**
     * Adds the specified permission to the list for the specified world. If world is null, adds to the group's global permission list
     *
     * @param world The name of the world we're attaching the node to
     * @param node  The name of the permission node
     *
     * @return true if the permission was attached successfully, otherwise false
     */
    public boolean addPermission(String world, String node);

    // same as above but sets world to null adding the node to all worlds
    public boolean addPermission(String node);

    /**
     * Removes the specified permission node from the list for the specified world. If world is null, removes from the group's global list.
     *
     * @param world The name of the world we're removing the node from
     * @param node  The name of the permission node
     *
     * @return true if the node is removed successfully, otherwise false
     */
    public boolean removePermission(String world, String node);

    // same as above but sets world to null removing the node from all worlds
    public boolean removePermission(String node);

    /**
     * Checks if the player has the global group permission.
     *
     * @param playerId to check group for
     *
     * @return true if player is in group
     */
    public boolean isPlayerInGroup(UUID playerId);

    /**
     * Checks if the player is in this permission group.
     *
     * @param world    to check
     * @param playerId to check group for
     *
     * @return true if player is in group
     */
    public boolean isPlayerInGroup(String world, UUID playerId);
}