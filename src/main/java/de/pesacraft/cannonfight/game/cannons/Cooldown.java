package de.pesacraft.cannonfight.game.cannons;

import org.bukkit.Bukkit;
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
	private int time;
	
	private int taskID;
	
	public Cooldown(int time) {
		this.time = time * 4; // 4 updates per second
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
		}.runTaskTimer(CannonFight.PLUGIN, 0, 5).getTaskId();
		
		return true;
	}
	
	public final boolean hasFinished() {
		return taskID == -1;
	}
	
	public final double done() {
		return (double) timeLeft / time;
	}
	
	public final boolean setTime(int time) {
		if (!hasFinished())
			// Timer running: cant change time
			return false;
		
		this.time = time;
		return true;
	}
	
	protected final void cancel() {
		if (!hasFinished()) {
			Bukkit.getScheduler().cancelTask(taskID);
			taskID = -1;
		}
	}
	
	protected abstract void update();
	
	protected abstract void finished();
}
