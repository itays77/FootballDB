import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Team {
    private int teamId;
    private String teamName;
    private Coach coach;
    private List<Player> players;
    private int points;
    private int numOfWins;
    private int numOfDraws;
    private int numOfLosses;


    // Constructor
    public Team(String teamName, Coach coach) {
        this.teamName = teamName;
        this.coach = coach;
        this.players = new ArrayList<>();
        this.points = 0;
        this.numOfDraws = 0;
        this.numOfLosses = 0;
        this.numOfWins = 0;
    }

    // Getters and setters
    public int getTeamId() { return teamId; }
    public void setTeamId(int teamId) { this.teamId = teamId; }
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    public Coach getCoach() { return coach; }
    public void setCoach(Coach coach) { this.coach = coach; }
    public List<Player> getPlayers() { return players; }
    public void setPlayers(List<Player> players) { this.players = players; }
    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    // Method to add a player to the team
    public void addPlayer(Player player) {
        if (players.size() < 11) {
            players.add(player);
        } else {
            System.out.println("Team is full. Cannot add more players.");
        }
    }

    // Database operations
    public void insertTeam(FootballDBConnection dbConn) throws SQLException {
        String sql = "INSERT INTO teams (team_name, coach_id, points) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = dbConn.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, this.teamName);
            pstmt.setInt(2, this.coach.getCoachId());
            pstmt.setInt(3, this.points);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        this.teamId = rs.getInt(1);
                    }
                }
            }
        }

        // Insert players into team_players table
        sql = "INSERT INTO team_players (team_id, player_id) VALUES (?, ?)";
        try (PreparedStatement pstmt = dbConn.getConnection().prepareStatement(sql)) {
            for (Player player : players) {
                pstmt.setInt(1, this.teamId);
                pstmt.setInt(2, player.getPlayerId());
                pstmt.executeUpdate();
            }
        }
    }

    public static Team getTeamById(FootballDBConnection dbConn, int teamId) throws SQLException {
        String sql = "SELECT t.*, c.* FROM teams t JOIN coaches c ON t.coach_id = c.coach_id WHERE t.team_id = ?";
        try (PreparedStatement pstmt = dbConn.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, teamId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Coach coach = new Coach(rs.getString("coach_name"), rs.getInt("experience"), rs.getInt("age"));
                    coach.setCoachId(rs.getInt("coach_id"));
                    Team team = new Team(rs.getString("team_name"), coach);
                    team.setTeamId(rs.getInt("team_id"));
                    team.setPoints(rs.getInt("points"));

                    // Fetch players
                    String playerSql = "SELECT p.* FROM players p JOIN team_players tp ON p.player_id = tp.player_id WHERE tp.team_id = ?";
                    try (PreparedStatement playerPstmt = dbConn.getConnection().prepareStatement(playerSql)) {
                        playerPstmt.setInt(1, teamId);
                        try (ResultSet playerRs = playerPstmt.executeQuery()) {
                            while (playerRs.next()) {
                                Player player = new Player(playerRs.getString("player_name"), playerRs.getInt("age"), playerRs.getInt("shirt_number"));
                                player.setPlayerId(playerRs.getInt("player_id"));
                                player.setGoals(playerRs.getInt("goals"));
                                team.addPlayer(player);
                            }
                        }
                    }

                    return team;
                }
            }
        }
        return null;
    }

    public void updateTeam(FootballDBConnection dbConn) throws SQLException {
        Connection conn = dbConn.getConnection();
        conn.setAutoCommit(false);
        try {
            // Update team information
            String teamSql = "UPDATE teams SET team_name = ?, coach_id = ?, points = ? WHERE team_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(teamSql)) {
                pstmt.setString(1, this.teamName);
                pstmt.setInt(2, this.coach.getCoachId());
                pstmt.setInt(3, this.points);
                pstmt.setInt(4, this.teamId);

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Updating team failed, no rows affected.");
                }
            }

            // Update team players
            String deleteSql = "DELETE FROM team_players WHERE team_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteSql)) {
                pstmt.setInt(1, this.teamId);
                pstmt.executeUpdate();
            }

            String insertSql = "INSERT INTO team_players (team_id, player_id) VALUES (?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                for (Player player : this.players) {
                    pstmt.setInt(1, this.teamId);
                    pstmt.setInt(2, player.getPlayerId());
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }
}
