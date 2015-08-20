package net.meano.ls.commands;

import java.util.logging.Level;
import net.meano.ls.LoginSecurity;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RegisterCommand implements CommandExecutor {
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		LoginSecurity plugin = LoginSecurity.instance;
		if (!(sender instanceof Player)) {
			sender.sendMessage("只有玩家才能使用此命令！");
			return true;
		}
		Player player = (Player) sender;
		String name = player.getName().toLowerCase();
		if (plugin.data.isRegistered(name)) {
			player.sendMessage(ChatColor.RED + "您已经注册过了，想要修改密码请使用 /changepass 命令");
			return true;
		}
		if (args.length < 1) {
			player.sendMessage(ChatColor.RED + "参数不够");
			player.sendMessage("命令使用方法：" + cmd.getUsage());
			return true;
		}

		String password = plugin.hasher.hash(args[0]);
		plugin.data.register(name, password, plugin.hasher.getTypeId(), player.getAddress().getAddress().toString());
		plugin.authList.remove(name);
		plugin.thread.timeout.remove(name);
		plugin.rehabPlayer(player, name);
		player.sendMessage(ChatColor.GREEN + "您的密码是： " + args[0] + "注册成功，请牢记密码！");
		LoginSecurity.log.log(Level.INFO, "[LoginSecurity] {0} 注册成功！", player.getName());
		return true;
	}
}
