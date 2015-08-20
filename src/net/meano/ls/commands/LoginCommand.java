package net.meano.ls.commands;

import java.util.logging.Level;
import net.meano.ls.LoginSecurity;
import net.meano.ls.encryption.PasswordManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LoginCommand implements CommandExecutor {
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		LoginSecurity plugin = LoginSecurity.instance;
		if (!(sender instanceof Player)) {
			sender.sendMessage("只有玩家才能使用此命令！");
			return true;
		}

		Player player = (Player) sender;
		String name = player.getName().toLowerCase();

		if (!plugin.authList.containsKey(name)) {
			player.sendMessage(ChatColor.RED + "你已经登录了！");
			return true;
		}
		if (!plugin.data.isRegistered(name)) {
			player.sendMessage(ChatColor.RED + "还未设置密码！");
			return true;
		}
		if (args.length < 1) {
			player.sendMessage(ChatColor.RED + "参数不够！");
			player.sendMessage("命令使用方法：" + cmd.getUsage());
			return true;
		}
		if (PasswordManager.checkPass(name, args[0])) {
			plugin.authList.remove(name);
			plugin.thread.timeout.remove(name);
			plugin.rehabPlayer(player, name);
			player.sendMessage(ChatColor.GREEN + "成功登录，欢迎回到Meano服。");
			LoginSecurity.log.log(Level.INFO, "[LoginSecurity] {0} 成功登录", player.getName());
		} else {
			player.sendMessage(ChatColor.RED + "密码错误，请检查密码大小写等问题。");
			LoginSecurity.log.log(Level.WARNING, "[LoginSecurity] {0} 键入了错误的密码", player.getName());
		}

		return true;
	}
}
