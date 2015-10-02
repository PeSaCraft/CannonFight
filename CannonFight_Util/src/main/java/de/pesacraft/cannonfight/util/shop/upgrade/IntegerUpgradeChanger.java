package de.pesacraft.cannonfight.util.shop.upgrade;

public class IntegerUpgradeChanger implements UpgradeChanger<Integer> {

	private int slow;
	private int fast;
	
	public IntegerUpgradeChanger(int slow, int fast) {
		this.slow = Math.abs(slow);
		this.fast = Math.abs(fast);
	}

	@Override
	public void increaseFast(Upgrade<Integer> upgrade) {
		upgrade.setValue(upgrade.getValue() + fast);
	}

	@Override
	public void increaseSlow(Upgrade<Integer> upgrade) {
		upgrade.setValue(upgrade.getValue() + slow);
	}

	@Override
	public void decreaseSlow(Upgrade<Integer> upgrade) {
		upgrade.setValue(upgrade.getValue() - slow);	
	}

	@Override
	public void decreaseFast(Upgrade<Integer> upgrade) {
		upgrade.setValue(upgrade.getValue() - fast);
	}
}
