package db;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final int LOCAL_PORT = 5555; // Local port number use to bind SSH tunnel
    private static final String REMOTE_HOST = "pgsql3.mif"; // Remote database server, as per SSH forwarding settings
    private static final int REMOTE_PORT = 5432; // Remote port number of your PostgreSQL, as per SSH forwarding settings
    private static final String SSH_USER = "username"; // SSH login username
    private static final String SSH_PASSWORD = "password"; // SSH login password
    private static final String SSH_HOST = "uosis.mif.vu.lt"; // SSH server
    private static final int SSH_PORT = 22; // SSH port number

    private static Session session;

    public static void setupJDBCTunnel() throws Exception {
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        config.put("PreferredAuthentications", "password");
        config.put("MaxAuthTries", "2");
        JSch.setConfig("LogLevel", "4");

        JSch jsch = new JSch();
        session = jsch.getSession(SSH_USER, SSH_HOST, SSH_PORT);
        session.setPassword(SSH_PASSWORD);
        session.setConfig(config);

        session.setPortForwardingL(LOCAL_PORT, REMOTE_HOST, REMOTE_PORT);
        session.connect();
        System.out.println("Connected to SSH");
    }

    public static Connection connect() throws Exception {
        setupJDBCTunnel();

        String jdbcUrl = "jdbc:postgresql://localhost:" + LOCAL_PORT + "/studentu?sslmode=prefer";
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(jdbcUrl, "username", "password");
            System.out.println("Connected to the PostgreSQL server successfully.");
        } catch (SQLException e) {
            System.out.println("Connection Failed! Check output console");
            e.printStackTrace();
        }
        return conn;
    }

    public static void disconnect(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                System.out.println("Connection closed successfully.");
            } catch (SQLException e) {
                System.out.println("Failed to close connection!");
                e.printStackTrace();
            }
        }
        if (session != null && session.isConnected()) {
            session.disconnect();
            System.out.println("SSH Disconnected");
        }
    }
}


