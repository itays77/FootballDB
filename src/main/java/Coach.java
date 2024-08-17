import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Coach {
    private int coachId;
    private String coachName;
    private int experience;
    private int age;

    // Constructor
    public Coach(String coachName, int experience, int age) {
        this.coachName = coachName;
        this.experience = experience;
        this.age = age;
    }

    // Getters and setters
    public int getCoachId() { return coachId; }
    public void setCoachId(int coachId) { this.coachId = coachId; }
    public String getCoachName() { return coachName; }
    public void setCoachName(String coachName) { this.coachName = coachName; }
    public int getExperience() { return experience; }
    public void setExperience(int experience) { this.experience = experience; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    // Database operations
    public void insertCoach(FootballDBConnection dbConn) throws SQLException {
        String sql = "INSERT INTO coaches (coach_name, experience, age) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = dbConn.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, this.coachName);
            pstmt.setInt(2, this.experience);
            pstmt.setInt(3, this.age);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        this.coachId = rs.getInt(1);
                    }
                }
            }
        }
    }

    public static Coach getCoachById(FootballDBConnection dbConn, int coachId) throws SQLException {
        String sql = "SELECT * FROM coaches WHERE coach_id = ?";
        try (PreparedStatement pstmt = dbConn.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, coachId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Coach coach = new Coach(rs.getString("coach_name"), rs.getInt("experience"), rs.getInt("age"));
                    coach.setCoachId(rs.getInt("coach_id"));
                    return coach;
                }
            }
        }
        return null;
    }

    public static List<Coach> getCoachesWithoutTeam(FootballDBConnection dbConn) throws SQLException {
        List<Coach> coachesWithoutTeam = new ArrayList<>();
        String sql = "SELECT c.* FROM coaches c LEFT JOIN teams t ON c.coach_id = t.coach_id WHERE t.team_id IS NULL";
        try (PreparedStatement pstmt = dbConn.getConnection().prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Coach coach = new Coach(rs.getString("coach_name"), rs.getInt("experience"), rs.getInt("age"));
                coach.setCoachId(rs.getInt("coach_id"));
                coachesWithoutTeam.add(coach);
            }
        }
        return coachesWithoutTeam;
    }

    public void updateCoach(FootballDBConnection dbConn) throws SQLException {
        String sql = "UPDATE coaches SET coach_name = ?, experience = ?, age = ? WHERE coach_id = ?";
        try (PreparedStatement pstmt = dbConn.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, this.coachName);
            pstmt.setInt(2, this.experience);
            pstmt.setInt(3, this.age);
            pstmt.setInt(4, this.coachId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating coach failed, no rows affected.");
            }
        }
    }


}