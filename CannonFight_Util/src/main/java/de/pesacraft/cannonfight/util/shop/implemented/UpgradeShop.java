package de.pesacraft.cannonfight.util.shop.implemented;

import static com.mongodb.client.model.Filters.eq;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.collect.Lists;
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
	public static final String NAME_SLOTS = "Slots";
	
	protected static final Map<Integer, Upgrade<Integer>> SLOT_UPGRADES = new HashMap<Integer, Upgrade<Integer>>();
	
	/**
	 * ItemStack representing the slot upgrade
	 */
	protected static final ItemStack ITEM_LIVES;
	/**
	 * The slot upgrade name in the database
	 */
	public static final String NAME_LIVES = "Lives";
	
	protected static final Map<Integer, Upgrade<Integer>> LIFE_UPGRADES = new HashMap<Integer, Upgrade<Integer>>();
	
	private static final ShopGroup shop;
		
	static {
		COLLECTION = Collection.ITEMS();
		
		Document doc = COLLECTION.find(eq("name", NAME_SLOTS)).first();
		
		if (doc != null) {
			// Cannon in database
			
			ITEM_SLOTS = ItemSerializer.deserialize((Document) doc.get("item"));
			
			Document upgrades = (Document) doc.get("upgrades");
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
				upgrades.put(String.valueOf(i), u);
			}
			
			doc = new Document("name", NAME_SLOTS);
			
			doc = doc.append("item", new Document(ItemSerializer.serialize(ITEM_SLOTS)));
			
			doc = doc.append("upgrades", new Document(upgrades));
			
			COLLECTION.insertOne(doc);
		}
		
		doc = COLLECTION.find(eq("name", NAME_LIVES)).first();
		
		if (doc != null) {
			// Cannon in database
			
			ITEM_LIVES = ItemSerializer.deserialize((Document) doc.get("item"));
			
			Document upgrades = (Document) doc.get("upgrades");
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
				upgrades.put(String.valueOf(i), u);
			}
			
			doc = new Document("name", NAME_LIVES);
			
			
			doc = doc.append("item", new Document(ItemSerializer.serialize(ITEM_LIVES)));
			
			doc = doc.append("upgrades", new Document(upgrades));
			
			COLLECTION.insertOne(doc);
		}
		
		shop = new ShopGroup(new ShopMaker() {
			@Override
			public Shop createShop(CannonFighter c) {
				final ItemStack fill = new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.YELLOW.getData());
				ItemMeta meta = fill.getItemMeta();
				meta.setDisplayName(Language.getStringMaker("info.has-coins", false).replace("%coins%", Language.formatCoins(c.getCoins())).getString());
				fill.setItemMeta(meta);
				
				final ItemStack slotItem = setupSlotItem(c);
				final ItemStack lifeItem = setupLifeItem(c);
				
				Shop s = new Shop(Language.get("shop.upgrades.name", false), new ClickHandler() {
					
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
								p.sendMessage(Language.get("error.max-upgraded", true));
								return;
							}
							
							if (!p.upgradeSlots()) {
								// not enough coins for upgrade
								p.sendMessage(Language.get("error.not-enough-coins", true));
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
								p.sendMessage(Language.get("error.max-upgraded", true));
								return;
							}
							
							if (!p.upgradeLives()) {
								// not enough coins for upgrade
								p.sendMessage(Language.get("error.not-enough-coins", true));
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
				}, 3);
				
				s.fill(fill);
				
				s.set(1 * 9 + 3, slotItem); // (1, 3) Slots
				s.set(1 * 9 + 5, lifeItem); // (1, 5) Lives
				
				return s;
			}

			private ItemStack setupSlotItem(CannonFighter p) {
				ItemStack i = ITEM_SLOTS.clone();
				ItemMeta m = i.getItemMeta();
				
				Upgrade<Integer> upgrade = getSlotsUpgradeForLevel(p.getSlotsLevel() + 1);
				
				List<String> lore = Lists.newArrayList(Language.getStringMaker("shop.upgrades.slots.lore", false).replace("%slots%", Language.formatSlots(p.getSlots())).getString().split("\n"));
				
				if (upgrade != null) {
					lore.addAll(Lists.newArrayList(Language.getStringMaker("shop.upgrades.slots.lore.upgradable", false).replace("%slots%", Language.formatSlots(upgrade.getValue())).replace("%price%", Language.formatCoins(upgrade.getPrice())).getString().split("\n")));
				}
				else
					lore.addAll(Lists.newArrayList(Language.get("shop.upgrades.slots.lore.max-reached", false).split("\n")));
					
				m.setLore(lore);
				
				i.setItemMeta(m);
				
				return i;
			}
			
			private ItemStack setupLifeItem(CannonFighter p) {
				ItemStack i = ITEM_LIVES.clone();
				ItemMeta m = i.getItemMeta();
				
				Upgrade<Integer> upgrade = getLivesUpgradeForLevel(p.getLivesLevel() + 1);
				
				List<String> lore = Lists.newArrayList(Language.getStringMaker("shop.upgrades.lives.lore", false).replace("%lives%", Language.formatLives(p.getLives())).getString().split("\n"));
				
				if (upgrade != null) {
					lore.addAll(Lists.newArrayList(Language.getStringMaker("shop.upgrades.lives.lore.upgradable", false).replace("%lives%", Language.formatLives(upgrade.getValue())).replace("%price%", Language.formatCoins(upgrade.getPrice())).getString().split("\n")));
				}
				else
					lore.addAll(Lists.newArrayList(Language.get("shop.upgrades.lives.lore.max-reached", false).split("\n")));
					
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
