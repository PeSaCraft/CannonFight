package de.pesacraft.cannonfight.util.shop.upgrade;

public interface UpgradeChanger<T> {

	public void increaseFast(Upgrade<T> upgrade);
	
	public void increaseSlow(Upgrade<T> upgrade);
	
	public void decreaseSlow(Upgrade<T> upgrade);
	
	public void decreaseFast(Upgrade<T> upgrade);
}
