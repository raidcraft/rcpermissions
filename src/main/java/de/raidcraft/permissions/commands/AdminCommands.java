package de.raidcraft.permissions.commands;

import com.sk89q.minecraft.util.commands.*;
import de.raidcraft.permissions.PermissionsPlugin;
import de.raidcraft.util.PastebinPoster;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Dragonfire
 */
public class AdminCommands {

    public AdminCommands(PermissionsPlugin module) {

    }

    @Command(
            aliases = {"rcperm"},
            desc = "RC Permission Plugin"
    )
    @NestedCommand(PermCommands.class)
    public void perm(CommandContext context, CommandSender sender) throws CommandException {

    }

    public static class PermCommands {
        private final PermissionsPlugin plugin;

        public PermCommands(PermissionsPlugin module) {
            this.plugin = module;
        }

        @Command(
                aliases = {"reload"},
                desc = "Reloads the permission plugin"
        )
        @CommandPermissions("rcperm.admin.reload")
        public void reload(CommandContext args, CommandSender sender) {

            plugin.reload();
            sender.sendMessage(ChatColor.GREEN + "Reloaded Permission Plugin sucessfully!");
        }

        @Command(
                aliases = {"list", "permissions"},
                desc = "Prints all permissions"
        )
        @CommandPermissions("rcperm.list")
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
