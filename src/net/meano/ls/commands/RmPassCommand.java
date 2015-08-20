package net.meano.ls.commands;

import net.meano.ls.LoginSecurity;
import net.meano.ls.encryption.PasswordManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RmPassCommand implements CommandExecutor {
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		LoginSecurity plugin = LoginSecurity.instance;
		if (!(sender instanceof Player)) {
			sender.sendMessage("只有玩家才能使用此命令！");
			return true;
		}

		Player player = (Player) sender;
		String name = player.getName().toLowerCase();

		if (!plugin.data.isRegistered(name)) {
			player.sendMessage(ChatColor.RED + "您还没有注册！");
			return true;
		}
		if (args.length < 1) {
			player.sendMessage(ChatColor.RED + "参数不够！");
			player.sendMessage("命令使用方法：" + cmd.getUsage());
			return true;
		}
		if (!PasswordManager.checkPass(name, args[0])) {
			player.sendMessage(ChatColor.RED + "旧密码不正确！");
			return true;
		}
		if (plugin.required) {
			player.sendMessage(ChatColor.RED + "服务器需要密码才能登录！");
			return true;
		}

		plugin.data.removeUser(name);
		player.sendMessage(ChatColor.GREEN + "成功移除了你的密码！");
		return true;
	}
}