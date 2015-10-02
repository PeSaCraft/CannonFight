package de.pesacraft.cannonfight.util.shop.upgrade;

public class DoubleUpgradeChanger implements UpgradeChanger<Double> {

	private int priceSlow;
	private int priceFast;

	private double valueSlow;
	private double valueFast;

	private int initialPrice;
	private double initialValue;
	
	public DoubleUpgradeChanger() {
		this(10, 100, 0.1, 1);
	}
	
	public DoubleUpgradeChanger(int priceSlow, int priceFast, double valueSlow, double valueFast) {
		this(priceSlow, priceFast, valueSlow, valueFast, 100, 2.5);
	}
	
	public DoubleUpgradeChanger(int priceSlow, int priceFast, double valueSlow, double valueFast, int initialPrice, double initialValue) {
		this.priceSlow = Math.abs(priceSlow);
		this.priceFast = Math.abs(priceFast);
		
		this.valueSlow = Math.abs(valueSlow);
		this.valueFast = Math.abs(valueFast);
		
		this.initialPrice = initialPrice;
		this.initialValue = initialValue;
	}

	@Override
	public int getInitialPrice() {
		return initialPrice;
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
	public void increasePriceFast(Upgrade<Double> upgrade) {
		upgrade.setPrice(upgrade.getPrice() + priceFast);
	}

	@Override
	public void increasePriceSlow(Upgrade<Double> upgrade) {
		upgrade.setPrice(upgrade.getPrice() + priceSlow);
	}

	@Override
	public void decreasePriceSlow(Upgrade<Double> upgrade) {
		upgrade.setPrice(upgrade.getPrice() - priceSlow);
	}

	@Override
	public void decreasePriceFast(Upgrade<Double> upgrade) {
		upgrade.setPrice(upgrade.getPrice() - priceFast);
	}

	@Override
	public Double getInitialValue() {
		return initialValue;
	}

	@Override
	public Double getSlowValueChange() {
		return valueSlow;
	}

	@Override
	public Double getFastValueChange() {
		return valueFast;
	}

	@Override
	public void increaseValueFast(Upgrade<Double> upgrade) {
		upgrade.setValue(upgrade.getValue() + valueFast);
	}

	@Override
	public void increaseValueSlow(Upgrade<Double> upgrade) {
		upgrade.setValue(upgrade.getValue() + valueSlow);
	}

	@Override
	public void decreaseValueSlow(Upgrade<Double> upgrade) {
		upgrade.setValue(upgrade.getValue() - valueSlow);
	}

	@Override
	public void decreaseValueFast(Upgrade<Double> upgrade) {
		upgrade.setValue(upgrade.getValue() - valueFast);
	}

}
