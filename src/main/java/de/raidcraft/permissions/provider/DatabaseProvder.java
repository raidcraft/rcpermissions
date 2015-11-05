package de.raidcraft.permissions.provider;

import com.avaje.ebean.SqlRow;
import de.raidcraft.permissions.PermissionsPlugin;
import de.raidcraft.permissions.groups.Group;
import de.raidcraft.permissions.groups.SimpleGroup;
import de.raidcraft.permissions.tables.TPermission;
import de.raidcraft.permissions.tables.TPermissionGroupMember;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Dragonfire
 */
public class DatabaseProvder implements RCPermissionsProvider<PermissionsPlugin> {

    @Getter
    private final PermissionsPlugin plugin;
    @Getter
    private final List<Group> groups = new ArrayList<>();
    @Getter
    private Group defaultGroup;

    public DatabaseProvder(PermissionsPlugin plugin) {
        this.plugin = plugin;
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
