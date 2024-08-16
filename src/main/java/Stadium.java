import java.sql.*;

public class Stadium {
    private int stadiumId;
    private String stadiumName;
    private int capacity;

    // Constructor
    public Stadium(String stadiumName, int capacity) {
        this.stadiumName = stadiumName;
        this.capacity = capacity;
    }

    // Getters and setters
    public int getStadiumId() { return stadiumId; }
    public void setStadiumId(int stadiumId) { this.stadiumId = stadiumId; }
    public String getStadiumName() { return stadiumName; }
    public void setStadiumName(String stadiumName) { this.stadiumName = stadiumName; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    // Database operations
    public void insertStadium(FootballDBConnection dbConn) throws SQLException {
        String sql = "INSERT INTO stadiums (stadium_name, capacity) VALUES (?, ?)";
        try (PreparedStatement pstmt = dbConn.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, stadiumName);
            pstmt.setInt(2, capacity);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        this.stadiumId = rs.getInt(1);
                    }
                }
            }
        }
    }

    public static Stadium getStadiumById(FootballDBConnection dbConn, int stadiumId) throws SQLException {
        String sql = "SELECT * FROM stadiums WHERE stadium_id = ?";
        try (PreparedStatement pstmt = dbConn.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, stadiumId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Stadium stadium = new Stadium(rs.getString("stadium_name"), rs.getInt("capacity"));
                    stadium.setStadiumId(rs.getInt("stadium_id"));
                    return stadium;
                }
            }
        }
        return null;
    }

    // You can add update and delete methods as needed


}