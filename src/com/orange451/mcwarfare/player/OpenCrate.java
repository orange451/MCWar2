package com.orange451.mcwarfare.player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;

import com.orange451.chestAPI.ChestAPI;
import com.orange451.chestAPI.ChestMenu;
import com.orange451.mcwarfare.MCWarfare;
import com.orange451.mcwarfare.arena.ArenaType;
import com.orange451.mcwarfare.arena.KitGun;
import com.orange451.mcwarfare.player.sql.SQLController;
import com.orange451.mcwarfare.util.Util;

public class OpenCrate implements Runnable {
	private GamePlayer player;
	private double speed = 1.4;
	private int currentSlot = 0;
	private int lastSlot = 0;
	private double count;
	private int ticks;
	private ArrayList<GameItem> items;
	
	public int taskId;
	
	public OpenCrate(GamePlayer player) {
		this.player = player;
		this.items = new ArrayList<GameItem>();
		
		fillItems();
		
		this.speed += MCWarfare.getPlugin().getRandom().nextInt(32) / 64d;
	}

	@Override
	public void run() {
		ticks++;
		if (player.getPlayer() != null && !player.getPlayer().isDead()) {
			if (player.getArena().getArenaType().equals(ArenaType.SPAWN)) {
				if (speed > 0) {
					speed -= 1/48d;
					count += speed;
					if (count > 1) {
						count = 0;
						lastSlot = currentSlot;
						currentSlot++;
						
						if (lastSlot != currentSlot) {
							player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.NOTE_PIANO, 1, 1);
							updateScrollingInventory();
						}
					}
				}else{
					executeEnd();
				}
			}else{
				cancel();
				player.getPlayer().sendMessage("Error opening up your crate. Please try again later");
			}
		}
		
