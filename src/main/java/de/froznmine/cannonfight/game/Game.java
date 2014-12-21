package de.froznmine.cannonfight.game;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import de.froznmine.lobby.game.GameUser;
import de.froznmine.lobby.game.GameUser.PlayerRole;
import de.froznmine.lobby.game.GameState;
import de.froznmine.lobby.game.GameUsers;
import de.froznmine.lobby.game.IGame;
import de.froznmine.lobby.user.Users;

public class Game implements IGame {
	private Arena arena;
	private List<GameUser> participants;
	private static Location lobbyLocation;
	private GameState state;
	
	public Game(Arena arena) {
		this.arena = arena;
		this.state = GameState.LOBBY;
		this.participants = new ArrayList<GameUser>();
	}
	
	@Override
	public boolean addPlayer(Player p) {
		int players = 0;
		
		for (GameUser user : participants)
			if (user.getRole() == PlayerRole.PLAYER) players++;
		
		GameUser user = GameUsers.get(p);
		
		if (!participants.contains(user)) participants.add(user);
		
		if (players >= arena.getMaxPlayers()) user.setRole(PlayerRole.SPECTATOR);
		else user.setRole(PlayerRole.PLAYER);
		
		int tries = 0;
		while (!user.teleport(lobbyLocation))
			if (tries++ == 10) return false;
		
		for (Player online : Bukkit.getOnlinePlayers()) {
			if (participants.contains(GameUsers.get(online))) p.showPlayer(online);
			else p.hidePlayer(online);
		}
		
		return true;
	}
	
	@Override
	public void removePlayer(Player p) {
		GameUser user = GameUsers.get(p);

		if (!participants.contains(user)) return;
		
		participants.remove(user);
		
		if (state == GameState.LOBBY) {
			for (GameUser participant : participants) {
				if (participant.getRole() == PlayerRole.SPECTATOR) {
					participant.setRole(PlayerRole.SPECTATOR);
					break;
				}
			}
		}
	}
	
	@Override
	public void start() {
		for (GameUser user : participants) {
			switch (user.getRole()) {
			case PLAYER:
				arena.teleport(user);
				// TODO teleport to arena
				break;
			case SPECTATOR:
				// TODO teleport to arena but spectate
				break;
			default:
				break;
			}
		}
	}

	@Override
	public int getMaxPlayers() {
		return arena.getMaxPlayers();
	}
	
}
