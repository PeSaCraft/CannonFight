package de.pesacraft.cannonfight.util.shop;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

import de.pesacraft.cannonfight.data.players.CannonFighter;

public class ShopGroup {
	private Map<UUID, Shop> shops = new HashMap<UUID, Shop>();
	private ShopMaker maker;
	
	public ShopGroup(ShopMaker maker) {
		this.maker = maker;
	}
	
	public void open(CannonFighter c) {
		if (c.getPlayer().getOpenInventory() != null)
			c.getPlayer().closeInventory();
		
		if (shops.containsKey(c.getPlayer().getUniqueId()))
			shops.get(c.getPlayer().getUniqueId()).openInventory(c);
		else {
			Shop s = maker.createShop(c);
			shops.put(c.getPlayer().getUniqueId(), s);
			s.openInventory(c);
		}
	}
	
	public void regenerate(CannonFighter c) {
		shops.put(c.getPlayer().getUniqueId(), maker.createShop(c));
	}
}
