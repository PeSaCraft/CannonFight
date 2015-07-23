package de.pesacraft.cannonfight.game.listener;


import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import de.pesacraft.cannonfight.game.cannons.Cannon;
import de.pesacraft.cannonfight.game.cannons.Cannons;
import de.pesacraft.cannonfight.game.exceptions.player.NotEnoughAmmoException;

public class PlayerUseCannonListener implements Listener {

	@EventHandler(priority = EventPriority.NORMAL)
	public void onItemUse(PlayerInteractEvent e) {
		if (e.getAction() == Action.PHYSICAL) return;
		
		if (e.getItem() == null) return;
		
		for (Cannon c : Cannons.getCannons()) {
			if (c.getItemStack().equals(e.getItem())) {
				try {
					c.fire(null);
				} catch (Exception ex) {
					if (ex instanceof NotEnoughAmmoException)
						((NotEnoughAmmoException) ex).getCannonFighter().getPlayer().sendMessage(ex.getLocalizedMessage());
					ex.printStackTrace();
				}
			}
		}
	}
}
