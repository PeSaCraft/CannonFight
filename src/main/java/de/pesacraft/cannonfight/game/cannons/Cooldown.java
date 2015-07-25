package de.pesacraft.cannonfight.game.cannons;

import org.bukkit.scheduler.BukkitRunnable;

import de.pesacraft.cannonfight.CannonFight;

public abstract class Cooldown {
	/**
	 * The cooldowns remaining time
	 */
	private int timeLeft;
	/**
	 * The cooldowns time
	 */
	private final int time;
	
	private int taskID;
	
	public Cooldown(int time) {
		this.time = time;
		taskID = -1;
	}

	public final boolean start() {
		if (taskID != -1)
			return false;
		
		timeLeft = time;
		
		taskID = new BukkitRunnable() {
			
			@Override
			public void run() {
				if (timeLeft-- <= 0) {
					this.cancel();
					taskID = -1;
					finished();
				}
				else
					update();
			}
		}.runTaskTimer(CannonFight.PLUGIN, 0, 20).getTaskId();
		
		return true;
	}
	
	public final boolean hasFinished() {
		return taskID == -1;
	}
	
	public final double done() {
		return 1 - ((double) timeLeft / time);
	}
	
	protected abstract void update();
	
	protected abstract void finished();
}
