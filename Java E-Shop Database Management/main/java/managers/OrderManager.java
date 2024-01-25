package managers;

import java.sql.*;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.Set;

import java.text.SimpleDateFormat;

public class OrderManager {
    private static final Scanner scanner = new Scanner(System.in);

    public static void createNewOrder(Connection dbConnection) {
        int phoneId, quantity;
        String customerEmail;
        int newOrderId = -1;
        Set<Integer> selectedPhones = new HashSet<>();

        try {
            dbConnection.setAutoCommit(false); // Start transaction

            customerEmail = getValidCustomerEmail(dbConnection);

            String insertOrderQuery = "INSERT INTO orders (email, total_price, date, status) VALUES (?, 0, CURRENT_DATE, 'Paid') RETURNING order_nr";
            try (PreparedStatement pstmt = dbConnection.prepareStatement(insertOrderQuery)) {
                pstmt.setString(1, customerEmail);

                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    newOrderId = rs.getInt("order_nr");
                }
            }

            String userInput;
            do {
                if (selectedPhones.size() >= getTotalPhoneCount(dbConnection)) {
                    System.out.println("All available phones have been selected for the order.");
                    break;
                }

                phoneId = getValidPhoneId(dbConnection, selectedPhones);
                quantity = getValidQuantity();

                selectedPhones.add(phoneId);

                String insertOrderPhoneQuery = "INSERT INTO order_phone (order_nr, phone_id, quantity, status) VALUES (?, ?, ?, 'Paid')";
                try (PreparedStatement pstmt = dbConnection.prepareStatement(insertOrderPhoneQuery)) {
                    pstmt.setInt(1, newOrderId);
                    pstmt.setInt(2, phoneId);
                    pstmt.setInt(3, quantity);
                    pstmt.executeUpdate();
                }

                do {
                    System.out.print("Add another phone to the order? (y/n): ");
                    userInput = scanner.next().trim().toLowerCase();
                    if ("y".equals(userInput)) {
                        break;
                    } else if ("n".equals(userInput)) {
                        break;
                    } else {
                        System.out.println("Invalid input. Please enter 'y' for yes or 'n' for no.");
                    }
                } while (!"y".equals(userInput) && !"n".equals(userInput));

            } while ("y".equals(userInput));

            dbConnection.commit(); // Commit transaction if all operations are successful

            System.out.println("New order created successfully with Order ID: " + newOrderId);
        } catch (SQLException e) {
            System.out.println("Database error occurred. Rolling back transaction.");
            try {
                dbConnection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Rolling back transaction.");
            try {
                dbConnection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            scanner.next();
        } finally {
            try {
                dbConnection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static int getTotalPhoneCount(Connection dbConnection) throws SQLException {
        try (Statement stmt = dbConnection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS total FROM phone")) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        }
        return 0;
    }

    private static String getValidCustomerEmail(Connection dbConnection) throws SQLException {
        String customerEmail;
        Statement stmt = dbConnection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM customer");

        System.out.println("Available Customers:");
        System.out.println(String.format("%-11s | %-15s | %-30s | %-20s", "Name", "Surname", "Email", "Address"));
        System.out.println("_________________________________________________________________________________");
        while (rs.next()) {
            String name = rs.getString("name");
            String surname = rs.getString("surname");
            String email = rs.getString("email");
            String address = rs.getString("address");
            System.out.println(String.format("%-11s | %-15s | %-30s | %-20s", name, surname, email, address));
        }

        System.out.print("Enter a valid Customer Email: ");
        while (true) {
            try {
                customerEmail = scanner.next();
                if (isValidCustomerEmail(dbConnection, customerEmail)) {
                    return customerEmail;
                } else {
                    System.out.print("Invalid Customer Email. Please try again: ");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a valid email.");
                scanner.next();
            }
        }
    }
    private static boolean isValidCustomerEmail(Connection dbConnection, String customerEmail) throws SQLException {
        PreparedStatement pstmt = dbConnection.prepareStatement("SELECT COUNT(*) FROM customer WHERE email = ?");
        pstmt.setString(1, customerEmail);
        ResultSet rs = pstmt.executeQuery();
        rs.next();
        int count = rs.getInt(1);
        return count > 0;
    }

    private static int getValidPhoneId(Connection dbConnection, Set<Integer> selectedPhones) throws SQLException {
        int phoneId;
        Statement stmt = dbConnection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM phone");

        System.out.println("Available Phones:");
        System.out.println("Phone ID | Model              | Price      | Color    | Storage (GB)");
        System.out.println("-------------------------------------------------------------------");
        while (rs.next()) {
            phoneId = rs.getInt("phone_id");
            if (!selectedPhones.contains(phoneId)) {
                int storage = rs.getInt("storage");
                double price = rs.getDouble("price");
                String color = rs.getString("color");
                String model = rs.getString("model");
                System.out.println(String.format("%-8s | %-18s | %-10s | %-8s | %-5s", phoneId, model, price, color, storage));
            }
        }

        System.out.print("Enter a valid Phone ID: ");
        while (true) {
            try {
                phoneId = scanner.nextInt();
                if (!selectedPhones.contains(phoneId) && isValidPhone(dbConnection, phoneId)) {
                    return phoneId;
                } else {
                    System.out.print("Invalid or already selected Phone ID. Please try again: ");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.next();
            }
        }
    }

    private static boolean isValidPhone(Connection dbConnection, int phoneId) throws SQLException {
        PreparedStatement pstmt = dbConnection.prepareStatement("SELECT COUNT(*) FROM phone WHERE phone_id = ?");
        pstmt.setInt(1, phoneId);
        ResultSet rs = pstmt.executeQuery();
        rs.next();
        int count = rs.getInt(1);
        return count > 0;
    }

    private static int getValidQuantity() {
        int quantity;
        System.out.print("Enter the quantity: ");
        while (true) {
            try {
                quantity = scanner.nextInt();
                if (quantity > 0) {
                    return quantity;
                } else {
                    System.out.print("Invalid quantity. The number must be greater than 0. Please try again: ");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a valid integer.");
                scanner.next();
            }
        }
    }

    public static void displayOrdersForCustomer(Connection dbConnection) {

        try {
            Statement stmt = dbConnection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM customer");

            System.out.println(String.format("%-15s | %-15s | %-30s | %-20s", "Name", "Surname", "Email", "Address"));
            System.out.println("____________________________________________________________________________________");
            while (rs.next()) {
                String name = rs.getString("name");
                String surname = rs.getString("surname");
                String email = rs.getString("email");
                String address = rs.getString("address");
                System.out.println(String.format("%-15s | %-15s | %-30s | %-20s", name, surname, email, address));
            }
            System.out.print("Enter the customer's email: ");
            String customerEmail = scanner.next().trim();

            if (!isValidCustomerEmail(dbConnection, customerEmail)) {
                System.out.println("Invalid customer email.");
                return;
            }

            String query = "SELECT * FROM orders WHERE email = ?";
            try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
                pstmt.setString(1, customerEmail);
                ResultSet rss = pstmt.executeQuery();

                System.out.println("Orders for Customer Email " + customerEmail + ":");
                System.out.println(String.format("%-8s | %-11s | %-11s | %-12s", "Order Nr", "Total Price", "Date", "Status"));
                System.out.println("_________________________________________________");
                while (rss.next()) {

                    int order_nr = rss.getInt("order_nr");
                    double total_price = rss.getDouble("total_price");
                    java.sql.Date date = rss.getDate("date");
                    String status = rss.getString("status");
                    System.out.println(String.format("%-8s | $ %-9s | %-11s | %-12s", order_nr, total_price, date, status));
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error occurred.");
            e.printStackTrace();
        }
    }
}
