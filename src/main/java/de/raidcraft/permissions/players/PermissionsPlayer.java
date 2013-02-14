package de.raidcraft.permissions.players;

import de.raidcraft.permissions.provider.PermissionsProvider;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Silthus
 */
public class PermissionsPlayer implements Player {

    private final PermissionsProvider provider;
    private final String name;
    private final Set<String> groups;

    public PermissionsPlayer(PermissionsProvider provider, OfflinePlayer player) {

        this.provider = provider;
        this.name = player.getName().toLowerCase();
        this.groups = provider.getPlayerGroups(name);

        for (World world : provider.getPlugin().getServer().getWorlds()) {

            Permission perm = new Permission("player." + this.name + "." + world.getName(), PermissionDefault.FALSE);
            provider.getPlugin().getServer().getPluginManager().removePermission(perm);
            perm.getChildren().clear();
            provider.getPlugin().getServer().getPluginManager().addPermission(perm);
            perm.recalculatePermissibles();
        }
    }

    @Override
    public String getName() {

        return name;
    }

    @Override
    public Set<String> getGroups() {

        return new HashSet<>(this.groups);
    }

    public String getMasterPermission(String world) {

        return "player." + this.name + "." + world;
    }

}
