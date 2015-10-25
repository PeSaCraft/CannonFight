package de.pesacraft.cannonfight.util.shop;

import de.pesacraft.cannonfight.util.CannonFighter;

public class ShopGroup {
	private ShopMaker maker;
	
	public ShopGroup(ShopMaker maker) {
		this.maker = maker;
	}
	
	public void open(CannonFighter c) {
		Shop s = maker.createShop(c);
		s.openInventory(c);
	}
}
