package de.pesacraft.cannonfight.game.cannons.usable;

import java.util.Map;

import org.bukkit.entity.Player;

import de.pesacraft.cannonfight.data.players.CannonFighter;
import de.pesacraft.cannonfight.game.cannons.Cannon;
import de.pesacraft.cannonfight.game.exceptions.player.NotEnoughAmmoException;

public class SplitBombCannon extends Cannon {

	public SplitBombCannon(Map<String, Object> map) {
		super(map);
	}

	@Override
	public void fire(CannonFighter fighter) throws NotEnoughAmmoException {
		if (fighter.getAmmo() < this.neededAmmo)
			throw new NotEnoughAmmoException(fighter, this.neededAmmo, fighter.getAmmo());
		Player p = fighter.getPlayer();
	}

}
