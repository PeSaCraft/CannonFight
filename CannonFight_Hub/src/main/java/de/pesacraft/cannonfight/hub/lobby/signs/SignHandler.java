package de.pesacraft.cannonfight.hub.lobby.signs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import de.pesacraft.cannonfight.hub.CannonFightHub;
import de.pesacraft.cannonfight.hub.communication.CommunicationHubClient;
import de.pesacraft.cannonfight.util.Language;

public class SignHandler implements Listener {
	private static SignHandler instance;
	private static final String JOIN_LINE = ChatColor.AQUA + Language.get("sign.join.line0.display"); //$NON-NLS-1$
	
	private Map<String, List<Location>> signs;
	
	public SignHandler() {
		instance = this;
		
		Bukkit.getPluginManager().registerEvents(this, CannonFightHub.PLUGIN);
		
		signs = new HashMap<String, List<Location>>();
		
		File file = new File(CannonFightHub.PLUGIN.getDataFolder(), "signs.yml"); //$NON-NLS-1$
		
		try {
			file.createNewFile();
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		Configuration config = YamlConfiguration.loadConfiguration(file);
		
		for (String arena : config.getKeys(false)) {
			@SuppressWarnings("unchecked")
			List<Location> locations = (List<Location>) config.getList(arena);
			
			signs.put(arena, locations);
			
			// initialise signs
			updateSign(arena, 0);
		}
	}
	
	public void save() {
		File file = new File(CannonFightHub.PLUGIN.getDataFolder(), "signs.yml"); //$NON-NLS-1$
		
		try {
			file.createNewFile();
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		FileConfiguration config = new YamlConfiguration();
		
		for (Entry<String, List<Location>> entry : signs.entrySet())
			config.set(entry.getKey(), entry.getValue());
		
		try {
			config.save(file);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	@EventHandler
	public void onSignClick(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (event.getClickedBlock().getState() instanceof Sign) {
				Sign sign = (Sign) event.getClickedBlock().getState();
				
				if (sign.getLine(0).equals(JOIN_LINE)) {
					if (!event.getPlayer().isSneaking()) {
						// join game
						String arena = ChatColor.stripColor(sign.getLine(1));
		
						CommunicationHubClient.getInstance().sendPlayer(arena, event.getPlayer().getName());
					}
					else {
						// spectate game when sneaking
						String arena = ChatColor.stripColor(sign.getLine(1));
						
						CommunicationHubClient.getInstance().sendSpectatorToArena(arena, event.getPlayer().getName());	
					}
				}
			}
			return;
		}
	}
	
	@EventHandler
	public void onSignChange(SignChangeEvent event) {
		Player p = event.getPlayer();
		
		if (!ChatColor.stripColor(event.getLine(0)).equals(Language.get("sign.join.line0.enter"))) //$NON-NLS-1$
			// sign no join sign: not relevant for this handler
			return;
		
		// sign is a join sign
		if (!p.hasPermission("cannonfight.sign.create.join")) { //$NON-NLS-1$
			p.sendMessage(Language.get("error.no-permission")); //$NON-NLS-1$
			event.setCancelled(true);
			return;
		}
		
		// player has permission
		
		String arena = ChatColor.stripColor(event.getLine(1));
		
		event.setLine(0, JOIN_LINE);
		event.setLine(1, ChatColor.AQUA + arena);
		event.setLine(2, ChatColor.GREEN + Language.get("sign.join.sign-not-activated")); //$NON-NLS-1$
	
		List<Location> locs = signs.get(arena);
		
		if (locs == null) {
			locs = new ArrayList<Location>();
			signs.put(arena, locs);
		}
		
		locs.add(event.getBlock().getLocation());
	}
	
	public void updateSign(String arena, int amount) {
		Iterator<Location> locs = signs.get(arena).iterator();
		
		while (locs.hasNext()) {
			Location l = locs.next();
			
			Block b = l.getBlock();
			
			if (!(b.getState() instanceof Sign)) {
				// there is no sign anymore
				locs.remove();
				continue;
			}
			
			// there is still a sign
			Sign sign = (Sign) b.getState();
			sign.setLine(2, amount + Language.get("sign.join.player-waiting")); //$NON-NLS-1$
			sign.update();
		}
	}
	
	public static SignHandler getInstance() {
		return instance;
	}
}
