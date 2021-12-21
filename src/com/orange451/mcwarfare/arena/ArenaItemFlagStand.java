package com.orange451.mcwarfare.arena;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.orange451.mcwarfare.MCWarfare;
import com.orange451.mcwarfare.arena.killstreaks.MapItem;
import com.orange451.mcwarfare.player.GamePlayer;
import com.orange451.pvpgunplus.RaycastHelper;

public class ArenaItemFlagStand extends ArenaItem {
	private Location flagLocation;
	private Team team;
	private ArenaItemFlag flag;
	private Arena arena;
	
	private ArrayList<ReturnBlock> returnBlocks = new ArrayList<ReturnBlock>();
	
	public ArenaItemFlagStand(Arena arena, Location location, Team team) {
		this.flagLocation = location;
		this.team = team;
		this.arena = arena;
	}

	@Override
	public void start() {
		System.out.println("Setting up " + this.team.toString() + " flag at (" + flagLocation.getBlockX() + "," + flagLocation.getBlockY() + "," + flagLocation.getBlockZ() + ")");
		if (this.flag != null) {
			this.flag.stop();
		}
		this.flag = new ArenaItemFlag(this, team);
		
		returnBlocks.clear();
		returnBlocks.add(new ReturnBlock(this.flagLocation.getBlock()));
		this.flagLocation.getBlock().setType(Material.FENCE);
	}
	
	public Location getLocation() {
		return this.flagLocation.clone();
	}
	
	public ArenaItemFlag getFlag() {
		return this.flag;
	}
	
	@Override
	public void remove() {
		if (this.flag != null)
			flag.stop();
		
		for (int i = 0; i < returnBlocks.size(); i++) {
			ReturnBlock block = returnBlocks.get(i);
			block.revert();
		}
	}
	
	@Override
	public void tick() {
		ArrayList<Entity> entities = RaycastHelper.getNearbyEntities(flagLocation, 2);
		for (int i = entities.size() - 1; i >= 0; i--) {
			Entity e = entities.get(i);
			if (e instanceof Player) {
				GamePlayer gplayer = MCWarfare.getPlugin().getArenaManager().getGamePlayer(((Player)e).getName());
				if (gplayer != null) {
					ArrayList<ArenaItem> items = this.arena.getArenaItems();
					for (int ii = items.size() - 1; ii >= 0; ii--) {
						ArenaItem item = items.get(ii);
						if (item instanceof ArenaItemFlagStand) {
							ArenaItemFlagStand stand = (ArenaItemFlagStand)item;
							if (!stand.getTeam().equals(this.team) && stand.getFlag().getCarrier() != null) {
								if (stand.getFlag().getCarrier().equals(gplayer)) {
									//score the flag
									gplayer.getArena().broadcastMessage(gplayer.getTag() + ChatColor.GOLD + " has captured the " + gplayer.getArena().getTeamColor(stand.getTeam()) + stand.getTeam().toString().toLowerCase() + ChatColor.GOLD + " flag");
									gplayer.getProfile().addCredits(10);
									gplayer.getProfile().giveXp(100);
									stand.getFlag().setCarrier(null);
									stand.getFlag().spawn();
									
									if (gplayer.getTeam().equals(Team.BLUE)) {
										gplayer.getArena().bluescore++;
									}else{
										gplayer.getArena().redscore++;
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	public Team getTeam() {
		return this.team;
	}

	@Override
	public void onDeath(GamePlayer player) {
		if (flag.getCarrier() == null)
			return;
		if (flag.getCarrier().equals(player))  {
			flag.setCarrier(null);
			flag.spawn();
			
			player.getArena().broadcastMessage(player.getTag() + ChatColor.GOLD + " has dropped the " + player.getArena().getTeamColor(team) + this.team.toString().toLowerCase() + ChatColor.GOLD + " flag");
		}
	}
}
