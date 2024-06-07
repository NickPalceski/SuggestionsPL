package me.nickpalceski.suggestionspl;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AddSuggestionCommand implements CommandExecutor {
    private final SuggestionsPL plugin;

    public AddSuggestionCommand(SuggestionsPL plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }

        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();

        // Check if player is already in the process of adding a suggestion
        if (plugin.getPlayersAddingSuggestion().containsKey(playerUUID)) {
            player.sendMessage(ChatColor.YELLOW + "You are already in the process of adding a suggestion.");
            return true;
        }

        // Start the process of adding a suggestion
        player.sendMessage(ChatColor.AQUA + "Please type the " + ChatColor.BOLD + "name" + ChatColor.AQUA + " of your suggestion in chat.");
        player.sendMessage("-----------------------------------------------");
        Map<String, String> suggestionData = new HashMap<>();
        suggestionData.put("stage", "name");
        plugin.getPlayersAddingSuggestion().put(playerUUID, suggestionData);
        return true;
    }
}

