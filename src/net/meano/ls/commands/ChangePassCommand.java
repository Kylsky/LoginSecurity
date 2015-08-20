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
			sender.sendMessage("ֻ����Ҳ���ʹ�ã�");
			return true;
		}

		Player player = (Player) sender;
		String name = player.getName().toLowerCase();

		if (!plugin.data.isRegistered(name)) {
			player.sendMessage(ChatColor.RED + "�㻹δ�ڷ�������ע��");
			return true;
		}
		if (args.length < 2) {
			player.sendMessage(ChatColor.RED + "��������ȷ");
			player.sendMessage("����ʹ�÷���: " + cmd.getUsage());
			return true;
		}
		if (!PasswordManager.checkPass(name, args[0])) {
			player.sendMessage(ChatColor.RED + "���벻��ȷ");
			LoginSecurity.log.log(Level.WARNING, "[LoginSecurity] {0} ��������ʧ�ܣ�", player.getName());
			return true;
		}

		String newPass = plugin.hasher.hash(args[1]);
		plugin.data.updatePassword(name, newPass, plugin.hasher.getTypeId());
		player.sendMessage(ChatColor.GREEN + "�ɹ���������Ϊ�� " + args[1]);
		LoginSecurity.log.log(Level.INFO, "[LoginSecurity] {0} �ɹ��޸������룡", player.getName());

		return true;
	}
}
