package de.pesacraft.cannonfight.util.shop;

import org.bukkit.inventory.ItemStack;

import de.pesacraft.cannonfight.data.players.CannonFighter;

public class ItemSelectEvent {
	private CannonFighter fighter;
	private ItemStack item;
	private boolean closeInv;
	
	public ItemSelectEvent(CannonFighter fighter, ItemStack item) {
		this.fighter = fighter;
		this.item = item;
		this.closeInv = false;
	}
	
	public CannonFighter getFighter() {
		return fighter;
	}
	
	public ItemStack getItem() {
		return item;
	}
	
	public boolean closeInventory() {
		return closeInv;
	}
	
	public void setCloseInventory(boolean closeInv) {
		this.closeInv = closeInv;
	}
}
