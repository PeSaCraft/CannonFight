package de.pesacraft.cannonfight.game.util.shop;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

import de.pesacraft.cannonfight.game.players.CannonFighter;

public class ShopGroup {
	private ShopMaker maker;
	
	public ShopGroup(ShopMaker maker) {
		this.maker = maker;
	}
	
	public void open(CannonFighter c) {
		if (c.getPlayer().getOpenInventory() != null)
			c.getPlayer().closeInventory();
		
		Shop s = maker.createShop(c);
		s.openInventory(c);
	}
}
