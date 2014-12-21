package de.froznmine.cannonfight.game;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;

import de.froznmine.lobby.game.GameUser;
import de.froznmine.lobby.game.arena.IArena;

public class Arena implements IArena {
	private static final long serialVersionUID = 4698771078013639643L;

	private final String schematicName;
	private final int maxPlayers;
	
	public Arena(File file) {
		// load stuff
		this.schematicName = (String) map.get("schematic");
		this.maxPlayers = (Integer) map.get("maxPlayers");
	}
	
	@Override
	public boolean load(Location loc) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean delete() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getMaxPlayers() {
		return this.maxPlayers;
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("schematic", schematicName);
		map.put("maxPlayers", maxPlayers);
		
		return map;
	}

	@Override
	public boolean teleport(GameUser player) {
		
		return false;
	}

}
