package com.orange451.mcwarfare.arena;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.orange451.mcwarfare.MCWarfare;
import com.orange451.mcwarfare.player.GamePlayer;
import com.orange451.mcwarfare.util.Util;
import com.orange451.pvpgunplus.RaycastHelper;

public class ArenaItemGoldGun extends ArenaItem {
	private int ticksSinceHeld = 29;
	private GamePlayer carrier;
	private Arena arena;
	private Item item;
	
	public ArenaItemGoldGun(Arena arena) {
		this.arena = arena;
	}
	
	@Override
	public void tick() {
		if (carrier == null) {
			ticksSinceHeld++;
			
			if (item != null) {
				ArrayList<Entity> entities = RaycastHelper.getNearbyEntities(item.getLocation(), 1.5);
				for (int i = entities.size() - 1; i >= 0; i--) {
					Entity e = entities.get(i);
					if (e instanceof Player) {
						GamePlayer gplayer = MCWarfare.getPlugin().getArenaManager().getGamePlayer(((Player)e).getName());
						if (gplayer != null) {
							giveGun(gplayer);
							arena.broadcastMessage(ChatColor.RED + gplayer.getPlayer().getName() + ChatColor.WHITE + " has picked up the "+ ChatColor.GOLD + "Golden Gun!");
							return;
						}
					}
				}
			}
		}else{
			ArrayList<GamePlayer> players = arena.getPlayers();
			for (int i = players.size() - 1; i >= 0; i--) {
				GamePlayer player = players.get(i);
				if (player != null && carrier.getPlayer() != null)
					player.getPlayer().setCompassTarget(carrier.getPlayer().getLocation());
			}
			if (carrier.getPlayer() == null)
				carrier = null;
		}
		
		if (ticksSinceHeld > 30 || (carrier != null && (carrier.getPlayer() == null || !carrier.getPlayer().isOnline()))) {
			GamePlayer gplayer = arena.getPlayers().get(MCWarfare.getPlugin().getRandom().nextInt(arena.getPlayers().size()));
			arena.broadcastMessage("" + ChatColor.BOLD + ChatColor.GOLD + "Golden gun lost! " + ChatColor.RESET + ChatColor.RED + gplayer.getPlayer().getName() + ChatColor.WHITE + " now has the gun!");
			
			giveGun(gplayer);
		}
	}
	
	@Override
	public void onDeath(GamePlayer player) {
		ticksSinceHeld = 0;
		if (this.carrier != null && player.equals(carrier)) {
			arena.broadcastMessage(ChatColor.RED + carrier.getPlayer().getName() + ChatColor.WHITE + " has lost the "+ ChatColor.GOLD + "Golden Gun!");
			KitGun kgun = MCWarfare.getPlugin().getGunManager().getGun("goldengun");
			if (kgun != null) {
				this.item = carrier.getPlayer().getWorld().dropItem(carrier.getPlayer().getLocation(), kgun.getItemStack());
				this.item.setPickupDelay(99999);
			}
			
			this.carrier = null;
		}
	}
	
	public void giveGun(GamePlayer player) {
		if (this.item != null) {
			this.item.remove();
			this.item = null;
		}
		this.carrier = player;
		this.ticksSinceHeld = 0;
		KitGun kgun = MCWarfare.getPlugin().getGunManager().getGun("goldengun");
		if (kgun != null) {
			carrier.getPlayer().getInventory().clear();
			carrier.getPlayer().getInventory().addItem(kgun.getItemStack());
			carrier.getPlayer().getInventory().addItem(Util.namedItemStack(Material.ENDER_PEARL, 64, ChatColor.GRAY + "Ammo", null, null));
		}
	}
}
