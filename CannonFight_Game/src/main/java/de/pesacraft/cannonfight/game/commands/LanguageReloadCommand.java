package de.pesacraft.cannonfight.game.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;

import de.pesacraft.cannonfight.game.CannonFightGame;
import de.pesacraft.cannonfight.util.Language;

public class LanguageReloadCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 0) {
			// dieser Spieler
			
			if (!sender.hasPermission("cannonfight.command.language-reload")) {
				sender.sendMessage(Language.get("error.no-permission", true));
				return true;
			}
			
			CannonFightGame.LOGGER.info(Language.get("info.reloading.language", false));
			Language.loadLanguage(CannonFightGame.PLUGIN, CannonFightGame.PLUGIN.getConfig().getString("language"));
			CannonFightGame.LOGGER.info(Language.get("info.reloaded.language", false));
			return true;
		}
		
		sender.sendMessage(Language.get("error.wrong-usage", true).replaceAll("%command%", "/" + label));
		return true;
	}
		
}
