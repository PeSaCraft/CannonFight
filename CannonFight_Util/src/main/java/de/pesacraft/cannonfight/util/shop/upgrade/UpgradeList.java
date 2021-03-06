package de.pesacraft.cannonfight.util.shop.upgrade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.collect.Lists;

import de.pesacraft.cannonfight.util.Language;
import de.pesacraft.cannonfight.util.cannons.Cannons;
import de.pesacraft.cannonfight.util.shop.ClickHandler;
import de.pesacraft.cannonfight.util.shop.ItemInteractEvent;
import de.pesacraft.cannonfight.util.shop.Shop;

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
		while (getLevels() < level) {
			// fill list to the wanted level
			this.add(upgrade.clone());
		}
		
		// actual index is one less
		level--;
		
		return this.set(level, upgrade);
	}

	private void removeLevel(int level) {
		level--;
		this.remove(level);
	}
	
	public Upgrade<T> getForLevel(int level) {
		level--;
		try {
			return this.get(level);
		}
		catch (IndexOutOfBoundsException ex) {
			return null;
		}
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

	@SuppressWarnings("deprecation")
	public Shop getUpgradeSetupShop(final int level) {
		
		Upgrade<T> u = getForLevel(level);
		
		if (u == null) {
			// new level
			// initialise with value of level before
			u = getForLevel(level - 1).clone();
			
			if (u == null) {
				// seems to be first level
				// initial values have to be fetched from the upgrade changer
				u = new Upgrade<T>(getUpgradeChanger().getInitialPrice(), getUpgradeChanger().getInitialValue());
			}
			
			// level wasn't there, has to be put in list.
			setLevel(level, u);
		}
		
		final Upgrade<T> upgrade = u;
		
		final ItemStack fill = new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.LIGHT_BLUE.getData());
		
		final ItemStack prev = new ItemStack(Material.ARROW);
		final ItemStack next = new ItemStack(Material.ARROW);
		
		final ItemStack changePriceDownFast = new ItemStack(Material.WOOL, 1, DyeColor.RED.getData());
		final ItemStack changePriceDownSlow = new ItemStack(Material.WOOL, 1, DyeColor.RED.getData());
		final ItemStack price = new ItemStack(Material.WOOL);
		final ItemStack changePriceUpSlow = new ItemStack(Material.WOOL, 1, DyeColor.GREEN.getData());
		final ItemStack changePriceUpFast = new ItemStack(Material.WOOL, 1, DyeColor.GREEN.getData());
		
		final ItemStack changeValueDownFast = new ItemStack(Material.WOOL, 1, DyeColor.RED.getData());
		final ItemStack changeValueDownSlow = new ItemStack(Material.WOOL, 1, DyeColor.RED.getData());
		final ItemStack value = new ItemStack(Material.WOOL);
		final ItemStack changeValueUpSlow = new ItemStack(Material.WOOL, 1, DyeColor.GREEN.getData());
		final ItemStack changeValueUpFast = new ItemStack(Material.WOOL, 1, DyeColor.GREEN.getData());
		
		final ItemStack delete = new ItemStack(Material.BARRIER);
		
		ItemMeta meta = prev.getItemMeta();
		meta.setDisplayName(Language.getStringMaker("shop.specific-cannon.setup.upgrade.level.change", false).replace("%level%", String.valueOf(level - 1)).getString());
		prev.setItemMeta(meta);
		
		meta = next.getItemMeta();
		
		if (getLevels() == level)
			// next will be new one
			meta.setDisplayName(Language.getStringMaker("shop.specific-cannon.setup.upgrade.level.create", false).replace("%level%", String.valueOf(level + 1)).getString());
		else
			// next already exists
			meta.setDisplayName(Language.getStringMaker("shop.specific-cannon.setup.upgrade.level.change", false).replace("%level%", String.valueOf(level + 1)).getString());
		
		next.setItemMeta(meta);
		
		// setup price items
		meta = changePriceDownFast.getItemMeta();
		meta.setDisplayName(Language.get("shop.specific-cannon.setup.upgrade.level.price.decrease.strong", false));
		List<String> lore = Lists.newArrayList(Language.getStringMaker("shop.specific-cannon.setup.upgrade.level.price.decrease.strong.value", false).replace("%price%", Language.formatCoins(getUpgradeChanger().getFastPriceChange())).getString().split("\n"));
		meta.setLore(lore);
		changePriceDownFast.setItemMeta(meta);
		
		meta = changePriceDownSlow.getItemMeta();
		meta.setDisplayName(Language.get("shop.specific-cannon.setup.upgrade.level.price.decrease.slight", false));
		lore = Lists.newArrayList(Language.getStringMaker("shop.specific-cannon.setup.upgrade.level.price.decrease.slight.value", false).replace("%price%", Language.formatCoins(getUpgradeChanger().getSlowPriceChange())).getString().split("\n"));
		meta.setLore(lore);
		changePriceDownSlow.setItemMeta(meta);
		
		meta = price.getItemMeta();
		meta.setDisplayName(Language.get("shop.specific-cannon.setup.upgrade.level.price.current.title", false));
		lore = Lists.newArrayList(Language.getStringMaker("shop.specific-cannon.setup.upgrade.level.price.current.value", false).replace("%price%", Language.formatCoins(upgrade.getPrice())).getString().split("\n"));
		meta.setLore(lore);
		price.setItemMeta(meta);
		
		meta = changePriceUpSlow.getItemMeta();
		meta.setDisplayName(Language.get("shop.specific-cannon.setup.upgrade.level.price.increase.slight", false));
		lore = Lists.newArrayList(Language.getStringMaker("shop.specific-cannon.setup.upgrade.level.price.increase.slight.value", false).replace("%price%", Language.formatCoins(getUpgradeChanger().getSlowPriceChange())).getString().split("\n"));
		meta.setLore(lore);
		changePriceUpSlow.setItemMeta(meta);
		
		meta = changePriceUpFast.getItemMeta();
		meta.setDisplayName(Language.get("shop.specific-cannon.setup.upgrade.level.price.increase.strong", false));
		lore = Lists.newArrayList(Language.getStringMaker("shop.specific-cannon.setup.upgrade.level.price.increase.strong.value", false).replace("%price%", Language.formatCoins(getUpgradeChanger().getFastPriceChange())).getString().split("\n"));
		meta.setLore(lore);
		changePriceUpFast.setItemMeta(meta);
		
		// setup value items
		meta = changeValueDownFast.getItemMeta();
		meta.setDisplayName(Language.get("shop.specific-cannon.setup.upgrade.level.value.decrease.strong", false));
		lore = Lists.newArrayList(Language.getStringMaker("shop.specific-cannon.setup.upgrade.level.value.decrease.strong.value", false).replace("%value%", getUpgradeChanger().getFastValueChange().toString()).getString().split("\n"));
		meta.setLore(lore);
		changeValueDownFast.setItemMeta(meta);
		
		meta = changeValueDownSlow.getItemMeta();
		meta.setDisplayName(Language.get("shop.specific-cannon.setup.upgrade.level.value.decrease.slight", false));
		lore = Lists.newArrayList(Language.getStringMaker("shop.specific-cannon.setup.upgrade.level.value.decrease.slight.value", false).replace("%value%", getUpgradeChanger().getSlowValueChange().toString()).getString().split("\n"));
		meta.setLore(lore);
		changeValueDownSlow.setItemMeta(meta);
		
		meta = value.getItemMeta();
		meta.setDisplayName(Language.get("shop.specific-cannon.setup.upgrade.level.value.current.title", false));
		lore = Lists.newArrayList(Language.getStringMaker("shop.specific-cannon.setup.upgrade.level.value.current.value", false).replace("%value%", upgrade.getValue().toString()).getString().split("\n"));
		meta.setLore(lore);
		value.setItemMeta(meta);
		
		meta = changeValueUpSlow.getItemMeta();
		meta.setDisplayName(Language.get("shop.specific-cannon.setup.upgrade.level.value.increase.slight", false));
		lore = Lists.newArrayList(Language.getStringMaker("shop.specific-cannon.setup.upgrade.level.value.increase.slight.value", false).replace("%value%", getUpgradeChanger().getSlowValueChange().toString()).getString().split("\n"));
		meta.setLore(lore);
		changeValueUpSlow.setItemMeta(meta);
		
		meta = changeValueUpFast.getItemMeta();
		meta.setDisplayName(Language.get("shop.specific-cannon.setup.upgrade.level.value.increase.strong", false));
		lore = Lists.newArrayList(Language.getStringMaker("shop.specific-cannon.setup.upgrade.level.value.increase.strong.value", false).replace("%value%", getUpgradeChanger().getFastValueChange().toString()).getString().split("\n"));
		meta.setLore(lore);
		changeValueUpFast.setItemMeta(meta);
		
		meta = delete.getItemMeta();
		meta.setDisplayName(Language.get("shop.specific-cannon.setup.upgrade.level.delete", false));
		delete.setItemMeta(meta);
		
		Shop s = new Shop(Language.getStringMaker("shop.specific-cannon.setup.upgrade.level.name", false).replace("%level%", String.valueOf(level)).getString(), new ClickHandler() {
		
			@Override
			public void onItemInteract(ItemInteractEvent event) {
				if (!event.isPickUpAction())
					return;
				
				ItemStack item = event.getItemInSlot();
				
				if (item.isSimilar(fill))
					return;
				
				if (item.isSimilar(prev)) {
					event.setNextShop(getUpgradeSetupShop(level - 1));
					return;
				}
				
				if (item.isSimilar(next)) {
					event.setNextShop(getUpgradeSetupShop(level + 1));
					return;
				}
				
				// price modification, clicking on price does nothing
				if (item.isSimilar(changePriceDownFast)) {
					getUpgradeChanger().decreasePriceFast(upgrade);
					event.setNextShop(getUpgradeSetupShop(level));
					return;
				}
				
				if (item.isSimilar(changePriceDownSlow)) {
					getUpgradeChanger().decreasePriceSlow(upgrade);
					event.setNextShop(getUpgradeSetupShop(level));
					return;
				}
				
				if (item.isSimilar(changePriceUpSlow)) {
					getUpgradeChanger().increasePriceSlow(upgrade);
					event.setNextShop(getUpgradeSetupShop(level));
					return;
				}
				
				if (item.isSimilar(changePriceUpFast)) {
					getUpgradeChanger().increasePriceFast(upgrade);
					event.setNextShop(getUpgradeSetupShop(level));
					return;
				}
				
				// value modification, clicking on value does nothing
				if (item.isSimilar(changeValueDownFast)) {
					getUpgradeChanger().decreaseValueFast(upgrade);
					event.setNextShop(getUpgradeSetupShop(level));
					return;
				}
				
				if (item.isSimilar(changeValueDownSlow)) {
					getUpgradeChanger().decreaseValueSlow(upgrade);
					event.setNextShop(getUpgradeSetupShop(level));
					return;
				}
				
				if (item.isSimilar(changeValueUpSlow)) {
					getUpgradeChanger().increaseValueSlow(upgrade);
					event.setNextShop(getUpgradeSetupShop(level));
					return;
				}
				
				if (item.isSimilar(changeValueUpFast)) {
					getUpgradeChanger().increaseValueFast(upgrade);
					event.setNextShop(getUpgradeSetupShop(level));
					return;
				}
				
				if (item.isSimilar(delete)) {
					removeLevel(level);
					
					if (getLevels() == 0)
						// back to main when no level is left.
						// nothing else is possible with the provided information
						event.setNextShop(Cannons.getUpgradeSetupShop());
					else {
						if (level - 1 == 0)
							// level 0 doesn't exist, have to stay in level 1, which exists!
							event.setNextShop(getUpgradeSetupShop(1));
						else
							// go to the level before the deleted one.
							// otherwise, e.g. if there were 3 levels and I
							// delete level 3 It would recreate level 3, thus
							// deleting a level would be impossible
							event.setNextShop(getUpgradeSetupShop(level - 1));
					}
					return;
				}
			}
		}, 6);
		
		s.fill(fill);
		
		if (level != 1)
			// level 1 has no previous
			s.set(0 * 9 + 0, prev); // (0, 0) Previous level
		
		s.set(0 * 9 + 8, next); // (0, 8) Next level
		
		s.set(1 * 9 + 2, changePriceDownFast); // (1, 2) Change price down fast
		s.set(1 * 9 + 3, changePriceDownSlow); // (1, 3) Change price down slow
		s.set(1 * 9 + 4, price); // (1, 4) current price
		s.set(1 * 9 + 5, changePriceUpSlow); // (1, 5) Change price up slow
		s.set(1 * 9 + 6, changePriceUpFast); // (1, 6) Change price up fast
		
		s.set(2 * 9 + 2, changeValueDownFast); // (2, 2) Change value down fast
		s.set(2 * 9 + 3, changeValueDownSlow); // (2, 3) Change value down slow
		s.set(2 * 9 + 4, value); // (2, 4) current value
		s.set(2 * 9 + 5, changeValueUpSlow); // (2, 5) Change value up slow
		s.set(2 * 9 + 6, changeValueUpFast); // (2, 6) Change value up fast
		
		s.set(4 * 9 + 4, delete); // (4, 4) Delete upgrade
		
		return s;
	}
}
