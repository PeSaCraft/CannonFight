package de.pesacraft.cannonfight.util.shop.upgrade;

public class DoubleUpgradeChanger implements UpgradeChanger<Double> {

	private double slow;
	private double fast;
	
	public DoubleUpgradeChanger(double slow, double fast) {
		this.slow = Math.abs(slow);
		this.fast = Math.abs(fast);
	}
	
	@Override
	public void increaseFast(Upgrade<Double> upgrade) {
		upgrade.setValue(upgrade.getValue() + fast);
	}

	@Override
	public void increaseSlow(Upgrade<Double> upgrade) {
		upgrade.setValue(upgrade.getValue() + slow);
	}

	@Override
	public void decreaseSlow(Upgrade<Double> upgrade) {
		upgrade.setValue(upgrade.getValue() - slow);
	}

	@Override
	public void decreaseFast(Upgrade<Double> upgrade) {
		upgrade.setValue(upgrade.getValue() - fast);
	}

}
