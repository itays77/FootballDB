import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        FootballDBConnection dbConn = new FootballDBConnection();
        Connection conn = null;
        try {
            dbConn.connect();
            conn = dbConn.getConnection();
            conn.setAutoCommit(false);  // Disable auto-commit

            // Create a coach
            Coach coach = new Coach("Jurgen Klopp", 20, 53);
            coach.insertCoach(dbConn);
            System.out.println("Inserted coach: " + coach.getCoachName());

            // Create and insert players
            List<Player> players = new ArrayList<>();
            for (int i = 1; i <= 11; i++) {
                Player player = new Player("Player " + i, 25 + i, i);
                player.insertPlayer(dbConn);
                players.add(player);
                System.out.println("Inserted player: " + player.getPlayerName());
            }

            // Create a team
            Team team = new Team("Liverpool FC", coach);

            // Add players to the team
            for (Player player : players) {
                team.addPlayer(player);
            }

            // Insert the team
            team.insertTeam(dbConn);
            System.out.println("Inserted team: " + team.getTeamName());

            // Commit the transaction
            conn.commit();
            System.out.println("Transaction committed");

            // Retrieve and print the inserted team data
            Team retrievedTeam = Team.getTeamById(dbConn, team.getTeamId());
            System.out.println("\nRetrieved team: " + retrievedTeam.getTeamName());
            System.out.println("Coach: " + retrievedTeam.getCoach().getCoachName());
            System.out.println("Players:");
            for (Player player : retrievedTeam.getPlayers()) {
                System.out.println("- " + player.getPlayerName() + " (Shirt: " + player.getShirtNumber() + ")");
            }

        } catch (SQLException | ClassNotFoundException e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    System.out.println("Transaction is being rolled back");
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);  // Reset auto-commit to true
                }
                dbConn.closeConnection();
                System.out.println("Database connection closed");
            } catch (SQLException e) {
                System.out.println("Error closing database connection: " + e.getMessage());
            }
        }
    }
}