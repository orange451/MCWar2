package com.orange451.mcwarfare.arena;

import com.orange451.mcwarfare.MCWarfare;
import com.orange451.mcwarfare.player.GamePlayer;
import com.orange451.mcwarfare.util.PacketUtils;
import com.orange451.pvpgunplus.RaycastHelper;
import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class ArenaItemDominationFlag extends ArenaItem {
	private Location flagLocation;
	private Team capturedBy;
	private int side = 50;
	private Arena arena;
	private String flagName;
	private ArrayList<ReturnBlock> returnBlocks = new ArrayList<ReturnBlock>();

	public ArenaItemDominationFlag(Arena arena, Location location, String flagName) {
		this.flagLocation = location.clone().add(0.0D, -1.0D, 0.0D);
		this.arena = arena;
		this.flagName = flagName;
	}

	public void start() {
		System.out.println("Setting up domination flag at (" + this.flagLocation.getBlockX() + "," + this.flagLocation.getBlockY() + "," + this.flagLocation.getBlockZ() + ")");
		setup();
		recolor();
	}

	public void setup() {
		this.returnBlocks.clear();
		for (int i = -4; i <= 4; i++) {
			for (int ii = -4; ii <= 4; ii++) {
				Location t = this.flagLocation.clone().add(i, 0.0D, ii);
				if (t.distance(this.flagLocation) <= 3.0D) {
					this.returnBlocks.add(new ReturnBlock(t.getBlock()));
				}
			}
		}
		this.returnBlocks.add(new ReturnBlock(this.flagLocation.clone().add(0.0D, 1.0D, 0.0D).getBlock()));
		this.returnBlocks.add(new ReturnBlock(this.flagLocation.clone().add(0.0D, 2.0D, 0.0D).getBlock()));
		this.returnBlocks.add(new ReturnBlock(this.flagLocation.clone().add(0.0D, 3.0D, 0.0D).getBlock()));
		this.returnBlocks.add(new ReturnBlock(this.flagLocation.clone().add(0.0D, 4.0D, 0.0D).getBlock()));
	}

	public Location getLocation() {
		return this.flagLocation.clone();
	}

	public void remove()
	{
		super.remove();
		for (int i = 0; i < this.returnBlocks.size(); i++) {
			ReturnBlock block = (ReturnBlock)this.returnBlocks.get(i);
			block.revert();
		}
	}

	public void recolor()
	{
		ArrayList<Block> blocks = new ArrayList<Block>();
		for (int i = -4; i <= 4; i++) {
			for (int ii = -4; ii <= 4; ii++) {
				Location t = this.flagLocation.clone().add(i, 0.0D, ii);
				if (t.distance(this.flagLocation) <= 3.0D) {
					blocks.add(t.getBlock());
				}
			}
		}
		blocks.add(this.flagLocation.clone().add(0.0D, 4.0D, 0.0D).getBlock());
		this.flagLocation.clone().add(0.0D, 1.0D, 0.0D).getBlock().setType(Material.FENCE);
		this.flagLocation.clone().add(0.0D, 2.0D, 0.0D).getBlock().setType(Material.FENCE);
		this.flagLocation.clone().add(0.0D, 3.0D, 0.0D).getBlock().setType(Material.FENCE);

		byte color = 0;
		if (this.capturedBy != null) {
			color = (byte)getTeamAsByteColor(this.capturedBy);
		}
		for (int i = 0; i < blocks.size(); i++) {
			((Block)blocks.get(i)).setType(Material.WOOL);
			((Block)blocks.get(i)).setData(color);
		}
	}

	public int getTeamAsByteColor(Team team) {
		if (team.equals(Team.BLUE))
			return 11;
		return 14;
	}

	public void tick()
	{
		boolean enemyOnFlag = false;
		Team teamOnFlag = null;

		ArrayList<GamePlayer> playersOnFlag = new ArrayList<GamePlayer>();

		ArrayList<Entity> entities = RaycastHelper.getNearbyEntities(this.flagLocation, 3.0D);
		for (int i = entities.size() - 1; i >= 0; i--) {
			Entity e = (Entity)entities.get(i);
			if ((e instanceof Player)) {
				GamePlayer gplayer = MCWarfare.getPlugin().getArenaManager().getGamePlayer(((Player)e).getName());
				if (gplayer != null) {
					if (teamOnFlag == null)
						teamOnFlag = gplayer.getTeam();
					if (!gplayer.getTeam().equals(teamOnFlag))
						enemyOnFlag = true;
					playersOnFlag.add(gplayer);
				}
			}
		}

		if ((!enemyOnFlag) && (teamOnFlag != null) && (playersOnFlag.size() > 0)) {
			int oldside = this.side;

			if (teamOnFlag.equals(Team.BLUE))
				this.side += playersOnFlag.size() * 6;
			if (teamOnFlag.equals(Team.RED)) {
				this.side -= playersOnFlag.size() * 6;
			}
			if (this.side >= 100) {
				this.side = 100;
				if ((this.capturedBy == null) || (!this.capturedBy.equals(Team.BLUE))) {
					this.capturedBy = Team.BLUE;
					announceFlagNotification("capture", this.capturedBy);
					this.announceTeamMessage("We have captured " + ChatColor.RED + this.flagName, Team.BLUE);
					this.announceTeamMessage("We have lost " + ChatColor.RED + this.flagName, Team.RED);
				}
				recolor();
			}
			if (this.side <= 0) {
				this.side = 0;
				if ((this.capturedBy == null) || (!this.capturedBy.equals(Team.RED))) {
					this.capturedBy = Team.RED;
					announceFlagNotification("capture", this.capturedBy);
					this.announceTeamMessage("We have captured " + ChatColor.RED + this.flagName, Team.RED);
					this.announceTeamMessage("We have lost " + ChatColor.RED + this.flagName, Team.BLUE);
				}
				recolor();
			}

			if (this.side != oldside) {
				int percent = (int) side;
				if (playersOnFlag.get(0).getTeam().equals(Team.RED))
					percent = (int) (100-side);
				
				for (int i = 0; i < playersOnFlag.size(); i++) {
					((GamePlayer)playersOnFlag.get(i)).getPlayer().sendMessage(ChatColor.BLUE + "Capturing " + ChatColor.AQUA + this.flagName + ChatColor.RED + "  " + percent + "%");
				}

				pop();
			}
		}

		if (this.capturedBy != null) {
			if (this.capturedBy.equals(Team.BLUE)) {
				this.arena.bluescore += 1;
			}
			if (this.capturedBy.equals(Team.RED))
				this.arena.redscore += 1;
		}
	}

	private void pop() {
		Location popLoc = this.flagLocation.clone().add(0.0D, 4.0D, 0.0D);
		popLoc.getWorld().playEffect(popLoc, Effect.STEP_SOUND, Material.WOOL.getId());
	}
	
	private void announceTeamMessage(String string, Team team) {
		ArrayList<GamePlayer> players = this.arena.getPlayers();
		for (int i = 0; i < players.size(); i++) {
			if (players.get(i).getTeam().equals(team)) {
				PacketUtils.displayTextBar(string, players.get(i).getPlayer(), 160);
			}
		}
	}

	private void announceFlagNotification(String string, Team team) {
		Sound s1 = null;
		Sound s2 = null;
		if (string.equals("capture")) {
			if (this.flagName.equals("A")) {
				s1 = Sound.PIG_DEATH;
				s2 = Sound.CREEPER_DEATH;
			}
			if (this.flagName.equals("B")) {
				s1 = Sound.IRONGOLEM_DEATH;
				s2 = Sound.ENDERMAN_DEATH;
			}
			if (this.flagName.equals("C")) {
				s1 = Sound.SKELETON_DEATH;
				s2 = Sound.SPIDER_DEATH;
			}
		}

		if ((s1 != null) && (s2 != null)) {
			ArrayList<GamePlayer> players = this.arena.getPlayers();
			for (int i = 0; i < players.size(); i++) {
				if (((GamePlayer)players.get(i)).getTeam().equals(team)) {
					((GamePlayer)players.get(i)).getPlayer().playSound(((GamePlayer)players.get(i)).getPlayer().getLocation(), s1, 8.0F, 1.0F);
				} else {
					((GamePlayer)players.get(i)).getPlayer().playSound(((GamePlayer)players.get(i)).getPlayer().getLocation(), s2, 8.0F, 1.0F);
				}
			}
		}
	}

	public Team getTeamThatControlsFlag()
	{
		return this.capturedBy;
	}
}