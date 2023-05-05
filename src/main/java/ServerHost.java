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
    private  static  Runnable broadcastingRunnable;
    private  final Runnable addClientRunnable;
    private static volatile Socket clientSocket;
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
        ServerHost.setBroadcastingMessage("");
        this.serverListening = new Runnable() {
            @Override
            public void run() {
                try {
                    serverSocket = new ServerSocket(port);
                    while (!getCommand().equals("EXIT")) {
                        try {
                            //TODO auth + encryption + decryption
//                            serverSocket.setSoTimeout(5000);
                             ServerHost.setClientSocket(serverSocket.accept());
                            if (clients.isEmpty())
                                Main.setLoadingThreadLoop(false);
                            Thread addClientThread = new Thread(addClientRunnable);
                            addClientThread.setName("add-client-thread");
                            addClientThread.start();
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
                    clientThread.setName("client-send-thread-when-broadcasting");
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
        this.addClientRunnable = new Runnable() {
            @Override
            public void run() {
                ServerHost.addClient(ServerHost.getClientSocket());
            }
        };
        Thread serverThread = new Thread(this.getServerListening());
        serverThread.setName("server-thread-to-listen");
        serverThread.start();
        System.out.println(ANSI_BLUE+"SERVER IS NOW LISTENING ON PORT "+this.getPort()+ANSI_BLUE);
        Thread loadingThread = new Thread(Main.getLoadingRunnable());
        loadingThread.setName("loading-symbols");
        loadingThread.start();
    }

    public static void broadcast(String message) {
        Thread broadcastingThread = new Thread(ServerHost.getBroadcastingRunnable());
        broadcastingThread.setName("broadcasting-thread");
        ServerHost.setBroadcastingMessage(message);
        broadcastingThread.start();
    }

    public synchronized static String getBroadcastingMessage() {
        return broadcastingMessage;
    }

    public static void setBroadcastingMessage(String broadcastingMessage) {
        ServerHost.broadcastingMessage = broadcastingMessage;
    }
    public static void endConnection(Socket clientSocket,User user){
        clients.remove(clientSocket.getInetAddress().toString());
        System.out.println(ANSI_BLUE+ user.getNickname()+
                " LEFT THE SERVER"+ANSI_BLUE);
        System.out.println(ANSI_BLUE+"Current server size : "+clients.size()+ANSI_BLUE);
        System.out.println(ANSI_PURPLE +
                "_____ _____ _____ _____ _____ _____ _____ _____ _____ \n" +
                "\\____\\\\____\\\\____\\\\____\\\\____\\\\____\\\\____\\\\____\\\\____\\\n");
    }
    public static void addClient(Socket clientSocket) {
        System.out.println(ANSI_PURPLE +
                "_____ _____ _____ _____ _____ _____ _____ _____ _____ \n" +
                "\\____\\\\____\\\\____\\\\____\\\\____\\\\____\\\\____\\\\____\\\\____\\\n");
        SingleClient singleClient = new SingleClient();
        clients.put(clientSocket.getInetAddress().toString(),singleClient);
        System.out.println(ANSI_BLUE+"Current server size : "+clients.size()+ANSI_BLUE);
        singleClient = new SingleClient(clientSocket,true);

    }

    public synchronized String getCommand() {
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

    public synchronized static Socket getClientSocket() {
        return clientSocket;
    }

    public static void setClientSocket(Socket clientSocket) {
        ServerHost.clientSocket = clientSocket;
    }
}
