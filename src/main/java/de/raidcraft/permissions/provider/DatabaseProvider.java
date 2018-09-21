package de.raidcraft.permissions.provider;

import de.raidcraft.api.permissions.Group;
import de.raidcraft.api.permissions.RCPermissionsProvider;
import de.raidcraft.permissions.PermissionsPlugin;
import de.raidcraft.permissions.groups.SimpleGroup;
import de.raidcraft.permissions.tables.TPermission;
import de.raidcraft.permissions.tables.TPermissionGroupMember;
import io.ebean.SqlRow;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Dragonfire
 */
public class DatabaseProvider implements RCPermissionsProvider<PermissionsPlugin> {

    @Getter
    private final PermissionsPlugin plugin;
    @Getter
    private final List<Group> groups = new ArrayList<>();
    @Getter
    private Group defaultGroup;

    public DatabaseProvider(PermissionsPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        loadGroups();
    }

    private void loadGroups() {
        String sql = "SELECT DISTINCT group_ FROM rc_permission";
        List<SqlRow> rows = plugin.getDatabase().createSqlQuery(sql).findList();
        rows.stream().forEach(row -> loadGroup(row.getString("group_")));
        // try to find default group
        for (Group group : groups) {
            if (group.getName().equals("default")) {
                defaultGroup = group;
                break;
            }
        }
    }

    private void loadGroup(String name) {
        Map<String, Set<String>> sortedPerms = new HashMap<>();
        Set<String> globalPerm = new HashSet<>();
        List<TPermission> perms = plugin.getDatabase()
                .find(TPermission.class)
                .where()
                .eq("group_", name)
                .findList();
        for (TPermission perm : perms) {
            if (perm.getWorld() == null) {
                globalPerm.add(perm.getPermission());
            } else {
                String world = perm.getWorld();
                if (!sortedPerms.containsKey(world)) {
                    sortedPerms.put(world, new HashSet<>());
                }
                sortedPerms.get(world).add(perm.getPermission());
            }
        }
        SimpleGroup group = new SimpleGroup(this, name, sortedPerms, globalPerm);
        groups.add(group);
    }

    @Override
    public Set<String> getPlayerGroups(UUID player) {
        Set<String> playerGroups = plugin.getDatabase().find(TPermissionGroupMember.class)
                .where()
                .eq("player", player.toString())
                .findList()
                .stream()
                .map(TPermissionGroupMember::getGroup)
                .collect(Collectors.toSet());
        if (this.defaultGroup != null) {
            playerGroups.add(this.defaultGroup.getName());
        }
        return playerGroups;
    }
}
