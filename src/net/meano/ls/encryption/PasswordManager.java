package net.meano.ls.encryption;

import net.meano.ls.LoginSecurity;
import net.meano.ls.data.DataManager;

public class PasswordManager {
	public static boolean checkPass(String uuid, String password) {
		LoginSecurity plugin = LoginSecurity.instance;
		DataManager data = plugin.data;
		String realPass = data.getPassword(uuid);
		int type = data.getEncryptionTypeId(uuid);
		EncryptionType etype = EncryptionType.fromInt(type);
		return etype.checkPass(password, realPass);
	}
}
