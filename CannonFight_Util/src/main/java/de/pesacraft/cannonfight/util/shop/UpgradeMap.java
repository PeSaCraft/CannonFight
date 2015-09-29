package de.pesacraft.cannonfight.util.shop;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bson.Document;

public class UpgradeMap extends HashMap<String, UpgradeList<?>> {
	
	private static final long serialVersionUID = 6809923567668862582L;

	public UpgradeMap() {
		super(1);
	}
	
	public <T> void setUpgrade(String name, Entry<String, Object> entry, Class<T> type) {
		UpgradeList<T> upgrades;
		
		if (this.containsKey(name))
			upgrades = (UpgradeList<T>) this.get(name);
		else {
			upgrades = new UpgradeList<T>();
			this.put(name, upgrades);
		}
		
		int level = Integer.parseInt(entry.getKey());
		Upgrade<T> upgrade = new Upgrade<T>((Document) entry.getValue());
		upgrades.setLevel(level, upgrade);
	}
	
	public <T> void setUpgrade(String name, int level, int price, T value, Class<T> type) {
		UpgradeList<T> upgrades;
		
		if (this.containsKey(name))
			upgrades = (UpgradeList<T>) this.get(name);
		else {
			upgrades = new UpgradeList<T>();
			this.put(name, upgrades);
		}
		
		Upgrade<T> upgrade = new Upgrade<T>(price, value);
		upgrades.setLevel(level, upgrade);
	}
	
	public <T> void setPrice(String name, int level, int price, Class<T> type) {
		getUpgrade(name, level, type).setPrice(price);
	}
	
	public <T> void setValue(String name, int level, T value, Class<T> type) {
		getUpgrade(name, level, type).setValue(value);
	}

	public <T> Upgrade<T> getUpgrade(String name, int level, Class<T> type) {
		if (this.containsKey(name))
			return (Upgrade<T>) this.get(name).getForLevel(level);
	
		UpgradeList<T> upgrades = new UpgradeList<T>();
	
		Upgrade<T> upgrade;
		try {
			upgrade = new Upgrade<T>(100, type.newInstance());
		} catch (InstantiationException | IllegalAccessException ex) {
			// cannot instantiate it, initialize with null
			upgrade = new Upgrade<T>(100, null);
		} 
		upgrades.setLevel(level, upgrade);
		this.put(name, upgrades);
		
		return upgrade;
	}

	public int getLevels(String name) {
		if (this.containsKey(name))
			return this.get(name).getLevels();
		
		// upgrade not existant: no levels for that upgrade
		return 0;
	}
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<String, Object>();
		
		for (Entry<String, UpgradeList<?>> entry : this.entrySet())
			map.put(entry.getKey(), entry.getValue().serialize());

		return map;
	}
}
