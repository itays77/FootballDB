import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class FootballDBConnection {
    private static final String JDBC_DRIVER = "org.postgresql.Driver";
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/footballdb";
    private static final String USER = "postgres";
    private static final String PASS = "password";

    private Connection conn;

    public void connect() throws SQLException, ClassNotFoundException {
        Class.forName(JDBC_DRIVER);
        conn = DriverManager.getConnection(DB_URL, USER, PASS);
    }

    public void closeConnection() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }

    public ResultSet executeQuery(String sqlQuery) throws SQLException {
        if (conn == null || conn.isClosed()) {
            throw new SQLException("Connection is not properly initialized.");
        }
        Statement stmt = conn.createStatement();
        return stmt.executeQuery(sqlQuery);
    }

    public int executeUpdate(String sqlQuery) throws SQLException {
        if (conn == null || conn.isClosed()) {
            throw new SQLException("Connection is not properly initialized.");
        }
        Statement stmt = conn.createStatement();
        return stmt.executeUpdate(sqlQuery);
    }

    public PreparedStatement prepareStatement(String sqlQuery) throws SQLException {
        if (conn == null || conn.isClosed()) {
            throw new SQLException("Connection is not properly initialized.");
        }
        return conn.prepareStatement(sqlQuery);
    }

    public Connection getConnection() {
        return this.conn;
    }
}