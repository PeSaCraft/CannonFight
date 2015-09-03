package de.pesacraft.cannonfight.game.util;

import java.util.Map;
import java.util.Map.Entry;

import org.bson.Document;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemSerializer {
	
	public static Map<String, Object> serialize(ItemStack item) {
		Map<String, Object> serialized = item.serialize();
		
		for (Entry<String, Object> entry : serialized.entrySet()) {
			if (entry.getKey().equals("meta")) {
				entry.setValue(((ItemMeta) entry.getValue()).serialize());
				break;
			}
		}
		
		return serialized;
	}

	@SuppressWarnings("unchecked")
	public static ItemStack deserialize(Map<String, Object> map) {
		ItemMeta meta = null;
		
		for (Entry<String, Object> entry : map.entrySet()) {
			if (entry.getKey().equals("meta")) {
				meta = (ItemMeta) ConfigurationSerialization.deserializeObject((Map<String, Object>) entry.getValue(), ConfigurationSerialization.getClassByAlias("ItemMeta"));
				break;
			}
		}
		
		map.remove("meta");

		ItemStack item = ItemStack.deserialize(map);
		
		if (meta != null)
			item.setItemMeta(meta);
		
		return item;
	}
}
