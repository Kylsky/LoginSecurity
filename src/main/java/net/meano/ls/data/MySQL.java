package net.meano.ls.data;


import java.sql.Connection;

import java.sql.DatabaseMetaData;

import java.sql.DriverManager;

import java.sql.PreparedStatement;

import java.sql.ResultSet;

import java.sql.SQLException;

import java.sql.Statement;

import java.util.logging.Level;

import java.util.logging.Logger;
import net.meano.ls.encryption.EncryptionType;
import org.bukkit.configuration.file.FileConfiguration;


public class MySQL
implements DataManager
{
	private Logger log = Logger.getLogger("Minecraft.LoginSecruity");
	private FileConfiguration config;
	private Connection con;
	private String table;

	
	public MySQL(FileConfiguration config, String table)
	{
		this.config = config;
		this.table = table;
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			} catch (ClassNotFoundException e) {
			this.log.log(Level.SEVERE, "Failed to load MySQL driver", e);
			}
		}

	public String SameIP(String ip) {
		return "xxx";
	}

	public void openConnection()
	{
		String host = this.config.getString("MySQL.host", "localhost");
		String port = String.valueOf(this.config.getInt("MySQL.port", 3306));
		String database = this.config.getString("MySQL.database", "bukkit");
		String user = this.config.getString("MySQL.username", "root");
		String pass = this.config.getString("MySQL.password", "");
		try
		{
			this.con = DriverManager.getConnection("jdbc:mysql://" + host + ':' + port + '/' + database + '?' + "user=" + user + "&password=" + pass);
			
			Statement st = this.con.createStatement();
			st.setQueryTimeout(30);
			st.executeUpdate("CREATE TABLE IF NOT EXISTS " + this.table + " (username VARCHAR(130) NOT NULL UNIQUE,password VARCHAR(300) NOT NULL,encryption INT,ip VARCHAR(130) NOT NULL);");
			} catch (SQLException e) {
			this.log.log(Level.SEVERE, "Faield to load MySQL", e);
			}
		}

	
	public void closeConnection()
	{
		try {
			if (this.con != null)
				this.con.close();
			} catch (SQLException e) {
			this.log.log(Level.SEVERE, "Failed to close SQLite connection", e);
			}
		}

	
	public boolean isRegistered(String user)
	{
		try {
			PreparedStatement ps = this.con.prepareStatement("SELECT * FROM " + this.table + " WHERE username=?;");
			ps.setString(1, user);
			ResultSet result = ps.executeQuery();
			return result.next();
			} catch (SQLException e) {
			this.log.log(Level.SEVERE, "Failed to get data from SQLite db", e);
			}
		return false;
		}

	
	public void register(String user, String password, int encryption, String ip)
	{
		try
		{
			PreparedStatement ps = this.con.prepareStatement("INSERT INTO " + this.table + "(username,password,encryption,ip) VALUES(?,?,?,?);");
			ps.setString(1, user);
			ps.setString(2, password);
			ps.setInt(3, encryption);
			ps.setString(4, ip);
			ps.executeUpdate();
			} catch (SQLException e) {
			this.log.log(Level.SEVERE, "Failed to create user", e);
			}
		}

	
	public void updatePassword(String user, String password, int encryption)
	{
		try {
			PreparedStatement ps = this.con.prepareStatement("UPDATE " + this.table + " SET password=?,encryption=? WHERE username=?;");
			ps.setString(1, password);
			ps.setInt(2, encryption);
			ps.setString(3, user);
			ps.executeUpdate();
			} catch (SQLException e) {
			this.log.log(Level.SEVERE, "Failed to update user password", e);
			}
		}

	
	public void updateIp(String user, String ip)
	{
		try {
			PreparedStatement ps = this.con.prepareStatement("UPDATE " + this.table + " SET ip=? WHERE username=?;");
			ps.setString(1, ip);
			ps.setString(2, user);
			ps.executeUpdate();
			} catch (SQLException e) {
			this.log.log(Level.SEVERE, "Failed to update user ip", e);
			}
		}

	
	public String getPassword(String user)
	{
		try {
			PreparedStatement ps = this.con.prepareStatement("SELECT * FROM " + this.table + " WHERE username=?;");
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

	
	public int getEncryptionTypeId(String user)
	{
		try
		{
			PreparedStatement ps = this.con.prepareStatement("SELECT * FROM " + this.table + " WHERE username=?;");
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

	
	public String getIp(String user)
	{
		try
		{
			PreparedStatement ps = this.con.prepareStatement("SELECT * FROM " + this.table + " WHERE username=?;");
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

	
	public void removeUser(String user)
	{
		try
		{
			PreparedStatement ps = this.con.prepareStatement("DELETE FROM " + this.table + " WHERE username=?;");
			ps.setString(1, user);
			ps.executeUpdate();
			} catch (SQLException e) {
			this.log.log(Level.SEVERE, "Failed to remove user", e);
			}
		}

	
	public Connection getConnection()
	{
		return this.con;
		}

	
	public ResultSet getAllUsers()
	{
		try {
			PreparedStatement ps = this.con.prepareStatement("SELECT * FROM " + this.table + "");
			return ps.executeQuery();
		} catch (SQLException e) {
			}
		return null;
		}

	
	public boolean tableExists(String name)
	{
		try {
			DatabaseMetaData dbm = this.con.getMetaData();
			ResultSet tables = dbm.getTables(null, null, name, null);
			return tables.next();
			} catch (SQLException e) {
			this.log.log(Level.SEVERE, "Failed to check if table exists", e);
			}
		return false;
		}

	
	public void dropTable(String name)
	{
		try {
			Statement st = this.con.createStatement();
			st.setQueryTimeout(30);
			st.executeUpdate("DROP TABLE " + name);
			} catch (SQLException e) {
			this.log.log(Level.SEVERE, "Failed to drop table", e);
			}
		}
	
}

