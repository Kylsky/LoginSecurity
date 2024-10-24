package net.meano.ls;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class ThreadManager {
	private LoginSecurity plugin;
	private BukkitTask msg;
	private BukkitTask ses;
	private BukkitTask to;
	private BukkitTask main = null;
	public Map<String, Integer> session = new HashMap<String, Integer>();
	public Map<String, Integer> timeout = new HashMap<String, Integer>();
	private long nextRefresh;

	public ThreadManager(LoginSecurity plugin) {
		this.plugin = plugin;
	}

	public synchronized Map<String, Integer> getSession() {
		return this.session;
	}

	public void startMainTask() {
		this.nextRefresh = (System.currentTimeMillis() + 3000000L);
		this.main = this.plugin.getServer().getScheduler().runTaskTimer(this.plugin, new Runnable() {
			public void run() {
				long time = System.currentTimeMillis();
				if (time >= ThreadManager.this.nextRefresh) {
					if ((ThreadManager.this.plugin != null) && (ThreadManager.this.plugin.data != null)) {
						ThreadManager.this.plugin.data.closeConnection();
						ThreadManager.this.plugin.data.openConnection();
					}
					ThreadManager.this.nextRefresh = (System.currentTimeMillis() + 3000000L);
				}
			}
		}, 20L, 20L);
	}

	public void stopMainTask() {
		if (this.main != null) {
			this.main.cancel();
			this.main = null;
		}
	}

	public void startMsgTask() {
		this.msg = this.plugin.getServer().getScheduler().runTaskTimer(this.plugin, new Runnable() {
			public void run() {
				for (Player player : Bukkit.getServer().getOnlinePlayers()) {
					String name = player.getName();
					if (ThreadManager.this.plugin.authList.containsKey(name)) {
						boolean register = ((Boolean) ThreadManager.this.plugin.authList.get(name)).booleanValue();
						if (register) {
							player.sendMessage(ChatColor.RED + "请按按键[T]  然后输入 /register <自定密码> 来设定进入的游戏密码。");
							player.sendMessage(ChatColor.RED + "注意：命令需要英文状态下输入，密码不要太简单，/register和<自定密码>之间有空格。");
						} else {
							player.sendMessage(ChatColor.RED + "请按按键[T]  然后输入 /login <密码> 来登录服务器。");
							player.sendMessage(ChatColor.RED + "注意：如果你第一次来到本服，看到此消息则你的游戏ID已被注册，请更换ID再进入。");
						}
					}
				}
			}
		}, 200L, 200L);
	}

	public void stopMsgTask() {
		if (this.msg != null) {
			this.msg.cancel();
		}
		this.msg = null;
	}

	public void startSessionTask() {
		this.ses = this.plugin.getServer().getScheduler().runTaskTimer(this.plugin, new Runnable() {
			public void run() {
				Iterator<?> it = ThreadManager.this.getSession().keySet().iterator();
				while (it.hasNext()) {
					String user = (String) it.next();
					int current = ((Integer) ThreadManager.this.getSession().get(user)).intValue();
					if (current >= 1) {
						current--;
						ThreadManager.this.getSession().put(user, Integer.valueOf(current));
					} else {
						it.remove();
					}
				}
			}
		}, 20L, 20L);
	}

	public void stopSessionTask() {
		if (this.ses != null) {
			this.ses.cancel();
		}

		this.ses = null;
	}

	public void startTimeoutTask() {
		this.to = this.plugin.getServer().getScheduler().runTaskTimer(this.plugin, new Runnable() {
			public void run() {
				Iterator<String> it = ThreadManager.this.timeout.keySet().iterator();
				while (it.hasNext()) {
					String user = (String) it.next();
					int current = ((Integer) ThreadManager.this.timeout.get(user)).intValue();
					if (current >= 1) {
						current--;
						ThreadManager.this.timeout.put(user, Integer.valueOf(current));
					} else {
						it.remove();
						Player player = Bukkit.getPlayer(user);
						if ((player != null) && (player.isOnline())) {
							if ((ThreadManager.this.plugin.spawntp) && (ThreadManager.this.plugin.loginLocations.containsKey(user))) {
								Location fixedLocation = (Location) ThreadManager.this.plugin.loginLocations.remove(user);
								fixedLocation.add(0.0D, 0.2D, 0.0D);
								player.teleport(fixedLocation);
							}

							player.kickPlayer("登录超时！");
							LoginSecurity.log.log(Level.INFO, "{0} 因登录超时被踢出服务器！", player.getName());
						}
					}
				}
			}
		}, 20L, 20L);
	}

	public void stopTimeoutTask() {
		if (this.to != null) {
			this.to.cancel();
		}
		this.to = null;
	}
}