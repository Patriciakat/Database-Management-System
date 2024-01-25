package managers;

import java.sql.*;
import java.util.InputMismatchException;
import java.util.Scanner;

public class ShipmentManager {

    private static final Scanner scanner = new Scanner(System.in);

    public static void updateShipmentStatus(Connection dbConnection) {
        try {
            displayAllShipmentsInTransit(dbConnection);

            System.out.print("Enter the Shipment ID to update its status to 'Delivered': ");
            int shipmentId = scanner.nextInt();
            scanner.nextLine();

            if (!doesShipmentExist(dbConnection, shipmentId)) {
                System.out.println("Shipment with the specified ID does not exist.");
                return;
            }

            String updateSQL = "UPDATE Shipment SET delivery_status = 'Delivered' WHERE shipment_id = ? AND delivery_status = 'In Transit';";
            try (PreparedStatement pstmt = dbConnection.prepareStatement(updateSQL)) {
                pstmt.setInt(1, shipmentId);
                int affectedRows = pstmt.executeUpdate();

                if (affectedRows > 0) {
                    System.out.println("Shipment status updated successfully.");
                } else {
                    System.out.println("Failed to update shipment status. The shipment may not be in transit.");
                }
            }

        } catch (SQLException e) {
            System.out.println("An error occurred while updating the shipment status.");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Invalid input. Please enter a valid shipment ID.");
            scanner.nextLine();
        }
    }
    private static void displayAllShipmentsInTransit(Connection dbConnection) throws SQLException {
        String selectSQL = "SELECT shipment_id, order_nr, tracking_number, delivery_status FROM Shipment WHERE delivery_status = 'In Transit';";
        try (PreparedStatement pstmt = dbConnection.prepareStatement(selectSQL);
             ResultSet rs = pstmt.executeQuery()) {

            System.out.println("Shipment_ID | Order_NR | Tracking_Number | Delivery_Status");
            System.out.println("__________________________________________________________");
            while (rs.next()) {
                int shipmentId = rs.getInt("shipment_id");
                int orderNr = rs.getInt("order_nr");
                String trackingNumber = rs.getString("tracking_number");
                String deliveryStatus = rs.getString("delivery_status");
                System.out.println(shipmentId + "           | " + orderNr + "       | " + trackingNumber + "     | " + deliveryStatus);
            }
        }
    }
    private static boolean doesShipmentExist(Connection dbConnection, int shipmentId) throws SQLException {
        String existSQL = "SELECT COUNT(*) FROM Shipment WHERE shipment_id = ?;";
        try (PreparedStatement pstmt = dbConnection.prepareStatement(existSQL)) {
            pstmt.setInt(1, shipmentId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }
}