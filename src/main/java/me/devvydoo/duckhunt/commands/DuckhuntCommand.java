package me.devvydoo.duckhunt.commands;

import me.devvydoo.duckhunt.Duckhunt;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DuckhuntCommand implements CommandExecutor {

    private Duckhunt plugin;
    private DuckhuntAdminGUI adminGUI;

    public DuckhuntCommand(Duckhunt plugin) {
        this.plugin = plugin;
        this.adminGUI = new DuckhuntAdminGUI(plugin.getGame());
        Bukkit.getServer().getPluginManager().registerEvents(adminGUI, plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.isOp()){
            sender.sendMessage(ChatColor.RED + "Only admins may use this command!");
            return true;
        }

        if (sender instanceof Player)
            adminGUI.openInterface((Player)sender);

        return true;
    }
}
