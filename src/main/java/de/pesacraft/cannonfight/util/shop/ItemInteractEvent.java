package de.pesacraft.cannonfight.util.shop;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import de.pesacraft.cannonfight.data.players.CannonFighter;

public class ItemInteractEvent {
	private CannonFighter fighter;
	private ItemStack item;
	private boolean closeInv;
	private boolean cancelAction;
	private InventoryAction action;
	private int rawSlot;
	
	public ItemInteractEvent(InventoryClickEvent event) {
		this.fighter = CannonFighter.get((Player) event.getWhoClicked());
		this.item = event.getCurrentItem();
		this.closeInv = false;
		this.cancelAction = true;
		this.action = event.getAction();
		this.rawSlot = event.getRawSlot();
	}

	public CannonFighter getFighter() {
		return fighter;
	}
	
	public ItemStack getItem() {
		return item;
	}
	
	public InventoryAction getAction() {
		return action;
	}
	
	public int getRawSlot() {
		return rawSlot;
	}
	
	public boolean closeInventory() {
		return closeInv;
	}
	
	public boolean cancelAction() {
		return cancelAction;
	}
	
	public void setCloseInventory(boolean closeInv) {
		this.closeInv = closeInv;
	}
	
	public void setCancelAction(boolean cancelAction) {
		this.cancelAction = cancelAction;
	}

	public boolean isPickUpAction() {
		return getAction() == InventoryAction.PICKUP_ALL || getAction() == InventoryAction.PICKUP_HALF || getAction() == InventoryAction.PICKUP_ONE || getAction() == InventoryAction.PICKUP_SOME;
	}
}
