package de.raidcraft.permissions.players;

import de.raidcraft.permissions.groups.Group;
import org.bukkit.permissions.PermissionAttachment;

import java.util.UUID;

/**
 * Represents a player registered with the Privileges plugin
 *
 * @author Silthus
 */
public interface Player {

    public UUID getPlayerId();

    public PermissionAttachment getAttachment();

    public void addGroup(Group group);

    public Group addGroup(String group);

    public void removeGroup(Group group);

    public Group removeGroup(String group);

    public boolean hasPermission(String node);
}
