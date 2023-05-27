import org.fusesource.jansi.Ansi;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;


public class ClientConnection {
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
            System.out.println(Ansi.ansi().fg(Ansi.Color.MAGENTA).a("\bIP : "+roomDecrypted[0]).reset());
            System.out.println(Ansi.ansi().fg(Ansi.Color.MAGENTA).a("\bPORT : "+Integer.parseInt(roomDecrypted[1])).reset());
            setClientSocket(new Socket(roomDecrypted[0],Integer.parseInt(roomDecrypted[1])));
            getUser().setConnected(true);
        }catch (IOException ioSocketException){
            System.out.println(Ansi.ansi().fg(Ansi.Color.RED).a(
                    "ERROR ROOM ENTERED CAN'T BE FOUND "+ioSocketException).reset());
            getUser().setConnected(false);
            System.exit(500);
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
                        message = Main.decrypt(message,Main.stringToKey(Main.getUser().getSecretKey()));
                        System.out.println();
                    }
                    catch (IOException readingException){
                        System.out.println(Ansi.ansi().fg(Ansi.Color.RED).a(
                                "ERROR WHILE READING FROM SERVER "+readingException).reset());
                        System.out.println(Ansi.ansi().fg(Ansi.Color.BLUE).a(
                                "MAYBE THE SERVER CLOSED CONNECTION ").reset());
                        setKeepRunning(false);
                        System.exit(500);
                    }catch (Exception e){
                        System.out.println(Ansi.ansi().fg(Ansi.Color.RED).a("ERROR WHILE DECRYPTING THE MESSAGE"+e));
                    }

                    //todo decryption of received message
                    if(message.equals("")){

                        System.out.println(Ansi.ansi().fg(Ansi.Color.BLUE).a(
                                "MAYBE THE SERVER CLOSED CONNECTION").reset());
                        setKeepRunning(false);
                        System.exit(500);
                    }

                    System.out.println(Ansi.ansi().fg(Ansi.Color.YELLOW).a(message).reset());
                }
            }
        };
        sendingRunnable = new Runnable() {
            @Override
            public void run() {
                try{
                    getClientSocketOutputStream().write(getMessageToSend().getBytes());
                    getClientSocketOutputStream().flush();
                }catch (IOException writeException){
                    System.out.println(Ansi.ansi().fg(Ansi.Color.RED).a(
                            "ERROR WHILE WRITING ON SERVER "+writeException).reset());
                }
            }
        };
        setMessageToSend("'nickname'"+":"
                +"'"+getUser().getNickname()+"'\n");
        Thread sendingThread = new Thread(sendingRunnable);
        Thread readingThread = new Thread(readingRunnable);
        readingThread.setName("reading-socket-client-thread");
        if(getUser().isConnected()){
            try{
                clientSocketInputStream = new BufferedInputStream(getClientSocket().getInputStream());
            }catch (IOException inputStreamException){
                System.out.println(Ansi.ansi().fg(Ansi.Color.BLUE).a(
                        "ERROR CLIENT SOCKET INPUT STREAM "+inputStreamException).reset());
            }
            try{
                clientSocketOutputStream = new BufferedOutputStream(getClientSocket().getOutputStream());
            }catch (IOException outputStreamException){
                System.out.println(Ansi.ansi().fg(Ansi.Color.BLUE).a(
                        "ERROR CLIENT SOCKET OUTPUT STREAM "+outputStreamException).reset());
            }
            System.out.println(Ansi.ansi().fg(Ansi.Color.MAGENTA).a(
                    " ** ** ** WELCOME! ** ** ** ").reset());
            System.out.println(Ansi.ansi().fg(Ansi.Color.MAGENTA).a(
                    "CONNECTION IS ESTABLISHED! YOU CAN SEND MESSAGES NOW").reset());
            sendingThread.start();
            readingThread.start();
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
        try {
            ClientConnection.messageToSend  = Main.encrypt(messageToSend,
                    Main.stringToKey(Main.getUser().getSecretKey()))+'\n';
        }catch (Exception e){
            System.out.println(Ansi.ansi().fg(Ansi.Color.RED).a("ERROR WHILE ENCRYPTING THE MESSAGE"+e));
        }
    }
}
