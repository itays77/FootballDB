import java.sql.*;
import java.util.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Random;

public class DataInitializer {
    private static final Random random = new Random();
    private static final String[] TEAM_NAMES = {
            "Manchester City", "Real Madrid", "Bayern Munich", "Manchester United", "Juventus",
            "Barcelona", "Liverpool", "Atletico Madrid", "Paris Saint Germen", "Tottenham Hotspurs"
    };

    private static final String[] COACH_NAMES = {
            "Pep Guardiola", "Carlo Ancelotti", "Thomas Tuchel", "Mauricio Pochettino", "Zinedine Zidane",
            "Diego Simeone", "Jurgen Klopp", "Antonio Conte", "Jose Mourinho", "Hansi Flick", "Miro Barbiro", "Kippi Ben Kipod"
    };

    private static final String[] PLAYER_NAMES = {

            // Manchester City
            "Kevin De Bruyne", "Erling Haaland", "Phil Foden", "Ruben Dias", "Raheem Sterling",
            "Ilkay Gundogan", "Bernardo Silva", "Kyle Walker", "Joao Cancelo", "Rodri", "Ederson",

            // Real Madrid
            "Karim Benzema", "Toni Kroos", "Luka Modric", "Vinicius Jr.", "Federico Valverde",
            "Thibaut Courtois", "David Alaba", "Eder Militao", "Ferland Mendy", "Dani Carvajal", "Eduardo Camavinga",

            // Bayern Munich
            "Joshua Kimmich", "Alphonso Davies", "Thomas Muller", "Robert Lewandowski", "Leroy Sane",
            "Kingsley Coman", "Leon Goretzka", "Dayot Upamecano", "Manuel Neuer", "Lucas Hernandez", "Serge Gnabry",

            // Manchester United
            "Bruno Fernandes", "Marcus Rashford", "Jadon Sancho", "Cristiano Ronaldo", "Paul Pogba",
            "Harry Maguire", "Luke Shaw", "Aaron Wan-Bissaka", "David De Gea", "Raphael Varane", "Scott McTominay",

            // Juventus
            "Paulo Dybala", "Federico Chiesa", "Leonardo Bonucci", "Giorgio Chiellini", "Matthijs de Ligt",
            "Wojciech Szczesny", "Alex Sandro", "Juan Cuadrado", "Manuel Locatelli", "Alvaro Morata", "Arthur Melo",

            // Barcelona
            "Frenkie de Jong", "Ansu Fati", "Pedri", "Gerard Pique", "Sergio Busquets",
            "Memphis Depay", "Ousmane Dembele", "Marc-Andre ter Stegen", "Jordi Alba", "Ronald Araujo", "Gavi",

            // Liverpool
            "Mohamed Salah", "Sadio Mane", "Virgil van Dijk", "Alisson Becker", "Trent Alexander-Arnold",
            "Andy Robertson", "Fabinho", "Jordan Henderson", "Diogo Jota", "Thiago Alcantara", "Roberto Firmino",

            // Atletico Madrid
            "Joao Felix", "Luis Suarez", "Koke", "Jan Oblak", "Marcos Llorente",
            "Antoine Griezmann", "Jose Gimenez", "Yannick Carrasco", "Renan Lodi", "Stefan Savic", "Rodrigo De Paul",

            // Paris Saint Germen
            "Lionel Messi", "Neymar Jr.", "Kylian Mbappe", "Marquinhos", "Achraf Hakimi",
            "Sergio Ramos", "Marco Verratti", "Gianluigi Donnarumma", "Presnel Kimpembe", "Leandro Paredes", "Angel Di Maria",

            // Tottenham Hotspurs
            "Harry Kane", "Son Heung-min", "Dele Alli", "Hugo Lloris", "Tanguy Ndombele",
            "Eric Dier", "Giovani Lo Celso", "Lucas Moura", "Steven Bergwijn", "Japhet Tanganga", "Pierre-Emile Hojbjerg",
            "Shiran Yeini", "Eran Zahavi", "Baruch Dego", "Dor Peretz", "Neta Lavi", "Eitan Tibi", "Omri Ben Harush", "Eli Dasa", "Yonatan Cohen", "Yahav Gurfinkel", "Haim Revivo",  "Eyal Berkovich", "Yossi Benayoun", "Maor Melikson"
    };

