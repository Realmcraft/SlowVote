package com.swifteh;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

import com.vexsoftware.votifier.model.VotifierEvent;

public class SlowVote extends JavaPlugin implements Listener, CommandExecutor {
	private String host = "localhost";
	private String port = "3306";
	private String username = "slowvote";
	private String password = "password";
	private String database = "Voting";
	private double checkTime = 5.0D;
	private double distSpam = 20D;
	static String warning;
	static String pastDue;
	private String walkMessage;
	private String lastServiceName;
	private String voteMessage;
	boolean slow = false;
	static MySQLDatabase mySQLDatabase;
	protected static Plugin instance;
	static final float defaultRunSpeed = 0.2F;
	public static double Hour = 24.5D;
	static public Location spawn;
	YamlConfiguration config;
	public static Map walkDist = new Map();

	public void onEnable() {
		instance = this;
		getServer().getPluginManager().registerEvents(this, this);
		try {
			File f = new File(getDataFolder().getAbsolutePath());
			if (!f.exists())
				f.mkdirs();
			f = new File(getDataFolder().getAbsolutePath() + "/config.yml");
			config = new YamlConfiguration();
			if (!f.exists()) {
				f.createNewFile();
				config.load(f);
				config.set(
						"warning",
						"RealmCraft needs your vote to stay alive! If you don't "
								+ "vote in the next %TIME% minutes, you will be slowed");
				config.set("past",
						"You have been slowed because you ahve not voted!");
				config.set("host", "localhost");
				config.set("port", "3306");
				config.set("username", "");
				config.set("password", "");
				config.set("database", "Voting");
				config.set("debug", false);
				config.set("spawn", null);
				config.set("distSpam", 20D);
				config.set("walkMes", "Because");
				config.save(f);
			}
			config.load(f);
			warning = config.getString("warning", "ERR");
			pastDue = config.getString("past", "ERR");
			host = config.getString("host", "localhost");
			port = config.getString("port", "3306");
			username = config.getString("username", "root");
			password = config.getString("password", "");
			database = config.getString("database", "Voting");
			// debug = config.getBoolean("debug", false);
			spawn = str2Loc(config.getString("spawn", null));
			slow = config.getBoolean("creeper", false);
			distSpam = config.getDouble("distSpam", 20D);
			walkMessage = ChatColor.translateAlternateColorCodes('&',
					config.getString("walkMes", "Because I told you to"));
			lastServiceName = config.getString("lastservicename",
					"MinecraftServers.org");
			voteMessage = ChatColor
					.translateAlternateColorCodes(
							'&',
							config.getString("votemessage",
									"%NAME% has voted! Thanks for supporting RealmCraft!"));
			mySQLDatabase = new MySQLDatabase(this.host, this.port,
					this.username, this.password, this.database);
			mySQLDatabase.open();
			getServer().getScheduler().runTaskTimer(this, new Runnable() {
				public void run() {
					Player[] players = Bukkit.getOnlinePlayers();
					SlowVote.this
							.getServer()
							.getScheduler()
							.runTaskAsynchronously(
									SlowVote.instance,
									new checkRunnable(players, SlowVote.Hour,
											slow, checkTime * 20 * 60));
				}
			}, 1L, (long) (this.checkTime * 20.0D * 60.0D));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onDisable() {
		try {
			mySQLDatabase.close();
		} catch (Exception localException) {
		}
	}

	@EventHandler
	public void OnJoin(PlayerJoinEvent e) {
		if (mySQLDatabase == null)
			return;
		getServer().getScheduler().runTaskAsynchronously(this,
				new joinRunnable(e.getPlayer().getName()));
	}

	@EventHandler
	public void onSprint(PlayerToggleSprintEvent event) {
		boolean sprinting = event.isSprinting();
		if (!sprinting)
			return;
		if (event.getPlayer().hasPotionEffect(PotionEffectType.SLOW))
			event.setCancelled(true);
	}

	public boolean onCommand(CommandSender sender, Command cmd, String cmdlbl,
			String[] args) {
		if ((cmdlbl.equalsIgnoreCase("svreset"))
				&& ((sender.hasPermission("sv.mod")) || (sender.getName()
						.equalsIgnoreCase("sergeantmajorme")))) {
			if (args.length != 1)
				return true;
			Player p = Bukkit.getPlayer(args[0]);
			if (p != null)
				p.setWalkSpeed(0.2F);
			try {
				int r = mySQLDatabase
						.update("UPDATE SET timestamp = NOW() WHERE User = '"
								+ args[0] + "';");
				if (r != 1) {
					sender.sendMessage("Could not update timestamp");
				} else
					sender.sendMessage("Timestamp updated");
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (cmdlbl.equalsIgnoreCase("svsetspawn")
				&& sender.hasPermission("sv.admin")) {
			if (!(sender instanceof Player))
				return true;
			spawn = ((Player) sender).getLocation();
			config.set("spawn", loc2str(spawn));
			try {
				config.save(getDataFolder().getAbsolutePath() + "/config.yml");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	public Location str2Loc(String s) {
		try {
			String[] s1 = s.split(" ");
			Location loc = new Location(getServer().getWorld(s1[0]),
					str2d(s1[1]), str2d(s1[2]), str2d(s1[3]),
					(float) str2d(s1[4]), (float) str2d(s1[5]));
			return loc;
		} catch (Exception e) {
			return null;
		}
	}

	public double str2d(String s) {
		return Double.parseDouble(s);
	}

	public static String loc2str(Location loc) {
		String output = loc.getWorld().getName();
		output = output.concat(" " + loc.getX() + " " + loc.getY() + " "
				+ loc.getZ() + " " + loc.getYaw() + " " + loc.getPitch());
		return output;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onLeave(PlayerQuitEvent e) {
		walkDist.remove(e.getPlayer());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onWalk(PlayerMoveEvent e) {
		// Needed for dist calc
		if (e.getTo().getWorld().equals(e.getFrom().getWorld())) {
			double i = walkDist.get(e.getPlayer());
			if (i >= 0) {
				i += e.getFrom().distanceSquared(e.getTo());
				if (i > distSpam) {
					e.getPlayer().sendMessage(walkMessage);
					i = 0;
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onVote(VotifierEvent e) {
		if (e.getVote().getServiceName().equals(lastServiceName)) {
			Bukkit.broadcastMessage(voteMessage.replace("%NAME%", e.getVote()
					.getUsername()));
		}
	}
}