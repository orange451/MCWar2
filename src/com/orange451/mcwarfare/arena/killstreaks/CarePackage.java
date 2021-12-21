package com.orange451.mcwarfare.arena.killstreaks;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.ItemStack;

import com.orange451.mcwarfare.MCWarfare;
import com.orange451.mcwarfare.arena.Team;
import com.orange451.mcwarfare.player.DamageType;
import com.orange451.mcwarfare.player.GamePlayer;
import com.orange451.pvpgunplus.ParticleEffects;
import com.orange451.pvpgunplus.RaycastHelper;

public class CarePackage extends NonBlockPlayerPlacedItem {
	private int task;
	private int ticks;
	private Item grenade;
	private Location lastLocation;
	
	public boolean falling = true;
	public boolean started = false;
	
	public CarePackage(final GamePlayer owner, Location location) {
		super(owner, location);
		
		lastLocation = location;
		
		ItemStack thrown = new ItemStack(Material.GHAST_TEAR, 1);
		this.grenade = owner.getPlayer().getWorld().dropItem(location, thrown);
		this.grenade.setPickupDelay(9999999);
		this.grenade.setVelocity(owner.getPlayer().getLocation().getDirection().normalize().multiply(0.75));
		
		this.owner.getArena().broadcastMessage(this.owner.getTag() + ChatColor.RED + " CALLED IN CAREPACKAGE!");
		
		this.task = Bukkit.getScheduler().scheduleSyncRepeatingTask(MCWarfare.getPlugin(), new Runnable() {

			@Override
			public void run() {
				ticks++;
				
				if (ticks > 20 * 120) {
					remove();
				}
				
				if (grenade != null) {
					ParticleEffects.sendParticle(null, 128, "reddust", grenade.getLocation().add(0, 0.5, 0), 0.0f, 0.0f, 0.0f, 0.0f, 2);
				}
				
				if (started) { //If the chest has started to fall
					if (falling) {
						Location newLoc = getLocation().clone().add(0, -0.3, 0);
						if (!newLoc.getBlock().equals(lastLocation.getBlock())) {
							if (newLoc.getBlock().getType().equals(Material.AIR)) {
								lastLocation.getBlock().setType(Material.AIR);
								lastLocation = newLoc;
								newLoc.getBlock().setType(Material.CHEST);
							}else{
								falling = false;
								lastLocation.getBlock().getLocation().add(0, 1, 0).getBlock().setType(Material.STEP);
								if (grenade != null) {
									grenade.remove();
									grenade = null;
								}
								setLocation(lastLocation);
							}
						}
						setLocation(newLoc);
					}
				}else{
					if (grenade == null || grenade.getVelocity().getY() == 0) {
						started = true;
						setLocation(grenade.getLocation().add(0, 48, 0));
						getLocation().getBlock().setType(Material.CHEST);
						lastLocation = getLocation();
					}
				}
			}
			
		}, 1L, 1L);
	}
	
	public Location getChestLocation() {
		return this.lastLocation;
	}
	
	@Override
	public void remove() {
		if (grenade != null) {
			grenade.remove();
		}
		if (lastLocation != null) {
			if (lastLocation.getBlock().getType().equals(Material.CHEST)) {
				lastLocation.getBlock().setType(Material.AIR);
				lastLocation.clone().add(0, 1, 0).getBlock().setType(Material.AIR);
				lastLocation = null;
			}
		}
		Bukkit.getScheduler().cancelTask(task);
		super.remove();
	}

	public void givePlayerItems() {
		//I GOT REALLY LAZY, THIS WILL CHANGE TO A SYSTEM LATER ON
		int pointer = 0;
		int r1 = MCWarfare.getPlugin().getRandom().nextInt(8);
		int r2 = MCWarfare.getPlugin().getRandom().nextInt(14);
		int r3 = MCWarfare.getPlugin().getRandom().nextInt(18);
		int r4 = MCWarfare.getPlugin().getRandom().nextInt(26);
		if (r1 == 4)
			pointer = 2;
		if (r2 == 4)
			pointer = 4;
		if (r3 == 4)
			pointer = 3;
		if (r4 == 4)
			pointer = 5;
		MCWarfare.getPlugin().serverKillstreaks.get(pointer).execute(owner);
		this.remove();
	}
}
