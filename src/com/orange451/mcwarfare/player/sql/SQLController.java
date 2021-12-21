package com.orange451.mcwarfare.player.sql;

import com.orange451.mcwarfare.MCWarfare;
import com.orange451.mcwarfare.player.Clan;
import com.orange451.mcwarfare.player.GameProfile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.Bukkit;

public class SQLController implements Runnable {
	private Connection c;
	private ArrayList<GameProfile> loadProfiles = new ArrayList<GameProfile>();
	private ArrayList<StatMCWar> stats = new ArrayList<StatMCWar>();
	private boolean loadClans;
	private boolean running = true;
	private PreparedStatement preparedPlayerStatement;
	public boolean doSave = false;
	public boolean silent = true;
	
	private long startTime;
	
	public SQLController() {
		this.reconnectSQL();
	}
	
	public void reconnectSQL() {
		try{
			this.c.close();
			this.c = null;
		}catch(Exception e) {
			//
		}
		this.c = MCWarfare.getPlugin().getNewDatabaseConnection();

		try {
			String sql = "UPDATE `MCWarProfile` set user_kills=?" + " , " +
					"user_deaths=? " + " , " +
					"user_level=? " + " , " +
					"user_xp=? " + " , " +
					"user_credits=? " + " , " +
					"owned_guns=? " + " , " +
					"defined_classes=? " + " , " +
					"permissions=? " + " , " +
					"user_perks=? " + " , " +
					"user_killstreaks=? " + " , " +
					"equipped_killstreaks=? " + " , " +
					"max_classes=? " +
					"WHERE `user_name`=?";

			preparedPlayerStatement = c.prepareStatement(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.startTime = System.currentTimeMillis();
	}

	@Override
	public void run() {
		while(running) {
			if (!silent) {
				System.out.println(running + " / " + doSave);
			}
			if (doSave) {
				if (stats.size() > 0) {
					System.out.println("Saving " + stats.size() + " profiles");
					for (int i = stats.size() - 1; i >= 0; i--) {
						if (i < stats.size()) {
							StatMCWar stat = (StatMCWar)stats.get(i);
							saveStats(stat);
							stats.remove(i);
						}
					}
				}
			}
			
			for (int i = loadProfiles.size() - 1; i >= 0; i--) {
				if (i < loadProfiles.size()) {
					loadProfile(loadProfiles.get(i));
				}
				loadProfiles.remove(i);
			}
			
			//Handle loading clans
			if (loadClans) {
				System.out.println("Preparing to load clans...");
				loadClans = false;
				int counter = 0;
				try {
					if (c != null) {
						MCWarfare.getPlugin().loadedClans.clear();
						String query = "SELECT * FROM `MCWarClans`";
						Statement st = c.createStatement();
						ResultSet result = st.executeQuery(query);
						while (result.next()) { //Loop through all the loaded clans
							Clan c = new Clan(result);
							MCWarfare.getPlugin().loadedClans.add(c);
							counter++;
						}
					}
				} catch(Exception e) {
					//
				}
				System.out.println("Loaded " + counter + " clans");
			}
			
			if (System.currentTimeMillis() - startTime > (1000 * 60) * 5) {
				this.reconnectSQL();
			}
		}
	}

	public void loadProfile(GameProfile gameProfile) {
		if (MCWarfare.getPlugin().multiThread)
			gameProfile.loadProfile(c, gameProfile.getPlayername());
		else
			gameProfile.loadProfile(gameProfile.getPlayername());
	}
	
	public void saveStats(final StatMCWar stat) {
		final String playerName = stat.playername;
		boolean firstTimePlaying = stat.isFirstTimePlaying();
		try {
			if (c != null) {
				Statement st = c.createStatement();
				if (!firstTimePlaying) {
					preparedPlayerStatement.setInt(1, stat.user_kills);
					preparedPlayerStatement.setInt(2, stat.user_deaths);
					preparedPlayerStatement.setInt(3, stat.user_level);
					preparedPlayerStatement.setInt(4, stat.user_xp);
					preparedPlayerStatement.setInt(5, stat.user_credits);
					preparedPlayerStatement.setString(6, stat.owned_guns);
					preparedPlayerStatement.setString(7, stat.defined_classes);
					preparedPlayerStatement.setString(8, stat.permissions);
					preparedPlayerStatement.setString(9, stat.user_perks);
					preparedPlayerStatement.setString(10, stat.user_killstreaks);
					preparedPlayerStatement.setString(11, stat.equipped_killstreaks);
					preparedPlayerStatement.setInt(12, stat.max_classes);
					preparedPlayerStatement.setString(13, playerName);
					
					int rowsAffected = preparedPlayerStatement.executeUpdate();

					//String query = "UPDATE `MCWarProfile` SET " + values + "  WHERE `user_name` = '" + playerName + "'";
					//st.executeUpdate(query);
					System.out.println(preparedPlayerStatement.toString());
				} else {
					String query = "INSERT INTO MCWarProfile(`user_name`) VALUES('" + playerName + "')";
					boolean result = st.execute(query);
					System.out.println("Result of creating profile for " + playerName + ": " + result);
					//update player stats a second after after creating them
					Bukkit.getScheduler().scheduleSyncDelayedTask(MCWarfare.getPlugin(), new Runnable() {
						public void run() {
							saveStats(playerName, stat, false);
						}
					}, 21);
					System.out.println(query);
				}
			} else {
				MCWarfare.getPlugin().print("Cannot update profile for " + playerName + " (no database connection)");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void stopSaving() {
		this.running = false;
		this.doSave = false;
	}
	
	public void saveStats(String playerName, StatMCWar mcwarStat, boolean firstTimePlaying) {
		MCWarfare.getPlugin().multiThread = true;
		this.stats.add(mcwarStat);
	}

	/*public void saveProfile(GameProfile gameProfile) {
		this.loadProfiles.add(gameProfile);
	}*/

	public void loadAllClans() {
		this.loadClans = true;
	}
}