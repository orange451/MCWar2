package com.orange451.mcwarfare.player;

public class GameRank {
	private String tag;
	private String rank;
	private int level;
	
	public GameRank(String rank, String tag, int level) {
		this.rank = rank;
		this.tag = tag;
		this.level = level;
	}
	
	public String getTag() {
		return this.tag;
	}
	
	public String getRankName() {
		return this.rank;
	}
	
	public int getlevel() {
		return this.level;
	}
}
