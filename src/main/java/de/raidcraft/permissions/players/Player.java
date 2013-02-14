package de.raidcraft.permissions.players;

import de.raidcraft.permissions.groups.Group;
import org.bukkit.permissions.PermissionAttachment;

import java.util.Set;

/**
 * Represents a player registered with the Privileges plugin
 *
 * @author Silthus
 */
public interface Player {

    public String getName();

    public PermissionAttachment getAttachment();

    public void addGroup(Group group);

    public Group addGroup(String group);

    public void removeGroup(Group group);

    public Group removeGroup(String group);

    /**
     * Gets this player's group
     *
     * @return The most powerful group of which this player is a member
     */
    public Set<Group> getGroups();

    /**
     * Fetches a string representing this player's master permission for the given world
     *
     * @param world The world on which the player is currently playing
     *
     * @return The name of the permission
     */
    public String getMasterPermission(String world);

}
