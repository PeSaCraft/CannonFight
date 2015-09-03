package de.pesacraft.cannonfight.lobby.signs;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import de.pesacraft.cannonfight.CannonFight;
import de.pesacraft.cannonfight.Language;
import de.pesacraft.cannonfight.commands.JoinCommand;
import de.pesacraft.cannonfight.data.players.CannonFighter;
import de.pesacraft.cannonfight.game.Arena;
import de.pesacraft.cannonfight.game.Arenas;
import de.pesacraft.cannonfight.game.GameManager;
import de.pesacraft.cannonfight.lobby.shops.CannonShop;

public class JoinSigns implements Listener {
	public JoinSigns() {
		Bukkit.getPluginManager().registerEvents(this, CannonFight.PLUGIN);
	}
	
	@EventHandler
	public void onSignChange(SignChangeEvent event) {
		Player p = event.getPlayer();
		
		if (!ChatColor.stripColor(event.getLine(1)).equals("[CFJoin]"))
			// sign no join sign: not relevant for this handler
			return;
		
		// sign is a join sign
		if (!p.hasPermission("cannonfight.sign.create.join")) {
			p.sendMessage(Language.get("error.no-permission"));
			event.setCancelled(true);
			return;
		}
		
		Arena arena = Arenas.getArena(ChatColor.stripColor(event.getLine(2)));
		// player has permission
		if (arena == null) {
			// such an arena doesn't exist!
			p.sendMessage(Language.get("error.no-such-arena"));
			event.setCancelled(true);
			return;	
		}
		// arena exists
		GameManager man = GameManager.getForArena(arena);
		
		event.setLine(0, ChatColor.AQUA + "CannonFight");
		event.setLine(1, ChatColor.AQUA + "[" + ChatColor.GOLD + "Join" + ChatColor.AQUA + "]");
		event.setLine(2, ChatColor.AQUA + ChatColor.stripColor(event.getLine(2)));
		event.setLine(3, ChatColor.GREEN + "" + man.getQueueSize() + " Spieler wartend");
	
		man.addJoinSign((Sign) event.getBlock().getState());
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onSignClick(final PlayerInteractEvent event) {
		if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK)
			// cannot be a sign click
			return;
		
		if (event.getClickedBlock().getType() != Material.WALL_SIGN && event.getClickedBlock().getType() != Material.SIGN_POST)
			// no sign clicked
			return;
		
		final Sign sign = (Sign) event.getClickedBlock().getState();
		final String[] lines = sign.getLines();
		
		if (!lines[0].equals(ChatColor.AQUA + "CannonFight") || !lines[1].equals(ChatColor.AQUA + "[" + ChatColor.GOLD + "Join" + ChatColor.AQUA + "]"))
			// no valid sign
			return;
				
		// valid sign, cancel usage
		event.setCancelled(true);
		
		CannonFighter c = CannonFighter.get(event.getPlayer());
		
		if (!c.hasPermission("cannonfight.sign.use.join")) {
			c.sendMessage(Language.get("error.no-permission"));
			return;
		}
		
		if (c.isInGame() || c.isInQueue()) {
			c.sendMessage(Language.get("error.has-to-leave-before-join"));
			return;
		}
		
		// player has enough permission
		Arena a = Arenas.getArena(ChatColor.stripColor(lines[2]));
		GameManager g = GameManager.getForArena(a);
		
		g.addJoinSign(sign);
		g.updateSigns();
		
		JoinCommand.join(c, a);
	}
}
