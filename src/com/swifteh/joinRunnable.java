package com.swifteh;

import java.sql.ResultSet;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class joinRunnable implements Runnable {
	String name;

	public joinRunnable(String name) {
		this.name = name;
	}

	public void run() {
		try {
			ResultSet result = SlowVote.mySQLDatabase
					.query("SELECT `timestamp` FROM votes WHERE LOWER(User) = '"
							+ name.toLowerCase() + "';");
			// Player found in database
			//System.out.println("1");
			if (result.next()) {
				final double difference = (System.currentTimeMillis() - result
						.getTimestamp(1).getTime()) / 60000.0D;
				//System.out.println("2");
				if (difference / 60.0D > SlowVote.Hour) {
					Bukkit.getScheduler().runTaskLater(SlowVote.instance,
							new Runnable() {

								@Override
								public void run() {
									//System.out.println("3");
									Player p = Bukkit.getPlayerExact(name);
									//System.out.println("4");
									if (p != null){
										//System.out.println("5");
										if (SlowVote.spawn != null) p.teleport(SlowVote.spawn);
										else Bukkit.getLogger().info("Error retrieving slowvote spawn, report to Sgt");
										//System.out.println("6");
										if (SlowVote.DemotePerm != null && SlowVote.DemoteCommand != null && !p.hasPermission(SlowVote.DemotePerm)){
											//System.out.println("7");
											Bukkit.dispatchCommand(Bukkit.getConsoleSender(), SlowVote.DemoteCommand.replace("%NAME%", p.getName()));
										}
										//System.out.println("8");
									}
									//System.out.println("9");
									return;
								}

							}, 20L);
				}
				//System.out.println("10");
				return;
			} else {
				if (SlowVote.mySQLDatabase
						.create("INSERT INTO votes(`User`, `timestamp`, `mend`, `total`) "/*, consecutive) */+"VALUES('"
								+ this.name.toLowerCase()
								+ "', DATE_ADD(NOW(), INTERVAL 2 DAY), 1, 1);"))
					Bukkit.getLogger().info(
							"Created table data for " + this.name);
				else
					Bukkit.getLogger().info(
							"Could not create table data for " + this.name);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
}