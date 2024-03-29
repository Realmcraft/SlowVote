package com.swifteh;

import java.sql.ResultSet;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class checkRunnable implements Runnable {
	Player[] players;
	double Hour;
	boolean slow;
	double time;

	public checkRunnable(Player[] players, double hour, boolean slow, double d) {
		this.players = players;
		this.Hour = hour;
		this.slow = slow;
		time = d;
	}

	public void run() {
		for (Player p : this.players)
			try {
				ResultSet result = SlowVote.mySQLDatabase
						.query("SELECT `timestamp` FROM votes WHERE LOWER(User) = '"
								+ p.getName().toLowerCase() + "';");
				if (result.first()) {
					final double difference = (System.currentTimeMillis() - result
							.getTimestamp(1).getTime()) / 60000.0D;

					if (difference / 60.0D > this.Hour) {
						/*SlowVote.mySQLDatabase
								.update("UPDATE votes SET consecutive = NOW() WHERE user = '"
										+ p.getName() + "';");*/
						final Player hold = p;
						Bukkit.getScheduler().runTask(SlowVote.instance,
								new Runnable() {
									public void run() {
										double dist = SlowVote.walkDist.get(hold.getName());
										if (dist < 0) SlowVote.walkDist.add(hold.getName(), 0);
										hold.sendMessage(ChatColor.RED
												+ SlowVote.pastDue);
										if (slow
												&& !hold.hasPermission("sv.bypass")) {
//											hold.removePotionEffect(PotionEffectType.SLOW);
											hold.addPotionEffect(new PotionEffect(
													PotionEffectType.SLOW,
													(int) time - 2, 3));
										}
									}
								});
					} else if (difference > this.Hour * 60.0D - 5.0D) {
						final Player hold = p;
						final String copy = SlowVote.warning
								.replace(
										"%TIME%",
										String.valueOf((int) (checkRunnable.this.Hour * 60.0D - difference)));
						Bukkit.getScheduler().runTask(SlowVote.instance,
								new Runnable() {
									public void run() {
										hold.sendMessage(ChatColor.RED + copy);
									}

								});
					}
					else if (SlowVote.walkDist.get(p.getName()) > 0) SlowVote.walkDist.remove(p);

				}

			} catch (Exception e) {
				Bukkit.getLogger().severe("SV Error: Coult not check for " + p.getName());
				Bukkit.getLogger().severe("Error " + e.getLocalizedMessage());
			}
	}
}