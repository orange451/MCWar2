package com.orange451.mcwarfare.player;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.orange451.mcwarfare.MCWarfare;

public class GameClass {
	private ItemStack primary;
	private ItemStack secondary;
	private ItemStack lethal;
	private ItemStack tactical;
	private ItemStack knife;
	private String perkName;
	private boolean changed;
	private boolean loaded = false;
	
	public GameClass() {
		//
	}
	
	public GameClass setPrimary(int id, int amt, byte dat) {
		if (loaded)
			changed = true;
		
		this.primary = setItemStack(id, amt, dat);
		return this;
	}
	
	public GameClass setSecondary(int id, int amt, byte dat) {
		if (loaded)
			changed = true;
		
		this.secondary = setItemStack(id, amt, dat);
		return this;
	}
	
	public GameClass setLethal(int id, int amt, byte dat) {
		if (loaded)
			changed = true;
		
		this.lethal = setItemStack(id, amt, dat);
		return this;
	}
	
	public GameClass setTactical(int id, int amt, byte dat) {
		if (loaded)
			changed = true;
		
		this.tactical = setItemStack(id, amt, dat);
		return this;
	}
	
	public GameClass setKnife(int id, int amt, byte dat) {
		if (loaded)
			changed = true;
		
		this.knife = setItemStack(id, amt, dat);
		return this;
	}
	
	public GameClass setPerk(String name) {
		if (loaded)
			changed = true;
		
		this.perkName = name;
		return this;
	}
	
	private ItemStack setItemStack(int id, int amt, byte dat) {
		if (dat <= 0) {
			return new ItemStack(id, amt);
		}
		return new ItemStack(id, amt, dat);
	}

	public ItemStack getPrimary() {
		return primary;
	}
	
	public ItemStack getSecondary() {
		return secondary;
	}
	
	public ItemStack getLethal() {
		if (lethal.getType().equals(Material.AIR) || lethal.getType().equals(Material.SNOW_BALL)) {
			ItemStack ret = new ItemStack(Material.SNOW_BALL);
			ItemMeta meta = ret.getItemMeta();
			meta.setDisplayName("No Lethal");
			ret.setItemMeta(meta);
			return ret;
		}
		return lethal;
	}
	
	public ItemStack getTactical() {
		if (tactical.getType().equals(Material.AIR) || tactical.getType().equals(Material.SNOW_BALL)) {
			ItemStack ret = new ItemStack(Material.SNOW_BALL);
			ItemMeta meta = ret.getItemMeta();
			meta.setDisplayName("No Tactical");
			ret.setItemMeta(meta);
			return ret;
		}
		return tactical;
	}
	
	public ItemStack getKnife() {
		return new ItemStack(knife.getType(), 1);
	}
	
	public String getPerk() {
		return perkName;
	}
	
	public void setLoaded() {
		this.loaded = true;
	}

	public boolean isChanged() {
		return this.changed;
	}

	public void reset() {
		setPrimary(MCWarfare.getPlugin().getGunManager().getFirstGunByType("primary"), 1, (byte)0);
		setSecondary(MCWarfare.getPlugin().getGunManager().getFirstGunByType("secondary"), 1, (byte)0);
		setLethal(0, 0, (byte)0);
		setTactical(0, 0, (byte)0);
		setKnife(Material.IRON_SWORD.getId(), 1, (byte)0);
		setPerk("");
	}
	
}
