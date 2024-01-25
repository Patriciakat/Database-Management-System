import db.DatabaseConnection;
import ui.Menu;

import java.sql.Connection;

public class Main {
    public static void main(String[] args) {
        Connection connection = null;
        try {
            connection = DatabaseConnection.connect();
            Menu menu = new Menu();
            int choice;
            boolean exit = false;
            do {
                choice = menu.displayMenu();
                exit = menu.handleUserChoice(choice, connection);
            } while (!exit);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseConnection.disconnect(connection);
        }
    }
}
