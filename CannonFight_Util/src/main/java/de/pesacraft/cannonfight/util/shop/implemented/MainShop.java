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
import de.pesacraft.cannonfight.util.Language;
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
			@SuppressWarnings("deprecation")
			@Override
			public Shop createShop(CannonFighter c) {
				final ItemStack fill = new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.PURPLE.getData());
				ItemMeta meta = fill.getItemMeta();
				meta.setDisplayName(ChatColor.AQUA + Language.get("info.has-coins") + ChatColor.GOLD + c.getCoins() + " Coins"); //$NON-NLS-1$ //$NON-NLS-2$
				fill.setItemMeta(meta);
				
				final ItemStack cannonItem = setupCannonItem();
				final ItemStack upgradeItem = setupUpgradeItem();
				final ItemStack setupItem = setupSetupItem();
				final ItemStack powerupItem = setupPowerupItem();
				
				Shop s = new Shop(Language.get("shop.name.general"), new ClickHandler() { //$NON-NLS-1$
					
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
				
				m.setDisplayName(Language.get("shop.general.power-ups.name")); //$NON-NLS-1$
				
				List<String> lore = new ArrayList<String>();
				lore.add(ChatColor.GREEN + Language.get("shop.general.power-ups.lore")); //$NON-NLS-1$
				lore.add(ChatColor.GREEN + "kaufen und diese verbessern."); //$NON-NLS-1$
				
				m.setLore(lore);
				
				i.setItemMeta(m);
				return i;
			}

			private ItemStack setupSetupItem() {
				ItemStack i = new ItemStack(Material.ANVIL);
				ItemMeta m = i.getItemMeta();
				
				m.setDisplayName(Language.get("shop.general.setup.name")); //$NON-NLS-1$
				
				List<String> lore = new ArrayList<String>();
				lore.add(ChatColor.GREEN + Language.get("shop.general.setup.lore")); //$NON-NLS-1$
				lore.add(ChatColor.GREEN + "für das Spiel einrichten und"); //$NON-NLS-1$
				lore.add(ChatColor.GREEN + "dir aussuchen was du verwendest."); //$NON-NLS-1$
				
				m.setLore(lore);
				
				i.setItemMeta(m);
				return i;
			}

			private ItemStack setupUpgradeItem() {
				ItemStack i = new ItemStack(Material.NETHER_STAR);
				ItemMeta m = i.getItemMeta();
				
				m.setDisplayName(Language.get("shop.general.upgrades.name")); //$NON-NLS-1$
				
				List<String> lore = new ArrayList<String>();
				lore.add(ChatColor.GREEN + Language.get("shop.general.upgrades.lore")); //$NON-NLS-1$
				lore.add(ChatColor.GREEN + "wie Leben und Slots kaufen."); //$NON-NLS-1$
				
				m.setLore(lore);
				
				i.setItemMeta(m);
				return i;
			}

			private ItemStack setupCannonItem() {
				ItemStack i = new ItemStack(Material.BLAZE_ROD);
				ItemMeta m = i.getItemMeta();
				
				m.setDisplayName(Language.get("shop.general.cannons.name")); //$NON-NLS-1$
				
				List<String> lore = new ArrayList<String>();
				lore.add(ChatColor.GREEN + Language.get("shop.general.cannons.lore")); //$NON-NLS-1$
				lore.add(ChatColor.GREEN + "und Upgrades für diese kaufen."); //$NON-NLS-1$
				
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
