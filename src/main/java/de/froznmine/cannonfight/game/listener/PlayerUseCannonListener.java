package de.froznmine.cannonfight.game.listener;

import java.util.Iterator;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import de.froznmine.cannonfight.game.cannons.Cannon;
import de.froznmine.cannonfight.game.cannons.Cannons;

public class PlayerUseCannonListener implements Listener {

	@EventHandler(priority = EventPriority.NORMAL)
	public void onItemUse(PlayerInteractEvent e) {
		if (e.getAction() == Action.PHYSICAL) return;
		
		if (e.getItem() == null) return;
		
		for (Cannon c : Cannons.getCannons()) {
			if (c.getItemStack().equals(e.getItem())) {
				c.fire();
			}
		}
	}
}
