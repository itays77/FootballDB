import java.util.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Match {
    private int matchId;
    private Team team1;
    private Team team2;
    private Referee referee;
    private Stadium stadium;
    private int team1Score;
    private int team2Score;

    public Match(Team team1, Team team2, Referee referee, Stadium stadium, int team1Score, int team2Score) {
        this.team1 = team1;
        this.team2 = team2;
        this.referee = referee;
        this.stadium = stadium;
        this.team1Score = team1Score;
        this.team2Score = team2Score;
    }

    public void insertMatch(FootballDBConnection dbConn) throws SQLException {
        String sql = "INSERT INTO matches (team1_id, team2_id, referee_id, stadium_id, team1_score, team2_score) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = dbConn.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, team1.getTeamId());
            pstmt.setInt(2, team2.getTeamId());
            pstmt.setInt(3, referee.getRefereeId());
            pstmt.setInt(4, stadium.getStadiumId());
            pstmt.setInt(5, team1Score);
            pstmt.setInt(6, team2Score);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating match failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    this.matchId = generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating match failed, no ID obtained.");
                }
            }
        }

        // Update team points
        updateTeamPoints(dbConn);
    }

    private void updateTeamPoints(FootballDBConnection dbConn) throws SQLException {
        String sql = "UPDATE teams SET points = wins * 3 + draws, wins = wins + ?, draws = draws + ?, losses = losses + ? WHERE team_id = ?";
        try (PreparedStatement pstmt = dbConn.getConnection().prepareStatement(sql)) {
            // Update team1
            pstmt.setInt(1, team1Score > team2Score ? 1 : 0);
            pstmt.setInt(2, team1Score == team2Score ? 1 : 0);
            pstmt.setInt(3, team1Score < team2Score ? 1 : 0);
            pstmt.setInt(4, team1.getTeamId());
            pstmt.executeUpdate();

            // Update team2
            pstmt.setInt(1, team2Score > team1Score ? 1 : 0);
            pstmt.setInt(2, team2Score == team1Score ? 1 : 0);
            pstmt.setInt(3, team2Score < team1Score ? 1 : 0);
            pstmt.setInt(4, team2.getTeamId());
            pstmt.executeUpdate();
        }
    }

    // Getters and setters
    public int getMatchId() { return matchId; }
    public void setMatchId(int matchId) { this.matchId = matchId; }
    public Team getTeam1() { return team1; }
    public void setTeam1(Team team1) { this.team1 = team1; }
    public Team getTeam2() { return team2; }
    public void setTeam2(Team team2) { this.team2 = team2; }
    public Referee getReferee() { return referee; }
    public void setReferee(Referee referee) { this.referee = referee; }
    public Stadium getStadium() { return stadium; }
    public void setStadium(Stadium stadium) { this.stadium = stadium; }
    public int getTeam1Score() { return team1Score; }
    public void setTeam1Score(int team1Score) { this.team1Score = team1Score; }
    public int getTeam2Score() { return team2Score; }
    public void setTeam2Score(int team2Score) { this.team2Score = team2Score; }

    @Override
    public String toString() {
        return "Match{" +
                "matchId=" + matchId +
                ", team1=" + team1.getTeamName() +
                ", team2=" + team2.getTeamName() +
                ", referee=" + referee.getRefereeName() +
                ", stadium=" + stadium.getStadiumName() +
                ", team1Score=" + team1Score +
                ", team2Score=" + team2Score +
                '}';
    }
}
