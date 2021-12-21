package com.orange451.mcwarfare.arena;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.orange451.mcwarfare.MCWarfare;
import com.orange451.mcwarfare.player.DamageType;
import com.orange451.mcwarfare.player.GamePlayer;
import com.orange451.mcwarfare.util.PacketUtils;
import com.orange451.mcwarfare.util.Util;
import com.orange451.pvpgunplus.RaycastHelper;

public class ArenaItemExplosiveArrow extends ArenaItem {
	private Arena arena;
	private int ticks = 0;
	private Arrow item;
	private GamePlayer shooter;
	
	public ArenaItemExplosiveArrow(Arena arena, Location location, GamePlayer shooter) {
		this.arena = arena;
		this.shooter = shooter;
		this.item = shooter.getPlayer().launchProjectile(Arrow.class);
	}
	
	@Override
	public void tick() {
		ticks++;
		if (item != null) {
			Util.playSound(Sound.NOTE_PLING, item.getLocation(), 4, 6);
		}
		
		if (ticks > 5) {
			
			double radius = 2.25;
			getLocation().getWorld().playSound(getLocation(), Sound.EXPLODE, 1, 2);
			com.orange451.pvpgunplus.ParticleEffects.sendParticle(null, 64, "explode", getLocation().clone().add(0, 1, 0), 0.3f, 0.3f, 0.3f, 0.2f, 96);
			List<Entity> entities = this.getLocation().getWorld().getEntities();
			for (int d = 1; d <= 2; d++) {
				for (int i = entities.size() - 1; i>= 0; i--) {
					if (entities.get(i) instanceof LivingEntity) {
						LivingEntity entity = ((LivingEntity) entities.get(i));
						if (entity.getLocation().distance(this.getLocation()) < radius/d) {
							if (entity instanceof Player) {
								MCWarfare.getPlugin().damagePlayer((Player)entity, 20, DamageType.EXPLOSION, shooter.getPlayer());
							} else {
								entity.damage(20, this.shooter.getPlayer());
								entity.setLastDamage(0);
							}
						}
					}
				}
			}
			
			
			if (item != null)
				item.remove();
			arena.removeArenaItem(this);
		}
	}

	private Location getLocation() {
		return item.getLocation();
	}
}
