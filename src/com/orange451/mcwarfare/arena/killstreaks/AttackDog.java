package com.orange451.mcwarfare.arena.killstreaks;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.server.v1_7_R1.EntityWolf;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftWolf;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.orange451.mcwarfare.MCWarfare;
import com.orange451.mcwarfare.arena.Team;
import com.orange451.mcwarfare.player.DamageType;
import com.orange451.mcwarfare.player.GamePlayer;
import com.orange451.pvpgunplus.ParticleEffects;
import com.orange451.pvpgunplus.RaycastHelper;

public class AttackDog extends NonBlockPlayerPlacedItem {
	private int task;
	private int ticks;
	public Wolf dog;
	
	public AttackDog(final GamePlayer owner, Location location) {
		super(owner, location);
		
		this.dog = (Wolf) location.getWorld().spawnEntity(location, EntityType.WOLF);
		this.dog.setOwner(owner.getPlayer());
		this.dog.setTamed(true);
		this.dog.setAngry(true);
		
		this.dog.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 900, 3));
		
		if (owner.getTeam().equals(Team.BLUE)) {
			this.dog.setCollarColor(DyeColor.BLUE);
		}
		
		this.task = Bukkit.getScheduler().scheduleSyncRepeatingTask(MCWarfare.getPlugin(), new Runnable() {

			@Override
			public void run() {
				ticks++;
				if ((dog.getHealth() <= 0) || (dog.isDead()) || (ticks > (20 * 60))) {
					remove();
					return;
				}
				//dog.setTarget(null);
				setLocation(dog.getLocation());
				if (ticks % 20 == 0) {
					GamePlayer target = null;
					double distance = 999;
					ArrayList<Entity> entities = RaycastHelper.getNearbyEntities(getLocation(), 64);
					for (int i = entities.size() - 1; i >= 0; i--) {
						Entity e = entities.get(i);
						if (e instanceof Player) {
							GamePlayer gplayer = MCWarfare.getPlugin().getArenaManager().getGamePlayer(((Player)e).getName());
							if (gplayer != null && !gplayer.getTeam().equals(owner.getTeam())) {
								double dis = gplayer.getPlayer().getLocation().distance(dog.getLocation());
								if  (dis < distance) {
									distance = dis;
									target = gplayer;
								}
							}
						}
					}
					
					if (target != null) {
						//dog.setTarget(target.getPlayer());
						((EntityWolf)((CraftWolf)dog).getHandle()).b(((CraftLivingEntity)target.getPlayer()).getHandle());
					}
				}
			}
			
		}, 1L, 1L);
	}
	
	@Override
	public void remove() {
		if (dog != null) {
			dog.remove();
		}
		Bukkit.getScheduler().cancelTask(task);
		super.remove();
	}
}
