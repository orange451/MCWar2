package com.orange451.mcwarfare.arena.killstreaks;

import org.bukkit.Location;

import com.orange451.mcwarfare.MCWarfare;

public abstract class MapItem {
	protected Location location;
	
	public MapItem(Location location) {
		this.location = location;
	}
	
	public Location getLocation() {
		return this.location;
	}
	
	public void remove() {
		MCWarfare.getPlugin().removeMapEntity(this);
	}
	
	public void setLocation(Location location) {
		this.location = location;
	}
}
