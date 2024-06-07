package me.nickpalceski.suggestionspl;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class SuggestionsCommand implements CommandExecutor{
    private final SuggestionsPL plugin;

    public SuggestionsCommand(SuggestionsPL plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }

        Player player = (Player) sender;
        plugin.openSuggestionsGUI(player, 0);
        return true;
    }

}