		if (ticks > 1000)
			cancel();
	}
	
	private void cancel() {
		MCWarfare.getPlugin().getServer().getScheduler().cancelTask(taskId);
	}

	private void executeEnd() {
		cancel();
		
		player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.FIREWORK_LAUNCH, 2, 1);
		player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.FIREWORK_LARGE_BLAST, 1, 2);
		player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.FIREWORK_LARGE_BLAST2, 1, 2);
		
		GameItem opened = items.get(currentSlot);
		ItemStack opened_item = getItemStackFromGameItem(opened);
		
		ChestMenu menu = new ChestMenu("Opened: " + opened_item.getItemMeta().getDisplayName(), 3);
		
		menu.setItem(4, 0, Util.namedItemStack(Material.WATER_BUCKET, 1, "Arrow", null, null));
		menu.setItem(4, 2, Util.namedItemStack(Material.MILK_BUCKET, 1, "Arrow", null, null));
		menu.setItem(4, 1, opened_item);
		
		ChestAPI.getChestPlayer(player.getPlayer()).openChest(menu);
		
		//Begin saving new stuff
		player.getProfile().setChests(player.getProfile().getChests() - 1);
		
		if (opened instanceof KitGun) {
			player.getProfile().giveGun(((KitGun)opened).name);
		}else if (opened instanceof GameKillstreak) {
			player.getProfile().giveKillstreak(((GameKillstreak)opened).name);
		}else if (opened instanceof GamePerk) {
			player.getProfile().givePerk(((GamePerk)opened).name);
		}
		
		//player.getPlayer().sendMessage("You have opened a case, and got " + opened_item.getItemMeta().getDisplayName());
		player.getArena().broadcastMessage("----------------------------------------------------");
		player.getArena().broadcastMessage("| " + ChatColor.WHITE + player.getPlayer().getName() + ChatColor.GRAY + " opened a crate and got " + opened_item.getItemMeta().getDisplayName());
		player.getArena().broadcastMessage("----------------------------------------------------");
		player.getProfile().save(false, player.getArena().getSQLController());
		//player.getArena().getSQLController().doSave = true;
		
	}

	private void updateScrollingInventory() {
		ChestMenu menu = new ChestMenu("Opening crate...", 3);
		
		menu.setItem(4, 0, Util.namedItemStack(Material.WATER_BUCKET, 1, "Arrow", null, null));
		menu.setItem(4, 2, Util.namedItemStack(Material.MILK_BUCKET, 1, "Arrow", null, null));
		
		for (int i = 0; i < items.size(); i++) {
			int current = (int) (i) + 4 - currentSlot;
			if (current >= 0 && current < 9) {
				menu.setItem(current, 1, getItemStackFromGameItem(items.get(i)));
			}
		}
		
		ChestAPI.getChestPlayer(player.getPlayer()).openChest(menu);
	}
	
	public ItemStack getItemStackFromGameItem(GameItem item) {
		if (item instanceof KitGun) {
			KitGun gun = ((KitGun)item);
			return gun.getItemStack();
		}else if (item instanceof GameKillstreak) {
			GameKillstreak ks = ((GameKillstreak)item);
			return ks.getKillstreakAsItem();
		}else if (item instanceof GamePerk) {
			GamePerk perk = ((GamePerk)item);
			return perk.getPerkAsItem();
		}
		
		return null;
	}
	
	private void fillItems() {
		ArrayList<KitGun> guns = MCWarfare.getPlugin().loadedGuns;
		ArrayList<GameKillstreak> killstreaks = MCWarfare.getPlugin().serverKillstreaks;
		ArrayList<GamePerk> perks = MCWarfare.getPlugin().serverPerks;
		
		int gunScalar = 10;
		int perkScalar = 3;
		int killstreakScalar = 9;
		
		//Remove all guns under level 20
		for (int i = guns.size() - 1; i >= 0; i--) {
			if (guns.get(i).level < 20)
				guns.remove(i);
		}
		
		//Sort guns
		Collections.sort(guns, new Comparator<KitGun>() {
			@Override
			public int compare(KitGun arg0, KitGun arg1) {
				return (int)(arg0.level - arg1.level);
			}
		});
		
		
		//Sort killstreaks
		Collections.sort(killstreaks, new Comparator<GameKillstreak>() {
			@Override
			public int compare(GameKillstreak arg0, GameKillstreak arg1) {
				return (int)(arg0.getTier() - arg1.getTier());
			}
		});
		
		ArrayList<KitGun> newGuns= new ArrayList<KitGun>();
		ArrayList<GameKillstreak> newKs = new ArrayList<GameKillstreak>();
		ArrayList<GamePerk> newPerks = new ArrayList<GamePerk>();
		
		//Expand arrays
		for (int i = 0; i < guns.size(); i++) {
			double percent = 1 - ((double)i / (double)guns.size());
			if (percent < 0.2 || guns.get(i).level > 200 || guns.get(i).level < -1)
				percent = 0.2;
			
			int amount = (int) Math.ceil(percent * gunScalar);
			
			if ((guns.get(i).slot.equals("primary") || guns.get(i).slot.equals("secondary")) && !guns.get(i).name.toLowerCase().contains("gold")) {
				for (int ii = 0; ii < amount; ii++) {
					newGuns.add(guns.get(i));
				}
			}
		}
		
		for (int i = 0; i < killstreaks.size(); i++) {
			double percent = 1 - (killstreaks.size() / (double)i);
			if (percent < 0.01)
				percent = 0.01;
			
			int amount = (int) Math.ceil(percent * killstreakScalar);
			
			for (int ii = 0; ii < amount; ii++) {
				newKs.add(killstreaks.get(i));
			}
		}
		
		for (int i = 0; i < perks.size(); i++) {
			int amount = (int) Math.ceil(perkScalar);
			
			for (int ii = 0; ii < amount; ii++) {
				newPerks.add(perks.get(i));
			}
		}
		
		ArrayList<GameItem> totalList = new ArrayList<GameItem>();
		
		//Fill total list
		for (int i = 0; i < newGuns.size(); i++)
			totalList.add(newGuns.get(i));
		
		for (int i = 0; i < newKs.size(); i++)
			totalList.add(newKs.get(i));
		
		for (int i = 0; i < newPerks.size(); i++)
			totalList.add(newPerks.get(i));
		
		//Completely shuffle it
		Collections.shuffle(totalList, MCWarfare.getPlugin().getRandom());
		
		
		for (int i = 0; i < 200; i++) {
			GameItem item = totalList.get(MCWarfare.getPlugin().getRandom().nextInt(totalList.size()));
			items.add(item);
		}
	}
}