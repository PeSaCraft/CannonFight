package de.pesacraft.cannonfight.util.money;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.greatmancode.craftconomy3.Cause;
import com.greatmancode.craftconomy3.Common;
import com.greatmancode.craftconomy3.account.Account;
import com.greatmancode.craftconomy3.tools.interfaces.Loader;

import de.pesacraft.cannonfight.data.players.CannonFighter;

public class CraftConomyMoney implements Money {
	private Common craftconomy;
	
	public CraftConomyMoney(Plugin plugin) {
		this.craftconomy = (Common) ((Loader) plugin).getCommon();
	}

	@Override
	public int getMoney(CannonFighter c) {
		Player p = c.getUser().getPlayer();
		Account acc = craftconomy.getAccountManager().getAccount(p.getName(), false);
		return (int) acc.getBalance(p.getWorld().getName(), "CannonCoins");
	}

	@Override
	public boolean giveMoney(CannonFighter c, int amount, String... reason) {
		if (amount <= 0)
			return false;
		
		Player p = c.getUser().getPlayer();
		
		Account acc = craftconomy.getAccountManager().getAccount(p.getName(), false);
		
		double old = acc.getBalance(p.getWorld().getName(), "CannonCoins");
		
		return old < acc.deposit(amount, p.getWorld().getName(), "CannonCoins", Cause.PLUGIN, reason != null ? reason[0] : "");
	}

	@Override
	public boolean takeMoney(CannonFighter c, int amount, String... reason) {
		if (amount <= 0)
			return false;
		
		Player p = c.getUser().getPlayer();
		
		Account acc = craftconomy.getAccountManager().getAccount(p.getName(), false);
		
		if (!acc.hasEnough(amount, p.getWorld().getName(), "CannonCoins"))
			return false;
		
		double old = acc.getBalance(p.getWorld().getName(), "CannonCoins");
		
		return old > acc.withdraw(amount, p.getWorld().getName(), "CannonCoins", Cause.PLUGIN, reason != null ? reason[0] : "");
	
	}

	@Override
	public boolean hasEnoughMoney(CannonFighter c, int amount) {
		return getMoney(c) >= amount;
	}
}
