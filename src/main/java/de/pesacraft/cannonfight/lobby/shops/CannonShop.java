package de.pesacraft.cannonfight.lobby.shops;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.pesacraft.cannonfight.CannonFight;
import de.pesacraft.cannonfight.Language;
import de.pesacraft.cannonfight.data.players.CannonFighter;
import de.pesacraft.cannonfight.game.cannons.Cannon;
import de.pesacraft.cannonfight.game.cannons.CannonConstructor;
import de.pesacraft.cannonfight.game.cannons.Cannons;
import de.pesacraft.cannonfight.game.cannons.usable.FireballCannon;
import de.pesacraft.cannonfight.util.shop.ItemInteractEvent;
import de.pesacraft.cannonfight.util.shop.Shop;
import de.pesacraft.cannonfight.util.shop.ShopGroup;
import de.pesacraft.cannonfight.util.shop.ShopMaker;
import de.pesacraft.cannonfight.util.shop.ClickHandler;

public class CannonShop {
	private static final ShopGroup shop;
		
	static {
		shop = new ShopGroup(new ShopMaker() {
			@SuppressWarnings("deprecation")
			@Override
			public Shop createShop(CannonFighter c) {
				final ItemStack fill = new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.GREEN.getData());
				ItemMeta meta = fill.getItemMeta();
				meta.setDisplayName(ChatColor.AQUA + "Du hast " + ChatColor.GOLD + c.getCoins() + " Coins");
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
				
				Shop s = new Shop("Cannon Shop", new ClickHandler() {
					
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
									p.getCannon(cannon).openShop();
									return;
								}
								
								// has to buy cannon
								CannonConstructor constructor = Cannons.getConstructorByName(cannon);
								
								if (p.hasEnoughCoins(constructor.getPrice())) {
									Cannon c = constructor.buyNew(p);
									p.takeCoins(constructor.getPrice(), cannon + " gekauft");
									p.addCannon(c);
									
									// regenerate this shop, cannon isnt buyable anymore
									shop.regenerate(p);
									Bukkit.getScheduler().runTaskLater(CannonFight.PLUGIN, new Runnable() {
										
										@Override
										public void run() {
											openShopPage(p);
										}
									}, 1);
									
									return;
								}
								
								p.sendMessage(Language.get("error.not-enough-coins"));
								
								return;
							}
						}
					}

					@Override
					public void onInventoryClose(InventoryCloseEvent event) {}
				}, rows);
				
				s.fill(fill);
				
				int pos = 0; // add position
				for (ItemStack item : items) {
					s.set((1 + pos / 7) * 9 + 1 + (pos % 7), item); // 7 Items per row, starting at (1,1)
				}
				
				return s;
			}

			private ItemStack setupBuyItem(String cannonName) {
				CannonConstructor constructor = Cannons.getConstructorByName(cannonName);
				ItemStack i = constructor.getItem();
				
				ItemMeta m = i.getItemMeta();
				
				List<String> lore = new ArrayList<String>();
				lore.add(ChatColor.RED + "Diese Kanone besitzt du noch nicht.");
				lore.add(ChatColor.GREEN + "Du kannst sie für " + constructor.getPrice() + " Coins kaufen.");
				
				m.setLore(lore);
				
				i.setItemMeta(m);
				return i;
			}

			private ItemStack setupUpgradeItem(String cannonName) {
				CannonConstructor constructor = Cannons.getConstructorByName(cannonName);
				ItemStack i = constructor.getItem();
				
				ItemMeta m = i.getItemMeta();
				
				List<String> lore = new ArrayList<String>();
				lore.add(ChatColor.GREEN + "Diese Kanone besitzt du bereits.");
				lore.add(ChatColor.GREEN + "Klicke um sie zu verbessern.");
				
				m.setLore(lore);
				
				i.setItemMeta(m);
				return i;
			}
		});
	}
	
	public static void openShopPage(CannonFighter c) {
		shop.open(c);
	}
}
