import java.sql.*;

public class Referee {
    private int refereeId;
    private String refereeName;
    private int age;
    private int experience;

    // Constructor
    public Referee(String refereeName, int age, int experience) {
        this.refereeName = refereeName;
        this.age = age;
        this.experience = experience;
    }

    // Getters and setters
    public int getRefereeId() { return refereeId; }
    public void setRefereeId(int refereeId) { this.refereeId = refereeId; }
    public String getRefereeName() { return refereeName; }
    public void setRefereeName(String refereeName) { this.refereeName = refereeName; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public int getExperience() { return experience; }
    public void setExperience(int experience) { this.experience = experience; }

    // Database operations
    public void insertReferee(FootballDBConnection dbConn) throws SQLException {
        String sql = "INSERT INTO referees (referee_name, age, experience) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = dbConn.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, this.refereeName);
            pstmt.setInt(2, this.age);
            pstmt.setInt(3, this.experience);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        this.refereeId = rs.getInt(1);
                    }
                }
            }
        }
    }

    public static Referee getRefereeById(FootballDBConnection dbConn, int refereeId) throws SQLException {
        String sql = "SELECT * FROM referees WHERE referee_id = ?";
        try (PreparedStatement pstmt = dbConn.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, refereeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Referee referee = new Referee(rs.getString("referee_name"), rs.getInt("age"), rs.getInt("experience"));
                    referee.setRefereeId(rs.getInt("referee_id"));
                    return referee;
                }
            }
        }
        return null;
    }

    // You can add update and delete methods as needed
}
