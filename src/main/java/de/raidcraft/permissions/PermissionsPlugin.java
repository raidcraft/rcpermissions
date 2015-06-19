package de.raidcraft.permissions;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.wepif.PermissionsProvider;
import de.raidcraft.api.BasePlugin;
import de.raidcraft.permissions.groups.GroupManager;
import de.raidcraft.permissions.listeners.PlayerListener;
import de.raidcraft.permissions.players.PlayerManager;
import de.raidcraft.permissions.provider.RCPermissionsProvider;
import de.raidcraft.util.PastebinPoster;
import de.raidcraft.util.UUIDUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

        registerCommands(DebugCommand.class);
        registerEvents(new PlayerListener(this));

        // lets wait 1 tick after all plugins loaded and then register all permissions from all providers
        getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            public void run() {

                registerPermissions();
                updatePermissions();
                new VaultPerm(PermissionsPlugin.this);

            }
        }, 2 * 20);
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
    @Deprecated
    // TODO: UUID
    public boolean hasPermission(String name, String permission) {
        UUID playerId = UUIDUtil.convertPlayer(name);
        Player player = Bukkit.getPlayer(playerId);
        if (player == null) return false;
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
        if (playerName == null) return false;

        return hasPermission(playerName, permission);
    }

    @Override
    @Deprecated
    // TODO: UUID
    public boolean inGroup(String player, String group) {

        Set<String> groups = provider.getPlayerGroups(UUIDUtil.convertPlayer(player));
        if (groups == null) return false;
        return groups.contains(group);
    }

    @Override
    @Deprecated
    // TODO: UUID
    public String[] getGroups(String player) {

        Set<String> groups = provider.getPlayerGroups(UUIDUtil.convertPlayer(player));
        if (groups == null) return new String[]{};
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

    public static class DebugCommand {

        private final PermissionsPlugin plugin;

        public DebugCommand(PermissionsPlugin plugin) {

            this.plugin = plugin;
        }

        @Command(
                aliases = "permissions",
                desc = "Prints all permissions"
        )
        public void debug(CommandContext args, CommandSender sender) {

            List<String> permissions = sender.getEffectivePermissions().stream()
                    .map(PermissionAttachmentInfo::getPermission)
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());

            // lets send it to pastebin
            sender.sendMessage(ChatColor.YELLOW + "Pasting the debug output to pastebin...");
            PastebinPoster.paste(permissions.stream().collect(Collectors.joining("\n")), new PastebinPoster.PasteCallback() {
                @Override
                public void handleSuccess(String url) {

                    sender.sendMessage(ChatColor.GREEN + "Hero debug was pasted to: " + url);
                }

                @Override
                public void handleError(String err) {

                    sender.sendMessage(ChatColor.RED + "Error pasting hero debug output to pastebin!");
                }
            });
        }
    }
}
