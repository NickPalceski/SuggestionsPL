package me.nickpalceski.suggestionspl;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DeleteSuggestionCommand implements CommandExecutor {
    private final SuggestionsPL plugin;

    public DeleteSuggestionCommand(SuggestionsPL plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }

        Player player = (Player) sender;

        // Command logic for deleting suggestions
        plugin.openSuggestionsGUIForDeletion(player, 0);

        return true;
    }

}
