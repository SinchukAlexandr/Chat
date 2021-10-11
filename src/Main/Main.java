package Main;

import Server.Server;
import Client.Client;
import java.util.Scanner;

public class Main {
    public static final int PORT = 3636;

    public static void main(String[] args) {
        String clientName;
        String login;
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Press  S  to start server or  C  to start client.");
            char answer = Character.toLowerCase(scanner.nextLine().charAt(0));
            if (answer == 's') {
                new Server();
                System.out.println("Server stopped.");
                break;
            } else if (answer == 'c') {
                while (true) {
                    System.out.println("Enter you name:");
                    clientName = scanner.nextLine();
                    if (clientName.equalsIgnoreCase(""))
                        continue;
                    else break;
                }
                new Client(clientName);
                System.out.println("Client stopped.");
                break;
            } else
                System.out.println("Incorrect. Try again...");
        }
    }
}
