package com.orange451.mcwarfare.arena;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.orange451.mcwarfare.MCWarfare;
import com.orange451.mcwarfare.player.GamePlayer;
import com.orange451.mcwarfare.util.PacketUtils;
import com.orange451.pvpgunplus.RaycastHelper;

public class ArenaItemDogTag extends ArenaItem {
	private Arena arena;
	private int ticks = 0;
	private Item item;
	private GamePlayer killed;
	private int task;
	
	public ArenaItemDogTag(Arena arena, Location location, GamePlayer killed) {
		this.arena = arena;
		this.killed = killed;
		this.item = location.getWorld().dropItem(location, new ItemStack(Material.NAME_TAG, 1));
		this.item.setPickupDelay(99999);
		
		//Set up the tick event
		this.task = Bukkit.getScheduler().scheduleSyncRepeatingTask(MCWarfare.getPlugin(), new Runnable() {

			@Override
			public void run() {
				tick();
			}
			
		}, 1L, 1L);
	}
	
	@Override
	public void remove() {
		super.remove();
		Bukkit.getScheduler().cancelTask(this.task);
	}
	
	@Override
	public void tick() {
		ticks++;
		if (item != null) {
			ArrayList<Entity> entities = RaycastHelper.getNearbyEntities(item.getLocation(), 1);
			for (int i = entities.size() - 1; i >= 0; i--) {
				Entity e = entities.get(i);
				if (e instanceof Player) {
					GamePlayer gplayer = MCWarfare.getPlugin().getArenaManager().getGamePlayer(((Player)e).getName());
					if (gplayer != null) {
						
						//Handle giving out points
						if (!killed.getTeam().equals(gplayer.getTeam())) {
							if (gplayer.getTeam().equals(Team.BLUE))
								gplayer.getArena().bluescore++;
							else if (gplayer.getTeam().equals(Team.RED))
								gplayer.getArena().redscore++;
						}
						
						//Send the correct message
						if (!gplayer.getTeam().equals(killed.getTeam()))
							PacketUtils.displayTextBar(ChatColor.GREEN + "Kill confirmed!" + ChatColor.YELLOW + "  +20 xp!   +5 credits!", gplayer.getPlayer(), 60);
						else
							PacketUtils.displayTextBar(ChatColor.RED + "Kill denied!" + ChatColor.YELLOW + "  +20 xp!   +5 credits!", gplayer.getPlayer(), 60);
						
						gplayer.getProfile().addCredits(1);
						gplayer.getProfile().giveXp(20);
						if (item != null)
							item.remove();
						item = null;
						arena.removeArenaItem(this);						
						
						gplayer.getPlayer().playSound(gplayer.getPlayer().getLocation(), Sound.SILVERFISH_KILL, 8, 1);
					}
				}
			}
		}
		
		if (ticks > 30 * 20) {
			if (item != null)
				item.remove();
			item = null;
			arena.removeArenaItem(this);
		}
	}
}
