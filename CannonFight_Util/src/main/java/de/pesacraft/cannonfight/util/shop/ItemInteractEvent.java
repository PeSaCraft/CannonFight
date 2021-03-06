package de.pesacraft.cannonfight.util.shop;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import de.pesacraft.cannonfight.util.CannonFighter;

public class ItemInteractEvent {
	
	private InventoryView view;
	
	private CannonFighter fighter;
	
	private ItemStack slotItem;
	private ItemStack cursorItem;
	
	private boolean closeInv;
	private boolean cancelAction;
	
	private InventoryAction action;
	private ClickType clickType;
	
	private int hotbarKey;
	private int rawSlot;
	
	private Shop nextShop;
	
	public ItemInteractEvent(InventoryClickEvent event) {
		this.view = event.getView();
		this.fighter = CannonFighter.get((OfflinePlayer) event.getWhoClicked());
		this.slotItem = event.getCurrentItem();
		this.cursorItem = event.getCursor();
		this.closeInv = false;
		this.cancelAction = true;
		this.action = event.getAction();
		this.clickType = event.getClick();
		this.hotbarKey = clickType == ClickType.NUMBER_KEY ? event.getHotbarButton() : -1;
		this.rawSlot = event.getRawSlot();
	}

	public InventoryView getView() {
		return view;
	}
	
	public CannonFighter getFighter() {
		return fighter;
	}
	
	public ItemStack getItemInSlot() {
		return slotItem;
	}
	
	public ItemStack getItemInCursor() {
		return cursorItem;
	}
	
	public InventoryAction getAction() {
		return action;
	}
	
	public ClickType getClickType() {
		return clickType;
	}
	
	public int getHotbarKey() {
		return hotbarKey;
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

	public boolean isPlaceAction() {
		return getAction() == InventoryAction.PLACE_ALL || getAction() == InventoryAction.PLACE_ONE || getAction() == InventoryAction.PLACE_SOME;
	}

	public void setNextShop(Shop shop) {
		this.nextShop = shop;
	}
	
	public Shop getNextShop() {
		return nextShop;
	}
	
	public boolean nextShopSet() {
		return nextShop != null;
	}
}
