package com.orange451.mcwarfare.arena;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ArenaSpawn {
	private Location location;
	private Team team;
	private int lastTick = 800;
	
	public ArenaSpawn(Location location, Team team) {
		this.location = location;
		this.team = team;
	}
	
	public void spawn(Player player) {
		this.lastTick = 999;
		player.teleport(location);
	}
	
	public void tick() {
		this.lastTick--;
	}
	
	public int getLastTick() {
		return this.lastTick;
	}
	
	public Team getTeam() {
		return this.team;
	}

	public Location getLocation() {
		return this.location;
	}
}
