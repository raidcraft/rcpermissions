package de.raidcraft.permissions.groups;

import de.raidcraft.api.BasePlugin;
import de.raidcraft.api.permissions.Group;
import de.raidcraft.api.permissions.RCPermissionsProvider;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

import java.util.*;

/**
 * @author Silthus
 */
@SuppressWarnings("unused")
public class SimpleGroup implements Group {

    private final BasePlugin plugin;
    // the displayName of this group
    private final String name;
    private final Map<String, Set<String>> worldPermissions;
    private final Set<String> globalPermissions;

    public SimpleGroup(RCPermissionsProvider provider,
                       String name,
                       Map<String,
                       Set<String>> permissions,
                       String... globalPermissions) {

        this(provider, name, permissions, new HashSet<>(Arrays.asList(globalPermissions)));
    }

    public SimpleGroup(RCPermissionsProvider provider,
                       String name, Map<String,
                       Set<String>> permissions,
                       Set<String> globalPermissions) {

        this.plugin = provider.getPlugin();
        this.name = name;
        this.worldPermissions = permissions;
        this.globalPermissions = globalPermissions;
    }

    public SimpleGroup(BasePlugin plugin, String name) {

        this.plugin = plugin;
        this.name = name;
        this.worldPermissions = new HashMap<>();
        this.globalPermissions = new HashSet<>();
    }

    protected void registerPermissions() {

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

        Permission perm = plugin.getServer().getPluginManager().getPermission(getMasterPermission(world));
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
                Permission permission = plugin.getServer().getPluginManager().getPermission(getMasterPermission(world));
                if (node.startsWith("-")) {
                    permission.getChildren().put(node.substring(1), false);
                } else {
                    permission.getChildren().put(node, true);
                }
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
                Permission permission = plugin.getServer().getPluginManager().getPermission(getMasterPermission(world));
                permission.getChildren().remove(node);
                permission.recalculatePermissibles();
            }
            return success;
        }
        return false;
    }

    @Override
    public boolean isPlayerInGroup(String world, UUID playerId) {

        Player bukkitPlayer = Bukkit.getPlayer(playerId);
        return bukkitPlayer != null && bukkitPlayer.hasPermission(getMasterPermission(world));
    }

    @Override
    public boolean isPlayerInGroup(UUID playerId) {

        Player bukkitPlayer = Bukkit.getPlayer(playerId);
        return bukkitPlayer != null && bukkitPlayer.hasPermission(getGlobalMasterPermission());
    }

    @Override
    public String toString() {

        return "SimpleGroup{displayName=" + this.name + "}" + this.worldPermissions.toString().hashCode();
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
