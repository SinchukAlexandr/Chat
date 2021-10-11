package Server;

import Client.Client;
import Main.Main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {
    private List<Connection> connections = Collections.synchronizedList(new ArrayList<>());
    private Socket socket;
    private ServerSocket serverSocket;
    private static final String EXIT = "exit";

    public Server() {
        try {
            class FakeClient extends Thread {
                @Override
                public void run() {
                    new Client("FakeClient");
                }
            }
            FakeClient fakeClient = new FakeClient();
            fakeClient.start();
            serverSocket = new ServerSocket(Main.PORT, 20);
            while (true) {
                System.out.println("Server is wait to connection...");
                socket = serverSocket.accept();
                System.out.println("connect: " + socket.toString());
                Connection connection = new Connection(socket);
                connections.add(connection);
                connection.start();
                numberOfCurrentConnections(connections);
            }
        } catch (IOException e) {
            System.out.println("Server stopped.");
//        e.printStackTrace();
        } finally {
            closeAll();
        }
    }

    public int numberOfCurrentConnections(List<Connection> connection) {
        System.out.println("Number of current connections: " + connection.size());
        return connection.size();
    }

    public boolean clientExitCheck(String param, List<Connection> connections, Connection connection) {
        if (param.equalsIgnoreCase(EXIT)) {
            System.out.println("Client  are disconnected.");
            connections.remove(connection);
            System.out.println("Remove current connection");
            System.out.println("Number of current connections: " + connections.size());
            if (connections.size() == 0) {
                System.out.println("Stopped receiving incoming all messages to the server.");
                return true;
            }
            return true;
        }
        return false;
    }

    public void showAllNewClient(String name, boolean show) {
        while (!show) {
            if ((connections.get(connections.size() - 1)).getNameWho().equalsIgnoreCase(name)) {
                for (Connection connection : connections) {
                    if (connection.getNameWho().equalsIgnoreCase(name)) continue;
                    connection.output.println("--->");
                    connection.output.println(" ");
                    connection.output.println("Client " + name + " joined to chat.");
                    show = true;
                }
            }
        }
    }

    public void sendMessageToClient(String nameWho, String nameWhose, String message) {
        boolean send = false;
        for (Connection connection : connections) {
            if (connection.nameWho.equals(nameWhose)) {
                connection.output.println(nameWho);
                connection.output.println(nameWhose);
                connection.output.println(message);
                System.out.println("Message " + message + " sent from " + nameWho + " to client  " + nameWhose);
                send = true;
                break;
            }
        }
        if (!send) {
            System.out.println("Client " + nameWhose + "  no connected. Message not sent.");
            for (Connection connection : connections) {
                if (connection.nameWho.equals(nameWho)) {
                    connection.output.println("--->");
                    connection.output.println(" ");
                    connection.output.println("Client " + nameWhose + "  no connected to chat. Message not sent.");
                    break;
                }
            }
        }
    }

    public void sendMessageToAllClients(String message, String nameWho, String nameWhose) {
        boolean send = false;
        for (Connection connection : connections) {
            if (connection.nameWho.equalsIgnoreCase(nameWho)) continue;
            connection.output.println(nameWho);
            connection.output.println(nameWhose);
            connection.output.println(message);
            send = true;
            System.out.println("Message " + message + " sent from " + nameWho + " to " + nameWhose + " clients.");
        }
        if (!send) {
            System.out.println(nameWhose + " clients no connected. Message not sent.");
            for (Connection connection : connections) {
                if (connection.nameWho.equals(nameWho)) {
                    connection.output.println("--->");
                    connection.output.println(" ");
                    connection.output.println(nameWhose + " client no connected. Message not sent.");
                    break;
                }
            }
        }
    }

    public void closeAll() {
        try {
            serverSocket.close();
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    private class Connection extends Thread {
        private Socket socket;
        private BufferedReader input;
        private PrintWriter output;
        private String nameWho;
        private String nameWhose;
        private String message;
        private boolean showNewClient = false;

        public String getNameWho() {
            return this.nameWho;
        }

        public Connection(Socket socket) {
            try {
                this.socket = socket;
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output = new PrintWriter(socket.getOutputStream(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                while (true) {
                    nameWho = input.readLine();
                    System.out.println(nameWho);
                    if (clientExitCheck(nameWho, connections, this)) break;
//                    showAllNewClient(nameWho, this.showNewClient);

                    nameWhose = input.readLine();
                    System.out.println(nameWhose);
                    if (clientExitCheck(nameWhose, connections, this)) break;

                    message = input.readLine();
                    System.out.println(message);
                    if (clientExitCheck(message, connections, this)) break;

                    System.out.println("author " + nameWho + " to client " + nameWhose + " write : " + message);

                    if (nameWhose.equalsIgnoreCase("all")) {
                        sendMessageToAllClients(message, nameWho, nameWhose);
                    } else {
                        sendMessageToClient(nameWho, nameWhose, message);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                close();
            }
        }

        public void close() {
            try {
                input.close();
                output.close();
                socket.close();
                connections.remove(this);
                if (connections.size() == 1) {
                    serverSocket.close();
                }
            } catch (Exception e) {
                System.err.println(e);
            }
        }
    }
}


