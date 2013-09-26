package com.swifteh;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;

public class VoteListener implements Listener {
	
	SlowVote sv;
	static VoteListener instance;
	
	public VoteListener(SlowVote s){
		sv = s;
		instance = this;
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onVote(VotifierEvent e) {
		System.out.println(e.isAsynchronous());
		if (e.getVote().getServiceName().equals(sv.lastServiceName)) {
			final String user = e.getVote().getUsername();
			Bukkit.getScheduler().runTaskAsynchronously(sv, new Runnable() {
				@Override
				public void run() {
					try {
						String statement = "INSERT INTO votes (`User`, `timestamp`, `mend`, `total`) VALUES ('"
								+ user
								+ "', NOW(), 1, 1) ON DUPLICATE KEY UPDATE `timestamp` = NOW()";
						if (SlowVote.voteMend)
							statement = statement + ", `mend` = `mend` + 1";
						statement = statement + ", `total` = `total` + 1;";
						// boolean i =
						// mySQLDatabase.create("INSERT INTO votes (`User`, `timestamp`, `mend`) VALUES ('"
						// +
						// user +
						// "', NOW(), 1) ON DUPLICATE KEY UPDATE `timestamp` = NOW(), `mend` = `mend` + 1;");
						boolean i = SlowVote.mySQLDatabase.create(statement);
						if (!i)
							Bukkit.getLogger().info(
									"[SV] Could not update " + user);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			});
			Bukkit.broadcastMessage(sv.voteMessage.replace("%NAME%", e.getVote()
					.getUsername()));
			if (SlowVote.PromoteCommand == null || SlowVote.PromotePerm == null)
				return;
			Player p = Bukkit.getPlayer(e.getVote().getUsername());
			if (p != null
					&& p.hasPermission(SlowVote.PromotePerm)) {
				System.out.println("Running command" + SlowVote.PromoteCommand.replace("%NAME%", e.getVote()
								.getUsername()));
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
						SlowVote.PromoteCommand.replace("%NAME%", e.getVote()
								.getUsername()));
			}
		}
	}

	public static void testCommand(CommandSender sender, String arg) {
		Vote v = new Vote();
		v.setServiceName("MinecraftServers.org");
		v.setUsername(sender.getName());
		VotifierEvent e = new VotifierEvent(v);
		instance.onVote(e);
	}
}
