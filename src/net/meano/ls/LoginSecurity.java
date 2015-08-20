package net.meano.ls;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.meano.ls.commands.AdminCommand;
import net.meano.ls.commands.ChangePassCommand;
import net.meano.ls.commands.LoginCommand;
import net.meano.ls.commands.LogoutCommand;
import net.meano.ls.commands.RegisterCommand;
import net.meano.ls.commands.RmPassCommand;
import net.meano.ls.data.Converter;
import net.meano.ls.data.DataManager;
import net.meano.ls.data.MySQL;
import net.meano.ls.data.SQLite;
import net.meano.ls.encryption.EncryptionType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class LoginSecurity extends JavaPlugin {
	public DataManager data;
	public static LoginSecurity instance;
	public Map<String, Boolean> authList = new HashMap<String, Boolean>();
	public Map<String, Location> loginLocations = new HashMap<String, Location>();
	public List<String> messaging = new ArrayList<String>();
	public boolean required;
	public boolean blindness;
	public boolean sesUse;
	public boolean timeUse;
	public boolean spawntp;
	public int sesDelay;
	public int timeDelay;
	public static final Logger log = Logger.getLogger("Minecraft");
	public ThreadManager thread;
	public String prefix;
	public EncryptionType hasher;
	public Map<String, CommandExecutor> commandMap = new HashMap<String, CommandExecutor>();
	public static int PHP_VERSION;
	public static String encoder;
	private static Logger serverLog;
	private CommandFilter commandFilter = new CommandFilter();

	public void onEnable() {
		FileConfiguration config = getConfig();
		PluginManager pm = getServer().getPluginManager();

		config.addDefault("settings.password-required", Boolean.valueOf(false));
		config.addDefault("settings.encryption", "BCRYPT");
		config.addDefault("settings.encoder", "UTF-8");
		config.addDefault("settings.PHP_VERSION", Integer.valueOf(4));
		config.addDefault("settings.messager-api", Boolean.valueOf(true));
		config.addDefault("settings.blindness", Boolean.valueOf(true));
		config.addDefault("settings.fake-location", Boolean.valueOf(false));
		config.addDefault("settings.session.use", Boolean.valueOf(true));
		config.addDefault("settings.session.timeout (sec)", Integer.valueOf(60));
		config.addDefault("settings.timeout.use", Boolean.valueOf(true));
		config.addDefault("settings.timeout.timeout (sec)", Integer.valueOf(60));
		config.addDefault("settings.table prefix", "ls_");
		config.addDefault("MySQL.use", Boolean.valueOf(false));
		config.addDefault("MySQL.host", "localhost");
		config.addDefault("MySQL.port", Integer.valueOf(3306));
		config.addDefault("MySQL.database", "LoginSecurity");
		config.addDefault("MySQL.username", "root");
		config.addDefault("MySQL.password", "password");
		config.addDefault("MySQL.prefix", "");
		config.options().copyDefaults(true);
		saveConfig();

		instance = (LoginSecurity) pm.getPlugin("LoginSecurity");
		this.prefix = config.getString("settings.table prefix");
		this.data = getDataManager(config, "users.db");
		this.data.openConnection();
		this.thread = new ThreadManager(this);
		this.thread.startMsgTask();
		this.required = config.getBoolean("settings.password-required");
		this.blindness = config.getBoolean("settings.blindness");
		this.spawntp = config.getBoolean("settings.fake-location");
		this.sesUse = config.getBoolean("settings.session.use", true);
		this.sesDelay = config.getInt("settings.session.timeout (sec)", 60);
		this.timeUse = config.getBoolean("settings.timeout.use", true);
		this.timeDelay = config.getInt("settings.timeout.timeout (sec)", 60);
		PHP_VERSION = config.getInt("settings.PHP_VERSION", 4);
		this.hasher = EncryptionType.fromString(config.getString("settings.encryption"));
		String enc = config.getString("settings.encoder");
		if (enc.equalsIgnoreCase("utf-16"))
			encoder = "UTF-16";
		else {
			encoder = "UTF-8";
		}

		if (this.sesUse) {
			this.thread.startSessionTask();
		}
		if (this.timeUse) {
			this.thread.startTimeoutTask();
		}

		this.thread.startMainTask();
		this.thread.startMsgTask();
		checkConverter();
		pm.registerEvents(new LoginListener(this), this);
		registerCommands();
		if (config.contains("options")) {
			config.set("options", null);
			saveConfig();
		}

		// try {
		// Metrics metrics = new Metrics(this);
		// metrics.start();
		// if (!config.getBoolean("settings.update-checker"));
		// }
		// catch (Exception e) {
		// log.info("[LoginSecurity] Failed sending stats to mcstats.org");
		// }

		serverLog = getServer().getLogger();
		this.commandFilter.prevFilter = log.getFilter();
		serverLog.setFilter(this.commandFilter);
		try {
			this.authList = loadAuthList();
		} catch (IOException ex) {
			log.log(Level.SEVERE, "[LoginSecurity] Could not read from auth list!");
		} catch (ClassNotFoundException ex) {
			log.log(Level.SEVERE, "[LoginSecurity] Could not read from auth list (bad data)!");
		}
	}

	public void onDisable() {
		if (this.data != null) {
			this.data.closeConnection();
		}
		if (this.thread != null) {
			this.thread.stopMsgTask();
			this.thread.stopSessionTask();
		}

		serverLog.setFilter(this.commandFilter.prevFilter);
		this.commandFilter.prevFilter = null;
		try {
			saveAuthList(this.authList);
		} catch (IOException ex) {
			log.log(Level.SEVERE, "[LoginSecurity] Could not save to auth list (check permissions?)");
		}
	}

	public void saveAuthList(Map<String, Boolean> map) throws IOException {
		File file = new File(getDataFolder(), "authList");
		FileOutputStream fout = new FileOutputStream(file);
		ObjectOutputStream out = new ObjectOutputStream(fout);
		out.writeObject(map);
		out.close();
		fout.close();
	}

	public Map<String, Boolean> loadAuthList() throws IOException,
			ClassNotFoundException {
		File file = new File(getDataFolder(), "authList");
		FileInputStream fin = new FileInputStream(file);
		ObjectInputStream in = new ObjectInputStream(fin);
		@SuppressWarnings("unchecked")
		Map<String, Boolean> map = (HashMap<String, Boolean>) in.readObject();
		in.close();

		file.delete();
		fin.close();
		return map;
	}

	private DataManager getDataManager(FileConfiguration config, String fileName) {
		if (config.getBoolean("MySQL.use")) {
			return new MySQL(config, getConfig().getString("MySQL.prefix", "") + "users");
		}
		return new SQLite(new File(getDataFolder(), fileName));
	}

	private void checkConverter() {
		PluginManager pm = getServer().getPluginManager();

		File file = new File(getDataFolder(), "data.yml");
		if (file.exists()) {
			Converter conv = new Converter(Converter.FileType.YAML, file);
			conv.convert();
		}
		file = new File(getDataFolder(), "data.db");
		if ((file.exists()) && ((this.data instanceof MySQL))) {
			Converter conv = new Converter(Converter.FileType.SQLite, file);
			conv.convert();
		}
		if ((this.data instanceof MySQL)) {
			MySQL mysql = (MySQL) this.data;
			if (mysql.tableExists("passwords")) {
				Converter conv = new Converter(Converter.FileType.OldToNewMySQL, null);
				conv.convert();
			}
		}
		Plugin xAuth = pm.getPlugin("xAuth");
		if ((xAuth != null) && (xAuth.isEnabled())) {
			Converter conv = new Converter(Converter.FileType.xAuth, null);
			conv.convert();
			log.info("[LoginSecurity] 正在从 xAuth 转换数据到 LoginSecurity。");
		}
	}

	public void registerCommands() {
		this.commandMap.clear();
		this.commandMap.put("login", new LoginCommand());
		this.commandMap.put("register", new RegisterCommand());
		this.commandMap.put("changepass", new ChangePassCommand());
		this.commandMap.put("rmpass", new RmPassCommand());
		this.commandMap.put("logout", new LogoutCommand());
		this.commandMap.put("lac", new AdminCommand());
		for (Entry<String, CommandExecutor> entry : this.commandMap.entrySet()) {
			String cmd = (String) entry.getKey();
			CommandExecutor ex = (CommandExecutor) entry.getValue();
			getCommand(cmd).setExecutor(ex);
		}
	}

	// �?查上�?次登录的IP
	public boolean checkLastIp(Player player) {
		String name = player.getName().toLowerCase();
		if (this.data.isRegistered(name)) {
			String lastIp = this.data.getIp(name);
			String currentIp = player.getAddress().getAddress().toString();
			return lastIp.equalsIgnoreCase(currentIp);
		}
		return false;
	}

	// 玩家登录处理
	public void playerJoinPrompt(Player player, String name) {
		for (Player p : Bukkit.getOnlinePlayers()) {
			if ((player != p) && (player.getName().equalsIgnoreCase(p.getName()))) {
				player.kickPlayer(ChatColor.YELLOW + "游戏ID为 " + p.getName() + " 的玩家已经在服务器登录了！");
				return;
			}
		}
		if ((this.sesUse) && (this.thread.getSession().containsKey(name)) && (checkLastIp(player))) {
			player.sendMessage(ChatColor.GREEN + "继续上一次的登录会话!");
			return;
		}
		if (this.data.isRegistered(name)) {
			this.authList.put(name, Boolean.valueOf(false));
			player.sendMessage(ChatColor.AQUA + "亲爱的玩家你好，欢迎回到Minecraft Meano服！");
			player.sendMessage(ChatColor.RED + "请按按键[T]  然后输入 /login <密码> 来登录服务器。");
			player.sendMessage(ChatColor.RED + "注意：第一次来到Meano服的玩家，看到此消息则你的游戏ID已被注册，请更换ID。");
		} else if (this.required) {
			this.authList.put(name, Boolean.valueOf(true));
			player.sendMessage(ChatColor.AQUA + "新人你好，欢迎来到Minecraft Meano服！");
			player.sendMessage(ChatColor.RED + "请按按键[T]  然后输入 /register <自定密码> 来设定进入的游戏密码�?");
			player.sendMessage(ChatColor.RED + "注意：命令需要英文状态下输入，密码不要太简单，/register <自定密码>之间有空格。");
		} else {
			return;
		}
		debilitatePlayer(player, name, false);
	}

	public void debilitatePlayer(Player player, String name, boolean logout) {
		if (this.timeUse) {
			this.thread.timeout.put(name, Integer.valueOf(this.timeDelay));
		}
		if (this.blindness) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 1728000, 15));
		}
		if ((this.spawntp) && (!logout)) {
			this.loginLocations.put(name, player.getLocation().clone());
			player.teleport(player.getWorld().getSpawnLocation());
		}
	}

	public void rehabPlayer(Player player, String name) {
		player.removePotionEffect(PotionEffectType.BLINDNESS);
		if ((this.spawntp) && (this.loginLocations.containsKey(name))) {
			Location fixedLocation = (Location) this.loginLocations.remove(name);
			fixedLocation.add(0.0D, 0.2D, 0.0D);
			player.teleport(fixedLocation);
		}
		player.setRemainingAir(player.getMaximumAir());
	}
}