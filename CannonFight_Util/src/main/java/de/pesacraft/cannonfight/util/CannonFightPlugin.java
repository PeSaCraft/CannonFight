package de.pesacraft.cannonfight.util;

import org.bukkit.plugin.java.JavaPlugin;

public abstract class CannonFightPlugin extends JavaPlugin {
	abstract public boolean isActivePlayer(CannonFighter c);
}
