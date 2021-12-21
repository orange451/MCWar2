package com.orange451.mcwarfare.arena;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class ReturnBlock {
	private Material mat;
	private byte bdata;
	private Location location;
	
	public ReturnBlock(Block b) {
		this.location = b.getLocation();
		this.mat = b.getType();
		this.bdata = b.getData();
	}
	
	public void revert() {
		location.getBlock().setType(mat);
		location.getBlock().setData(bdata);
	}

	public Block getBlock() {
		return location.getBlock();
	}
}
