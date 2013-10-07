package de.raidcraft.permissions.groups;

import de.raidcraft.api.BasePlugin;
import de.raidcraft.permissions.provider.RCPermissionsProvider;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Silthus
 */
@SuppressWarnings("unused")
public class SimpleGroup implements Group {

    private final RCPermissionsProvider provider;
    // the name of this group
    private final String name;
    private final Map<String, Set<String>> worldPermissions;
    private final Set<String> globalPermissions;

    public SimpleGroup(RCPermissionsProvider provider, String name, Map<String, Set<String>> permissions, String... globalPermissions) {

        this.provider = provider;
        this.name = name;
        this.worldPermissions = permissions;
        this.globalPermissions = new HashSet<>(Arrays.asList(globalPermissions));
    }

    protected void registerPermissions() {

        BasePlugin plugin = provider.getPlugin();

        for (World world : plugin.getServer().getWorlds()) {

            // this was already defined in the group manager
            Permission worldNode = plugin.getServer().getPluginManager().getPermission(getMasterPermission(world.getName()));

            // all the children we will register in bukkit
            Map<String, Boolean> children = new LinkedHashMap<>();

            if (worldPermissions.containsKey(world.getName())) {

                for (String node : worldPermissions.get(world.getName())) {
                    if (node.startsWith("-")) {
                        children.put(node.substring(1), false);
                    } else {
                        children.put(node, true);
                    }
                }
            }

            // also add all global permissions
            for (String node : globalPermissions) {
                if (node.startsWith("-")) {
                    children.put(node.substring(1), false);
                } else {
                    children.put(node, true);
                }
            }

            // actually register all the permissions in bukkit
            worldNode.getChildren().putAll(children);
            worldNode.recalculatePermissibles();
        }
    }

    @Override
    public String getName() {

        return this.name;
    }

    @Override
    public String getGlobalMasterPermission() {

        return "group." + name;
    }

    @Override
    public String getMasterPermission(String world) {

        return "group." + name + "." + world;
    }

    @Override
    public Set<String> getPermissions(String world) {

        if (!worldPermissions.containsKey(world)) {
            worldPermissions.put(world, new HashSet<String>());
        }
        return Collections.unmodifiableSet(worldPermissions.get(world));
    }

    @Override
    public boolean hasPermission(String permission, String world) {

        Permission perm = provider.getPlugin().getServer().getPluginManager().getPermission(getMasterPermission(world));
        if (perm != null && perm.getChildren().containsKey(permission)) {
            return perm.getChildren().get(permission);
        }
        return false;
    }

    @Override
    public boolean addPermission(String node) {

        return addPermission(null, node);
    }

    @Override
    public boolean addPermission(String world, String node) {

        if (node != null) {
            boolean success;
            if (world != null && !world.equals("null")) {
                if (!worldPermissions.containsKey(world)) {
                    worldPermissions.put(world, new HashSet<String>());
                }
                success = worldPermissions.get(world).add(node);
            } else {
                for (World w : Bukkit.getWorlds()) {
                    addPermission(w.getName(), node);
                }
                return true;
            }
            if (success) {
                Permission permission = provider.getPlugin().getServer().getPluginManager().getPermission(getMasterPermission(world));
                permission.getChildren().put(node, true);
                permission.recalculatePermissibles();
            }
            return success;
        }
        return false;
    }

    @Override
    public boolean removePermission(String node) {

        return removePermission(null, node);
    }

    @Override
    public boolean removePermission(String world, String node) {

        if (node != null) {
            boolean success;
            if (world != null && !world.equals("null")) {
                success = worldPermissions.containsKey(world) && worldPermissions.get(world).remove(node);
            } else {
                for (String w : worldPermissions.keySet()) {
                    removePermission(w, node);
                }
                return true;
            }
            if (success) {
                Permission permission = provider.getPlugin().getServer().getPluginManager().getPermission(getMasterPermission(world));
                permission.getChildren().remove(node);
                permission.recalculatePermissibles();
            }
            return success;
        }
        return false;
    }

    @Override
    public boolean isPlayerInGroup(String world, String player) {

        Player bukkitPlayer = Bukkit.getPlayer(player);
        return bukkitPlayer != null && bukkitPlayer.hasPermission(getMasterPermission(world));
    }

    @Override
    public boolean isPlayerInGroup(String player) {

        Player bukkitPlayer = Bukkit.getPlayer(player);
        return bukkitPlayer != null && bukkitPlayer.hasPermission(getGlobalMasterPermission());
    }

    @Override
    public String toString() {

        return "SimpleGroup{name=" + this.name + "}" + this.worldPermissions.toString().hashCode();
    }

    @Override
    public int hashCode() {

        int hash = 7 * 19 + this.toString().hashCode();
        hash = hash * 19 + this.worldPermissions.size();
        return hash;
    }

    @Override
    public boolean equals(Object that) {

        if (that == null) {
            return false;
        }
        if (this == that) {
            return true;
        }
        if (this.getClass() != that.getClass()) {
            return false;
        }
        SimpleGroup group = (SimpleGroup) that;
        return this.toString().equals(group.toString());
    }

}
