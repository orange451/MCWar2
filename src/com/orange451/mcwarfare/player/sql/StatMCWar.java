package com.orange451.mcwarfare.player.sql;

public class StatMCWar {
	private boolean firstTimePlaying;
	
	public String playername;
	public int user_kills;
	public int user_deaths;
	public int user_level;
	public int user_xp;
	public int user_credits;
	public String owned_guns;
	public String defined_classes;
	public String permissions;
	public String user_perks;
	public String user_killstreaks;
	public String equipped_killstreaks;
	public int max_classes;
	

	public StatMCWar(String playername, boolean firstTimePlaying) {
		this.playername = playername;
		this.firstTimePlaying = firstTimePlaying;
	}

	public boolean isFirstTimePlaying() {
		return this.firstTimePlaying;
	}
}