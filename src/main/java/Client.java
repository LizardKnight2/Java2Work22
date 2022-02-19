import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static String SERVER_ADDRESS = "localhost";
    public static int SERVER_PORT = 8080;
    public static String SHUTDOWN = "/end";
    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private Scanner scanner;

    public Client() throws IOException {
        scanner = new Scanner(System.in);
        openConnection();
    }

    private void openConnection() throws IOException {
        initializeNetwork();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        String messageFromServer = inputStream.readUTF();
                        System.out.println(messageFromServer);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        String text = scanner.nextLine();
                        if (text.equals(SHUTDOWN)) {
                            closeConnection();
                        } else {
                            sendMessage(text);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    private void initializeNetwork() throws IOException {
        socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
        inputStream = new DataInputStream(socket.getInputStream());
        outputStream = new DataOutputStream(socket.getOutputStream());

    }

    public void sendMessage(String message) {
        try {
            outputStream.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        System.exit(1);
    }

    public void closeConnection() throws IOException {
        inputStream.close();
        outputStream.close();
        socket.close();
    }


    public static void main(String[] args) throws IOException {
        new Client();
    }
}
