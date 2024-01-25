package managers;

import java.sql.*;
import java.util.InputMismatchException;
import java.util.Scanner;

public class CustomerManager {
    private static final Scanner scanner = new Scanner(System.in);

    public static void removeCustomer(Connection dbConnection) {
        try {
            String checkQuery = "SELECT email, name, surname, address FROM customer "
                    + "WHERE NOT EXISTS (SELECT 1 FROM orders WHERE email = customer.email)";
            Statement stmt = dbConnection.createStatement();
            ResultSet rs = stmt.executeQuery(checkQuery);

            if (!rs.isBeforeFirst()) {
                System.out.println("No customers are available for deletion (all have placed at least one order).");
                return;
            }
            System.out.println(String.format("%-15s | %-15s | %-30s | %-20s", "Name", "Surname", "Email", "Address"));
            System.out.println("____________________________________________________________________________________");
            while (rs.next()) {
                String name = rs.getString("name");
                String surname = rs.getString("surname");
                String email = rs.getString("email");
                String address = rs.getString("address");
                System.out.println(String.format("%-15s | %-15s | %-30s | %-20s", name, surname, email, address));
            }


            System.out.print("Enter the Customer Email to delete: ");
            while (true) {
                try {
                    String customerEmail = scanner.next();
                    String deleteQuery = "DELETE FROM customer WHERE email = ? AND NOT EXISTS "
                            + "(SELECT 1 FROM orders WHERE email = customer.email)";

                    try (PreparedStatement pstmt = dbConnection.prepareStatement(deleteQuery)) {
                        pstmt.setString(1, customerEmail);

                        int affectedRows = pstmt.executeUpdate();
                        if (affectedRows > 0) {
                            System.out.println("Customer deleted successfully.");
                        } else {
                            System.out.println("Failed to delete customer. They may have placed an order or do not exist.");
                        }
                    }
                    break;
                } catch (SQLException e) {
                    System.out.println("Database error occurred.");
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error occurred.");
            e.printStackTrace();
        }
    }
}
