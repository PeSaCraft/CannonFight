package de.pesacraft.cannonfight.util.shop.implemented;

import static com.mongodb.client.model.Filters.eq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.mongodb.client.MongoCollection;

import de.pesacraft.cannonfight.util.CannonFightUtil;
import de.pesacraft.cannonfight.util.Collection;
import de.pesacraft.cannonfight.util.ItemSerializer;
import de.pesacraft.cannonfight.util.Language;
import de.pesacraft.cannonfight.util.CannonFighter;
import de.pesacraft.cannonfight.util.shop.ClickHandler;
import de.pesacraft.cannonfight.util.shop.ItemInteractEvent;
import de.pesacraft.cannonfight.util.shop.Shop;
import de.pesacraft.cannonfight.util.shop.ShopGroup;
import de.pesacraft.cannonfight.util.shop.ShopMaker;
import de.pesacraft.cannonfight.util.shop.upgrade.Upgrade;

@SuppressWarnings("deprecation")
public class UpgradeShop {
	
	private static final MongoCollection<Document> COLLECTION;
	
	/**
	 * ItemStack representing the slot upgrade
	 */
	protected static final ItemStack ITEM_SLOTS;
	/**
	 * The slot upgrade name in the database
	 */
	public static final String NAME_SLOTS = "Slots"; //$NON-NLS-1$
	
	protected static final Map<Integer, Upgrade<Integer>> SLOT_UPGRADES = new HashMap<Integer, Upgrade<Integer>>();
	
	/**
	 * ItemStack representing the slot upgrade
	 */
	protected static final ItemStack ITEM_LIVES;
	/**
	 * The slot upgrade name in the database
	 */
	public static final String NAME_LIVES = "Lives"; //$NON-NLS-1$
	
	protected static final Map<Integer, Upgrade<Integer>> LIFE_UPGRADES = new HashMap<Integer, Upgrade<Integer>>();
	
	private static final ShopGroup shop;
		