    private static final String[] STADIUM_NAMES = {
            "Etihad Stadium", "Santiago Bernabeu", "Allianz Arena", "Old Trafford", "Allianz Stadium",
            "Camp Nou", "Anfield", "Wanda Metropolitano", "Parc des Princes", "Tottenham Hotspur Stadium"
    };

    private static final String[] REFEREE_NAMES = {
            "Michael Oliver", "Felix Brych", "Bjorn Kuipers", "Cuneyt Cakir", "Daniele Orsato",
            "Anthony Taylor", "Clement Turpin", "Danny Makkelie", "Slavko Vincic", "Antonio Mateu Lahoz"
    };



    public static void initializeData(FootballDBConnection dbConn) {
        Connection conn = null;
        try {
            dbConn.connect();
            conn = dbConn.getConnection();
            conn.setAutoCommit(false);

            List<Team> teams = new ArrayList<>();
            List<Player> allPlayers = new ArrayList<>();

            // Create all players first, maintaining the original order
            for (String playerName : PLAYER_NAMES) {
                Player player = createPlayer(dbConn, playerName, 0); // We'll set shirt numbers later
                allPlayers.add(player);
            }

            Collections.shuffle(allPlayers); // Shuffle to randomize team assignment

            int playerIndex = 0;
            for (int i = 0; i < TEAM_NAMES.length; i++) {
                Coach coach = createCoach(dbConn, COACH_NAMES[i], i);
                Team team = new Team(TEAM_NAMES[i], coach);

                Set<Integer> usedShirtNumbers = new HashSet<>();
                // Add 11 players to each team
                for (int j = 0; j < 11 && playerIndex < allPlayers.size(); j++, playerIndex++) {
                    Player player = allPlayers.get(playerIndex);
                    int shirtNumber;
                    do {
                        shirtNumber = random.nextInt(20) + 1;
                    } while (usedShirtNumbers.contains(shirtNumber));
                    usedShirtNumbers.add(shirtNumber);
                    player.setShirtNumber(shirtNumber);
                    team.addPlayer(player);
                }

                team.insertTeam(dbConn);
                teams.add(team);
                System.out.println("Inserted team: " + team.getTeamName());
            }

            // Add remaining players without a team
            addRemainingPlayers(dbConn, allPlayers.subList(playerIndex, allPlayers.size()));

            List<Stadium> stadiums = createStadiums(dbConn);
            List<Referee> referees = createReferees(dbConn);

            simulateMatches(dbConn, teams, stadiums, referees);

            conn.commit();
            System.out.println("All data inserted successfully.");
        } catch (SQLException e) {
            // ... (error handling remains unchanged)
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            // ... (cleanup code remains unchanged)
        }
    }

    private static void addRemainingPlayers(FootballDBConnection dbConn, List<Player> remainingPlayers) throws SQLException {
        System.out.println("Players without a team:");
        for (Player player : remainingPlayers) {
            System.out.println(player.getPlayerName());
        }
    }

    private static void addRemainingCoaches(FootballDBConnection dbConn, List<Coach> remainingCoaches) throws SQLException {
        System.out.println("Coaches without a team:");
        for (Coach coach : remainingCoaches) {
            System.out.println(coach.getCoachName());
        }
    }

    private static Coach createCoach(FootballDBConnection dbConn, String coachName, int index) throws SQLException {
        int experience = 5 + index;
        int age = 40 + index;
        Coach coach = new Coach(coachName, experience, age);
        coach.insertCoach(dbConn);
        return coach;
    }

