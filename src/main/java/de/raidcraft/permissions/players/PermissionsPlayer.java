package de.raidcraft.permissions.players;

import de.raidcraft.permissions.PermissionsPlugin;
import de.raidcraft.permissions.groups.Group;
import de.raidcraft.permissions.groups.SimpleGroup;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.UUID;

/**
 * @author Silthus
 */
public class PermissionsPlayer implements Player {

    private final PermissionsPlugin plugin;
    @Getter
    private final UUID playerId;
    @Getter
    private PermissionAttachment attachment;
    private final Group playerGroup;

    public PermissionsPlayer(PermissionsPlugin plugin, OfflinePlayer player) {

        this.plugin = plugin;
        this.playerId = player.getUniqueId();
        this.playerGroup = new SimpleGroup(plugin, player.getName());
        plugin.getGroupManager().updateGroupPermissions(playerGroup);
        if (player.isOnline()) {
            this.attachment = player.getPlayer().addAttachment(plugin);
        }
    }

    protected void registerPermissions() {

        org.bukkit.entity.Player player = Bukkit.getPlayer(getPlayerId());
        if (attachment == null) {
            attachment = player.getPlayer().addAttachment(plugin);
        }
        // clear the player's permissions
        for (PermissionAttachmentInfo info : player.getEffectivePermissions()) {
            PermissionAttachment att = info.getAttachment();
            if (att == null) {
                continue;
            }
            att.unsetPermission(info.getPermission());
        }
        // unset all old permissions
        attachment.getPermissions().clear();

        // add all the main nodes of the groups
        for (String groupName : plugin.getProvider().getPlayerGroups(getPlayerId())) {
            Group group = plugin.getGroupManager().getGroup(groupName);
            addGroup(group);
        }
        // add our player group to the permissions
        addGroup(playerGroup);
    }

    private void recalculatePermissions() {

        org.bukkit.entity.Player player = Bukkit.getPlayer(getPlayerId());
        if (player != null) player.recalculatePermissions();
    }

    @Override
    public void addPermission(String permission) {

        playerGroup.addPermission(permission);
        recalculatePermissions();
    }

    @Override
    public void addPermission(String world, String permission) {

        playerGroup.addPermission(world, permission);
        recalculatePermissions();
    }

    @Override
    public void removePermission(String permission) {

        playerGroup.removePermission(permission);
        recalculatePermissions();
    }

    @Override
    public void removePermission(String world, String permission) {

        playerGroup.removePermission(world, permission);
        recalculatePermissions();
    }

    @Override
    public void addGroup(Group group) {

        org.bukkit.entity.Player player = Bukkit.getPlayer(getPlayerId());
        if (player == null) {
            return;
        }
        // register the global group node
        attachment.setPermission(group.getGlobalMasterPermission(), true);
        // attachment.setPermission(group.getMasterPermission(player.getWorld().getName()), true);
        player.recalculatePermissions();
    }

    @Override
    public Group addGroup(String group) {

        Group g = plugin.getGroupManager().getGroup(group);
        if(g.getGlobalMasterPermission().equalsIgnoreCase(plugin.getGroupManager().getDefaultGroup().getName())) {
            // Create dummy group
            g = new SimpleGroup(plugin, group);
        }
        addGroup(g);
        return g;
    }

    @Override
    public void removeGroup(Group group) {

        org.bukkit.entity.Player player = Bukkit.getPlayer(getPlayerId());
        if (player == null) {
            return;
        }
        // register the global group node
        attachment.unsetPermission(group.getGlobalMasterPermission());
        // attachment.unsetPermission(group.getMasterPermission(player.getWorld().getName()));
        player.recalculatePermissions();
    }

    @Override
    public Group removeGroup(String group) {

        Group g = plugin.getGroupManager().getGroup(group);
        if(g.getGlobalMasterPermission().equalsIgnoreCase(plugin.getGroupManager().getDefaultGroup().getName())) {
            // Create dummy group
            g = new SimpleGroup(plugin, group);
        }
        removeGroup(g);
        return g;
    }

    @Override
    public boolean hasPermission(String node) {

        return attachment != null && getAttachment().getPermissible().hasPermission(node);
    }
}
