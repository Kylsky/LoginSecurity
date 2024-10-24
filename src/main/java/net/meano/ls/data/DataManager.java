package net.meano.ls.data;

import java.sql.Connection;
import java.sql.ResultSet;

public abstract interface DataManager {
	public abstract void openConnection();

	public abstract void closeConnection();

	public abstract boolean isRegistered(String paramString);

	public abstract void register(String paramString1, String paramString2, int paramInt, String paramString3);

	public abstract void updatePassword(String paramString1, String paramString2, int paramInt);

	public abstract void updateIp(String paramString1, String paramString2);

	public abstract String getPassword(String paramString);

	public abstract int getEncryptionTypeId(String paramString);

	public abstract String getIp(String paramString);

	public abstract String SameIP(String ip);

	public abstract void removeUser(String paramString);

	public abstract ResultSet getAllUsers();

	public abstract Connection getConnection();
}

/*
 * Location: I:\MinecraftServer\Spigot\plugins\LoginSecurity-2.0.7.jar Qualified
 * Name: com.lenis0012.bukkit.ls.data.DataManager JD-Core Version: 0.6.2
 */