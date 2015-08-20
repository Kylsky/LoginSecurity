package net.meano.ls.xAuth;

import net.meano.ls.encryption.Encryptor;
import net.meano.ls.util.EncryptionUtil;

public class SaltedWhirlpool implements Encryptor {
	@Override
	public boolean check(String check, String real) {
		check = EncryptionUtil.getSaltedWhirlpool(real, check);
		return check.equals(real);
	}

	@Override
	public String hash(String value) {
		return null;
	}
}
