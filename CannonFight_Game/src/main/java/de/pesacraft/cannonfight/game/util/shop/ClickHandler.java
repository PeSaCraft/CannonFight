package de.pesacraft.cannonfight.game.util.shop;

import org.bukkit.event.inventory.InventoryCloseEvent;

public interface ClickHandler {
	public void onItemInteract(ItemInteractEvent event);
	
	public void onInventoryClose(InventoryCloseEvent event);
}
