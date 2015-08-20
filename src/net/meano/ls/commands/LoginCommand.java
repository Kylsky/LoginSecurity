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
			sender.sendMessage("ֻ����Ҳ���ʹ�ô����");
			return true;
		}

		Player player = (Player) sender;
		String name = player.getName().toLowerCase();

		if (!plugin.authList.containsKey(name)) {
			player.sendMessage(ChatColor.RED + "���Ѿ���¼�ˣ�");
			return true;
		}
		if (!plugin.data.isRegistered(name)) {
			player.sendMessage(ChatColor.RED + "��δ�������룡");
			return true;
		}
		if (args.length < 1) {
			player.sendMessage(ChatColor.RED + "����������");
			player.sendMessage("����ʹ�÷�����" + cmd.getUsage());
			return true;
		}
		if (PasswordManager.checkPass(name, args[0])) {
			plugin.authList.remove(name);
			plugin.thread.timeout.remove(name);
			plugin.rehabPlayer(player, name);
			player.sendMessage(ChatColor.GREEN + "�ɹ���¼����ӭ�ص�Meano����");
			LoginSecurity.log.log(Level.INFO, "[LoginSecurity] {0} �ɹ���¼", player.getName());
		} else {
			player.sendMessage(ChatColor.RED + "����������������Сд�����⡣");
			LoginSecurity.log.log(Level.WARNING, "[LoginSecurity] {0} �����˴��������", player.getName());
		}

		return true;
	}
}
