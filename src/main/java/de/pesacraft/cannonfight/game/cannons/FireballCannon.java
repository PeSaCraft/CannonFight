package de.pesacraft.cannonfight.game.cannons;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import de.pesacraft.cannonfight.CannonFight;
import de.pesacraft.cannonfight.data.players.CannonFighter;
import de.pesacraft.cannonfight.util.Database;

public class FireballCannon extends Cannon {
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
	private double radius;
	private double damage;
	private ItemStack item;
	
	public FireballCannon(int levelAmmo, int levelCooldown, int levelRadius, int levelDamage) throws SQLException {
		super(Database.execute("SELECT cooldown FROM " + Database.getTablePrefix() + "cannonUpgrades WHERE id = " + id + " AND level = " + levelCooldown, true).getInt("cooldown"));
	
		currentAmmo = maxAmmo = Database.execute("SELECT ammo FROM " + Database.getTablePrefix() + "cannonUpgrades WHERE id = " + id + " AND level = " + levelAmmo, true).getInt("ammo");
			
		radius = Database.execute("SELECT custom1 FROM " + Database.getTablePrefix() + "cannonUpgrades WHERE id = " + id + " AND level = " + levelRadius, true).getDouble("custom1");
		damage = Database.execute("SELECT custom2 FROM " + Database.getTablePrefix() + "cannonUpgrades WHERE id = " + id + " AND level = " + levelDamage, true).getDouble("custom2");
	
		item = ITEM.clone();
	}

	@Override
	public ItemStack getItem() {
		return item;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean fire(CannonFighter fighter) {
		if (!hasFinished())
			return false;
		
		if (--currentAmmo == 0)
			start();
		else
			item.setAmount(currentAmmo);
		
		Player p = fighter.getPlayer();
		
		Block b = p.getLocation().getBlock().getRelative(0, -1, 0);
		
		Material m = b.getType();
		
		b.setType(Material.SKULL);
		Skull skull = (Skull) b.getState();
		skull.setSkullType(SkullType.PLAYER);
		skull.setOwner("FroznMine");
		skull.update(true);
		
		FallingBlock fb = p.getWorld().spawnFallingBlock(p.getLocation(), Material.SKULL, b.getData());
	
		b.setType(m);
		fb.setVelocity(new Vector(0, 50, 0));
		
		return true;
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
