import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ServerHost {
    public static final String ANSI_RED = "\u001B[31m";
    private static ArrayList<SingleClient> clients;
    private static Thread broadcastingThread;
    private static volatile String broadcastingMessage;
    //volatile is JAVA keyword used for threads to make a member attribute to be shared among all threads
    // even when it's changed it will be changed everywhere (all threads)
    private final Thread serverListening;
    private volatile String command = "";
    private Integer port;
    private ServerSocket serverSocket;

    ServerHost(Integer port) {
        clients = new ArrayList<SingleClient>();
        this.port = port;
        ServerHost.broadcastingMessage = "";
        serverListening = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    serverSocket = new ServerSocket(port);
                    while (!command.equals("EXIT")) {
                        try {
                            //TODO auth + encryption + decryption
                            serverSocket.setSoTimeout(5000);
                            Socket clientSocket = serverSocket.accept();
                            if (clients.isEmpty())
                                Main.setKeepRunning(false);
                            addClient(clientSocket);
                        } catch (IOException serverIOE) {
                            System.out.println(ANSI_RED + "IOE ERROR IN THREAD (serverListening) WITH ID"
                                    + serverListening.getId() + " " + serverIOE + " RETRYING");
                        }
                    }
                    for (SingleClient singleClient : clients) {
                        singleClient.endConnection();
                    }
                    serverSocket.close();
                    Main.setKeepRunning(false);
                } catch (IOException serverIOE) {
                    System.out.println(ANSI_RED + "IOE ERROR IN THREAD (serverListening) WITH ID"
                            + serverListening.getId() + " " + serverIOE);
                }
            }
        });
        broadcastingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                SingleClient.setMessageToSend(ServerHost.getBroadcastingMessage());
                for (SingleClient singleClient : clients) {
                    singleClient.getSendToClient().start();
                }
                SingleClient.setMessageToSend("");
            }
        });
        serverListening.start();
    }

    public static void broadcast(String message) {
        broadcastingThread.start();
        ServerHost.setBroadcastingMessage(message);
    }

    public static String getBroadcastingMessage() {
        return broadcastingMessage;
    }

    public static void setBroadcastingMessage(String broadcastingMessage) {
        ServerHost.broadcastingMessage = broadcastingMessage;
    }

    public void addClient(Socket clientSocket) {
        clients.add(new SingleClient(clientSocket));
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }
}
