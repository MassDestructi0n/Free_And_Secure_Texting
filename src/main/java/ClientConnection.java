import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientConnection {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";
    private Runnable readingRunnable;
    private Runnable sendingRunnable;
    private volatile User user = new User();
    private volatile BufferedOutputStream clientSocketOutputStream;
    private volatile BufferedInputStream clientSocketInputStream;
    private volatile Socket clientSocket;
    private static Boolean keepRunning;
    private static String messageToSend;
    public ClientConnection(User user){
        setKeepRunning(true);
        setUser(user);
        //todo decryption of the room -> to ip:port
        //todo validation of room
        String roomDecrypted[] = getUser().getRoom().split(":");
        Thread loadingThread = new Thread(Main.getLoadingRunnable());
        loadingThread.start();
        try{
            setClientSocket(new Socket(roomDecrypted[0],Integer.parseInt(roomDecrypted[1])));
            getUser().setConnected(true);
        }catch (IOException ioSocketException){
            System.out.println(ANSI_RED+"ERROR ROOM ENTERED CAN'T BE FOUND "+ioSocketException);
            getUser().setConnected(false);
        }finally {
            Main.setLoadingThreadLoop(false);
        }
        readingRunnable = new Runnable() {
            @Override
            public void run() {
                while (keepRunning){
                    int ascii;
                    String message = "";
                    try{
                        while ((ascii = getClientSocketInputStream().read())!=-1){
                            if((char) ascii == '\n'){
                                break;
                            }
                            message += (char)ascii;
                        }
                    }catch (IOException readingException){
                        System.out.println(ANSI_RED+"ERROR WHILE READING FROM SERVER "+readingException);
                        setKeepRunning(false);
                    }
                    //todo decryption of received message
                    System.out.println(ANSI_BLUE+message);
                }
            }
        };
        sendingRunnable = new Runnable() {
            @Override
            public void run() {
                //todo encryption of the message
                try{
                    getClientSocketOutputStream().write((getUser().getNickname()+' '+getMessageToSend()).getBytes());
                    getClientSocketOutputStream().flush();
                }catch (IOException writeException){
                    System.out.println(ANSI_RED+"ERROR WHILE WRITING ON SERVER "+writeException);
                }
            }
        };
        Thread readingThread = new Thread(readingRunnable);
        readingThread.setName("reading-socket-client-thread");
        if(getUser().isConnected()){
            try{
                clientSocketInputStream = new BufferedInputStream(getClientSocket().getInputStream());
            }catch (IOException inputStreamException){
                System.out.println(ANSI_RED+"ERROR CLIENT SOCKET INPUT STREAM "+inputStreamException);
            }
            try{
                clientSocketOutputStream = new BufferedOutputStream(getClientSocket().getOutputStream());
            }catch (IOException outputStreamException){
                System.out.println(ANSI_RED+"ERROR CLIENT SOCKET OUTPUT STREAM "+outputStreamException);
            }
            readingThread.start();
            System.out.println(ANSI_PURPLE+" ** ** ** WELCOME! ** ** ** ");
            System.out.println(ANSI_PURPLE+"CONNECTION IS ESTABLISHED! YOU CAN SEND MESSAGES NOW");
        }
    }
    public void sendMessage(){
        Thread sendingThread = new Thread(sendingRunnable);
        sendingThread.setName("sending-client-message-thread");
        sendingThread.start();
    }

    public synchronized User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public synchronized Socket getClientSocket() {
        return clientSocket;
    }

    public void setClientSocket(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public synchronized BufferedOutputStream getClientSocketOutputStream() {
        return clientSocketOutputStream;
    }

    public synchronized BufferedInputStream getClientSocketInputStream() {
        return clientSocketInputStream;
    }

    public static void setKeepRunning(Boolean keepRunning) {
        ClientConnection.keepRunning = keepRunning;
    }

    public static String getMessageToSend() {
        return messageToSend;
    }

    public static void setMessageToSend(String messageToSend) {
        ClientConnection.messageToSend = messageToSend;
    }
}
