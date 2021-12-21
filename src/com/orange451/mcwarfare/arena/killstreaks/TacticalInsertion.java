package com.orange451.mcwarfare.arena.killstreaks;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.orange451.mcwarfare.MCWarfare;
import com.orange451.mcwarfare.arena.Team;
import com.orange451.mcwarfare.player.DamageType;
import com.orange451.mcwarfare.player.GamePlayer;
import com.orange451.pvpgunplus.ParticleEffects;

public class TacticalInsertion extends PlayerPlacedMapItem {
	private int task;
	
	public TacticalInsertion(final GamePlayer owner, Location location) {
		super(owner, location);
		
		this.task = Bukkit.getScheduler().scheduleSyncRepeatingTask(MCWarfare.getPlugin(), new Runnable() {

			@Override
			public void run() {
				if (owner.getTeam().equals(Team.BLUE))
					ParticleEffects.sendParticle(null, 128, "dripWater", getLocation().clone().add(0.6, 1, 0.6), 0.0f, 0.0f, 0.0f, 1f, 2);
				else
					ParticleEffects.sendParticle(null, 128, "dripLava", getLocation().clone().add(0.6, 1, 0.6), 0.0f, 0.0f, 0.0f, 1f, 2);
			}
			
		}, 1L, 1L);
	}
	
	@Override
	public void remove() {
		Bukkit.getScheduler().cancelTask(task);
		super.remove();
	}
}
