import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Player {
    private int playerId;
    private String playerName;
    private int age;
    private int shirtNumber;
    private int goals;

    // Constructors
    public Player() {}

    public Player(String playerName, int age, int shirtNumber) {
        this.playerName = playerName;
        this.age = age;
        this.shirtNumber = shirtNumber;
        this.goals = 0;
    }

    // Getters and Setters
    public int getPlayerId() { return playerId; }
    public void setPlayerId(int playerId) { this.playerId = playerId; }
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public int getShirtNumber() { return shirtNumber; }
    public void setShirtNumber(int shirtNumber) { this.shirtNumber = shirtNumber; }
    public int getGoals() { return goals; }
    public void setGoals(int goals) { this.goals = goals; }

    // Database operations
    public void insertPlayer(FootballDBConnection dbConn) throws SQLException {
        String sql = "INSERT INTO players (player_name, age, shirt_number, goals) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = dbConn.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, this.playerName);
            pstmt.setInt(2, this.age);
            pstmt.setInt(3, this.shirtNumber);
            pstmt.setInt(4, this.goals);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        this.playerId = generatedKeys.getInt(1);
                    }
                }
            }
        }
    }

    public static Player getPlayerById(FootballDBConnection dbConn, int playerId) throws SQLException {
        String sql = "SELECT * FROM players WHERE player_id = ?";
        try (PreparedStatement pstmt = dbConn.prepareStatement(sql)) {
            pstmt.setInt(1, playerId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Player player = new Player();
                player.setPlayerId(rs.getInt("player_id"));
                player.setPlayerName(rs.getString("player_name"));
                player.setAge(rs.getInt("age"));
                player.setShirtNumber(rs.getInt("shirt_number"));
                player.setGoals(rs.getInt("goals"));
                return player;
            }
        }
        return null;
    }

    public static List<Player> getPlayersWithoutTeam(FootballDBConnection dbConn) throws SQLException {
        List<Player> playersWithoutTeam = new ArrayList<>();
        String sql = "SELECT p.* FROM players p LEFT JOIN team_players tp ON p.player_id = tp.player_id WHERE tp.team_id IS NULL";
        try (PreparedStatement pstmt = dbConn.getConnection().prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Player player = new Player();
                player.setPlayerId(rs.getInt("player_id"));
                player.setPlayerName(rs.getString("player_name"));
                player.setAge(rs.getInt("age"));
                player.setShirtNumber(rs.getInt("shirt_number"));
                player.setGoals(rs.getInt("goals"));
                playersWithoutTeam.add(player);
            }
        }
        return playersWithoutTeam;
    }


    public void updatePlayer(FootballDBConnection dbConn) throws SQLException {
        String sql = "UPDATE players SET player_name = ?, age = ?, shirt_number = ?, goals = ? WHERE player_id = ?";
        try (PreparedStatement pstmt = dbConn.prepareStatement(sql)) {
            pstmt.setString(1, this.playerName);
            pstmt.setInt(2, this.age);
            pstmt.setInt(3, this.shirtNumber);
            pstmt.setInt(4, this.goals);
            pstmt.setInt(5, this.playerId);
            pstmt.executeUpdate();
        }
    }

    public void deletePlayer(FootballDBConnection dbConn) throws SQLException {
        String sql = "DELETE FROM players WHERE player_id = ?";
        try (PreparedStatement pstmt = dbConn.prepareStatement(sql)) {
            pstmt.setInt(1, this.playerId);
            pstmt.executeUpdate();
        }
    }
}
