package dbtest2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Dbtest2 {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Connection con = null;

        try {
            // Connect to the database
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Attempting to connect to the database...");
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/ClientRentalDB", "root", "Houssat_99");
            System.out.println("Connection to the database was successful!");

            // Menu system
            boolean running = true;

            while (running) {
                System.out.println("\n--- MENU SYSTEM ---");
                System.out.println("1. List all properties rented by a specific client");
                System.out.println("2. Find clients renting properties within a date range");
                System.out.println("3. List properties rented by clients whose names start with a specific letter");
                System.out.println("4. Find clients paying above a certain rent amount");
                System.out.println("5. Quit");
                System.out.print("Enter your choice: ");

                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline character

                switch (choice) {
                    case 1:
                        listPropertiesBySpecificClient(con, scanner);
                        break;

                    case 2:
                        findClientsByDateRange(con, scanner);
                        break;

                    case 3:
                        listPropertiesByClientInitial(con, scanner);
                        break;

                    case 4:
                        findClientsByRentAmount(con, scanner);
                        break;

                    case 5:
                        System.out.println("Exiting the program. Goodbye!");
                        running = false;
                        break;

                    default:
                        System.out.println("Invalid choice. Please try again.");
                        break;
                }
            }

        } catch (SQLException e) {
            System.out.println("SQL Error --> " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.out.println("Class error --> " + e.getMessage());
        } finally {
            // Close resources
            if (con != null) {
                try {
                    con.close();
                    System.out.println("Database connection closed.");
                } catch (SQLException e) {
                    System.out.println("Error closing the database connection: " + e.getMessage());
                }
            }
            scanner.close();
        }
    }

    // Query 1: List all properties rented by a specific client
    private static void listPropertiesBySpecificClient(Connection con, Scanner scanner) {
        System.out.print("Enter the client's name or part of their name: ");
        String clientName = scanner.nextLine();

        String sql = "SELECT p.property_address FROM Rentals r " +
                     "JOIN Clients c ON r.client_no = c.client_no " +
                     "JOIN Properties p ON r.property_no = p.property_no " +
                     "WHERE c.client_name LIKE ?";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, "%" + clientName + "%");
            ResultSet rs = stmt.executeQuery();
            System.out.println("\nProperties rented by clients matching \"" + clientName + "\":");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.println("Property: " + rs.getString("property_address"));
            }
            if (!found) {
                System.out.println("No properties found for the given client.");
            }
        } catch (SQLException e) {
            System.out.println("SQL Error --> " + e.getMessage());
        }
    }

    // Query 2: Find clients renting properties within a date range
    private static void findClientsByDateRange(Connection con, Scanner scanner) {
        System.out.print("Enter start date (YYYY-MM-DD): ");
        String startDate = scanner.nextLine();
        System.out.print("Enter end date (YYYY-MM-DD): ");
        String endDate = scanner.nextLine();

        String sql = "SELECT DISTINCT c.client_name FROM Rentals r " +
                     "JOIN Clients c ON r.client_no = c.client_no " +
                     "WHERE r.rent_start >= ? AND (r.rent_finish <= ? OR r.rent_finish IS NULL)";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, startDate);
            stmt.setString(2, endDate);
            ResultSet rs = stmt.executeQuery();
            System.out.println("\nClients renting properties between " + startDate + " and " + endDate + ":");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.println("Client: " + rs.getString("client_name"));
            }
            if (!found) {
                System.out.println("No clients found for the given date range.");
            }
        } catch (SQLException e) {
            System.out.println("SQL Error --> " + e.getMessage());
        }
    }

    // Query 3: List properties rented by clients whose names start with a specific letter
    private static void listPropertiesByClientInitial(Connection con, Scanner scanner) {
        System.out.print("Enter the starting letter of the client's name: ");
        String initial = scanner.nextLine();

        String sql = "SELECT p.property_address FROM Rentals r " +
                     "JOIN Clients c ON r.client_no = c.client_no " +
                     "JOIN Properties p ON r.property_no = p.property_no " +
                     "WHERE c.client_name LIKE ?";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, initial + "%");
            ResultSet rs = stmt.executeQuery();
            System.out.println("\nProperties rented by clients whose names start with \"" + initial + "\":");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.println("Property: " + rs.getString("property_address"));
            }
            if (!found) {
                System.out.println("No properties found for clients with the given initial.");
            }
        } catch (SQLException e) {
            System.out.println("SQL Error --> " + e.getMessage());
        }
    }

    // Query 4: Find clients paying above a certain rent amount
    private static void findClientsByRentAmount(Connection con, Scanner scanner) {
        System.out.print("Enter the minimum rent amount: ");
        double rentAmount = scanner.nextDouble();

        String sql = "SELECT DISTINCT c.client_name FROM Rentals r " +
                     "JOIN Clients c ON r.client_no = c.client_no " +
                     "WHERE r.monthly_rent > ?";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setDouble(1, rentAmount);
            ResultSet rs = stmt.executeQuery();
            System.out.println("\nClients paying above " + rentAmount + ":");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.println("Client: " + rs.getString("client_name"));
            }
            if (!found) {
                System.out.println("No clients found paying above the given rent amount.");
            }
        } catch (SQLException e) {
            System.out.println("SQL Error --> " + e.getMessage());
        }
    }
}
