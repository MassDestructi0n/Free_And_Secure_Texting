import org.fusesource.jansi.Ansi;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.HashMap;


public class ServerHost {
    private static Map<String,SingleClient> clients;
    private  static  Runnable broadcastingRunnable;
    private  final Runnable addClientRunnable;
    private static volatile Socket clientSocket;

    private static volatile String broadcastingMessage = "";
    //volatile is JAVA keyword used for threads to make a member attribute to be shared among all threads
    // even when it's changed it will be changed everywhere (all threads)
    private final Runnable serverListening;
    private volatile String command = "";
    private Integer port;
    private ServerSocket serverSocket;

    ServerHost(Integer port) {
        clients = new HashMap<String,SingleClient>();
        this.port = port;
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
                            System.out.println(Ansi.ansi().fg(Ansi.Color.RED).a("IOE ERROR IN THREAD (serverListening) "
                                    + serverIOE + " RETRYING").reset());
                        }
                    }
                    for (String key : clients.keySet()) {
                        SingleClient singleClient = clients.get(key);
                        singleClient.endConnection();
                    }
                    serverSocket.close();
                    Main.setLoadingThreadLoop(false);
                } catch (IOException serverIOE) {
                    System.out.println(Ansi.ansi().fg(Ansi.Color.RED).a("IOE ERROR IN THREAD (serverListening)"
                            + " " + serverIOE).reset());
                    System.exit(500);
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
                        System.out.println(Ansi.ansi().fg(Ansi.Color.RED).a(
                                interruptedException).reset());
                    }
                }
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
        System.out.println(Ansi.ansi().fg(Ansi.Color.BLUE).a(
                "SERVER IS NOW LISTENING ON PORT "+this.getPort()).reset());
        Thread loadingThread = new Thread(Main.getLoadingRunnable());
        loadingThread.setName("loading-symbols");
        loadingThread.start();
    }

    public static void broadcast() {
        Thread broadcastingThread = new Thread(ServerHost.getBroadcastingRunnable());
        broadcastingThread.setName("broadcasting-thread");
        broadcastingThread.start();
    }

    public synchronized static String getBroadcastingMessage() {
        return broadcastingMessage;
    }

    public static void setBroadcastingMessage(String broadcastingMessage) {
        if(broadcastingMessage.equals(""))
            ServerHost.broadcastingMessage = "";
        else{
            try {
                ServerHost.broadcastingMessage = Main.encrypt(broadcastingMessage,
                        Main.stringToKey(Main.getUser().getSecretKey()))+'\n';
            }catch (Exception e){
                System.out.println(Ansi.ansi().fg(Ansi.Color.RED).a("ERROR WHILE ENCRYPTING THE MESSAGE"+e));
            }
        }
    }

    public static void endConnection(Socket clientSocket,User user){
        clients.remove(user.getNickname()+clientSocket.getInetAddress().toString());
        System.out.println(Ansi.ansi().fg(Ansi.Color.BLUE).a(
                user.getNickname()+ " LEFT THE SERVER").reset());
        setBroadcastingMessage("<><> "+user.getNickname()+" LEFT THE SERVER <><>"+
                        "<><> Current server size : "+clients.size()+"<><>\n");
        broadcast();
        System.out.println(Ansi.ansi().fg(Ansi.Color.BLUE).a(
                "Current server size : "+clients.size()).reset());
        System.out.println(Ansi.ansi().fg(Ansi.Color.BLUE).a(
                "_____ _____ _____ _____ _____ _____ _____ _____ _____ \n" +
                "\\____\\\\____\\\\____\\\\____\\\\____\\\\____\\\\____\\\\____\\\\____\\\n").reset());
    }
    public static void addClient(Socket clientSocket) {
        System.out.println(Ansi.ansi().fg(Ansi.Color.BLUE).a(
                "_____ _____ _____ _____ _____ _____ _____ _____ _____ \n" +
                "\\____\\\\____\\\\____\\\\____\\\\____\\\\____\\\\____\\\\____\\\\____\\\n").reset());
        SingleClient singleClient = new SingleClient(clientSocket,true);
        clients.put(singleClient.getUser().getNickname()+clientSocket.getInetAddress().toString(),singleClient);
        System.out.println(Ansi.ansi().fg(Ansi.Color.BLUE).a(
                "Current server size : "+clients.size()).reset());
        setBroadcastingMessage(
                "<><> "+singleClient.getUser().getNickname()+" JOINED THE SERVER <><>"+
                "<><> Current server size : "+clients.size()+"<><>\n");
        broadcast();
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
