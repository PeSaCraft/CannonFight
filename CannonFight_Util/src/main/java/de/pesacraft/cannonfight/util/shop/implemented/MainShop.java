package de.pesacraft.cannonfight.util.shop.implemented;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import de.pesacraft.cannonfight.util.CannonFighter;
import de.pesacraft.cannonfight.util.shop.ClickHandler;
import de.pesacraft.cannonfight.util.shop.ItemInteractEvent;
import de.pesacraft.cannonfight.util.shop.Shop;
import de.pesacraft.cannonfight.util.shop.ShopGroup;
import de.pesacraft.cannonfight.util.shop.ShopMaker;
import de.pesacraft.cannonfight.util.shop.implemented.CannonShop;
import de.pesacraft.cannonfight.util.shop.implemented.SetupShop;
import de.pesacraft.cannonfight.util.shop.implemented.UpgradeShop;

public class MainShop {
	private static final ShopGroup shop;
	
	static {
		shop = new ShopGroup(new ShopMaker() {
			@Override
			public Shop createShop(CannonFighter c) {
				final ItemStack fill = new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.PURPLE.getData());
				ItemMeta meta = fill.getItemMeta();
				meta.setDisplayName(ChatColor.AQUA + "Du hast " + ChatColor.GOLD + c.getCoins() + " Coins");
				fill.setItemMeta(meta);
				
				final ItemStack cannonItem = setupCannonItem();
				final ItemStack upgradeItem = setupUpgradeItem();
				final ItemStack setupItem = setupSetupItem();
				final ItemStack powerupItem = setupPowerupItem();
				
				Shop s = new Shop("CannonFight Shop", new ClickHandler() {
					
					@Override
					public void onItemInteract(ItemInteractEvent event) {
						if (!event.isPickUpAction())
							return;
						
						ItemStack item = event.getItemInSlot();
						
						if (item.isSimilar(fill))
							return;
						
						if (item.isSimilar(cannonItem)) {
							// open cannonshop
							CannonShop.openShopPage(event.getFighter());
							return;
						}
						
						if (item.isSimilar(upgradeItem)) {
							// open upgradeshop
							UpgradeShop.openShopPage(event.getFighter());
							return;
						}
						
						if (item.isSimilar(setupItem)) {
							// open setup
							SetupShop.openShopPage(event.getFighter());
							return;
						}
						
						if (item.isSimilar(powerupItem)) {
							// open powerupshop
							return;
						}
					}

					@Override
					public void onInventoryClose(InventoryCloseEvent event) {}
				}, 3);
				
				s.fill(fill);
				
				s.set(1 * 9 + 1, cannonItem); // (1, 1) Cannons
				s.set(1 * 9 + 3, upgradeItem); // (1, 3) Upgrades
				s.set(1 * 9 + 5, setupItem); // (1, 5) Setup
				s.set(1 * 9 + 7, powerupItem); // (1, 7) Power-Ups
				
				return s;
			}

			private ItemStack setupPowerupItem() {
				ItemStack i = new ItemStack(Material.SUGAR);
				ItemMeta m = i.getItemMeta();
				
				m.setDisplayName("Power-Ups");
				
				List<String> lore = new ArrayList<String>();
				lore.add(ChatColor.GREEN + "Hier kannst du dir Power-Ups");
				lore.add(ChatColor.GREEN + "kaufen und diese verbessern.");
				
				m.setLore(lore);
				
				i.setItemMeta(m);
				return i;
			}

			private ItemStack setupSetupItem() {
				ItemStack i = new ItemStack(Material.ANVIL);
				ItemMeta m = i.getItemMeta();
				
				m.setDisplayName("Setup");
				
				List<String> lore = new ArrayList<String>();
				lore.add(ChatColor.GREEN + "Hier kannst du dein Inventar");
				lore.add(ChatColor.GREEN + "für das Spiel einrichten und");
				lore.add(ChatColor.GREEN + "dir aussuchen was du verwendest.");
				
				m.setLore(lore);
				
				i.setItemMeta(m);
				return i;
			}

			private ItemStack setupUpgradeItem() {
				ItemStack i = new ItemStack(Material.NETHER_STAR);
				ItemMeta m = i.getItemMeta();
				
				m.setDisplayName("Upgrades");
				
				List<String> lore = new ArrayList<String>();
				lore.add(ChatColor.GREEN + "Hier kannst du dir allgemeine Upgrades");
				lore.add(ChatColor.GREEN + "wie Leben und Slots kaufen.");
				
				m.setLore(lore);
				
				i.setItemMeta(m);
				return i;
			}

			private ItemStack setupCannonItem() {
				ItemStack i = new ItemStack(Material.BLAZE_ROD);
				ItemMeta m = i.getItemMeta();
				
				m.setDisplayName("Cannons");
				
				List<String> lore = new ArrayList<String>();
				lore.add(ChatColor.GREEN + "Hier kannst du dir neue Kanonen");
				lore.add(ChatColor.GREEN + "und Upgrades für diese kaufen.");
				
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
