package de.pesacraft.cannonfight.util.shop;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
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

public class Shop implements Listener {
	protected String name;
	protected ItemStack[] items;
	protected ClickHandler handler;
	
	public Shop(String name, ClickHandler handler, int rows) {
		this.name = name;
		this.handler = handler;
		this.items = new ItemStack[rows * 9];
		
		Bukkit.getPluginManager().registerEvents(this, CannonFightUtil.PLUGIN);
	}
	
	public Shop(String name, ClickHandler handler, ItemStack[] items) {
		if (items == null)
			items = new ItemStack[0];
		
		if (items.length % 9 != 0)
			throw new IllegalArgumentException("Array for inventory has to have a length dividable with 9");
		
		this.name = name;
		this.handler = handler;
		this.items = items;
		
		Bukkit.getPluginManager().registerEvents(this, CannonFightUtil.PLUGIN);
	}
	
	public void unregister() {
		HandlerList.unregisterAll(this);
	}
	
	public void set(int i, ItemStack item) {
		items[i] = item;
	}
	
	public void fill(ItemStack item) {
		for (int i = 0; i < items.length; i++)
			items[i] = item.clone();
	}
	
	public void openInventory(CannonFighter c) {
		Inventory inv = Bukkit.createInventory(null, items.length, name);
		
		inv.setContents(items);
		
		c.getPlayer().openInventory(inv);
	}
		
	@EventHandler
	public void onInventoryClick(final InventoryClickEvent event) {
		if (!event.getInventory().getName().equals(name))
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
		if (!event.getInventory().getName().equals(name))
			return;
		
		handler.onInventoryClose(event);
	}
}
