package de.raidcraft.permissions.players;

import de.raidcraft.permissions.groups.Group;
import org.bukkit.permissions.PermissionAttachment;

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

}
