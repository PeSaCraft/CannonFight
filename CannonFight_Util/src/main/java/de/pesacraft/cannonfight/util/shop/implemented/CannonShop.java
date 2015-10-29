package de.pesacraft.cannonfight.util.shop.implemented;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.collect.Lists;

import de.pesacraft.cannonfight.util.Language;
import de.pesacraft.cannonfight.util.CannonFighter;
import de.pesacraft.cannonfight.util.cannons.Cannon;
import de.pesacraft.cannonfight.util.cannons.CannonConstructor;
import de.pesacraft.cannonfight.util.cannons.Cannons;
import de.pesacraft.cannonfight.util.shop.ClickHandler;
import de.pesacraft.cannonfight.util.shop.ItemInteractEvent;
import de.pesacraft.cannonfight.util.shop.Shop;

public class CannonShop {
	public static Shop getCannonShop(CannonFighter c) {
		final ItemStack fill = new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.GREEN.getData());
		ItemMeta meta = fill.getItemMeta();
		meta.setDisplayName(Language.getStringMaker("info.has-coins", false).replace("%coins%", Language.formatCoins(c.getCoins())).getString());
		fill.setItemMeta(meta);
		
		Set<String> cannons = Cannons.getCannonSet();
		int rows = (int) Math.ceil(cannons.size() / 7.0) + 2; // 7 items per row, spacer row above and below
		
		final List<ItemStack> items = new ArrayList<ItemStack>();
		
		for (String cannon : cannons) {
			ItemStack item;
			if (c.hasCannon(cannon))
				// player has this cannon: upgradeable
				item = setupUpgradeItem(cannon);
			else
				// player doesn't own this cannon: buy
				item = setupBuyItem(cannon);
			
			items.add(item);
		}
		
		Shop s = new Shop(Language.get("shop.cannons.shop.name", false), new ClickHandler() {
			
			@Override
			public void onItemInteract(ItemInteractEvent event) {
				if (!event.isPickUpAction())
					return;
				
				ItemStack item = event.getItemInSlot();
				final CannonFighter p = event.getFighter();
				
				if (item.isSimilar(fill))
					return;
				
				for (ItemStack i : items) {
					if (item.isSimilar(i)) {
						String cannon = i.getItemMeta().getDisplayName();
						if (p.hasCannon(cannon)) {
							// owns cannon: open upgrade shop
							event.setNextShop(p.getCannon(cannon).getUpgradeShop());
							return;
						}
						
						// has to buy cannon
						CannonConstructor constructor = Cannons.getConstructorByName(cannon);
						
						if (p.hasEnoughCoins(constructor.getPrice())) {
							Cannon c = constructor.buyNew(p);
							p.takeCoins(constructor.getPrice());
							p.addCannon(c);
							
							event.setNextShop(getCannonShop(p));
							
							return;
						}
						
						p.sendMessage(Language.get("error.not-enough-coins", true));
						
						return;
					}
				}
			}
		}, rows);
		
		s.fill(fill);
		
		int pos = 0; // add position
		for (ItemStack item : items) {
			s.set((1 + pos / 7) * 9 + 1 + (pos % 7), item); // 7 Items per row, starting at (1,1)
			pos++;
		}
		
		return s;
	}

	private static ItemStack setupBuyItem(String cannonName) {
		CannonConstructor constructor = Cannons.getConstructorByName(cannonName);
		ItemStack i = constructor.getItem();
		
		ItemMeta m = i.getItemMeta();
		
		List<String> lore = Lists.newArrayList(Language.getStringMaker("shop.cannons.not-owning.lore", false).replace("%price%", Language.formatCoins(constructor.getPrice())).getString().split("\n"));
		
		m.setLore(lore);
		
		i.setItemMeta(m);
		return i;
	}

	private static ItemStack setupUpgradeItem(String cannonName) {
		CannonConstructor constructor = Cannons.getConstructorByName(cannonName);
		ItemStack i = constructor.getItem();
		
		ItemMeta m = i.getItemMeta();
		
		List<String> lore = Lists.newArrayList(Language.get("shop.cannons.owning.lore", false).split("\n"));
		
		m.setLore(lore);
		
		i.setItemMeta(m);
		return i;
	}
}
