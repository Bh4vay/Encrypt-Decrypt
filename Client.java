import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 12345);
             PrintWriter serverOut = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Scanner scanner = new Scanner(System.in)) {

            System.out.print("Enter your name: ");
            String name = scanner.nextLine();
            serverOut.println(name);

            String serverMessage = serverIn.readLine();
            System.out.println(serverMessage);

            Thread receiveThread = new Thread(new ReceiveMessage(serverIn));
            receiveThread.start();

            while (true) {
                System.out.print("Enter message: ");
                String message = scanner.nextLine();

                int key = 3; // Example Caesar Cipher key
                String encryptedMessage = caesarEncrypt(message, key);
                serverOut.println(encryptedMessage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ReceiveMessage implements Runnable {
        private BufferedReader serverIn;

        public ReceiveMessage(BufferedReader serverIn) {
            this.serverIn = serverIn;
        }

        public void run() {
            try {
                String message;
                while ((message = serverIn.readLine()) != null) {
                    int key = Integer.parseInt(serverIn.readLine()); // Read decryption key
                    String decryptedMessage = caesarDecrypt(message, key);

                    if (decryptedMessage.equalsIgnoreCase("!exit")) {
                        System.out.println("You have left the chat.");
                        System.exit(0);
                    }

                    System.out.println(decryptedMessage);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
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
