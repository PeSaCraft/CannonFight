package de.pesacraft.cannonfight.util.shop.upgrade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.inventory.ItemStack;

public class UpgradeList<T> extends ArrayList<Upgrade<T>> {

	private static final long serialVersionUID = 4648166422002372523L;

	private ItemStack item;
	
	private UpgradeChanger<T> upgradeChanger;
	
	/**
	 * Sets the Upgrade available at the specific level.
	 * If the list hasn't enough entries they will be filled.
	 * 
	 * For example if only level 1 and 2 are specified and you
	 * try to add level 5 it will set level 3 and 4 to a clone
	 * of what you want to be level 5.
	 * 
	 * @param level The level to set
	 * @param upgrade The upgrade to set
	 * @return The upgrade that was there before, will return
	 * a copy of upgrade if it was added.
	 */
	public Upgrade<T> setLevel(int level, Upgrade<T> upgrade) {
		while (this.size() < level) {
			// fill list to the wanted level
			this.add(upgrade.clone());
		}
		
		// actual index is one less
		level--;
		
		return this.set(level, upgrade);
	}

	public Upgrade<T> getForLevel(int level) {
		level--;
		return this.get(level);
	}

	public int getLevels() {
		return this.size();
	}
	
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<String, Object>();
		
		for (int i = 0; i < this.size(); i++)
			map.put(String.valueOf(i + 1), this.get(i));

		return map;
	}

	public void setItemStack(ItemStack item) {
		this.item = item.clone();
	}
	
	public ItemStack getItemStack() {
		return item;
	}
	
	public void setUpgradeChanger(UpgradeChanger<T> upgradeChanger) {
		this.upgradeChanger = upgradeChanger;
	}
	
	public UpgradeChanger<T> getUpgradeChanger() {
		return upgradeChanger;
	}
}
