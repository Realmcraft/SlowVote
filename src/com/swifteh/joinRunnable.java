package com.swifteh;

import java.sql.ResultSet;
import org.bukkit.Bukkit;

public class joinRunnable implements Runnable {
	String name;

	public joinRunnable(String name) {
		this.name = name;
	}

	public void run() {
		try {
			ResultSet result = SlowVote.mySQLDatabase
					.query("SELECT timestamp FROM Votes WHERE LOWER(User) = '"
							+ this.name.toLowerCase() + "';");
			if (result.next()){
				final double difference = (System.currentTimeMillis() - result
						.getTimestamp(1).getTime()) / 60000.0D;
				if (difference / 60.0D > SlowVote.Hour){
					Bukkit.getScheduler().runTaskLater(SlowVote.instance, new Runnable(){

						@Override
						public void run() {
							Bukkit.getPlayerExact(name).teleport(SlowVote.spawn);
						}
						
					}, 20L);
				}
				return;
			}
			if (SlowVote.mySQLDatabase
					.create("INSERT INTO Votes(User, timestamp, consecutive) VALUES('"
							+ this.name.toLowerCase()
							+ "', DATE_ADD(NOW(), INTERVAL 2 DAY), NOW());"))
				Bukkit.getLogger().info("Created table data for " + this.name);
			else
				Bukkit.getLogger().info(
						"Could not create table data for " + this.name);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
}