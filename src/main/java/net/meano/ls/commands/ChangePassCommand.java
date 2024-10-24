package net.meano.ls.commands;

import java.util.logging.Level;
import net.meano.ls.LoginSecurity;
import net.meano.ls.encryption.PasswordManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChangePassCommand implements CommandExecutor {
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		LoginSecurity plugin = LoginSecurity.instance;
		if (!(sender instanceof Player)) {
			sender.sendMessage("只有玩家才能使用！");
			return true;
		}

		Player player = (Player) sender;
		String name = player.getName().toLowerCase();

		if (!plugin.data.isRegistered(name)) {
			player.sendMessage(ChatColor.RED + "你还未在服务器上注册");
			return true;
		}
		if (args.length < 2) {
			player.sendMessage(ChatColor.RED + "参数不正确");
			player.sendMessage("命令使用方法: " + cmd.getUsage());
			return true;
		}
		if (!PasswordManager.checkPass(name, args[0])) {
			player.sendMessage(ChatColor.RED + "密码不正确");
			LoginSecurity.log.log(Level.WARNING, "[LoginSecurity] {0} 更改密码失败！", player.getName());
			return true;
		}

		String newPass = plugin.hasher.hash(args[1]);
		plugin.data.updatePassword(name, newPass, plugin.hasher.getTypeId());
		player.sendMessage(ChatColor.GREEN + "成功更改密码为：" + args[1]);
		LoginSecurity.log.log(Level.INFO, "[LoginSecurity] {0} 成功修改了密码！", player.getName());

		return true;
	}
}
