import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.HashMap;

public class ServerHost {
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_GREEN = "\u001B[32m";
    private static Map<String,SingleClient> clients;
    private static Runnable broadcastingRunnable;
    private static volatile String broadcastingMessage;
    //volatile is JAVA keyword used for threads to make a member attribute to be shared among all threads
    // even when it's changed it will be changed everywhere (all threads)
    private final Runnable serverListening;
    private volatile String command = "";
    private Integer port;
    private ServerSocket serverSocket;

    ServerHost(Integer port) {
        clients = new HashMap<String,SingleClient>();
        this.port = port;
        ServerHost.broadcastingMessage = "";
        this.serverListening = new Runnable() {
            @Override
            public void run() {
                try {
                    serverSocket = new ServerSocket(port);
                    while (!getCommand().equals("EXIT")) {
                        try {
                            //TODO auth + encryption + decryption
//                            serverSocket.setSoTimeout(5000);
                            Socket clientSocket = serverSocket.accept();
                            if (clients.isEmpty())
                                Main.setLoadingThreadLoop(false);
                            addClient(clientSocket);
                        } catch (IOException serverIOE) {
                            System.out.println(ANSI_RED + "IOE ERROR IN THREAD (serverListening) "
                                    + serverIOE + " RETRYING");
                        }
                    }
                    for (String key : clients.keySet()) {
                        SingleClient singleClient = clients.get(key);
                        singleClient.endConnection();
                    }
                    serverSocket.close();
                    Main.setLoadingThreadLoop(false);
                } catch (IOException serverIOE) {
                    System.out.println(ANSI_RED + "IOE ERROR IN THREAD (serverListening)"
                            + " " + serverIOE);
                }
            }
        };
        ServerHost.broadcastingRunnable = new Runnable() {
            @Override
            public void run() {
                for (String key : clients.keySet()) {
                    SingleClient singleClient = clients.get(key);
                    Thread clientThread = new Thread(singleClient.getSendToClient());
                    clientThread.start();
                    try{
                        clientThread.join();
                    }catch (InterruptedException interruptedException){
                        System.out.println(ANSI_RED+interruptedException+ANSI_BLUE);
                    }
                }
                ServerHost.setBroadcastingMessage("");
            }
        };
        Thread serverThread = new Thread(this.getServerListening());
        serverThread.start();
        System.out.println(ANSI_BLUE+"SERVER IS NOW LISTENING ON PORT "+this.getPort()+ANSI_BLUE);
        Thread loadingThread = new Thread(Main.getLoadingRunnable());
        loadingThread.start();
    }

    public static void broadcast(String message) {
        Thread broadcastingThread = new Thread(ServerHost.getBroadcastingRunnable());
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
        clients.put(clientSocket.getInetAddress().toString(),new SingleClient(clientSocket));
        System.out.println(ANSI_BLUE+"Current server size : "+clients.size()+ANSI_BLUE);
        System.out.println(ANSI_BLUE+clientSocket.getInetAddress()+" HAS JOINED THE SERVER "+ANSI_BLUE);
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

    public static Runnable getBroadcastingRunnable() {
        return broadcastingRunnable;
    }

    public Runnable getServerListening() {
        return serverListening;
    }
}
