package managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;

public class PhoneManager {
    private static final Scanner scanner = new Scanner(System.in);

    public static void uploadPhone(Connection dbConnection) {
        String model = promptForPhoneModel();
        String color = promptForPhoneColor();
        double price = promptForPhonePrice();
        int storage = promptForPhoneStorage();

        if (model == null || color == null || Double.isNaN(price)) {
            System.out.println("Phone uploading cancelled.");
            return;
        }

        String insertPhoneSQL = "INSERT INTO Phone (model, price, color, storage) VALUES (?, ?, ?, ?);";

        try (PreparedStatement pstmt = dbConnection.prepareStatement(insertPhoneSQL)) {
            pstmt.setString(1, model);
            pstmt.setDouble(2, price);
            pstmt.setString(3, color);
            pstmt.setInt(4, storage);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("New phone uploaded successfully.");
            } else {
                System.out.println("New phone uploading failed.");
            }
        } catch (SQLException e) {
            System.out.println("Error occurred while uploading new phone.");
            e.printStackTrace();
        }

        System.out.println("Phone details:");
        System.out.println("Model: " + model);
        System.out.println("Color: " + color);
        System.out.println("Price: $" + price);
        System.out.println("Storage: " + storage + " GB");
    }

    private static String promptForPhoneModel() {
        System.out.print("Model: ");
        String model = scanner.nextLine().trim();

        return model;
    }

    private static String promptForPhoneColor() {
        System.out.print("Color: ");
        String color = scanner.nextLine().trim();

        if (color.matches(".*\\d.*")) {
            System.out.println("Invalid phone color. Color should not contain numbers.");
            return null;
        }
        return color;
    }

    private static double promptForPhonePrice() {
        System.out.print("Price: ");

        while (!scanner.hasNextDouble()) {
            System.out.println("Invalid input. Please enter a valid double for price.");
            scanner.next();
        }
        double price = scanner.nextDouble();
        scanner.nextLine();
        return price;
    }

    private static int promptForPhoneStorage() {
        System.out.print("Storage: ");
        while (!scanner.hasNextInt()) {
            System.out.println("Invalid input. Please enter a valid integer for storage.");
            scanner.next();
        }
        int storage = scanner.nextInt();
        scanner.nextLine();
        return storage;

    }
}