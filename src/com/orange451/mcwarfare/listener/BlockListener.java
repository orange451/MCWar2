package com.orange451.mcwarfare.listener;

import com.orange451.mcwarfare.MCWarfare;
import com.orange451.mcwarfare.arena.killstreaks.C4;
import com.orange451.mcwarfare.arena.killstreaks.MapItem;
import com.orange451.mcwarfare.arena.killstreaks.TacticalInsertion;
import com.orange451.mcwarfare.player.GamePlayer;
import com.orange451.mcwarfare.util.Util;
import com.orange451.pvpgunplus.events.PVPGunPlusBulletCollideEvent;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class BlockListener implements Listener {
	
	@EventHandler(priority=EventPriority.NORMAL)
	public void onBulletHit(PVPGunPlusBulletCollideEvent event) {
		if (event.getBlockHit().getType().equals(org.bukkit.Material.LEVER)) {
			List<MapItem> mapItems = MCWarfare.getPlugin().getMapItems();
			for (int i = mapItems.size() - 1; i >= 0; i--) {
				if (mapItems.get(i).getLocation().equals(event.getBlockHit().getLocation())) {
					if (mapItems.get(i) instanceof C4) {
						((C4)mapItems.get(i)).detonate();
					}
				}
			}
		}
		if (event.getBlockHit().getType().equals(org.bukkit.Material.THIN_GLASS)) {
			MCWarfare.getPlugin().glassThinReplace.add(event.getBlockHit().getLocation());
			event.getBlockHit().setType(org.bukkit.Material.AIR);
		}
	}

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onBlockBreak(BlockBreakEvent event) {
		event.setCancelled(true);
		Player player = event.getPlayer();
		if (player != null) {
			try {
				if (event.getBlock().getType().toString().contains("REDSTONE_TORCH")) {
					GamePlayer gplayer = MCWarfare.getPlugin().getArenaManager().getGamePlayer(player.getName());
					List<MapItem> mapItems = MCWarfare.getPlugin().getMapItems();
					for (int i = mapItems.size() - 1; i >= 0; i--) {
						if (mapItems.get(i).getLocation().equals(event.getBlock().getLocation()) && gplayer != null) {
							if (mapItems.get(i) instanceof TacticalInsertion) {
								TacticalInsertion tacinsert = (TacticalInsertion)(mapItems.get(i));
								if (!tacinsert.getOwner().getTeam().equals(gplayer.getTeam()) || tacinsert.getOwner().equals(gplayer)) {
									tacinsert.remove();
									tacinsert.getOwner().getPlayer().sendMessage(ChatColor.RED + "Your tactical insertion has been removed!");
								}else{
									player.getPlayer().sendMessage(ChatColor.RED + "You cannot remove this tactical insertion!");
								}
							}
						}
					}
					return;
				}
				if ((!player.isOp()) && (Util.isBlockSolid(event.getBlock()))) {
					GamePlayer kp = MCWarfare.getPlugin().getArenaManager().getGamePlayer(player.getName());
					if (kp != null) {
						event.getPlayer().setHealth(0);
						event.getPlayer().damage(9999);
						player.sendMessage(ChatColor.RED + "NO BLOCK GLITCHING");
					}
				}
			} catch (Exception localException) {
				localException.printStackTrace();
			}
		}
	}

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		if (player != null && !player.isOp()) {
			ItemStack itm = event.getItemInHand();
			if ((itm != null) && ((itm.getTypeId() < 256) || (itm.getTypeId() == 397))) {
				if (itm.getTypeId() != Material.LEVER.getId() && itm.getTypeId() != Material.REDSTONE_TORCH_ON.getId()) {
					event.setCancelled(true);
					event.getPlayer().setHealth(0);
					event.getPlayer().damage(9999);
					player.sendMessage(ChatColor.RED + "NO BLOCK JUMPING");
				}
			}
		}
	}
}