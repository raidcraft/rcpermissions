package de.raidcraft.permissions;

import com.sk89q.wepif.PermissionsProvider;
import de.raidcraft.api.BasePlugin;
import de.raidcraft.permissions.commands.AdminCommands;
import de.raidcraft.permissions.groups.GroupManager;
import de.raidcraft.permissions.listeners.PlayerListener;
import de.raidcraft.permissions.players.PlayerManager;
import de.raidcraft.permissions.provider.DatabaseProvder;
import de.raidcraft.permissions.provider.RCPermissionsProvider;
import de.raidcraft.permissions.provider.VaultPerm;
import de.raidcraft.permissions.tables.TPermission;
import de.raidcraft.permissions.tables.TPermissionGroupMember;
import de.raidcraft.util.UUIDUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * @author Silthus
 */
public class PermissionsPlugin extends BasePlugin implements PermissionsProvider {

    private DatabaseProvder provider;
    @Getter
    private PlayerManager playerManager;
    @Getter
    private GroupManager groupManager;

    @Override
    public void enable() {
        registerCommands(AdminCommands.class);
        registerEvents(new PlayerListener(this));
        new VaultPerm(PermissionsPlugin.this);
        setupPermissions();
        provider = new DatabaseProvder(this);
        playerManager = new PlayerManager(this);
        groupManager = new GroupManager(this);
    }

    @Override
    public void disable() {
        playerManager.disable();
    }

    public void reload() {
        provider.reload();
        groupManager.reload();
        playerManager.reload();
    }

    public RCPermissionsProvider<? extends BasePlugin> getProvider() {
        if (provider == null) {
            getLogger().severe("No provider was registered! Shutting down the server to be save...");
            getServer().shutdown();
        }
        return provider;
    }

    @Override
    public List<Class<?>> getDatabaseClasses() {
        List<Class<?>> tables = new ArrayList<>();
        tables.add(TPermission.class);
        tables.add(TPermissionGroupMember.class);
        return tables;
    }

    // #############################################################
    // WorldEdit Permissions Interoperability Framework
    // http://wiki.sk89q.com/wiki/WEPIF

    @Override
    @Deprecated
    // TODO: UUID
    public boolean hasPermission(String name, String permission) {
        UUID playerId = UUIDUtil.convertPlayer(name);
        Player player = Bukkit.getPlayer(playerId);
        if (player == null) {
            return false;
        }
        String[] permParts = permission.split("\\.");
        if (player.hasPermission(permission)) {
            return true;
        } else if (permParts.length > 0 && permParts[0] != null) {
            return player.hasPermission(permParts[0] + ".*");
        }
        return false;
    }

    @Override
    @Deprecated
    // TODO: UUID
    public boolean hasPermission(String worldName, String playerName, String permission) {
        if (playerName == null) {
            return false;
        }
        return hasPermission(playerName, permission);
    }

    @Override
    @Deprecated
    // TODO: UUID
    public boolean inGroup(String player, String group) {
        Set<String> groups = provider.getPlayerGroups(UUIDUtil.convertPlayer(player));
        if (groups == null) {
            return false;
        }
        return groups.contains(group);
    }

    @Override
    @Deprecated
    // TODO: UUID
    public String[] getGroups(String player) {
        Set<String> groups = provider.getPlayerGroups(UUIDUtil.convertPlayer(player));
        if (groups == null) {
            return new String[]{};
        }
        return groups.toArray(new String[groups.size()]);
    }

    @Override
    public boolean hasPermission(OfflinePlayer player, String permission) {
        return hasPermission(player.getName(), permission);
    }

    @Override
    public boolean hasPermission(String worldName, OfflinePlayer player, String permission) {
        return hasPermission(worldName, player.getName(), permission);
    }

    @Override
    public boolean inGroup(OfflinePlayer player, String group) {
        return inGroup(player.getName(), group);
    }

    @Override
    public String[] getGroups(OfflinePlayer player) {
        return getGroups(player.getName());
    }

    // #############################################################
}
