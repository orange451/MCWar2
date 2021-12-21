package com.orange451.mcwarfare.arena.killstreaks;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.orange451.mcwarfare.MCWarfare;
import com.orange451.mcwarfare.player.DamageType;
import com.orange451.mcwarfare.player.GamePlayer;

public class C4 extends PlayerPlacedMapItem {
	private double radius = 4.4;
	
	public C4(GamePlayer owner, Location location) {
		super(owner, location);
	}
	
	public void detonate() {
		//getLocation().getWorld().createExplosion(getLocation(), 0.1f);
		if (getLocation().getBlock().getType().equals(Material.LEVER)) {
			getLocation().getWorld().playSound(getLocation(), Sound.EXPLODE, 1, 2);
			com.orange451.pvpgunplus.ParticleEffects.sendParticle(null, 64, "explode", getLocation().clone().add(0, 1, 0), 0.3f, 0.3f, 0.3f, 0.2f, 96);
			List<Entity> entities = this.getLocation().getWorld().getEntities();
			for (int i = entities.size() - 1; i>= 0; i--) {
				if (entities.get(i) instanceof LivingEntity) {
					LivingEntity entity = ((LivingEntity) entities.get(i));
					if (entity.getLocation().distance(this.getLocation()) < radius) {
						if (entity instanceof Player) {
							MCWarfare.getPlugin().damagePlayer((Player)entity, 26, DamageType.EXPLOSION, owner.getPlayer());
						} else {
							entity.damage(26, this.owner.getPlayer());
							entity.setLastDamage(0);
						}
					}
				}
			}
		}
		this.remove();
	}
}
