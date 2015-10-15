package de.pesacraft.cannonfight.util.cannons;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bson.Document;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import com.mongodb.client.model.Filters;

import de.pesacraft.cannonfight.util.Collection;
import de.pesacraft.cannonfight.util.ItemSerializer;
import de.pesacraft.cannonfight.util.Language;
import de.pesacraft.cannonfight.util.cannons.CannonConstructor;
import de.pesacraft.cannonfight.util.cannons.usable.FireballCannon;
import de.pesacraft.cannonfight.util.shop.ClickHandler;
import de.pesacraft.cannonfight.util.shop.ItemInteractEvent;
import de.pesacraft.cannonfight.util.shop.Shop;

public class Cannons {
	/**
	 * A list containing all available cannons
	 */
	private static final Map<String, CannonConstructor> cannons = new HashMap<String, CannonConstructor>();
	
	static {
		FireballCannon.setup();
	}
	
	public static Map<String, CannonConstructor> getCannons() {
		return cannons;
	}

	public static CannonConstructor getConstructorByName(String cannonName) {
		return cannons.get(cannonName);
	}

	public static void register(String name, CannonConstructor constructor) {
		cannons.put(name, constructor);
	}

	public static Set<String> getCannonSet() {
		return cannons.keySet();
	}

	public static void storeCannons() {	
		for (Entry<String, CannonConstructor> entry : cannons.entrySet()) {
			String name = entry.getKey();
			Document doc = new Document("name", name);	 //$NON-NLS-1$
			
			doc = doc.append("item", new Document(ItemSerializer.serialize(entry.getValue().getItem()))); //$NON-NLS-1$
			
			doc.putAll(Cannon.serializeUpgrades(name));
			
			Collection.ITEMS().replaceOne(Filters.eq("name", name), doc);	  //$NON-NLS-1$
		}
	}
	
	@SuppressWarnings("deprecation")
	public final static Shop getUpgradeSetupShop() {
		final Map<String, CannonConstructor> cannons = Cannons.getCannons();
		
		int rows = (int) Math.ceil((double) cannons.size() / 9);
		
		final ItemStack fill = new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.ORANGE.getData());
		
		Shop s = new Shop(Language.get("shop.name.cannon.setup"), new ClickHandler() { //$NON-NLS-1$
			
			@Override
			public void onItemInteract(ItemInteractEvent event) {
				if (!event.isPickUpAction())
					return;
				
				ItemStack item = event.getItemInSlot();
				
				if (item.isSimilar(fill))
					return;
				
				for (Entry<String, CannonConstructor> entry : cannons.entrySet()) {
					if (item.isSimilar(entry.getValue().getItem())) {
						event.setNextShop(Cannon.getUpgradeSetupShop(entry.getKey()));
						return;
					}
				}
			}

			@Override
			public void onInventoryClose(InventoryCloseEvent event) {}
		}, rows);
		
		s.fill(fill);
		
		int i = 0;
		
		for (Entry<String, CannonConstructor> entry : cannons.entrySet())
			s.set(i++, entry.getValue().getItem());
		
		return s;
	}
	
	public static String getDefaultCannon() {
		return FireballCannon.NAME;
	}
}
