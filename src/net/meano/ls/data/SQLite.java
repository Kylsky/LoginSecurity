package net.meano.ls.data;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.meano.ls.encryption.EncryptionType;

public class SQLite implements DataManager {
	private final Logger log = Logger.getLogger("Minecraft.LoginSecurity");
	private File file;
	private Connection con;

	public SQLite(File file) {
		this.file = file;
		File dir = file.getParentFile();
		dir.mkdir();
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				log.log(Level.SEVERE, "Failed to create file", e);
			}
		}
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			this.log.log(Level.SEVERE, "Failed to load SQLite driver", e);
		}
	}

	public void openConnection() {
		try {
			this.con = DriverManager.getConnection("jdbc:sqlite:" + this.file.getPath());
			Statement st = this.con.createStatement();
			st.setQueryTimeout(30);
			st.executeUpdate("CREATE TABLE IF NOT EXISTS users (username VARCHAR(130) NOT NULL UNIQUE,password VARCHAR(300) NOT NULL,encryption INT,ip VARCHAR(130) NOT NULL);");
		} catch (SQLException e) {
			this.log.log(Level.SEVERE, "Failed to open SQLite connection", e);
		}
	}

	public void closeConnection() {
		try {
			if (this.con != null)
				this.con.close();
		} catch (SQLException e) {
			this.log.log(Level.SEVERE, "Failed to close SQLite connection", e);
		}
	}

	public boolean isRegistered(String user) {
		try {
			PreparedStatement ps = this.con.prepareStatement("SELECT * FROM users WHERE username=?;");
			ps.setString(1, user);
			ResultSet result = ps.executeQuery();
			return result.next();
		} catch (SQLException e) {
			this.log.log(Level.SEVERE, "Failed to get data from SQLite db", e);
		}
		return false;
	}

	public void register(String user, String password, int encryption, String ip) {
		try {
			PreparedStatement ps = this.con.prepareStatement("INSERT INTO users(username,password,encryption,ip) VALUES(?,?,?,?);");
			ps.setString(1, user);
			ps.setString(2, password);
			ps.setInt(3, encryption);
			ps.setString(4, ip);
			ps.executeUpdate();
		} catch (SQLException e) {
			this.log.log(Level.SEVERE, "Failed to create user", e);
		}
	}

	public void updatePassword(String user, String password, int encryption) {
		try {
			PreparedStatement ps = this.con.prepareStatement("UPDATE users SET password=?,encryption=? WHERE username=?;");
			ps.setString(1, password);
			ps.setInt(2, encryption);
			ps.setString(3, user);
			ps.executeUpdate();
		} catch (SQLException e) {
			this.log.log(Level.SEVERE, "Failed to update user password", e);
		}
	}

	public String SameIP(String ip) {
		List<String> Players = new ArrayList<String>();
		String SameIPPlayers;
		try {
			PreparedStatement ps = this.con.prepareStatement("SELECT * FROM users WHERE ip=?;");
			ps.setString(1, ip);
			ResultSet result = ps.executeQuery();
			while (result.next()) {
				Players.add(result.getString("username"));
			}
		} catch (SQLException e) {
			this.log.log(Level.SEVERE, "查找相同IP玩家失败！", e);
		}
		SameIPPlayers = Players.toString();
		Players.clear();
		return SameIPPlayers;
	}

	public void updateIp(String user, String ip) {
		try {
			PreparedStatement ps = this.con.prepareStatement("UPDATE users SET ip=? WHERE username=?;");
			ps.setString(1, ip);
			ps.setString(2, user);
			ps.executeUpdate();
		} catch (SQLException e) {
			this.log.log(Level.SEVERE, "Failed to update user ip", e);
		}
	}

	public String getPassword(String user) {
		try {
			PreparedStatement ps = this.con.prepareStatement("SELECT * FROM users WHERE username=?;");
			ps.setString(1, user);
			ResultSet result = ps.executeQuery();
			if (result.next()) {
				return result.getString("password");
			}
			return null;
		} catch (SQLException e) {
			this.log.log(Level.SEVERE, "Failed to get user password", e);
		}
		return null;
	}

	public int getEncryptionTypeId(String user) {
		try {
			PreparedStatement ps = this.con.prepareStatement("SELECT * FROM users WHERE username=?;");
			ps.setString(1, user);
			ResultSet result = ps.executeQuery();
			if (result.next()) {
				return result.getInt("encryption");
			}
			return EncryptionType.MD5.getTypeId();
		} catch (SQLException e) {
			this.log.log(Level.SEVERE, "Failed to get user encryption type", e);
		}
		return EncryptionType.MD5.getTypeId();
	}

	public String getIp(String user) {
		try {
			PreparedStatement ps = this.con.prepareStatement("SELECT * FROM users WHERE username=?;");
			ps.setString(1, user);
			ResultSet result = ps.executeQuery();
			if (result.next()) {
				return result.getString("ip");
			}
			return null;
		} catch (SQLException e) {
			this.log.log(Level.SEVERE, "Failed to get user ip", e);
		}
		return null;
	}

	public void removeUser(String user) {
		try {
			PreparedStatement ps = this.con.prepareStatement("DELETE FROM users WHERE username=?;");
			ps.setString(1, user);
			ps.executeUpdate();
		} catch (SQLException e) {
			this.log.log(Level.SEVERE, "Failed to remove user", e);
		}
	}

	public Connection getConnection() {
		return this.con;
	}

	public ResultSet getAllUsers() {
		try {
			PreparedStatement ps = this.con.prepareStatement("SELECT * FROM users");
			return ps.executeQuery();
		} catch (SQLException e) {

		}
		return null;
	}
}

/*
 * Location: I:\MinecraftServer\Spigot\plugins\LoginSecurity-2.0.7.jar Qualified
 * Name: com.lenis0012.bukkit.ls.data.SQLite JD-Core Version: 0.6.2
 */