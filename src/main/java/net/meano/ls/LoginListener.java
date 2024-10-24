package net.meano.ls;

import net.meano.ls.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class LoginListener implements Listener {
	private LoginSecurity plugin;

	public LoginListener(LoginSecurity i) {
		this.plugin = i;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		String name = player.getName().toLowerCase();
		String ip = player.getAddress().getAddress().toString();
		String Sameip = plugin.data.SameIP(ip);
		if ((!Sameip.replaceAll("[\\[\\]]", "").equals(name)) || Sameip.length() == 2) {
			// player.sendMessage(ChatColor.GOLD+"你有可能是"+Sameip.replace(name+", ",
			// "").trim()+"的小号，此消息会在服务器记录，若您和小伙伴同用一个路由器，可忽略此消息。");
			plugin.getLogger().info(name + "可能是" + Sameip.replace(name + ", ", "") + "的小号");
		}
		if (!player.getName().equals(StringUtil.cleanString(player.getName()))) {
			player.kickPlayer("游戏ID>>" + name + "<<中包含不可用的字符串，可用的游戏ID由3-15个中文字符或英文字符或数字组成，请更换自己的游戏ID!");
			return;
		}
		this.plugin.playerJoinPrompt(player, name);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
		String name = event.getName().toLowerCase();
		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
			String pname = player.getName().toLowerCase();
			if (!this.plugin.authList.containsKey(pname)) {
				if (pname.equalsIgnoreCase(name)) {
					event.setLoginResult(Result.KICK_OTHER);
					event.setKickMessage("名为" + name + "的玩家已经处于在线状态。");
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		String name = player.getName().toLowerCase();
		String ip = player.getAddress().getAddress().toString();
		if (plugin.data.isRegistered(name)) {
			plugin.data.updateIp(name, ip);
			if (plugin.sesUse && !plugin.authList.containsKey(name)) {
				plugin.thread.getSession().put(name, plugin.sesDelay);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		String name = player.getName().toLowerCase();
		Location from = event.getFrom();
		Location to = event.getTo().clone();

		if (this.plugin.authList.containsKey(name)) {
			to.setX(from.getX());
			to.setZ(from.getZ());
			event.setTo(to);
		}
	}

	// 不能放置方块
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		String name = player.getName().toLowerCase();
		if (this.plugin.authList.containsKey(name))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		String name = player.getName().toLowerCase();
		if (this.plugin.authList.containsKey(name))
			event.setCancelled(true);
	}

	// 不能丢弃物品
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		Player player = event.getPlayer();
		String name = player.getName().toLowerCase();
		if (this.plugin.authList.containsKey(name))
			event.setCancelled(true);
	}

	// 登录前无法拾取物品
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		Player player = event.getPlayer();
		String name = player.getName().toLowerCase();
		if (this.plugin.authList.containsKey(name))
			event.setCancelled(true);
	}

	// 无法讲话
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent chat) {
		Player player = chat.getPlayer();
		String pname = player.getName().toLowerCase();
		if (this.plugin.authList.containsKey(pname)) {
			player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + ">>����δ��¼��ע�ᣬ��ʹ������/register ���� ��ע����Ϸ��ʹ��/login ���� ����¼��Ϸ��");
			chat.setMessage("");
			chat.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void OnHealthRegain(EntityRegainHealthEvent event) {
		Entity entity = event.getEntity();
		if (!(entity instanceof Player)) {
			return;
		}
		Player player = (Player) entity;
		String pname = player.getName().toLowerCase();

		if (this.plugin.authList.containsKey(pname))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void OnFoodLevelChange(FoodLevelChangeEvent event) {
		Entity entity = event.getEntity();
		if (!(entity instanceof Player)) {
			return;
		}
		Player player = (Player) entity;
		String pname = player.getName().toLowerCase();

		if (this.plugin.authList.containsKey(pname))
			event.setCancelled(true);
	}

	// 登录前无法开箱
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event) {
		Entity entity = event.getWhoClicked();
		if (!(entity instanceof Player)) {
			return;
		}
		Player player = (Player) entity;
		String pname = player.getName().toLowerCase();
		if (this.plugin.authList.containsKey(pname))
			event.setCancelled(true);
	}

	// 登录前受伤害保护
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onEntityDamage(EntityDamageEvent event) {
		Entity entity = event.getEntity();
		if ((entity instanceof Player)) {
			Player player = (Player) entity;
			String name = player.getName().toLowerCase();
			if (this.plugin.authList.containsKey(name))
				event.setCancelled(true);
		}
	}

	// 登录前毒药保护
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPotionSplash(PotionSplashEvent event) {
		for (LivingEntity entity : event.getAffectedEntities())
			if ((entity instanceof Player)) {
				Player player = (Player) entity;
				String name = player.getName().toLowerCase();
				if (this.plugin.authList.containsKey(name))
					event.setCancelled(true);
			}
	}

	// 登录前PvP保护
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		Entity defender = event.getEntity();
		Entity damager = event.getDamager();
		if ((defender instanceof Player)) {
			Player p1 = (Player) defender;
			String n1 = p1.getName().toLowerCase();
			if (this.plugin.authList.containsKey(n1)) {
				event.setCancelled(true);
				return;
			}
			if ((damager instanceof Player)) {
				Player p2 = (Player) damager;
				String n2 = p2.getName().toLowerCase();
				if (this.plugin.authList.containsKey(n2))
					event.setCancelled(true);
			}
		}
	}

	// 登录前保护玩家不受伤害
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onEntityTarget(EntityTargetEvent event) {
		Entity entity = event.getTarget();
		if ((entity instanceof Player)) {
			Player player = (Player) entity;
			String name = player.getName().toLowerCase();
			if (this.plugin.authList.containsKey(name))
				event.setCancelled(true);
		}
	}

	// 登录前无法进行交互
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event) {
		String name = event.getPlayer().getName().toLowerCase();
		if (this.plugin.authList.containsKey(name))
			event.setCancelled(true);
	}

	// 登录信息过滤
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();
		String name = player.getName().toLowerCase();
		if ((this.plugin.authList.containsKey(name)) && (!event.getMessage().toLowerCase().startsWith("/login")) && (!event.getMessage().toLowerCase().startsWith("/register"))) {
			player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + ">>����δ��¼��ע�ᣬ��ʹ������/register ���� ��ע����Ϸ��ʹ��/login ���� ����¼��Ϸ��");
			event.setCancelled(true);
		}
	}
}
