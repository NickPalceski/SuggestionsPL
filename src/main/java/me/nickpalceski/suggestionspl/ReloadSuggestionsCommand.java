package me.nickpalceski.suggestionspl;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReloadSuggestionsCommand implements CommandExecutor {
    private final SuggestionsPL plugin;

    public ReloadSuggestionsCommand(SuggestionsPL plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player && !sender.hasPermission("suggestions.reload")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        plugin.reloadConfig();
        plugin.setDiscordLink(plugin.getConfig().getString("link", "https://discord.gg/yourinvite"));
        sender.sendMessage(ChatColor.GREEN + "Suggestions plugin configuration reloaded.");
        return true;
    }
}
