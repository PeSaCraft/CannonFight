package de.pesacraft.cannonfight.util.shop.upgrade;

public class IntegerUpgradeChanger implements UpgradeChanger<Integer> {

	private int priceSlow;
	private int priceFast;

	private int valueSlow;
	private int valueFast;

	private int initialPrice;
	private int initialValue;
	
	public IntegerUpgradeChanger() {
		this(10, 100, 1, 5);
	}
	
	public IntegerUpgradeChanger(int priceSlow, int priceFast, int valueSlow, int valueFast) {
		this(priceSlow, priceFast, valueSlow, valueFast, 100, 5);
	}
	
	public IntegerUpgradeChanger(int priceSlow, int priceFast, int valueSlow, int valueFast, int initialPrice, int initialValue) {
		this.priceSlow = Math.abs(priceSlow);
		this.priceFast = Math.abs(priceFast);
		
		this.valueSlow = Math.abs(valueSlow);
		this.valueFast = Math.abs(valueFast);
		
		this.initialPrice = initialPrice;
		this.initialValue = initialValue;
	}

	@Override
	public int getInitialPrice() {
		return 100;
	}

	@Override
	public int getSlowPriceChange() {
		return priceSlow;
	}

	@Override
	public int getFastPriceChange() {
		return priceFast;
	}

	@Override
	public void increasePriceFast(Upgrade<Integer> upgrade) {
		upgrade.setPrice(upgrade.getPrice() + priceFast);
	}

	@Override
	public void increasePriceSlow(Upgrade<Integer> upgrade) {
		upgrade.setPrice(upgrade.getPrice() + priceSlow);	
	}

	@Override
	public void decreasePriceSlow(Upgrade<Integer> upgrade) {
		upgrade.setPrice(upgrade.getPrice() - priceSlow);
	}

	@Override
	public void decreasePriceFast(Upgrade<Integer> upgrade) {
		upgrade.setPrice(upgrade.getPrice() - priceFast);	
	}

	@Override
	public Integer getInitialValue() {
		return initialValue;
	}

	@Override
	public Integer getSlowValueChange() {
		return valueSlow;
	}

	@Override
	public Integer getFastValueChange() {
		return valueFast;
	}

	@Override
	public void increaseValueFast(Upgrade<Integer> upgrade) {
		upgrade.setValue(upgrade.getValue() + valueFast);
	}

	@Override
	public void increaseValueSlow(Upgrade<Integer> upgrade) {
		upgrade.setValue(upgrade.getValue() + valueSlow);
	}

	@Override
	public void decreaseValueSlow(Upgrade<Integer> upgrade) {
		upgrade.setValue(upgrade.getValue() - valueSlow);
	}

	@Override
	public void decreaseValueFast(Upgrade<Integer> upgrade) {
		upgrade.setValue(upgrade.getValue() - valueFast);
	}
}
