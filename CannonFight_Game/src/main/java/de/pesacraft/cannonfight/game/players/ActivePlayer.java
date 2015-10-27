package de.pesacraft.cannonfight.game.players;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.libs.joptsimple.internal.Strings;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import de.pesacraft.cannonfight.game.Cage;
import de.pesacraft.cannonfight.game.CageForm;
import de.pesacraft.cannonfight.game.CannonFightGame;
import de.pesacraft.cannonfight.util.CannonFighter;
import de.pesacraft.cannonfight.util.Language;
import de.pesacraft.cannonfight.util.Language.StringMaker;
import de.pesacraft.cannonfight.util.Language.TimeOutputs;
import de.pesacraft.cannonfight.util.cannons.Cannon;

public class ActivePlayer extends Participant {
	private int lives;
	private int kills;
	
	private Cage cage;
	
	public ActivePlayer(CannonFighter player, String server) {
		super(player, server);
		
		lives = player.getLives();
		kills = 0;
	}
	
	public void createCage() throws IllegalStateException {
		if (hasCage())
			throw new IllegalStateException(Language.get("error.only-one-cage-instance", false));
		
		cage = new Cage(CageForm.PLAYER, getPlayer().getPlayer().getLocation().getBlock());
		
		cage.createCage(new ItemStack(Material.BARRIER));
	}
	
	public void destroyCage() throws IllegalStateException {
		if (!hasCage())
			throw new IllegalStateException(Language.get("error.no-cage-created", false));
		
		cage.destroyCage();
		
		cage = null;
	}
	
	public void createPlatform() throws IllegalStateException {
		final Cage platform = new Cage(CageForm.PLATFORM, getPlayer().getPlayer().getLocation().getBlock());
		
		platform.createCage(new ItemStack(Material.BARRIER));
		
		final ActivePlayer a = this;
		
		new BukkitRunnable() {
			int time = CannonFightGame.PLUGIN.getConfig().getInt("game.time.platform");
			
			@Override
			public void run() {
				if (time == 0) {
					a.getPlayer().sendMessage(Language.get("info.platform.dissapears-now", true));
					platform.destroyCage();
					this.cancel();
					return;
				}
				
				Language.getStringMaker("info.platform.dissapears.time", true).replace("%time%", Language.formatTime(time, Language.TimeOutputs.SECONDS));
				time--;
			}
		}.runTaskTimer(CannonFightGame.PLUGIN, 0, 20);
	}
	
	public boolean teleportToGame(Location loc) {
		return getPlayer().teleport(loc);
	}
	
	public void looseLife() {
		if (isDead())
			return;
		lives--;
	}

	public boolean isDead() {
		return lives == 0;
	}
	
	public void addKill() {
		kills++;
	}
	
	public int getLives() {
		return lives;
	}
	
	public int getKills() {
		return kills;
	}

	public boolean hasCage() {
		return cage != null;
	}
	
	public void sendActionBar() {
		StringMaker valueFormat = Language.getStringMaker("general.cooldown.actionbar.format.value", false);
		List<String> cooldowns = new ArrayList<String>();
		
		for (Cannon c : getPlayer().getActiveItems()) {
			if (c == null)
				continue;
			if (c.hasFinished())
				continue;
			
			cooldowns.add(valueFormat.clone().replace("%cannon%", c.getName()).replace("%time%", Language.formatTime(c.getRemainingTime(), TimeOutputs.SECONDS)).getString());
		}
		
		if (cooldowns.isEmpty())
			return;
		
		String splitter = Language.get("general.cooldown.actionbar.splitter", false);
		String msg = Language.getStringMaker("general.cooldown.actionbar.format.message", false).replace("%values%", Strings.join(cooldowns, splitter)).getString();
		
		PacketPlayOutChat packet = new PacketPlayOutChat(ChatSerializer.a(msg), (byte) 2);
		((CraftPlayer) getPlayer().getPlayer()).getHandle().playerConnection.sendPacket(packet);
	}
}
