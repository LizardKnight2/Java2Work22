

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    public static String AUTHORIZATION = "/auth";
    private MyServer myServer;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private String name;

    public String getName() {
        return name;
    }

    public ClientHandler(MyServer myServer, Socket socket) {
        try {
            this.myServer = myServer;
            this.socket = socket;
            this.in = (new DataInputStream(socket.getInputStream()));
            this.out = (new DataOutputStream(socket.getOutputStream()));
            this.name = "";
            new Thread(() -> {
                try {
                    authentication();
                    readMessage();
                } catch (IOException exception) {
                    exception.printStackTrace();
                } finally {
                    closeConnection();
                }
            }).start();
        } catch (IOException exception) {
            throw new RuntimeException("Проблемы при создании обработчика клиента");
        }
    }

    public void authentication() throws IOException {
        while (true) {
            String str = in.readUTF();
            if (str.startsWith(AUTHORIZATION)) ;
            String[] parts = str.split("\\s");
            String nick =
                    myServer.getAuthService().getNickByLoginPass(parts[1], parts[2]);
            if (nick != null) {
                if (!myServer.isBusyNickName(nick)) {
                    sendMsg("/auth " + nick);
                    name = nick;
                    myServer.broadcastMsg(name + "зашел в чат");
                    myServer.subscribe(this);
                    return;
                } else {
                    sendMsg("Учетная запись же используется");

                }
            } else {
                sendMsg("Неверные логин или пароль");
            }

        }
    }

    public void readMessage() throws IOException {
        while (true) {
            String strFromClient = in.readUTF();
            System.out.println("от " + name + ": " + strFromClient);
            if (strFromClient.equals("/end")) {
                return;
            }
            if (strFromClient.startsWith("/w")) {
                String to = strFromClient.split(" ")[1];
                String msg = strFromClient.split(" ")[2];

            } else {
                myServer.broadcastMsg("( " + this.name + ") " + strFromClient);
            }
            out.flush();
            ;
            myServer.broadcastMsg(name + ": " + strFromClient);
        }
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public void closeConnection() {
        myServer.unsubscribe(this);
        myServer.broadcastMsg(name + "вышел из чата");
        try {
            in.close();

        } catch (IOException exception) {
            exception.printStackTrace();
        }
        try {
            out.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
