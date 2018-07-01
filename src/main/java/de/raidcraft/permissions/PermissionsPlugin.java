package de.raidcraft.permissions;

import com.sk89q.wepif.PermissionsResolver;
import de.raidcraft.RaidCraft;
import de.raidcraft.api.BasePlugin;
import de.raidcraft.api.RaidCraftException;
import de.raidcraft.api.permissions.RCPermissionsProvider;
import de.raidcraft.permissions.commands.AdminCommands;
import de.raidcraft.permissions.groups.GroupManager;
import de.raidcraft.permissions.listeners.PlayerListener;
import de.raidcraft.permissions.players.PlayerManager;
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
public class PermissionsPlugin extends BasePlugin implements PermissionsResolver {

    @Getter
    private PlayerManager playerManager;
    @Getter
    private GroupManager groupManager;

    @Override
    public void enable() {
        registerCommands(AdminCommands.class);
        registerEvents(new PlayerListener(this));
        new VaultPerm(PermissionsPlugin.this);
        playerManager = new PlayerManager(this);
        groupManager = new GroupManager(this);
        RaidCraft.setPermissionGroupManager(groupManager);

        // lets wait 1 tick after all plugins loaded and then register all permissions from all providers
        getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
            registerPermissions();
            updatePermissions();
        }, 2 * 20);

    }

    @Override
    public void disable() {
        playerManager.disable();
    }

    public void reload() {
        try {
            groupManager.reload();
            playerManager.reload();
        } catch (RaidCraftException e) {
            e.printStackTrace();
        }
    }

    public RCPermissionsProvider<? extends BasePlugin> getProvider() {
        RCPermissionsProvider provider = RaidCraft.getPermissionsProvider();
        if (provider == null) {
            getLogger().severe("No provider was registered! Shutting down the server to be save...");
            getServer().shutdown();
        }
        return provider;
    }

    private void registerPermissions() {

        playerManager = new PlayerManager(this);
        groupManager = new GroupManager(this);
    }

    private void updatePermissions() {

        this.reload();
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

        //RaidCraft.LOGGER.info("[RCPERM] hasPerm.: " + displayName + " | " + permission);

        UUID playerId = UUIDUtil.convertPlayer(name);
        Player player = Bukkit.getPlayer(playerId);
        if (player == null) {
            return false;
        }
        String[] permParts = permission.split("\\.");
        if (player.hasPermission(permission)) {
            return true;
        } else if (permParts.length > 0 && permParts[0] != null) {
            String starPerm = "";
            int i = 1;
            for(String part : permParts) {
                i++;
                starPerm += part + ".";
                if(i == permParts.length) {
                    starPerm += "*";
                    break;
                }
            }
            return player.hasPermission(starPerm);
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

        return hasPermission(player, "group." + group.toLowerCase());
    }

    @Override
    @Deprecated
    // TODO: UUID
    public String[] getGroups(String player) {
        Set<String> groups = getProvider().getPlayerGroups(UUIDUtil.convertPlayer(player));
        if (groups == null) {
            return new String[]{};
        }
        return groups.toArray(new String[groups.size()]);
    }

    @Override
    public boolean hasPermission(OfflinePlayer player, String permission) {
        return hasPermission(UUIDUtil.getNameFromUUID(player.getUniqueId()), permission);
    }

    @Override
    public boolean hasPermission(String worldName, OfflinePlayer player, String permission) {
        return hasPermission(worldName, UUIDUtil.getNameFromUUID(player.getUniqueId()), permission);
    }

    @Override
    public boolean inGroup(OfflinePlayer player, String group) {
        return inGroup(UUIDUtil.getNameFromUUID(player.getUniqueId()), group);
    }

    @Override
    public String[] getGroups(OfflinePlayer player) {
        return getGroups(UUIDUtil.getNameFromUUID(player.getUniqueId()));
    }

    @Override
    public void load() {

    }

    @Override
    public String getDetectionMessage() {
        return "Using Raid-Craft Permissions API";
    }

    // #############################################################
}
