package de.raidcraft.permissions;

import com.sk89q.wepif.PermissionsProvider;
import de.raidcraft.api.BasePlugin;
import de.raidcraft.permissions.groups.GroupManager;
import de.raidcraft.permissions.listeners.PlayerListener;
import de.raidcraft.permissions.players.PlayerManager;
import de.raidcraft.permissions.provider.RCPermissionsProvider;
import de.raidcraft.util.UUIDUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Set;

/**
 * @author Silthus
 */
public class PermissionsPlugin extends BasePlugin implements PermissionsProvider {

    private RCPermissionsProvider<? extends BasePlugin> provider;
    // managers and handlers
    private PlayerManager playerManager;
    private GroupManager groupManager;

    @Override
    public void enable() {

        registerEvents(new PlayerListener(this));

        // lets wait 1 tick after all plugins loaded and then register all permissions from all providers
        getServer().getScheduler().runTaskLater(this, new Runnable() {
            public void run() {

                registerPermissions();
                updatePermissions();

            }
        }, 20L);
    }

    @Override
    public void disable() {

        playerManager.disable();
    }

    public void reload() {

        playerManager.disable();
        groupManager.clean();
        registerPermissions();
        updatePermissions();
    }

    public <T extends BasePlugin> void registerProvider(RCPermissionsProvider<T> provider) {

        if (this.provider != null) {
            getLogger().severe(provider.getPlugin().getName() + " tried to register as Permission Provider when "
                    + this.provider.getPlugin().getName() + " already registered!");
        } else {
            this.provider = provider;
        }
    }

    public RCPermissionsProvider<? extends BasePlugin> getProvider() {

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

        groupManager.reload();
        playerManager.reload();
    }

    public PlayerManager getPlayerManager() {

        return this.playerManager;
    }

    public GroupManager getGroupManager() {

        return this.groupManager;
    }

    // WORLDEDIT PERMISSION PROVIDER METHODS

    @Override
    // TODO: UUID
    public boolean hasPermission(String name, String permission) {

        // RaidCraft.LOGGER.info("Permisson '" + permission + "' requested for " + name + "");
        Player player = Bukkit.getPlayer(name);
        if(player == null) return false;
        String[] permParts = permission.split("\\.");
        if(player.hasPermission(permission)) {
            return true;
        }
        else if(permParts.length > 0 && permParts[0] != null) {
            return player.hasPermission(permParts[0] + ".*");
        }
        return false;
    }

    @Override
    // TODO: UUID
    public boolean hasPermission(String worldName, String name, String permission) {

        Player player = Bukkit.getPlayer(name);
        if(player == null) return false;

        return hasPermission(name, permission);
    }

    @Override
    // TODO: UUID
    public boolean inGroup(String player, String group) {

        Set<String> groups = provider.getPlayerGroups(UUIDUtil.convertPlayer(player));
        if(groups == null) return false;
        return groups.contains(group);
    }

    @Override
    // TODO: UUID
    public String[] getGroups(String player) {

        Set<String> groups = provider.getPlayerGroups(UUIDUtil.convertPlayer(player));
        if(groups == null) return new String[]{};
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
}
