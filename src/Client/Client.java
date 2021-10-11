package Client;

import Main.Main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class Client {
    private static final String EXIT = "exit";
    private BufferedReader inputClient;
    private PrintWriter outputClient;
    private String OutClientName;
    private String messageOut;

    public Client(String myName) {
        Scanner scanner = new Scanner(System.in);
//        System.out.println("Enter IP address in format XXX.XXX.XXX.XXX for connect to server.");
//        String ip = scanner.nextLine();
        try (Socket socket = new Socket("localhost", Main.PORT)) {

            inputClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outputClient = new PrintWriter(socket.getOutputStream(), true);

//--------------------------INPUT--------------------------------------------------
            ReceiveMessage receiveMessage = new ReceiveMessage();
            receiveMessage.start();
//--------------------------OUTPUT--------------------------------------------------

            while (true) {
                if (myName.equalsIgnoreCase(EXIT)) {
                    receiveMessage.setStop();
                    receiveMessage.interrupt();
                    outputClient.println(myName);
                    break;
                }
                outputClient.println(myName);

                System.out.println("Enter destination name: ");
                OutClientName = scanner.nextLine();
                if (OutClientName.equalsIgnoreCase(EXIT)) {
                    receiveMessage.setStop();
                    receiveMessage.interrupt();
                    outputClient.println(OutClientName);
                    break;
                }
                int count = 0;
                while (true) {
                    count++;
                    System.out.println("Enter you message: ");
                    messageOut = scanner.nextLine();
                    if (messageOut.equalsIgnoreCase("change") | messageOut.equalsIgnoreCase(EXIT)) {
                        break;
                    }
                    if (count >= 2) {
                        outputClient.println(myName);
                        outputClient.println(OutClientName);
                        outputClient.println(messageOut);
                    } else {
                        outputClient.println(OutClientName);
                        outputClient.println(messageOut);
                    }
                }
                if (messageOut.equalsIgnoreCase(EXIT)) {
                    receiveMessage.setStop();
                    receiveMessage.interrupt();
                    outputClient.println(messageOut);
                    break;
                }
            }
            receiveMessage.join();
            System.out.println("Disconnect.Bye-bye!");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Server is not connected. Try later. catch");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            close();
//            System.out.println("######################## finally");
        }
    }

    private class ReceiveMessage extends Thread {
        public boolean stopped = true;
        private String nameIn;
        private String messageIn;
        private String MyNameIn;

        public void setStop() {
            this.stopped = false;
        }

        @Override
        public void run() {
            try {
                while (!(Thread.currentThread().isInterrupted())) {

                    nameIn = inputClient.readLine();
                    if (Thread.currentThread().isInterrupted()) break;

                    MyNameIn = inputClient.readLine();

                    messageIn = inputClient.readLine();

                    Date date = new Date();
                    SimpleDateFormat formatForDateNow = new SimpleDateFormat("dd.MM  HH:mm");
                    System.out.println("\t\t\t\t" + " (" + formatForDateNow.format(date) + ") " + nameIn + " : " + messageIn);
                }
                System.out.println("Receive Message from server stopped.");
            } catch (IOException e) {
                System.err.println("Ошибка при получении сообщения. catch ");
                e.printStackTrace();
            }
        }
    }

    private void close() {
        try {
            inputClient.close();
            outputClient.close();
        } catch (Exception e) {
//            System.out.println("in close catch " + Thread.currentThread().getName());
            System.err.println(e);

        }
    }
}



