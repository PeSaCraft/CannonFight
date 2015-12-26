package de.pesacraft.cannonfight.util;

import org.bukkit.plugin.java.JavaPlugin;

import de.pesacraft.cannonfight.util.game.BlockManager;

public abstract class CannonFightPlugin extends JavaPlugin {
	abstract public boolean isActivePlayer(CannonFighter c);
	
	abstract public BlockManager getBlockManager();
}
