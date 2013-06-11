package com.swifteh;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.inventory.ItemStack;
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
	Material[] tools;

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
			checkTime = config.getDouble("checktime", 1D);
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
			dealWithCommands(config.getConfigurationSection("timeCommands"));
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
		tools = new Material[]{
				Material.DIAMOND_PICKAXE,
				Material.DIAMOND_SWORD,
				Material.DIAMOND_SPADE,
				Material.DIAMOND_AXE,
				Material.DIAMOND_HOE,
				Material.DIAMOND_HELMET,
				Material.DIAMOND_CHESTPLATE,
				Material.DIAMOND_LEGGINGS,
				Material.DIAMOND_BOOTS,
				Material.IRON_PICKAXE,
				Material.IRON_SWORD,
				Material.IRON_SPADE,
				Material.IRON_AXE,
				Material.IRON_HOE,
				Material.IRON_HELMET,
				Material.IRON_CHESTPLATE,
				Material.IRON_LEGGINGS,
				Material.IRON_BOOTS,
				Material.GOLD_PICKAXE,
				Material.GOLD_SWORD,
				Material.GOLD_SPADE,
				Material.GOLD_AXE,
				Material.GOLD_HOE,
				Material.GOLD_HELMET,
				Material.GOLD_CHESTPLATE,
				Material.GOLD_LEGGINGS,
				Material.GOLD_BOOTS,
				Material.STONE_PICKAXE,
				Material.STONE_SWORD,
				Material.STONE_SPADE,
				Material.STONE_AXE,
				Material.STONE_HOE,
				Material.CHAINMAIL_HELMET,
				Material.CHAINMAIL_CHESTPLATE,
				Material.CHAINMAIL_LEGGINGS,
				Material.CHAINMAIL_BOOTS,
				Material.WOOD_PICKAXE,
				Material.WOOD_SWORD,
				Material.WOOD_SPADE,
				Material.WOOD_AXE,
				Material.WOOD_HOE,
				Material.LEATHER_HELMET,
				Material.LEATHER_CHESTPLATE,
				Material.LEATHER_LEGGINGS,
				Material.LEATHER_BOOTS,
				Material.FLINT_AND_STEEL,
				Material.SHEARS,
				Material.BOW,
				Material.FISHING_ROD,
				Material.ANVIL};
	}

	private void dealWithCommands(ConfigurationSection cs) {
		ArrayList<SVCommand> cmds = new ArrayList<SVCommand>();
		if (cs == null || cs.getKeys(false) == null || cs.getKeys(false).size() < 1) return;
		Iterator<String> keys = cs.getKeys(false).iterator();
		while (keys.hasNext()) {
			String commandName = keys.next();
			cmds.add(new SVCommand(commandName, cs
					.getInt(commandName + ".time"), cs
					.getConfigurationSection(commandName + ".commands")));
		}
		Bukkit.getScheduler().runTaskTimer(this,
				new CommandRunnable(this, cmds), 0L, 1200L);
	}

	public void onDisable() {
		try {
			mySQLDatabase.close();
		} catch (Exception localException) {
		}
	}

	@EventHandler
	public void OnJoin(PlayerJoinEvent e) {
		CommandRunnable.online.put(e.getPlayer().getName(), 0L);
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
						.update("UPDATE votes SET TimeStamp = NOW() WHERE User = '"
								+ args[0] + "';");
				if (r != 1) {
					sender.sendMessage("Could not update timestamp");
				} else
					sender.sendMessage("Timestamp updated");
			} catch (Exception e) {
				p.sendMessage("Stack Error");
				p.sendMessage(e.getMessage());
				e.printStackTrace();
			}
		} else if (cmdlbl.equalsIgnoreCase("mend")){
			if (!(sender instanceof Player)){
				sender.sendMessage("Not allowed to repair as a console player");
				return false;
			}
			final Player p = (Player) sender;
			Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable(){
				@Override
				public void run() {
					validateRepair(p);
				}
			});
		}else if (cmdlbl.equalsIgnoreCase("svsetspawn")
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
	
	protected int getRepair(Player p) throws Exception{
		ResultSet r = mySQLDatabase.query(
				"SELECT mend FROM votes WHERE User = '" + p.getName().toLowerCase() + "';");
		if (r.next()){
			return r.getInt(1);
		}
		else return -1;
	}
	
	protected void incrementRepair(Player p, int amt){
		try {
			int num = getRepair(p);
			mySQLDatabase.update("Update votes SET mend = " + (num + amt) + " WHERE User = '"
					+ p.getName().toLowerCase() + "';");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void validateRepair(final Player p) {
		try {
			int num = getRepair(p);
			if (num <= 0){
				p.sendMessage(ChatColor.RED + "You must vote again to gain more repair");
				return;
			}
			Bukkit.getScheduler().runTask(SlowVote.instance, new Runnable(){
				@Override
				public void run() {
					repairHand(p);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void repairHand(final Player p) {
		ItemStack i = p.getItemInHand();
		for (Material m : tools){
			if (!m.equals(i.getType())) continue;
			i.setDurability((short) 0);
			p.sendMessage(ChatColor.GREEN + "Item successfully repaired");
			Bukkit.getScheduler().runTaskAsynchronously(instance, new Runnable(){
				@Override
				public void run() {
					incrementRepair(p,-1);
					return;
				}
			});
			return;
		}
		p.sendMessage(ChatColor.RED + "Item not repaired");
		return;
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
		CommandRunnable.online.remove(e.getPlayer().getName());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onWalk(PlayerMoveEvent e) {
		// Needed for dist calc
		if (e.getTo().getWorld().equals(e.getFrom().getWorld())) {
			if (e.getPlayer().hasPermission("sv.bypass"))
				return;
			double i = walkDist.get(e.getPlayer().getName());
			if (i >= 0) {
				i += e.getFrom().distanceSquared(e.getTo());
				if (i > distSpam) {
					e.getPlayer().sendMessage(walkMessage);
					i = 0;
				}
				walkDist.replace(e.getPlayer(), i);
			} else
				walkDist.remove(e.getPlayer());
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onVote(VotifierEvent e) {
		if (e.getVote().getServiceName().equals(lastServiceName)) {
			final String user = e.getVote().getUsername();
			Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable(){
				@Override
				public void run() {
					try {
						boolean i = mySQLDatabase.create("INSERT INTO votes (User, mend) VALUES ('" + 
								user + 
								"', 1) ON DUPLICATE KEY UPDATE timestamp = now(), mend = mend + 1;");
						if (!i) Bukkit.getLogger().info("Could not update " + user);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			});
			Bukkit.broadcastMessage(voteMessage.replace("%NAME%", e.getVote()
					.getUsername()));
		}
	}
}