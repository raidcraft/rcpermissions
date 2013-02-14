package de.raidcraft.permissions.groups;

import de.raidcraft.api.BasePlugin;
import de.raidcraft.permissions.provider.PermissionsProvider;
import org.bukkit.World;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

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

    private final PermissionsProvider provider;
    // the name of this group
    private final String name;
    // maps the worlds to the permission nodes of that world
    private final Map<String, Set<String>> permissions;

    public SimpleGroup(PermissionsProvider provider, String name, Map<String, Set<String>> permissions) {

        this.provider = provider;
        this.name = name;
        this.permissions = permissions;

        BasePlugin plugin = provider.getPlugin();

        for (World world : plugin.getServer().getWorlds()) {

            String masterPermission = getMasterPermission(world.getName());
            Permission worldPerm = plugin.getServer().getPluginManager().getPermission(masterPermission);
            if (worldPerm == null) {
                worldPerm = new Permission(masterPermission);
            }
            // clear out all existing permissions
            worldPerm.setDefault(PermissionDefault.FALSE);
            worldPerm.getChildren().clear();

            // all the children we will register in bukkit
            Map<String, Boolean> children = new LinkedHashMap<>();

            if (permissions.containsKey(world.getName())) {
                for (String node : permissions.get(world.getName())) {
                    if (node.startsWith("-")) {
                        children.put(node.substring(1), false);
                    } else {
                        children.put(node, true);
                    }
                }
            }

            if (!children.containsKey("group." + name)) {
                children.put("group." + name, true);
            }
            // actually register all the permissions in bukkit
            worldPerm.getChildren().putAll(children);

            if (plugin.getServer().getPluginManager().getPermission(worldPerm.getName()) == null) {
                plugin.getServer().getPluginManager().addPermission(worldPerm);
            }
            worldPerm.recalculatePermissibles();
        }
    }

    @Override
    public String getName() {

        return this.name;
    }

    @Override
    public String getMasterPermission(String world) {

        return "master." + name + "." + world;
    }

    @Override
    public Set<String> getPermissions(String world) {

        if (!permissions.containsKey(world)) {
            permissions.put(world, new HashSet<String>());
        }
        return Collections.unmodifiableSet(permissions.get(world));
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
            boolean success = false;
            if (world != null && !world.equals("null")) {
                if (!permissions.containsKey(world)) {
                    permissions.put(world, new HashSet<String>());
                }
                success = permissions.get(world).add(node);
            } else {
                for (String w : permissions.keySet()) {
                    success = addPermission(w, node);
                }
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
            boolean success = true;
            if (world != null && !world.equals("null")) {
                success = permissions.containsKey(world) && permissions.get(world).remove(node);
            } else {
                for (String w : permissions.keySet()) {
                    success = removePermission(w, node);
                }
            }
            return success;
        }
        return false;
    }

    @Override
    public String toString() {

        return "SimpleGroup{name=" + this.name + "}" + this.permissions.toString().hashCode();
    }

    @Override
    public int hashCode() {

        int hash = 7 * 19 + this.toString().hashCode();
        hash = hash * 19 + this.permissions.size();
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
