package de.raidcraft.permissions.provider;

import de.raidcraft.api.permissions.Group;
import de.raidcraft.permissions.PermissionsPlugin;
import de.raidcraft.util.UUIDUtil;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

/**
 * @author Dragonfire
 */
public class VaultPerm extends Permission {


    private PermissionsPlugin plugin;

    public VaultPerm(PermissionsPlugin plugin) {

        this.plugin = plugin;
        Plugin vPlugin = Bukkit.getPluginManager().getPlugin("Vault");
        if (vPlugin == null) {
            plugin.getLogger().severe("Vault not found - cannot inject");
            return;
        }
        hookIntoVault(vPlugin);
    }

    private void hookIntoVault(Plugin vault) {

        try {
            Bukkit.getServicesManager().register(Permission.class, this, vault, ServicePriority.Normal);
            Permission testPerm = Bukkit.getServer().getServicesManager().getRegistration(Permission.class).getProvider();
            plugin.getLogger().info(plugin.getName() + " hooked into Vault, enabled: " + testPerm.isEnabled());
        } catch (Exception e) {
            plugin.getLogger().warning("cannot inject Vault - incompatible version?");
            e.printStackTrace();
        }
    }


    @Override
    public String getName() {

        return plugin.getName();
    }

    @Override
    public boolean isEnabled() {

        return true;
    }

    @Override
    public boolean hasSuperPermsCompat() {

        return true;
    }

    @Override
    @Deprecated
    public boolean playerHas(String worldName, String playerName, String node) {

        return plugin.getPlayerManager().hasPermission(UUIDUtil.convertPlayer(playerName), worldName, node);
    }

    public boolean playerHas(String world, OfflinePlayer player, String permission) {

        return plugin.getPlayerManager().hasPermission(player.getUniqueId(), world, permission);
    }

    @Override
    @Deprecated
    public boolean playerAdd(String world, String player, String permission) {

        plugin.getPlayerManager().getPlayer(UUID.fromString(player)).addPermission(world, permission);
        return true;
    }

    public boolean playerAdd(String world, OfflinePlayer player, String permission) {

        plugin.getPlayerManager().getPlayer(player.getUniqueId()).addPermission(world, permission);
        return true;
    }

    @Override
    @Deprecated
    public boolean playerRemove(String world, String player, String permission) {

        plugin.getPlayerManager().getPlayer(UUID.fromString(player)).removePermission(world, permission);
        return true;
    }

    @Override
    public boolean playerRemove(String world, OfflinePlayer player, String permission) {

        plugin.getPlayerManager().getPlayer(player.getUniqueId()).removePermission(world, permission);
        return true;
    }

    @Override
    public boolean groupHas(String worldName, String groupName, String node) {

        Group group = plugin.getGroupManager().getGroup(groupName);
        return group != null && group.hasPermission(node, worldName);
    }

    @Override
    public boolean groupAdd(String worldName, String groupName, String node) {

        Group group = plugin.getGroupManager().getGroup(groupName);
        if (group != null) {
            group.addPermission(worldName, node);
            return true;
        }
        return false;
    }

    @Override
    public boolean groupRemove(String worldName, String groupName, String node) {

        Group group = plugin.getGroupManager().getGroup(groupName);
        if (group != null) {
            group.removePermission(worldName, node);
            return true;
        }
        return false;
    }


    @Override
    @Deprecated
    public boolean playerInGroup(String worldName, String playerName, String groupName) {

        return plugin.getGroupManager().isPlayerInGroup(worldName, UUIDUtil.convertPlayer(playerName), groupName);
    }

    @Override
    public boolean playerInGroup(String world, OfflinePlayer player, String group) {

        return plugin.getGroupManager().isPlayerInGroup(world, player.getUniqueId(), group);
    }


    @Override
    @Deprecated
    public boolean playerAddGroup(String worldName, String playerName, String groupName) {

        return plugin.getGroupManager().addPlayerToGroup(UUIDUtil.convertPlayer(playerName), groupName) != null;
    }

    @Override
    public boolean playerAddGroup(String world, OfflinePlayer player, String group) {

        return plugin.getGroupManager().addPlayerToGroup(player.getUniqueId(), group) != null;
    }

    @Override
    @Deprecated
    public boolean playerRemoveGroup(String worldName, String playerName, String groupName) {

        return plugin.getGroupManager().removePlayerFromGroup(UUIDUtil.convertPlayer(playerName), groupName) != null;
    }

    @Override
    public boolean playerRemoveGroup(String world, OfflinePlayer player, String group) {

        return plugin.getGroupManager().removePlayerFromGroup(player.getUniqueId(), group) != null;
    }

    @Override
    @Deprecated
    public String[] getPlayerGroups(String world, String playerName) {

        Set<String> playerGroups = plugin.getProvider().getPlayerGroups(UUIDUtil.convertPlayer(playerName));
        return playerGroups.toArray(new String[0]);
    }

    @Override
    public String[] getPlayerGroups(String world, OfflinePlayer player) {

        Set<String> playerGroups = plugin.getProvider().getPlayerGroups(player.getUniqueId());
        return playerGroups.toArray(new String[0]);
    }

    @Override
    @Deprecated
    public String getPrimaryGroup(String s, String s2) {

        return plugin.getProvider().getDefaultGroup().getName();
    }

    @Override
    public String getPrimaryGroup(String world, OfflinePlayer player) {

        return getPrimaryGroup(world, player);
    }

    @Override
    public String[] getGroups() {

        ArrayList<String> groupStrings = new ArrayList<String>();
        Set<Group> groups = plugin.getGroupManager().getGroups();
        for (Group group : groups) {
            groupStrings.add(group.getName());
        }
        return groupStrings.toArray(new String[0]);
    }

    @Override
    public boolean hasGroupSupport() {

        return true;
    }
}
