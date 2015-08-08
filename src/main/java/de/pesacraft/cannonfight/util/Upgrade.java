package de.pesacraft.cannonfight.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

public class Upgrade<T> implements ConfigurationSerializable {
	private int price;
	private T value;
	
	public Upgrade(int price, T value) {
		this.price = price;
		this.value = value;
	}

	public Upgrade(Map<String, Object> map) {
		this.price = (Integer) map.get("price");
		this.value = (T) map.get("value");
	}
	
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("price", price);
		map.put("value", value);
		
		return map;
	}
	
	public int getPrice() {
		return price;
	}
	
	public T getValue() {
		return value;
	}
}
