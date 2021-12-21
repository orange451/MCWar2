package com.orange451.mcwarfare.util;

import java.lang.reflect.Field;
import java.util.HashMap;

import net.minecraft.server.v1_7_R1.DataWatcher;
import net.minecraft.server.v1_7_R1.EntityPlayer;
import net.minecraft.server.v1_7_R1.Packet;
import net.minecraft.server.v1_7_R1.PacketPlayInClientCommand;
import net.minecraft.server.v1_7_R1.PacketPlayOutRespawn;
import net.minecraft.server.v1_7_R1.PacketPlayOutSpawnEntityLiving;
import net.minecraft.server.v1_7_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_7_R1.PacketPlayOutEntityMetadata;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.orange451.mcwarfare.MCWarfare;

public class PacketUtils {
  public static final int ENTITY_ID = 1234;
	
	private static HashMap<String, Boolean> hasHealthBar = new HashMap<String, Boolean>();
	
	public static void sendPacket(Player player, Packet packet){
		if (player == null || packet == null)
			return;
		
		EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
		
		entityPlayer.playerConnection.sendPacket(packet);
	}
	
	//Accessing packets
	public static PacketPlayOutSpawnEntityLiving getMobPacket(String text, Location loc){
		PacketPlayOutSpawnEntityLiving mobPacket = new PacketPlayOutSpawnEntityLiving();

		DataWatcher watcher = getWatcher(text, 200);
		ReflectionHelper.setVariableValue(mobPacket, "l", watcher);
		
		ReflectionHelper.setVariableValue(mobPacket, "a", ENTITY_ID);
		ReflectionHelper.setVariableValue(mobPacket, "b", (byte) EntityType.ENDER_DRAGON.getTypeId());
		ReflectionHelper.setVariableValue(mobPacket, "c", (int) Math.floor(loc.getBlockX() * 32.0D));
		ReflectionHelper.setVariableValue(mobPacket, "d", (byte)Math.floor((loc.getBlockY() + 512) * 32.0D));
		ReflectionHelper.setVariableValue(mobPacket, "e", (byte)Math.floor(loc.getBlockZ() * 32.0D));
		ReflectionHelper.setVariableValue(mobPacket, "f", 0);
		ReflectionHelper.setVariableValue(mobPacket, "g", 0);
		ReflectionHelper.setVariableValue(mobPacket, "h", 0);
		ReflectionHelper.setVariableValue(mobPacket, "i", (byte)0);
		ReflectionHelper.setVariableValue(mobPacket, "j", (byte)0);
		ReflectionHelper.setVariableValue(mobPacket, "k", (byte)0);
			
		return mobPacket;
	}
	
	public static PacketPlayOutEntityDestroy getDestroyEntityPacket(){
		PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(new int[]{ENTITY_ID});
		return packet;
	}
	
	public static PacketPlayOutEntityMetadata getMetadataPacket(DataWatcher watcher){
		PacketPlayOutEntityMetadata metaPacket = new PacketPlayOutEntityMetadata();
		
		ReflectionHelper.setVariableValue(metaPacket, "a", (int) ENTITY_ID);
		
		try{
			Field b = PacketPlayOutEntityMetadata.class.getDeclaredField("b");
			
			b.setAccessible(true);
			b.set(metaPacket, watcher.c());
		} catch(Exception e){
			e.printStackTrace();
		}
		
		return metaPacket;
	}
		
	public static PacketPlayOutRespawn getRespawnPacket(){
		PacketPlayOutRespawn packet = new PacketPlayOutRespawn();

		ReflectionHelper.setVariableValue(packet, "a", 1);
		
		return packet;
	}
	
	public static DataWatcher getWatcher(String text, int health){
		DataWatcher watcher = new DataWatcher(null);
		
		watcher.a(0, (Byte) (byte) 0x20); //Flags, 0x20 = invisible
		watcher.a(6, (Float) (float) health);
		watcher.a(10, (String) text); //Entity name
		watcher.a(11, (Byte) (byte) 1); //Show name, 1 = show, 0 = don't show
		//watcher.a(16, (Integer) (int) health); //Wither health, 300 = full health
		
		return watcher;
	}
	
	//Other methods
	public static void displayTextBar(String text, final Player player, final int ticks){
		if (player != null) {
			if (text.length() > 64)
				text = text.substring(0, 63);
			PacketPlayOutSpawnEntityLiving mobPacket = getMobPacket(text, player.getLocation());
			
			sendPacket(player, mobPacket);
			hasHealthBar.put(player.getName(), true);
			
			new BukkitRunnable(){
				@Override
				public void run(){
					PacketPlayOutEntityDestroy destroyEntityPacket = getDestroyEntityPacket();
					
					sendPacket(player, destroyEntityPacket);
					hasHealthBar.put(player.getName(), false);
				}
			}.runTaskLater(MCWarfare.getPlugin(), (long)ticks);
		}
	}
	
	public static void displayLoadingBar(final String text, final String completeText, final Player player, final int healthAdd, final long delay, final boolean loadUp){
		PacketPlayOutSpawnEntityLiving mobPacket = getMobPacket(text, player.getLocation());
		
		sendPacket(player, mobPacket);
		hasHealthBar.put(player.getName(), true);
		
		new BukkitRunnable(){
			int health = (loadUp ? 0 : 300);
			
			@Override
			public void run(){
				if((loadUp ? health < 300 : health > 0)){
					DataWatcher watcher = getWatcher(text, health);
					PacketPlayOutEntityMetadata metaPacket = getMetadataPacket(watcher);
					
					sendPacket(player, metaPacket);
					
					if(loadUp){
						health += healthAdd;
					} else {
						health -= healthAdd;
					}
				} else {
					DataWatcher watcher = getWatcher(text, (loadUp ? 300 : 0));
					PacketPlayOutEntityMetadata metaPacket = getMetadataPacket(watcher);
					PacketPlayOutEntityDestroy destroyEntityPacket = getDestroyEntityPacket();
					
					sendPacket(player, metaPacket);					
					sendPacket(player, destroyEntityPacket);
					hasHealthBar.put(player.getName(), false);
					
					//Complete text
					PacketPlayOutSpawnEntityLiving mobPacket = getMobPacket(completeText, player.getLocation());
					
					sendPacket(player, mobPacket);
					hasHealthBar.put(player.getName(), true);
					
					DataWatcher watcher2 = getWatcher(completeText, 300);
					PacketPlayOutEntityMetadata metaPacket2 = getMetadataPacket(watcher2);
					
					sendPacket(player, metaPacket2);
					
					new BukkitRunnable(){
						@Override
						public void run(){
							PacketPlayOutEntityDestroy destroyEntityPacket = getDestroyEntityPacket();
							
							sendPacket(player, destroyEntityPacket);
							hasHealthBar.put(player.getName(), false);
						}
					}.runTaskLater(MCWarfare.getPlugin(), 40L);
					
					this.cancel();
				}
			}
		}.runTaskTimer(MCWarfare.getPlugin(), delay, delay);
	}
	
	public static void displayLoadingBar(final String text, final String completeText, final Player player, final int secondsDelay, final boolean loadUp){
		final int healthChangePerSecond = 300 / secondsDelay;
		
		displayLoadingBar(text, completeText, player, healthChangePerSecond, 20L, loadUp);
	}
}