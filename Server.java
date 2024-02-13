import java.io.*;
import java.net.*;
import java.lang.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Server {
    private static List<PrintWriter> clientWriters = new ArrayList<>();

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(12345);
            System.out.println("Server started. Waiting for clients...");

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Client connected: " + socket);

                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                clientWriters.add(out);

                Thread thread = new Thread(new ClientHandler(socket, out));
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket, PrintWriter out) {
            this.socket = socket;
            this.out = out;
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            try {
                String clientName = in.readLine();
                System.out.println(clientName + " joined the chat.");

                out.println("Welcome to the chat, " + clientName + "!");

                String input;
                while ((input = in.readLine()) != null) {
                    System.out.println(clientName + " (Encrypted): " + input);

                    // Prompt the server for the key
                    System.out.print("Enter decryption key: ");
                    Scanner scanner = new Scanner(System.in);
                    int key = scanner.nextInt();
                    scanner.nextLine(); // Consume the newline character left in the input buffer

                    String decryptedMessage = caesarDecrypt(input, key);
                    System.out.println(clientName + " (Decrypted): " + decryptedMessage);

                    String encryptedMessage = caesarEncrypt(decryptedMessage, key);
                    sendToAllClients(clientName + ": " + encryptedMessage);
                }

                System.out.println(clientName + " left the chat.");
                socket.close();
                clientWriters.remove(out);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void sendToAllClients(String message) {
        for (PrintWriter writer : clientWriters) {
            writer.println(message);
        }
    }

    private static String caesarEncrypt(String plain, int key) {
        StringBuilder result = new StringBuilder();
        for (char c : plain.toCharArray()) {
            if (Character.isLetter(c)) {
                char base = Character.isUpperCase(c) ? 'A' : 'a';
                c = (char) (((c - base + key) % 26) + base);
            }
            result.append(c);
        }
        return result.toString();
    }

    private static String caesarDecrypt(String cipher, int key) {
        return caesarEncrypt(cipher, 26 - key);
    }
}
