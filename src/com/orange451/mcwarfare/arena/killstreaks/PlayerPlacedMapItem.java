package com.orange451.mcwarfare.arena.killstreaks;

import org.bukkit.Location;

import com.orange451.mcwarfare.player.GamePlayer;

public abstract class PlayerPlacedMapItem extends MapItem {
	protected GamePlayer owner;
	
	public PlayerPlacedMapItem(GamePlayer owner, Location location) {
		super(location);
		this.owner = owner;
	}
	
	public GamePlayer getOwner() {
		return this.owner;
	}
}
