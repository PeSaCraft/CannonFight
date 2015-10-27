package de.pesacraft.cannonfight.util.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import de.pesacraft.cannonfight.util.CannonFightUtil;
import de.pesacraft.cannonfight.util.Language;

public class LanguageReloadCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 0) {
			if (!sender.hasPermission("cannonfight.command.language-reload")) {
				sender.sendMessage(Language.get("error.no-permission", true));
				return true;
			}
			
			CannonFightUtil.PLUGIN.getLogger().info(Language.get("info.reloading.language", false));
			Language.loadLanguage(CannonFightUtil.PLUGIN, CannonFightUtil.PLUGIN.getConfig().getString("language"));
			CannonFightUtil.PLUGIN.getLogger().info(Language.get("info.reloaded.language", false));
			return true;
		}
		
		sender.sendMessage(Language.get("error.wrong-usage", true).replaceAll("%command%", "/" + label));
		return true;
	}
		
}
