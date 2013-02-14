package de.raidcraft.permissions.provider;

import de.raidcraft.api.BasePlugin;
import de.raidcraft.permissions.groups.Group;

import java.util.List;
import java.util.Set;

/**
 * Defines a provider that takes care of the saving and loading of permission nodes.
 * The provider needs the be registered with the {@link de.raidcraft.permissions.PermissionsPlugin}
 * and then is queried when the Plugin loads.
 *
 * @author Silthus
 */
public interface PermissionsProvider<T extends BasePlugin> {

    public T getPlugin();

    // gets the registered default group that needs to be used
    public Group getDefaultGroup();

    /**
     * Gets a list of groups provided by the PermissionsProvider. All underlying
     * saving and storing of those groups is taken care of by the provider.
     *
     * @return List of constructed groups that should be made available for all players.
     */
    public List<Group> getGroups();

    /**
     * Gets a list of all the groups a player belongs to. Needs to return the groups
     * even if the player is not logged in.
     *
     * @param player to get the groups for
     * @return Groups of the player
     */
    public Set<String> getPlayerGroups(String player);
}
