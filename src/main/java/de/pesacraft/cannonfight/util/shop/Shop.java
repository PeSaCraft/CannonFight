package de.pesacraft.cannonfight.util.shop;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import de.pesacraft.cannonfight.CannonFight;
import de.pesacraft.cannonfight.data.players.CannonFighter;

public class Shop implements Listener {
	protected String name;
	protected ItemStack[] items;
	protected ClickHandler handler;
	
	public Shop(String name, ClickHandler handler, int rows) {
		this.name = name;
		this.handler = handler;
		this.items = new ItemStack[rows * 9];
		
		Bukkit.getPluginManager().registerEvents(this, CannonFight.PLUGIN);
	}
	
	public Shop(String name, ClickHandler handler, ItemStack[] items) {
		if (items == null)
			items = new ItemStack[0];
		
		if (items.length % 9 != 0)
			throw new IllegalArgumentException("Array for inventory has to have a length dividable with 9");
		
		this.name = name;
		this.handler = handler;
		this.items = items;
		
		Bukkit.getPluginManager().registerEvents(this, CannonFight.PLUGIN);
	}
	
	public void set(int i, ItemStack item) {
		items[i] = item;
	}
	
	public void fill(ItemStack item) {
		for (int i = 0; i < items.length; i++)
			items[i] = item.clone();
	}
	
	public void openInventory(CannonFighter c) {
		if (c.getPlayer().getOpenInventory() != null)
			c.getPlayer().closeInventory();
		
		Inventory inv = Bukkit.createInventory(null, items.length, name);
		
		inv.setContents(items);
		
		c.getPlayer().openInventory(inv);
	}
		
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (!event.getInventory().getName().equals(name))
			return;
		
		event.setCancelled(true);
		
		ItemSelectEvent e = new ItemSelectEvent(CannonFighter.get((Player) event.getWhoClicked()), event.getCurrentItem());
		
		handler.onItemSelect(e);
		
		if (e.closeInventory())
			event.getWhoClicked().closeInventory();
	}
}
