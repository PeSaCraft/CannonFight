package de.pesacraft.cannonfight.util.shop.upgrade;

public interface UpgradeChanger<T> {

	public int getInitialPrice();
	
	public int getSlowPriceChange();
	public int getFastPriceChange();
	
	public void increasePriceFast(Upgrade<T> upgrade);
	public void increasePriceSlow(Upgrade<T> upgrade);
	
	public void decreasePriceSlow(Upgrade<T> upgrade);
	public void decreasePriceFast(Upgrade<T> upgrade);
	
	public T getInitialValue();
	
	public T getSlowValueChange();
	public T getFastValueChange();
	
	public void increaseValueFast(Upgrade<T> upgrade);
	public void increaseValueSlow(Upgrade<T> upgrade);
	
	public void decreaseValueSlow(Upgrade<T> upgrade);
	public void decreaseValueFast(Upgrade<T> upgrade);
}
