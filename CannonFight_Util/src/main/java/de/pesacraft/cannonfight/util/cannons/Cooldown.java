package de.pesacraft.cannonfight.util.cannons;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import de.pesacraft.cannonfight.util.CannonFightUtil;

public abstract class Cooldown {
	private static final double REFRESHS_PER_SEC = 4; // 4 updates per second
	/**
	 * The cooldowns remaining time
	 */
	private double timeLeft;
	/**
	 * The cooldowns time
	 */
	private double time;
	
	private int taskID;
	
	public Cooldown(double time) {
		this.setTime(time);
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
				System.out.println(timeLeft + " Sek");
			}
		}.runTaskTimer(CannonFightUtil.PLUGIN, 0, (long) (20 / REFRESHS_PER_SEC)).getTaskId();
		
		return true;
	}
	
	public final boolean hasFinished() {
		return taskID == -1;
	}
	
	public final double done() {
		return timeLeft / time;
	}
	
	public final double getRemainingTime() {
		return timeLeft / REFRESHS_PER_SEC;
	}
	
	public final boolean setTime(double time) {
		if (!hasFinished())
			// Timer running: cant change time
			return false;
		
		this.time = time * REFRESHS_PER_SEC;
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