    private static Player createPlayer(FootballDBConnection dbConn, String playerName, int shirtNumber) throws SQLException {
        int age = random.nextInt(16) + 18;
        int goals = 0;  // Initialize goals to 0
        Player player = new Player(playerName, age, shirtNumber);
        player.setGoals(goals);
        player.insertPlayer(dbConn);
        return player;
    }

    private static List<Stadium> createStadiums(FootballDBConnection dbConn) throws SQLException {
        List<Stadium> stadiums = new ArrayList<>();
        for (String stadiumName : STADIUM_NAMES) {
            int capacity = random.nextInt(40000) + 30000; // Random capacity between 30,000 and 70,000
            Stadium stadium = new Stadium(stadiumName, capacity);
            stadium.insertStadium(dbConn);
            stadiums.add(stadium);
        }
        return stadiums;
    }

    private static List<Referee> createReferees(FootballDBConnection dbConn) throws SQLException {
        List<Referee> referees = new ArrayList<>();
        for (String refereeName : REFEREE_NAMES) {
            int age = random.nextInt(20) + 35; // Age between 35 and 54
            int experience = random.nextInt(15) + 5; // Experience between 5 and 19 years
            Referee referee = new Referee(refereeName, age, experience);
            referee.insertReferee(dbConn);
            referees.add(referee);
        }
        return referees;
    }

    private static void simulateMatches(FootballDBConnection dbConn, List<Team> teams, List<Stadium> stadiums, List<Referee> referees) throws SQLException {
        for (int i = 0; i < 50; i++) {
            Team team1 = teams.get(random.nextInt(teams.size()));
            Team team2 = teams.get(random.nextInt(teams.size()));
            while (team2.getTeamId() == team1.getTeamId()) {
                team2 = teams.get(random.nextInt(teams.size()));
            }

            Referee referee = referees.get(random.nextInt(referees.size()));
            Stadium stadium = stadiums.get(random.nextInt(stadiums.size()));

            int team1Score = random.nextInt(5);
            int team2Score = random.nextInt(5);

            Match match = new Match(team1, team2, referee, stadium, team1Score, team2Score);
            match.insertMatch(dbConn);

            updateTeamStats(dbConn, team1, team2, team1Score, team2Score);
            updatePlayerGoals(dbConn, team1, team1Score);
            updatePlayerGoals(dbConn, team2, team2Score);
        }
    }

    private static void updateTeamStats(FootballDBConnection dbConn, Team team1, Team team2, int team1Score, int team2Score) throws SQLException {
        String sql = "UPDATE teams SET wins = wins + ?, losses = losses + ?, draws = draws + ? WHERE team_id = ?";
        try (PreparedStatement pstmt = dbConn.getConnection().prepareStatement(sql)) {
            // Update team1
            pstmt.setInt(1, team1Score > team2Score ? 1 : 0);
            pstmt.setInt(2, team1Score < team2Score ? 1 : 0);
            pstmt.setInt(3, team1Score == team2Score ? 1 : 0);
            pstmt.setInt(4, team1.getTeamId());
            pstmt.executeUpdate();

            // Update team2
            pstmt.setInt(1, team2Score > team1Score ? 1 : 0);
            pstmt.setInt(2, team2Score < team1Score ? 1 : 0);
            pstmt.setInt(3, team1Score == team2Score ? 1 : 0);
            pstmt.setInt(4, team2.getTeamId());
            pstmt.executeUpdate();
        }
    }

    private static void updatePlayerGoals(FootballDBConnection dbConn, Team team, int goals) throws SQLException {
        List<Player> scorers = new ArrayList<>(team.getPlayers());
        Collections.shuffle(scorers);
        String sql = "UPDATE players SET goals = goals + 1 WHERE player_id = ?";
        try (PreparedStatement pstmt = dbConn.getConnection().prepareStatement(sql)) {
            for (int i = 0; i < goals && i < scorers.size(); i++) {
                pstmt.setInt(1, scorers.get(i).getPlayerId());
                pstmt.executeUpdate();
            }
        }
    }
}