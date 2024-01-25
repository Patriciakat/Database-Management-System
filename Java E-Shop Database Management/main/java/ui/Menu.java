package ui;

import managers.OrderManager;
import managers.CustomerManager;
import managers.PhoneManager;
import managers.ShipmentManager;

import java.sql.Connection;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Menu {
    private final Scanner scanner = new Scanner(System.in);

    public int displayMenu() {
        while (true) {
            System.out.println("1. Upload a new phone");
            System.out.println("2. Update Shipment Status");
            System.out.println("3. Remove Customer");
            System.out.println("4. Create a New Order");
            System.out.println("5. Search Orders by Customer");
            System.out.println("6. Exit");
            System.out.print("Enter choice: ");

            try {
                return scanner.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("Invalid input, please enter a number.");
                scanner.nextLine();
            }
        }
    }

    public boolean handleUserChoice(int choice, Connection dbConnection) {
        switch (choice) {
            case 1:
                PhoneManager.uploadPhone(dbConnection);
                break;
            case 2:
                ShipmentManager.updateShipmentStatus(dbConnection);
                break;
            case 3:
                CustomerManager.removeCustomer(dbConnection);
                break;
            case 4:
                OrderManager.createNewOrder(dbConnection);
                break;
            case 5:
                OrderManager.displayOrdersForCustomer(dbConnection);
                break;
            case 6:
                System.out.println("Exiting program.");
                return true;
            default:
                System.out.println("Invalid option, please try again.");
                break;
        }
        return false;
    }
}