package de.raidcraft.permissions;

import de.raidcraft.api.BasePlugin;
import de.raidcraft.permissions.groups.GroupManager;
import de.raidcraft.permissions.listeners.PlayerListener;
import de.raidcraft.permissions.players.PlayerManager;
import de.raidcraft.permissions.provider.PermissionsProvider;

/**
 * @author Silthus
 */
public class PermissionsPlugin extends BasePlugin {

    private PermissionsProvider<? extends BasePlugin> provider;
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
        }, 1L);
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

    public <T extends BasePlugin> void registerProvider(PermissionsProvider<T> provider) {

        if (this.provider != null) {
            getLogger().severe(provider.getPlugin().getName() + " tried to register as Permission Provider when "
                    + this.provider.getPlugin().getName() + " already registered!");
        } else {
            this.provider = provider;
        }
    }

    public PermissionsProvider<? extends BasePlugin> getProvider() {

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

}
