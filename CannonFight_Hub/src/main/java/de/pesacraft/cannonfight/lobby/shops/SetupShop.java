package de.pesacraft.cannonfight.lobby.shops;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitScheduler;

import de.pesacraft.cannonfight.CannonFight;
import de.pesacraft.cannonfight.Language;
import de.pesacraft.cannonfight.data.players.CannonFighter;
import de.pesacraft.cannonfight.game.cannons.Cannon;
import de.pesacraft.cannonfight.game.cannons.CannonConstructor;
import de.pesacraft.cannonfight.game.cannons.Cannons;
import de.pesacraft.cannonfight.util.shop.ClickHandler;
import de.pesacraft.cannonfight.util.shop.ItemInteractEvent;
import de.pesacraft.cannonfight.util.shop.Shop;
import de.pesacraft.cannonfight.util.shop.ShopGroup;
import de.pesacraft.cannonfight.util.shop.ShopMaker;

public class SetupShop {
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
				
				final Map<Cannon, ItemStack> available = new HashMap<Cannon, ItemStack>();
				
				for (String cannonName : cannons) {
					if (c.hasCannon(cannonName)) {
						// player has this cannon
						Cannon cannon = c.getCannon(cannonName);
						available.put(cannon, setupItem(c, cannon));
					}
					// else player doesn't own this cannon
				}
				
				int rows = (int) Math.ceil(available.size() / 7.0) + 2; // 7 items per row, spacer row above and below
				
				Shop s = new Shop("CannonFight Setup", new ClickHandler() {
					
					@Override
					public void onItemInteract(ItemInteractEvent event) {
						if (event.isPickUpAction()) {
							// pickup deselects item
						
							ItemStack item = event.getItemInSlot();
							CannonFighter p = event.getFighter();
								
							if (item.isSimilar(fill))
								return;
								
							for (Entry<Cannon, ItemStack> entry : available.entrySet()) {
								if (item.isSimilar(entry.getValue())) {
									// disable this
									disableCannon(p, entry.getKey());
									
									return;
								}
							}
							return;
						}
						
						if (event.getClickType() != ClickType.NUMBER_KEY)
							return;
						
						// only number clicks are important
						int key = event.getHotbarKey();
						
						ItemStack item = event.getItemInSlot();
						final CannonFighter p = event.getFighter();
							
						if (item.isSimilar(fill))
							return;
							
						for (Entry<Cannon, ItemStack> entry : available.entrySet()) {
							if (item.isSimilar(entry.getValue())) {
								// this is the clicked cannon
								Cannon cannon = entry.getKey();
								if (key < p.getSlots()){
									// get previous cannon in that slot
									Cannon old = p.getActiveItem(key);
									
									if (old != null)
										// there was a cannon before: remove it
										p.deselectCannon(old.getName());
						
									// if new cannon was selected they will switch
									if (p.isSelected(cannon.getName())) {
										// get cannons current position
										int pos = p.getActivePosition(cannon.getName());
										// remove it
										p.deselectCannon(cannon.getName());
										
										// set old if it exists
										if (old != null)
											p.selectCannonToSlot(pos, old.getName());
									}
									
									// put new cannon in desired slot
									p.selectCannonToSlot(key, cannon.getName());		
								}
								else {
									// not enough slots for that one
									p.sendMessage(Language.get("error.not-enough-slots"));
									return;
								}
								
								// something was changed
								// regenerate shop
								Bukkit.getScheduler().runTaskLater(CannonFight.PLUGIN, new Runnable() {
									
									@Override
									public void run() {
										openShopPage(p);
									}
								}, 1);
								return;
							}
						}
					}
					
					private void disableCannon(final CannonFighter player, Cannon cannon) {
						if (player.isSelected(cannon.getName())) {
							player.deselectCannon(cannon.getName());
							
							// something was changed
							// regenerate shop
							Bukkit.getScheduler().runTaskLater(CannonFight.PLUGIN, new Runnable() {
								
								@Override
								public void run() {
									openShopPage(player);
								}
							}, 1);
						}
						else {
							player.sendMessage(Language.get("info.cannon-not-selected"));
						}
					}

					@Override
					public void onInventoryClose(InventoryCloseEvent event) {}
				}, rows);
				
				s.fill(fill);
				
				int pos = 0; // add position
				for (ItemStack item : available.values()) {
					s.set((1 + pos / 7) * 9 + 1 + (pos % 7), item); // 7 Items per row, starting at (1,1)
					pos++;
				}
				
				return s;
			}

			private ItemStack setupItem(CannonFighter player, Cannon cannon) {
				ItemStack i = cannon.getItem();
				
				i.setAmount(1);
				
				ItemMeta m = i.getItemMeta();
				
				List<String> lore = new ArrayList<String>();
				
				if (player.isSelected(cannon.getName())) {
					// item selected
					int pos = player.getActivePosition(cannon.getName()) + 1;
					
					lore.add(ChatColor.GREEN + "Diese Kanone ist momentan dein " + ChatColor.GOLD + pos + ". Item");
					lore.add(ChatColor.GREEN + "Klick dieses Item an um es von den aktiven");
					lore.add(ChatColor.GREEN + "Items zu entfernen oder gebe eine andere");
					lore.add(ChatColor.GREEN + "Position an um die Items zu tauschen");
				}
				else {
					lore.add(ChatColor.GREEN + "Momentan verwendest du diese Kanone nicht");
					lore.add(ChatColor.GREEN + "Gib eine Nummer ein um das Item zu zuweisen.");
				}
				
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