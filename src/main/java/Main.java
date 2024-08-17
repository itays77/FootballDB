import java.sql.*;
import java.util.*;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static FootballDBConnection dbConn;



    public static void main(String[] args) {
        dbConn = new FootballDBConnection();

        initializeData();


        while (true) {
            displayMenu();
            int choice = getUserChoice();

            try {
                switch (choice) {
                    // View operations
                    case 1: viewAllTeams(); break;
                    case 2: viewTeamDetails(); break;
                    case 3: viewAllPlayers(); break;
                    case 4: viewAllCoaches(); break;
                    case 5: viewPlayersWithoutTeam(); break;
                    case 6: viewAllMatches(); break;
                    case 7: viewTopGolasScorers(); break;
                    case 8: viewLeagueStandings(); break;
                    // Update operations
                    case 9: updatePlayer(); break;
                    case 10: updateCoach(); break;
                    case 11: updateReferee(); break;
                    case 12: updateTeam(); break;
                    // Add operations
                    case 13: addPlayer(); break;
                    case 14: addCoach(); break;
                    case 15: addReferee(); break;
                    case 16: addStadium(); break;
                    case 17: addTeam(); break;
                    // Custom query and exit
                    case 18: customQuery(); break;
                    case 19:
                        System.out.println("Exiting program. Goodbye!");
                        System.exit(0);
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            } catch (ClassNotFoundException e) {
                System.out.println("Database driver not found: " + e.getMessage());
            }
        }
    }

    private static void displayMenu() {
        System.out.println("\n--- Football Database Menu ---");
        System.out.println("View Operations:");
        System.out.println("1. View all teams");
        System.out.println("2. View team details (players and coach)");
        System.out.println("3. View all players");
        System.out.println("4. View all coaches");
        System.out.println("5. View players without a team");
        System.out.println("6. View all matches");
        System.out.println("7. View players by goals");
        System.out.println("8. View league standings");
        System.out.println("Update Operations:");
        System.out.println("9. Update player");
        System.out.println("10. Update coach");
        System.out.println("11. Update referee");
        System.out.println("12. Update team");
        System.out.println("Add Operations:");
        System.out.println("13. Add a new player");
        System.out.println("14. Add a new coach");
        System.out.println("15. Add a new referee");
        System.out.println("16. Add a new stadium");
        System.out.println("17. Add a new team");
        System.out.println("Build Custom Query:");
        System.out.println("18. Custom Query");
        System.out.println("19. Exit");
        System.out.print("Enter your choice: ");
    }

    private static int getUserChoice() {
        while (!scanner.hasNextInt()) {
            System.out.println("That's not a valid number. Please try again.");
            scanner.next();
        }
        return scanner.nextInt();
    }

    private static void initializeData() {
        System.out.println("Initializing database with sample data...");
        DataInitializer.initializeData(dbConn);
        System.out.println("Database initialization complete.");
    }


    private static void viewAllTeams() {
        try {
            dbConn.connect();
            String sql = "SELECT team_id, team_name FROM teams";
            try (PreparedStatement pstmt = dbConn.getConnection().prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    System.out.println("Team ID: " + rs.getInt("team_id") + ", Name: " + rs.getString("team_name"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving teams: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                dbConn.closeConnection();
            } catch (SQLException e) {
                System.out.println("Error closing connection: " + e.getMessage());
            }
        }
    }

    private static String buildPlayerQuery() {
        StringBuilder queryBuilder = new StringBuilder("SELECT * FROM players");
        List<String> conditions = new ArrayList<>();

        addNumericFilter(conditions, "age", "player's age");
        addNumericFilter(conditions, "shirt_number", "shirt number");
        addNumericFilter(conditions, "goals", "number of goals");

        if (!conditions.isEmpty()) {
            queryBuilder.append(" WHERE ").append(String.join(" AND ", conditions));
        }

        return queryBuilder.toString();
    }

    private static String buildTeamQuery() {
        StringBuilder queryBuilder = new StringBuilder("SELECT * FROM teams");
        List<String> conditions = new ArrayList<>();

        addNumericFilter(conditions, "points", "points");
        addNumericFilter(conditions, "wins", "wins");
        addNumericFilter(conditions, "draws", "draws");
        addNumericFilter(conditions, "losses", "losses");

        if (!conditions.isEmpty()) {
            queryBuilder.append(" WHERE ").append(String.join(" AND ", conditions));
        }

        return queryBuilder.toString();
    }

    private static void addNumericFilter(List<String> conditions, String columnName, String displayName) {
        System.out.printf("Do you want to filter by %s? (y/n): ", displayName);
        if (scanner.nextLine().toLowerCase().equals("y")) {
            System.out.println("Choose filter type:");
            System.out.println("1. Exact number");
            System.out.println("2. Minimum");
            System.out.println("3. Maximum");
            System.out.println("4. Range");
            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1:
                    System.out.printf("Enter exact %s: ", displayName);
                    int exact = Integer.parseInt(scanner.nextLine());
                    conditions.add(columnName + " = " + exact);
                    break;
                case 2:
                    System.out.printf("Enter minimum %s: ", displayName);
                    int min = Integer.parseInt(scanner.nextLine());
                    conditions.add(columnName + " >= " + min);
                    break;
                case 3:
                    System.out.printf("Enter maximum %s: ", displayName);
                    int max = Integer.parseInt(scanner.nextLine());
                    conditions.add(columnName + " <= " + max);
                    break;
                case 4:
                    System.out.printf("Enter minimum %s: ", displayName);
                    int rangeMin = Integer.parseInt(scanner.nextLine());
                    System.out.printf("Enter maximum %s: ", displayName);
                    int rangeMax = Integer.parseInt(scanner.nextLine());
                    conditions.add(columnName + " BETWEEN " + rangeMin + " AND " + rangeMax);
                    break;
                default:
                    System.out.println("Invalid choice. Skipping this filter.");
            }
        }
    }

    private static void viewTeamDetails() {
        System.out.print("Enter team ID: ");
        int teamId = scanner.nextInt();

        try {
            dbConn.connect();
            String sql = "SELECT t.team_name, c.coach_name, p.player_name, p.shirt_number, p.age, p.goals " +
                    "FROM teams t " +
                    "LEFT JOIN coaches c ON t.coach_id = c.coach_id " +
                    "LEFT JOIN team_players tp ON t.team_id = tp.team_id " +
                    "LEFT JOIN players p ON tp.player_id = p.player_id " +
                    "WHERE t.team_id = ?";
            try (PreparedStatement pstmt = dbConn.getConnection().prepareStatement(sql)) {
                pstmt.setInt(1, teamId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    boolean first = true;
                    while (rs.next()) {
                        if (first) {
                            System.out.println("Team: " + rs.getString("team_name"));
                            System.out.println("Coach: " + rs.getString("coach_name"));
                            System.out.println("Players:");
                            first = false;
                        }
                        System.out.printf("Name: %-20s Shirt: %2d Age: %2d Goals: %2d%n",
                                rs.getString("player_name"),
                                rs.getInt("shirt_number"),
                                rs.getInt("age"),
                                rs.getInt("goals"));
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving team details: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                dbConn.closeConnection();
            } catch (SQLException e) {
                System.out.println("Error closing connection: " + e.getMessage());
            }
        }
    }

    private static void viewAllPlayers() {
        try {
            dbConn.connect();
            String sql = "SELECT player_id, player_name, age, shirt_number, goals FROM players";
            try (PreparedStatement pstmt = dbConn.getConnection().prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    System.out.printf("ID: %d, Name: %-20s Age: %2d Shirt: %2d Goals: %2d%n",
                            rs.getInt("player_id"),
                            rs.getString("player_name"),
                            rs.getInt("age"),
                            rs.getInt("shirt_number"),
                            rs.getInt("goals"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving players: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                dbConn.closeConnection();
            } catch (SQLException e) {
                System.out.println("Error closing connection: " + e.getMessage());
            }
        }
    }

    private static void viewAllCoaches() {
        try {
            dbConn.connect();
            String sql = "SELECT coach_id, coach_name, age, experience FROM coaches";
            try (PreparedStatement pstmt = dbConn.getConnection().prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    System.out.printf("ID: %d, Name: %-20s Age: %2d Experience: %2d years%n",
                            rs.getInt("coach_id"),
                            rs.getString("coach_name"),
                            rs.getInt("age"),
                            rs.getInt("experience"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving coaches: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                dbConn.closeConnection();
            } catch (SQLException e) {
                System.out.println("Error closing connection: " + e.getMessage());
            }
        }
    }

    private static void customQuery() throws SQLException, ClassNotFoundException {
        dbConn.connect();
        try {
            System.out.println("Select a table to query:");
            System.out.println("1. Matches");
            System.out.println("2. Players");
            System.out.println("3. Teams");
            int tableChoice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            String query;
            switch (tableChoice) {
                case 1:
                    query = buildMatchQuery();
                    break;
                case 2:
                    query = buildPlayerQuery();
                    break;
                case 3:
                    query = buildTeamQuery();
                    break;
                default:
                    System.out.println("Invalid choice.");
                    return;
            }

            System.out.println("Executing query: " + query);

            if (tableChoice != 1) { // For players and teams queries
                try (Statement stmt = dbConn.getConnection().createStatement();
                     ResultSet rs = stmt.executeQuery(query)) {
                    ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();

                    // Print column names
                    for (int i = 1; i <= columnCount; i++) {
                        System.out.printf("%-20s", metaData.getColumnName(i));
                    }
                    System.out.println();

                    // Print rows
                    while (rs.next()) {
                        for (int i = 1; i <= columnCount; i++) {
                            System.out.printf("%-20s", rs.getString(i));
                        }
                        System.out.println();
                    }
                }
            }
        } finally {
            dbConn.closeConnection();
        }
    }

    private static void viewLeagueStandings() throws SQLException, ClassNotFoundException {
        dbConn.connect();
        String sql = "SELECT t.team_id, t.team_name, t.points, t.wins, t.draws, t.losses, " +
                "p.player_name AS top_scorer, p.goals AS top_scorer_goals " +
                "FROM teams t " +
                "LEFT JOIN players p ON p.player_id = " +
                "(SELECT player_id FROM players " +
                "WHERE player_id IN (SELECT player_id FROM team_players WHERE team_id = t.team_id) " +
                "ORDER BY goals DESC LIMIT 1) " +
                "ORDER BY t.points DESC, t.wins DESC, (t.wins * 3 + t.draws) DESC";

        try (PreparedStatement pstmt = dbConn.getConnection().prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            System.out.println("\nLeague Standings:");
            System.out.printf("%-5s %-20s %-7s %-5s %-5s %-7s %-20s %-5s%n",
                    "ID", "Team Name", "Points", "Wins", "Draws", "Losses", "Top Scorer", "Goals");
            System.out.println("-".repeat(80));

            while (rs.next()) {
                System.out.printf("%-5d %-20s %-7d %-5d %-5d %-7d %-20s %-5d%n",
                        rs.getInt("team_id"),
                        rs.getString("team_name"),
                        rs.getInt("points"),
                        rs.getInt("wins"),
                        rs.getInt("draws"),
                        rs.getInt("losses"),
                        rs.getString("top_scorer"),
                        rs.getInt("top_scorer_goals"));
            }
        } finally {
            dbConn.closeConnection();
        }
    }

    private static String buildMatchQuery() throws SQLException {
        System.out.println("Available teams:");
        String teamQuery = "SELECT team_id, team_name FROM teams";
        try (Statement stmt = dbConn.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(teamQuery)) {
            while (rs.next()) {
                System.out.printf("%d: %s%n", rs.getInt("team_id"), rs.getString("team_name"));
            }
        }

        System.out.print("Enter the ID of the first team you want to query: ");
        int team1Id = Integer.parseInt(scanner.nextLine().trim());

        System.out.print("Enter the ID of the second team (or press Enter to skip): ");
        String team2Input = scanner.nextLine().trim();
        Integer team2Id = team2Input.isEmpty() ? null : Integer.parseInt(team2Input);

        StringBuilder queryBuilder = new StringBuilder();
        if (team2Id == null) {
            // Query for a single team
            queryBuilder.append(
                    "SELECT m.match_id, " +
                            "CASE WHEN m.team1_id = ? THEN t1.team_name ELSE t2.team_name END AS selected_team_name, " +
                            "CASE WHEN m.team1_id = ? THEN t2.team_name ELSE t1.team_name END AS opponent_team_name, " +
                            "CASE " +
                            "   WHEN (m.team1_id = ? AND m.team1_score > m.team2_score) OR (m.team2_id = ? AND m.team2_score > m.team1_score) THEN 'Win' " +
                            "   WHEN m.team1_score = m.team2_score THEN 'Draw' " +
                            "   ELSE 'Loss' " +
                            "END AS result, " +
                            "CASE WHEN m.team1_id = ? THEN m.team1_score ELSE m.team2_score END AS selected_team_score, " +
                            "CASE WHEN m.team1_id = ? THEN m.team2_score ELSE m.team1_score END AS opponent_team_score, " +
                            "r.referee_name, s.stadium_name " +
                            "FROM matches m " +
                            "JOIN teams t1 ON m.team1_id = t1.team_id " +
                            "JOIN teams t2 ON m.team2_id = t2.team_id " +
                            "JOIN referees r ON m.referee_id = r.referee_id " +
                            "JOIN stadiums s ON m.stadium_id = s.stadium_id " +
                            "WHERE m.team1_id = ? OR m.team2_id = ? " +
                            "ORDER BY m.match_id"
            );

            try (PreparedStatement pstmt = dbConn.getConnection().prepareStatement(queryBuilder.toString())) {
                for (int i = 1; i <= 8; i++) {
                    pstmt.setInt(i, team1Id);
                }

                try (ResultSet rs = pstmt.executeQuery()) {
                    System.out.println("\nMatch results for the selected team:");
                    System.out.printf("%-10s %-20s %-20s %-10s %-10s %-10s %-15s %-20s%n",
                            "Match ID", "Selected Team", "Opponent", "Result", "Score", "Opp Score", "Referee", "Stadium");
                    System.out.println("-".repeat(120));

                    while (rs.next()) {
                        System.out.printf("%-10d %-20s %-20s %-10s %-10d %-10d %-15s %-20s%n",
                                rs.getInt("match_id"),
                                rs.getString("selected_team_name"),
                                rs.getString("opponent_team_name"),
                                rs.getString("result"),
                                rs.getInt("selected_team_score"),
                                rs.getInt("opponent_team_score"),
                                rs.getString("referee_name"),
                                rs.getString("stadium_name"));
                    }
                }
            }
        } else {
            // Query for matches between two specific teams
            queryBuilder.append(
                    "SELECT m.match_id, " +
                            "t1.team_name AS team1_name, " +
                            "t2.team_name AS team2_name, " +
                            "m.team1_score, " +
                            "m.team2_score, " +
                            "r.referee_name, " +
                            "s.stadium_name " +
                            "FROM matches m " +
                            "JOIN teams t1 ON m.team1_id = t1.team_id " +
                            "JOIN teams t2 ON m.team2_id = t2.team_id " +
                            "JOIN referees r ON m.referee_id = r.referee_id " +
                            "JOIN stadiums s ON m.stadium_id = s.stadium_id " +
                            "WHERE (m.team1_id = ? AND m.team2_id = ?) OR (m.team1_id = ? AND m.team2_id = ?) " +
                            "ORDER BY m.match_id"
            );

            try (PreparedStatement pstmt = dbConn.getConnection().prepareStatement(queryBuilder.toString())) {
                pstmt.setInt(1, team1Id);
                pstmt.setInt(2, team2Id);
                pstmt.setInt(3, team2Id);
                pstmt.setInt(4, team1Id);

                try (ResultSet rs = pstmt.executeQuery()) {
                    System.out.println("\nMatches between the selected teams:");
                    System.out.printf("%-10s %-20s %-20s %-10s %-10s %-15s %-20s%n",
                            "Match ID", "Team 1", "Team 2", "Score 1", "Score 2", "Referee", "Stadium");
                    System.out.println("-".repeat(120));

                    while (rs.next()) {
                        System.out.printf("%-10d %-20s %-20s %-10d %-10d %-15s %-20s%n",
                                rs.getInt("match_id"),
                                rs.getString("team1_name"),
                                rs.getString("team2_name"),
                                rs.getInt("team1_score"),
                                rs.getInt("team2_score"),
                                rs.getString("referee_name"),
                                rs.getString("stadium_name"));
                    }
                }
            }
        }

        return queryBuilder.toString(); // Return the query string for potential future use
    }


    private static void viewPlayersWithoutTeam() {
        try {
            dbConn.connect();
            String sql = "SELECT p.player_id, p.player_name, p.age, p.shirt_number, p.goals " +
                    "FROM players p " +
                    "LEFT JOIN team_players tp ON p.player_id = tp.player_id " +
                    "WHERE tp.team_id IS NULL " +
                    "ORDER BY p.player_name";
            try (PreparedStatement pstmt = dbConn.getConnection().prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                System.out.println("Players without a team:");
                System.out.printf("%-5s %-20s %-5s %-5s %-5s%n", "ID", "Name", "Age", "Shirt", "Goals");
                while (rs.next()) {
                    System.out.printf("%-5d %-20s %-5d %-5d %-5d%n",
                            rs.getInt("player_id"),
                            rs.getString("player_name"),
                            rs.getInt("age"),
                            rs.getInt("shirt_number"),
                            rs.getInt("goals"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving players without a team: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.out.println("Database driver not found: " + e.getMessage());
        } finally {
            try {
                dbConn.closeConnection();
            } catch (SQLException e) {
                System.out.println("Error closing connection: " + e.getMessage());
            }
        }
    }

    private static void viewAllMatches() {
        try {
            dbConn.connect();
            String sql = "SELECT m.match_id, t1.team_name as team1, t2.team_name as team2, " +
                    "m.team1_score, m.team2_score, r.referee_name, s.stadium_name " +
                    "FROM matches m " +
                    "JOIN teams t1 ON m.team1_id = t1.team_id " +
                    "JOIN teams t2 ON m.team2_id = t2.team_id " +
                    "JOIN referees r ON m.referee_id = r.referee_id " +
                    "JOIN stadiums s ON m.stadium_id = s.stadium_id " +
                    "ORDER BY m.match_id";
            try (PreparedStatement pstmt = dbConn.getConnection().prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                System.out.println("\nAll Matches:");
                System.out.printf("%-5s %-20s %-5s %-20s %-15s %-25s%n",
                        "ID", "Team 1", "Score", "Team 2", "Referee", "Stadium");
                System.out.println("-".repeat(95));
                while (rs.next()) {
                    System.out.printf("%-5d %-20s %2d - %-2d %-20s %-15s %-25s%n",
                            rs.getInt("match_id"),
                            rs.getString("team1"),
                            rs.getInt("team1_score"),
                            rs.getInt("team2_score"),
                            rs.getString("team2"),
                            rs.getString("referee_name"),
                            rs.getString("stadium_name"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving matches: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.out.println("Database driver not found: " + e.getMessage());
        } finally {
            try {
                dbConn.closeConnection();
            } catch (SQLException e) {
                System.out.println("Error closing connection: " + e.getMessage());
            }
        }
    }


    private static void viewTopGolasScorers() {
        try {
            dbConn.connect();
            String sql = "SELECT player_id, player_name, age, shirt_number, goals FROM players ORDER BY goals DESC LIMIT 15";
            try (PreparedStatement pstmt = dbConn.getConnection().prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                System.out.println("Top 15 Players by goals:");
                System.out.printf("%-5s %-20s %-5s %-5s %-5s%n", "ID", "Name", "Age", "Shirt", "Goals");
                System.out.println("-".repeat(45));
                while (rs.next()) {
                    System.out.printf("%-5d %-20s %-5d %-5d %-5d%n",
                            rs.getInt("player_id"),
                            rs.getString("player_name"),
                            rs.getInt("age"),
                            rs.getInt("shirt_number"),
                            rs.getInt("goals"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving players: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.out.println("Database driver not found: " + e.getMessage());
        } finally {
            try {
                dbConn.closeConnection();
            } catch (SQLException e) {
                System.out.println("Error closing connection: " + e.getMessage());
            }
        }
    }

    private static void addPlayer() throws SQLException, ClassNotFoundException {
        scanner.nextLine(); // Consume newline
        System.out.print("Enter player name: ");
        String name = scanner.nextLine();
        System.out.print("Enter player age: ");
        int age = scanner.nextInt();
        System.out.print("Enter shirt number: ");
        int shirtNumber = scanner.nextInt();

        Player player = new Player(name, age, shirtNumber);
        dbConn.connect();
        player.insertPlayer(dbConn);
        dbConn.closeConnection();

        System.out.println("Player added successfully with ID: " + player.getPlayerId());
    }

    private static void addReferee() throws SQLException, ClassNotFoundException {
        scanner.nextLine(); // Consume newline
        System.out.print("Enter referee name: ");
        String name = scanner.nextLine();
        System.out.print("Enter referee age: ");
        int age = scanner.nextInt();
        System.out.print("Enter years of experience: ");
        int experience = scanner.nextInt();

        Referee referee = new Referee(name, age, experience);
        dbConn.connect();
        referee.insertReferee(dbConn);
        dbConn.closeConnection();

        System.out.println("Referee added successfully with ID: " + referee.getRefereeId());
    }

    private static void addCoach() throws SQLException, ClassNotFoundException {
        scanner.nextLine(); // Consume newline
        System.out.print("Enter coach name: ");
        String name = scanner.nextLine();
        System.out.print("Enter coach age: ");
        int age = scanner.nextInt();
        System.out.print("Enter years of experience: ");
        int experience = scanner.nextInt();

        Coach coach = new Coach(name, experience, age);
        dbConn.connect();
        coach.insertCoach(dbConn);
        dbConn.closeConnection();

        System.out.println("Coach added successfully with ID: " + coach.getCoachId());
    }

    private static void addStadium() throws SQLException, ClassNotFoundException {
        scanner.nextLine(); // Consume newline
        System.out.print("Enter stadium name: ");
        String name = scanner.nextLine();
        System.out.print("Enter stadium capacity: ");
        int capacity = scanner.nextInt();

        Stadium stadium = new Stadium(name, capacity);
        dbConn.connect();
        stadium.insertStadium(dbConn);
        dbConn.closeConnection();

        System.out.println("Stadium added successfully with ID: " + stadium.getStadiumId());
    }

    private static void addTeam() throws SQLException, ClassNotFoundException {
        dbConn.connect();
        try {
            scanner.nextLine(); // Consume newline
            System.out.print("Enter the name of the new team: ");
            String teamName = scanner.nextLine();

            // Select a coach
            List<Coach> availableCoaches = Coach.getCoachesWithoutTeam(dbConn);
            if (availableCoaches.isEmpty()) {
                System.out.println("No coaches available. Please add a coach first.");
                return;
            }
            System.out.println("Available coaches:");
            for (Coach coach : availableCoaches) {
                System.out.printf("%d: %s%n", coach.getCoachId(), coach.getCoachName());
            }
            System.out.print("Enter the ID of the coach for this team: ");
            int coachId = scanner.nextInt();
            Coach selectedCoach = Coach.getCoachById(dbConn, coachId);
            if (selectedCoach == null) {
                System.out.println("Invalid coach ID.");
                return;
            }

            Team newTeam = new Team(teamName, selectedCoach);

            // Automatically select 11 players without a team
            List<Player> availablePlayers = Player.getPlayersWithoutTeam(dbConn);
            if (availablePlayers.size() < 11) {
                System.out.println("Not enough players available. Team needs 11 players.");
                return;
            }

            Random random = new Random();
            for (int i = 0; i < 11; i++) {
                if (availablePlayers.isEmpty()) {
                    System.out.println("No more players available. Team created with " + i + " players.");
                    break;
                }
                int randomIndex = random.nextInt(availablePlayers.size());
                Player selectedPlayer = availablePlayers.remove(randomIndex);
                newTeam.addPlayer(selectedPlayer);
                System.out.println("Added " + selectedPlayer.getPlayerName() + " to the team.");
            }

            newTeam.insertTeam(dbConn);
            System.out.println("Team " + teamName + " has been created successfully with 11 players!");

        } finally {
            dbConn.closeConnection();
        }
    }

    private static void updatePlayer() throws SQLException, ClassNotFoundException {
        dbConn.connect();
        try {
            // First, display all players
            System.out.println("All players:");
            String allPlayersSql = "SELECT player_id, player_name, age, shirt_number, goals FROM players ORDER BY player_id";
            try (PreparedStatement pstmt = dbConn.getConnection().prepareStatement(allPlayersSql);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    System.out.printf("ID: %d, Name: %-20s Age: %2d, Shirt: %2d, Goals: %2d%n",
                            rs.getInt("player_id"),
                            rs.getString("player_name"),
                            rs.getInt("age"),
                            rs.getInt("shirt_number"),
                            rs.getInt("goals"));
                }
            }

            System.out.print("\nEnter player ID to update: ");
            int playerId = scanner.nextInt();
            scanner.nextLine();

            Player player = Player.getPlayerById(dbConn, playerId);
            if (player == null) {
                System.out.println("Player not found.");
                return;
            }

            System.out.println("Current player details:");
            System.out.printf("Name: %s, Age: %d, Shirt Number: %d, Goals: %d%n",
                    player.getPlayerName(), player.getAge(), player.getShirtNumber(), player.getGoals());

            System.out.print("Enter new name (or press Enter to keep current): ");
            String name = scanner.nextLine();
            if (!name.isEmpty()) {
                player.setPlayerName(name);
            }

            System.out.print("Enter new age (or -1 to keep current): ");
            int age = scanner.nextInt();
            if (age != -1) {
                player.setAge(age);
            }

            System.out.print("Enter new shirt number (or -1 to keep current): ");
            int shirtNumber = scanner.nextInt();
            if (shirtNumber != -1) {
                player.setShirtNumber(shirtNumber);
            }

            System.out.print("Enter new goals (or -1 to keep current): ");
            int goals = scanner.nextInt();
            if (goals != -1) {
                player.setGoals(goals);
            }

            player.updatePlayer(dbConn);
            System.out.println("Player updated successfully.");
        } finally {
            dbConn.closeConnection();
        }
    }

    private static void updateCoach() throws SQLException, ClassNotFoundException {
        dbConn.connect();
        try {
            System.out.print("Enter coach ID to update: ");
            int coachId = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            Coach coach = Coach.getCoachById(dbConn, coachId);
            if (coach == null) {
                System.out.println("Coach not found.");
                return;
            }

            System.out.println("Current coach details:");
            System.out.printf("Name: %s, Age: %d, Experience: %d years%n",
                    coach.getCoachName(), coach.getAge(), coach.getExperience());

            System.out.print("Enter new name (or press Enter to keep current): ");
            String name = scanner.nextLine();
            if (!name.isEmpty()) {
                coach.setCoachName(name);
            }

            System.out.print("Enter new age (or -1 to keep current): ");
            int age = scanner.nextInt();
            if (age != -1) {
                coach.setAge(age);
            }

            System.out.print("Enter new years of experience (or -1 to keep current): ");
            int experience = scanner.nextInt();
            if (experience != -1) {
                coach.setExperience(experience);
            }

            coach.updateCoach(dbConn);
            System.out.println("Coach updated successfully.");
        } finally {
            dbConn.closeConnection();
        }
    }
    private static void updateReferee() throws SQLException, ClassNotFoundException {
        dbConn.connect();
        try {
            System.out.print("Enter referee ID to update: ");
            int refereeId = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            Referee referee = Referee.getRefereeById(dbConn, refereeId);
            if (referee == null) {
                System.out.println("Referee not found.");
                return;
            }

            System.out.println("Current referee details:");
            System.out.printf("Name: %s, Age: %d, Experience: %d years%n",
                    referee.getRefereeName(), referee.getAge(), referee.getExperience());

            System.out.print("Enter new name (or press Enter to keep current): ");
            String name = scanner.nextLine();
            if (!name.isEmpty()) {
                referee.setRefereeName(name);
            }

            System.out.print("Enter new age (or -1 to keep current): ");
            int age = scanner.nextInt();
            if (age != -1) {
                referee.setAge(age);
            }

            System.out.print("Enter new years of experience (or -1 to keep current): ");
            int experience = scanner.nextInt();
            if (experience != -1) {
                referee.setExperience(experience);
            }

            referee.updateReferee(dbConn);
            System.out.println("Referee updated successfully.");
        } finally {
            dbConn.closeConnection();
        }
    }

    private static void updateTeam() throws SQLException, ClassNotFoundException {
        dbConn.connect();
        try {
            System.out.print("Enter team ID to update: ");
            int teamId = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            Team team = Team.getTeamById(dbConn, teamId);
            if (team == null) {
                System.out.println("Team not found.");
                return;
            }

            System.out.println("Current team details:");
            System.out.printf("Name: %s, Coach: %s%n", team.getTeamName(), team.getCoach().getCoachName());

            System.out.print("Enter new team name (or press Enter to keep current): ");
            String name = scanner.nextLine();
            if (!name.isEmpty()) {
                team.setTeamName(name);
            }

            System.out.print("Do you want to replace the coach? (y/n): ");
            if (scanner.nextLine().toLowerCase().equals("y")) {
                List<Coach> availableCoaches = Coach.getCoachesWithoutTeam(dbConn);
                if (availableCoaches.isEmpty()) {
                    System.out.println("No coaches available. Keeping current coach.");
                } else {
                    System.out.println("Available coaches:");
                    for (Coach coach : availableCoaches) {
                        System.out.printf("%d: %s%n", coach.getCoachId(), coach.getCoachName());
                    }
                    System.out.print("Enter the ID of the new coach: ");
                    int newCoachId = scanner.nextInt();
                    Coach newCoach = Coach.getCoachById(dbConn, newCoachId);
                    if (newCoach != null) {
                        team.setCoach(newCoach);
                    } else {
                        System.out.println("Invalid coach ID. Keeping current coach.");
                    }
                }
            }

            System.out.print("Do you want to replace a player? (y/n): ");
            if (scanner.nextLine().toLowerCase().equals("y")) {
                System.out.println("Current players:");
                List<Player> currentPlayers = team.getPlayers();
                for (int i = 0; i < currentPlayers.size(); i++) {
                    System.out.printf("%d: %s%n", i + 1, currentPlayers.get(i).getPlayerName());
                }
                System.out.print("Enter the number of the player to replace: ");
                int playerIndex = scanner.nextInt() - 1;
                scanner.nextLine(); // Consume newline

                if (playerIndex >= 0 && playerIndex < currentPlayers.size()) {
                    List<Player> availablePlayers = Player.getPlayersWithoutTeam(dbConn);
                    if (availablePlayers.isEmpty()) {
                        System.out.println("No players available for replacement.");
                    } else {
                        System.out.println("Available players:");
                        for (Player player : availablePlayers) {
                            System.out.printf("%d: %s%n", player.getPlayerId(), player.getPlayerName());
                        }
                        System.out.print("Enter the ID of the new player: ");
                        int newPlayerId = scanner.nextInt();
                        Player newPlayer = Player.getPlayerById(dbConn, newPlayerId);
                        if (newPlayer != null) {
                            currentPlayers.set(playerIndex, newPlayer);
                            team.setPlayers(currentPlayers);
                        } else {
                            System.out.println("Invalid player ID. No changes made.");
                        }
                    }
                } else {
                    System.out.println("Invalid player number. No changes made.");
                }
            }

            team.updateTeam(dbConn);
            System.out.println("Team updated successfully.");
        } finally {
            dbConn.closeConnection();
        }
    }


}

