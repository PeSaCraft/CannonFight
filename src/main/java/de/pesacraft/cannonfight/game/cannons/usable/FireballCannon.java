package de.pesacraft.cannonfight.game.cannons.usable;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import de.pesacraft.cannonfight.CannonFight;
import de.pesacraft.cannonfight.data.players.CannonFighter;
import de.pesacraft.cannonfight.game.cannons.Cannon;
import de.pesacraft.cannonfight.util.Database;

public class FireballCannon extends Cannon implements Listener {
	/**
	 * ItemStack representing the cannon ingame
	 */
	protected static final ItemStack ITEM;
	/**
	 * The cannons name
	 */
	protected static final String NAME = "FireballCannon";
	/**
	 * The cannons id
	 */
	protected static final int id;
	
	static {
		Configuration config = YamlConfiguration.loadConfiguration(new File(CannonFight.PLUGIN.getDataFolder() + "/cannons.yml"));
		
		ITEM = config.getItemStack(NAME);
		
		int i = -1;
		try {
			ResultSet result = Database.execute("SELECT id FROM " + Database.getTablePrefix() + "cannons WHERE name = " + NAME, false);
			if (!result.isBeforeFirst()) {
				// steht nicht drin!
				// spalte in spieler leveln hinzufuegen
				Database.execute("ALTER TABLE `" + Database.getTablePrefix() + "cannonLevels` ADD `" + NAME + "` INT NOT NULL", false);
				// cannon in db eintragen
				Database.execute("INSERT INTO `" + Database.getTablePrefix() + "cannons` (`id`, `name`) VALUES (NULL, '" + NAME + "')", false);
			}
			i = Database.execute("SELECT id FROM " + Database.getTablePrefix() + "cannons WHERE name = " + NAME, true).getInt("id");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		id = i;
	}

	@Override
	public String getName() {
		return NAME;
	}
	
	private int maxAmmo;
	private int currentAmmo;
	private float radius;
	private double damage;
	private ItemStack item;
	
	private List<Fireball> shoot;
	
	public FireballCannon(int levelAmmo, int levelCooldown, int levelRadius, int levelDamage) throws SQLException {
		super(Database.execute("SELECT cooldown FROM " + Database.getTablePrefix() + "cannonUpgrades WHERE id = " + id + " AND level = " + levelCooldown, true).getInt("cooldown"));
	
		currentAmmo = maxAmmo = Database.execute("SELECT ammo FROM " + Database.getTablePrefix() + "cannonUpgrades WHERE id = " + id + " AND level = " + levelAmmo, true).getInt("ammo");
			
		radius = Database.execute("SELECT custom1 FROM " + Database.getTablePrefix() + "cannonUpgrades WHERE id = " + id + " AND level = " + levelRadius, true).getFloat("custom1");
		damage = Database.execute("SELECT custom2 FROM " + Database.getTablePrefix() + "cannonUpgrades WHERE id = " + id + " AND level = " + levelDamage, true).getDouble("custom2");
	
		item = ITEM.clone();
		
		shoot = new ArrayList<Fireball>();
		
		Bukkit.getServer().getPluginManager().registerEvents(this, CannonFight.PLUGIN);
	}

	@Override
	public ItemStack getItem() {
		return item;
	}
	
	@Override
	public boolean fire(CannonFighter fighter) {
		if (!hasFinished())
			return false;
		
		if (--currentAmmo == 0)
			start();
		else
			item.setAmount(currentAmmo);
		
		Player p = fighter.getPlayer();
		
		Fireball fball = p.launchProjectile(Fireball.class);
		fball.setVelocity(fball.getVelocity().multiply(2));
		shoot.add(fball);
		
		return true;
	}
	
	@EventHandler
	public void onExplosionPrime(ExplosionPrimeEvent event) {
		if (shoot.contains(event.getEntity()))
			event.setRadius(radius);
	}
	
	@EventHandler
	public void onEntityDamageByEntity(final EntityDamageByEntityEvent event) {
		if (!shoot.contains(event.getDamager()))
			return;
		
		event.setDamage(damage);
		
		/* muss später aus der Liste entfernt werden,
		 * da es mehrere EntityDamageByEntityEvents gibt,
		 * pro getroffene Entity eins
		 */
		new BukkitRunnable() {
			@Override
			public void run() {
				shoot.remove(event.getDamager());
			}
		}.runTaskLater(CannonFight.PLUGIN, 1);
	}
	
	@Override
	public int getMaxAmmo() {
		return maxAmmo;
	}

	@Override
	public boolean hasAmmo() {
		return currentAmmo > 0;
	}

	@Override
	protected void update() {
		item.setDurability((short) (item.getType().getMaxDurability() * done()));
	}

	@Override
	protected void finished() {
		item.setDurability(item.getType().getMaxDurability());
	}
}
