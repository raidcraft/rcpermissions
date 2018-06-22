package de.raidcraft.permissions.players;

import de.raidcraft.api.permissions.Group;
import org.bukkit.permissions.PermissionAttachment;

import java.util.UUID;

/**
 * Represents a player registered with the Privileges plugin
 *
 * @author Silthus
 */
public interface Player {

    UUID getPlayerId();

    PermissionAttachment getAttachment();

    void addPermission(String permission);

    void addPermission(String world, String permission);

    void removePermission(String permission);

    void removePermission(String world, String permission);

    void addGroup(Group group);

    Group addGroup(String group);

    void removeGroup(Group group);

    Group removeGroup(String group);

    boolean hasPermission(String node);
}
