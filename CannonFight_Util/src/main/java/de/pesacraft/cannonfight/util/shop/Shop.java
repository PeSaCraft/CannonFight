package de.pesacraft.cannonfight.util.shop;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import de.pesacraft.cannonfight.util.CannonFightUtil;
import de.pesacraft.cannonfight.util.CannonFighter;
import de.pesacraft.cannonfight.util.Language;

public class Shop implements Listener {
	private static int shopCount = 0;
	
	private int id;
	protected String name;
	protected ItemStack[] items;
	protected ClickHandler handler;
	
	private boolean unregistered = false;
	private CannonFighter viewer;
	
	public Shop(String name, ClickHandler handler, int rows) {
		this.name = name;
		this.handler = handler;
		this.items = new ItemStack[rows * 9];
		
		id = shopCount++;
	}
	
	public Shop(String name, ClickHandler handler, ItemStack[] items) {
		if (items == null)
			items = new ItemStack[0];
		
		if (items.length % 9 != 0)
			throw new IllegalArgumentException(Language.get("error.shop.array-length-not-div-9", false));
		
		this.name = name;
		this.handler = handler;
		this.items = items;
		
		id = shopCount++;
	}
	
	public void unregister() {
		if (isUnregistered())
			throw new IllegalStateException(Language.get("error.shop.already-unregistered", false));
		
		HandlerList.unregisterAll(this);
		unregistered = true;
	}
	
	public boolean isUnregistered() {
		return unregistered;
	}
	
	public void set(int i, ItemStack item) {
		items[i] = item;
	}
	
	public void fill(ItemStack item) {
		for (int i = 0; i < items.length; i++)
			items[i] = item.clone();
	}
	
	public void openInventory(CannonFighter c) {
		if (isUnregistered())
			throw new IllegalStateException(Language.get("error.shop.already-unregistered", false));
		Inventory inv = Bukkit.createInventory(null, items.length, name);
		inv.setContents(items);
		this.viewer = c;
		c.getPlayer().openInventory(inv);
		Bukkit.getPluginManager().registerEvents(this, CannonFightUtil.PLUGIN);
	}
		
	@EventHandler
	public void onInventoryClick(final InventoryClickEvent event) {
		if (!event.getInventory().getName().equals(name) && CannonFighter.get((OfflinePlayer) event.getWhoClicked()) == viewer)
			return;
		
		final ItemInteractEvent e = new ItemInteractEvent(event);
		
		handler.onItemInteract(e);
		
		event.setCancelled(e.cancelAction());
		
		new BukkitRunnable() {
			
			@Override
			public void run() {
				if (e.closeInventory())
					event.getWhoClicked().closeInventory();
				if (e.nextShopSet())
					e.getNextShop().openInventory(CannonFighter.get(((OfflinePlayer) event.getWhoClicked())));
			}
		}.runTaskLater(CannonFightUtil.PLUGIN, 1);
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		if (!event.getInventory().getName().equals(name) && CannonFighter.get((OfflinePlayer) event.getPlayer()) != viewer)
			return;
		
		// unregister self when inventory is closing
		unregister();
	}
	
	@Override
	public int hashCode() {
		return name.hashCode() + viewer.hashCode() + id;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Shop))
			return false;
		Shop shop = (Shop) obj;
		return shop.id == this.id;
	}
}
