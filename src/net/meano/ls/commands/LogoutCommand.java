package net.meano.ls.commands;

import java.util.logging.Level;
import net.meano.ls.LoginSecurity;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LogoutCommand implements CommandExecutor {
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		LoginSecurity plugin = LoginSecurity.instance;
		if (!(sender instanceof Player)) {
			sender.sendMessage("只有玩家才能使用此命令。");
			return true;
		}

		Player player = (Player) sender;
		String name = player.getName().toLowerCase();

		if (plugin.authList.containsKey(name)) {
			player.sendMessage(ChatColor.RED + "您必须要登录才能使用此命令。");
			return true;
		}
		if (!plugin.data.isRegistered(name)) {
			player.sendMessage(ChatColor.RED + "您还没注册！");
		}

		plugin.authList.put(name, Boolean.valueOf(false));
		plugin.debilitatePlayer(player, name, true);

		if (plugin.sesUse) {
			plugin.thread.getSession().remove(name);
		}

		player.sendMessage(ChatColor.GREEN + "登出游戏，再见~");
		LoginSecurity.log.log(Level.INFO, "[LoginSecurity] {0} 登出游戏。", player.getName());
		return true;
	}
}

/*
 * Location: I:\MinecraftServer\Spigot\plugins\LoginSecurity-2.0.7.jar Qualified
 * Name: com.lenis0012.bukkit.ls.commands.LogoutCommand JD-Core Version: 0.6.2
 */