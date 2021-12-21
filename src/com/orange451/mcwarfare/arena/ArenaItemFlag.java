package com.orange451.mcwarfare.arena;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

import com.orange451.mcwarfare.MCWarfare;
import com.orange451.mcwarfare.player.GamePlayer;
import com.orange451.pvpgunplus.RaycastHelper;

public class ArenaItemFlag {
	private ArenaItemFlagStand stand;
	private GamePlayer carrier;
	private Location returnLoc;
	private int task;
	private int teamColor;
	private Team team;
	private Item item;
	private Item temp;
	
	public ArenaItemFlag(final ArenaItemFlagStand stand, Team team) {
		this.stand = stand;
		this.team = team;
		this.returnLoc = stand.getLocation().add(0.5, 2, 0.5);
		
		this.teamColor = 14;
		if (team.equals(Team.BLUE))
			this.teamColor = 11;
		
		
		//Set up the initial flag
		Bukkit.getScheduler().scheduleSyncDelayedTask(MCWarfare.getPlugin(), new Runnable() {

			@Override
			public void run() {
				spawn();
			}
			
		}, 80L);
		
		
		//Set up the tick event
		this.task = Bukkit.getScheduler().scheduleSyncRepeatingTask(MCWarfare.getPlugin(), new Runnable() {

			@Override
			public void run() {
				tick();
			}
			
		}, 1L, 1L);
	}
	
	public void tick() {
		if (this.carrier != null) {
			if (temp != null) {
				temp.remove();
			}
			this.temp = carrier.getPlayer().getWorld().dropItem(carrier.getPlayer().getEyeLocation().add(0, 0.5, 0), getFlagItemStack());
			this.temp.setVelocity(new Vector(0, 0.5, 0));
		}else{
			if (this.item != null)
				this.item.teleport(returnLoc);
			
			if (this.temp != null) {
				this.temp.remove();
				this.temp = null;
			}
		}
	}
	
	public ItemStack getFlagItemStack() {
		MaterialData data = new MaterialData(Material.WOOL.getId());
		data.setData((byte)this.teamColor);
		ItemStack itm = data.toItemStack(1);
		return itm;
	}
	
	public void spawn() {
		if (item != null)
			item.remove();
		
		ArrayList<Entity> entities = RaycastHelper.getNearbyEntities(this.returnLoc, 1);
		for (int i = entities.size() - 1; i >= 0; i--) {
			Entity e = entities.get(i);
			if (!(e instanceof Player)) {
				e.remove();
			}
		}
		
		this.carrier = null;
		this.item = returnLoc.getWorld().dropItem(returnLoc, getFlagItemStack() );
		this.item.setVelocity(new Vector(0, 0, 0));
	}
	
	public Item getItem() {
		return this.item;
	}
	
	public void onPickUp(GamePlayer player) {
		if (!player.getTeam().equals(this.team)) {
			this.item.remove();
			this.carrier = player;
			player.getArena().broadcastMessage(player.getTag() + ChatColor.GOLD + " has picked up the " + player.getArena().getTeamColor(team) + this.team.toString().toLowerCase() + ChatColor.GOLD + " flag");
		}
	}

	public void stop() {
		if (item != null) {
			this.item.remove();
			this.item = null;
			System.out.println("REMOVED FLAG");
		}
		
		Bukkit.getScheduler().cancelTask(this.task);
	}

	public GamePlayer getCarrier() {
		return this.carrier;
	}
	
	public void setCarrier(GamePlayer player) {
		this.carrier = player;
	}
}