	static {
		COLLECTION = Collection.ITEMS();
		
		Document doc = COLLECTION.find(eq("name", NAME_SLOTS)).first(); //$NON-NLS-1$
		
		if (doc != null) {
			// Cannon in database
			
			ITEM_SLOTS = ItemSerializer.deserialize((Document) doc.get("item")); //$NON-NLS-1$
			
			Document upgrades = (Document) doc.get("upgrades"); //$NON-NLS-1$
			for (Entry<String, Object> upgrade : upgrades.entrySet())
				SLOT_UPGRADES.put(Integer.parseInt(upgrade.getKey()), new Upgrade<Integer>((Document) upgrade.getValue()));
			
		}
		else {
			// Cannon not in database
			ITEM_SLOTS = new ItemStack(Material.RAILS);
			
			ItemMeta m = ITEM_SLOTS.getItemMeta();
			m.setDisplayName(NAME_SLOTS);
			ITEM_SLOTS.setItemMeta(m);
			
			Map<String, Object> upgrades = new HashMap<String, Object>();
			for (int i = 1; i <= 10; i++) {
				Upgrade<Integer> u = new Upgrade<Integer>(i * 1000, i);
				SLOT_UPGRADES.put(i, u);
				upgrades.put(i + "", u); //$NON-NLS-1$
			}
			
			doc = new Document("name", NAME_SLOTS); //$NON-NLS-1$
			
			doc = doc.append("item", new Document(ItemSerializer.serialize(ITEM_SLOTS))); //$NON-NLS-1$
			
			doc = doc.append("upgrades", new Document(upgrades)); //$NON-NLS-1$
			
			COLLECTION.insertOne(doc);
		}
		
		doc = COLLECTION.find(eq("name", NAME_LIVES)).first(); //$NON-NLS-1$
		
		if (doc != null) {
			// Cannon in database
			
			ITEM_LIVES = ItemSerializer.deserialize((Document) doc.get("item")); //$NON-NLS-1$
			
			Document upgrades = (Document) doc.get("upgrades"); //$NON-NLS-1$
			for (Entry<String, Object> upgrade : upgrades.entrySet())
				LIFE_UPGRADES.put(Integer.parseInt(upgrade.getKey()), new Upgrade<Integer>((Document) upgrade.getValue()));
			
		}
		else {
			// Cannon not in database
			ITEM_LIVES = new ItemStack(Material.INK_SACK, 1, DyeColor.RED.getData());
			
			ItemMeta m = ITEM_LIVES.getItemMeta();
			m.setDisplayName(NAME_LIVES);
			ITEM_LIVES.setItemMeta(m);
			
			Map<String, Object> upgrades = new HashMap<String, Object>();
			for (int i = 1; i <= 5; i++) {
				Upgrade<Integer> u = new Upgrade<Integer>(i * 10000, i);
				LIFE_UPGRADES.put(i, u);
				upgrades.put(i + "", u); //$NON-NLS-1$
			}
			
			doc = new Document("name", NAME_LIVES); //$NON-NLS-1$
			
			
			doc = doc.append("item", new Document(ItemSerializer.serialize(ITEM_LIVES))); //$NON-NLS-1$
			
			doc = doc.append("upgrades", new Document(upgrades)); //$NON-NLS-1$
			
			COLLECTION.insertOne(doc);
		}
		
		shop = new ShopGroup(new ShopMaker() {
			@Override
			public Shop createShop(CannonFighter c) {
				final ItemStack fill = new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.YELLOW.getData());
				ItemMeta meta = fill.getItemMeta();
				meta.setDisplayName(ChatColor.AQUA + Language.get("info.has-coins") + ChatColor.GOLD + c.getCoins() + " Coins"); //$NON-NLS-1$ //$NON-NLS-2$
				fill.setItemMeta(meta);
				
				final ItemStack slotItem = setupSlotItem(c);
				final ItemStack lifeItem = setupLifeItem(c);
				
				Shop s = new Shop(Language.get("shop.upgrade.name"), new ClickHandler() { //$NON-NLS-1$
					
					@Override
					public void onItemInteract(ItemInteractEvent event) {
						if (!event.isPickUpAction())
							return;
						
						ItemStack item = event.getItemInSlot();
						final CannonFighter p = event.getFighter();
						
						if (item.isSimilar(fill))
							return;
						
						if (item.isSimilar(slotItem)) {
							// upgrade slots
							if (!SLOT_UPGRADES.containsKey(p.getSlotsLevel() + 1)) {
								// max reached
								p.sendMessage(Language.get("error.max-upgraded")); //$NON-NLS-1$
								return;
							}
							
							if (!p.upgradeSlots()) {
								// not enough coins for upgrade
								p.sendMessage(Language.get("error.not-enough-coins")); //$NON-NLS-1$
								return;
							}
							
							// upgrade done
							// regenerate shop
							Bukkit.getScheduler().runTaskLater(CannonFightUtil.PLUGIN, new Runnable() {
								
								@Override
								public void run() {
									openShopPage(p);
								}
							}, 1);
							return;
						}
						
						if (item.isSimilar(lifeItem)) {
							// upgrade slots
							if (!LIFE_UPGRADES.containsKey(p.getSlotsLevel() + 1)) {
								// max reached
								p.sendMessage(Language.get("error.max-upgraded")); //$NON-NLS-1$
								return;
							}
							
							if (!p.upgradeLives()) {
								// not enough coins for upgrade
								p.sendMessage(Language.get("error.not-enough-coins")); //$NON-NLS-1$
								return;
							}
							
							// upgrade done
							// regenerate shop
							Bukkit.getScheduler().runTaskLater(CannonFightUtil.PLUGIN, new Runnable() {
								
								@Override
								public void run() {
									openShopPage(p);
								}
							}, 1);
							return;
						}
					}

					@Override
					public void onInventoryClose(InventoryCloseEvent event) {}
				}, 3);
				
				s.fill(fill);
				
				s.set(1 * 9 + 3, slotItem); // (1, 3) Slots
				s.set(1 * 9 + 5, lifeItem); // (1, 5) Lives
				
				return s;
			}

			private ItemStack setupSlotItem(CannonFighter p) {
				ItemStack i = ITEM_SLOTS.clone();
				ItemMeta m = i.getItemMeta();
				
				List<String> lore = new ArrayList<String>();
				
				Upgrade<Integer> upgrade = getSlotsUpgradeForLevel(p.getSlotsLevel() + 1);
				
				lore.add(ChatColor.GREEN + Language.get("shop.upgrade.slots.lore")); //$NON-NLS-1$
				lore.add(ChatColor.GREEN + "Momentan hast du " + ChatColor.GOLD + p.getSlots() + " Slots"); //$NON-NLS-1$ //$NON-NLS-2$
				
				if (upgrade != null) {
					lore.add(ChatColor.GREEN + Language.get("shop.upgrade.slots.lore.upgradable") + ChatColor.GOLD + upgrade.getValue() + " Slots"); //$NON-NLS-1$ //$NON-NLS-2$
					lore.add(ChatColor.GREEN + "kostet " + ChatColor.GOLD + upgrade.getPrice()); //$NON-NLS-1$
				}
				else
					lore.add(ChatColor.RED + Language.get("shop.upgrade.slots.lore.max-reached")); //$NON-NLS-1$
					
				m.setLore(lore);
				
				i.setItemMeta(m);
				
				return i;
			}
			
			private ItemStack setupLifeItem(CannonFighter p) {
				ItemStack i = ITEM_LIVES.clone();
				ItemMeta m = i.getItemMeta();
				
				List<String> lore = new ArrayList<String>();
				
				Upgrade<Integer> upgrade = getLivesUpgradeForLevel(p.getLivesLevel() + 1);
				
				lore.add(ChatColor.GREEN + Language.get("shop.upgrade.lives.lore")); //$NON-NLS-1$
				lore.add(ChatColor.GREEN + "Momentan hast du " + ChatColor.GOLD + p.getLives() + " Leben"); //$NON-NLS-1$ //$NON-NLS-2$
				
				if (upgrade != null) {
					lore.add(ChatColor.GREEN + Language.get("shop.upgrade.lives.lore.upgradable") + ChatColor.GOLD + upgrade.getValue() + " Leben"); //$NON-NLS-1$ //$NON-NLS-2$
					lore.add(ChatColor.GREEN + "kostet " + ChatColor.GOLD + upgrade.getPrice()); //$NON-NLS-1$
				}
				else
					lore.add(ChatColor.RED + Language.get("shop.upgrade.lives.lore.max-reached")); //$NON-NLS-1$
					
				m.setLore(lore);
				
				i.setItemMeta(m);
				
				return i;
			}
		});
	}
	
	public static void openShopPage(CannonFighter c) {
		shop.open(c);
	}

	public static Upgrade<Integer> getSlotsUpgradeForLevel(int level) {
		return SLOT_UPGRADES.get(level);
	}
	
	public static Upgrade<Integer> getLivesUpgradeForLevel(int level) {
		return LIFE_UPGRADES.get(level);
	}
}
