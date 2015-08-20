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
			sender.sendMessage("ֻ����Ҳ���ʹ�ô����");
			return true;
		}

		Player player = (Player) sender;
		String name = player.getName().toLowerCase();

		if (!plugin.data.isRegistered(name)) {
			player.sendMessage(ChatColor.RED + "����û��ע�ᣡ");
			return true;
		}
		if (args.length < 1) {
			player.sendMessage(ChatColor.RED + "����������");
			player.sendMessage("����ʹ�÷�����" + cmd.getUsage());
			return true;
		}
		if (!PasswordManager.checkPass(name, args[0])) {
			player.sendMessage(ChatColor.RED + "�����벻��ȷ��");
			return true;
		}
		if (plugin.required) {
			player.sendMessage(ChatColor.RED + "��������Ҫ������ܵ�¼��");
			return true;
		}

		plugin.data.removeUser(name);
		player.sendMessage(ChatColor.GREEN + "�ɹ��Ƴ���������룡");
		return true;
	}
}