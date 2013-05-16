package com.swifteh;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;

public class CommandRunnable implements Runnable {

	ArrayList<SVCommand> cmds;
	SlowVote sv;
	public static ConcurrentHashMap<String, Long> online = new ConcurrentHashMap<String, Long>();

	public CommandRunnable(SlowVote slowVote, ArrayList<SVCommand> cmds) {
		this.cmds = cmds;
		sv = slowVote;
	}

	@Override
	public void run() {
		for (String p : online.keySet()) {
			long current = online.get(p);
			online.remove(p);
			online.put(p, current + 1);
			for (SVCommand svc : cmds) {
				if (svc.time == current) {
					final ArrayList<String> finalcmds = svc.commands;
					final String hold = p;
					Bukkit.getScheduler().runTask(sv, new Runnable() {
						@Override
						public void run() {
							for (String s : finalcmds)
								Bukkit.getServer().dispatchCommand(
										Bukkit.getConsoleSender(),
										s.replace("%NAME%", hold));
						}
					});
				}
			}
		}
	}

}
