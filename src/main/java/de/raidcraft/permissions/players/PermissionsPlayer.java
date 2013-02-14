package de.raidcraft.permissions.players;

import de.raidcraft.permissions.PermissionsPlugin;
import de.raidcraft.permissions.groups.Group;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;

/**
 * @author Silthus
 */
public class PermissionsPlayer implements Player {

    private final PermissionsPlugin plugin;
    private final String name;
    private final PermissionAttachment attachment;

    public PermissionsPlayer(PermissionsPlugin plugin, OfflinePlayer player) {

        this.plugin = plugin;
        this.name = player.getName().toLowerCase();
        this.attachment = player.getPlayer().addAttachment(plugin);
    }

    protected void registerPermissions() {

        org.bukkit.entity.Player player = Bukkit.getOfflinePlayer(getName()).getPlayer();
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
        for (String groupName : plugin.getProvider().getPlayerGroups(getName())) {
            Group group = plugin.getGroupManager().getGroup(groupName);
            addGroup(group);
        }
    }

    @Override
    public String getName() {

        return name;
    }

    @Override
    public PermissionAttachment getAttachment() {

        return attachment;
    }

    @Override
    public void addGroup(Group group) {

        org.bukkit.entity.Player player = Bukkit.getPlayer(getName());
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
        addGroup(g);
        return g;
    }

    @Override
    public void removeGroup(Group group) {

        org.bukkit.entity.Player player = Bukkit.getPlayer(getName());
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
        removeGroup(g);
        return g;
    }

}
