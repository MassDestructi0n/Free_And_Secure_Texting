import org.fusesource.jansi.Ansi;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class SingleClient {
    private  volatile BufferedInputStream clientSocketInputStream;
    private  volatile BufferedOutputStream clientSocketOutputStream;
    private volatile User user = new User();
    private volatile Boolean isInitial;
    private  Boolean keepRunning = true;
    private Runnable readFromClient;
    private Runnable sendToClient;
    private volatile Socket  clientSocket;

    public SingleClient(Socket clientSocket,Boolean isInitial) {
        setInitial(isInitial);
        setClientSocket(clientSocket);
        try {
            setClientSocketInputStream(new BufferedInputStream(clientSocket.getInputStream()));
            setClientSocketOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));
            readFromClient = new Runnable() {
                @Override
                public void run() {
                    //TODO decryption
                    while (keepRunning) {
                        int ascii;
                        String message = "";

                        try {
                            while ((ascii = getClientSocketInputStream().read()) != -1){
                                if((char) ascii == '\n'){
                                    break;
                                }
                                message += (char) ascii;
                            }

                            message = Main.decrypt(message,Main.stringToKey(Main.getUser().getSecretKey()));
                            if(getInitial()){
                                /*
                                data received structure :
                                'id':'123'\n
                                'nickname':'grass'\n        // <--- this represents the "line" object down below
                                */
                                Boolean connectionStatus = true;
                                String userData[] = message.split("\n");
                                for(String line : userData){
                                    int singleQuoteCounter = 0;
                                    for(byte asciiLine : line.getBytes()) {
                                        if ((char) asciiLine == '\'')
                                            ++singleQuoteCounter;
                                    }
                                    line = line.replaceAll("'","");
                                    String lineKeyValue[] = line.split(":");
                                    //todo validation
                                    if(lineKeyValue.length != 2 || singleQuoteCounter != 4){
                                        getClientSocketOutputStream().write(("VALIDATION NOT PASSED IN INITIAL CONNECTION "+
                                                "IN SingleClient").getBytes());
                                        getClientSocketOutputStream().flush();
                                        System.out.println(Ansi.ansi().fg(Ansi.Color.RED).a("VALIDATION NOT PASSED IN INITIAL CONNECTION "+

                                                "IN SingleClient"+getClientSocket().getInetAddress()).reset());
                                        getUser().setNickname("UNKNOWN SPIRIT");
                                        connectionStatus = false;
                                        break;
                                    }
                                    switch (lineKeyValue[0]){
                                        case "nickname": {
                                            user.setNickname(lineKeyValue[1]);
                                            break;
                                        }
                                        default:
                                            System.out.println(Ansi.ansi().fg(Ansi.Color.RED).a(
                                                    "VALIDATION NOT PASSED IN INITIAL CONNECTION "+ "IN SingleClient"
                                                            +getClientSocket().getInetAddress()).reset());
                                            break;
                                    }
                                }
                                System.out.println(Ansi.ansi().fg(Ansi.Color.BLUE).a(user.getNickname()+" JOINED THE SERVER ").reset());
                                if(!connectionStatus){
                                    ServerHost.endConnection(getClientSocket(),getUser());
                                    endConnection();
                                }
                                 setInitial(false);
                                break;
                            }
                            else if(getClientSocket().isClosed() || message.trim().equals("EXIT")){
                                //todo add client ip address or someway to make the exiting message generic
                                keepRunning = false;
                                ServerHost.setBroadcastingMessage(message+'\n'+getUser().getNickname()+
                                        " LEFT THE SERVER"+'\n');
                                ServerHost.endConnection(getClientSocket(),getUser());
                                endConnection();
                            }
                            if(message.equals("")){
                                ServerHost.endConnection(getClientSocket(),getUser());
                                break;
                            }

                            System.out.println(Ansi.ansi().fg(Ansi.Color.YELLOW).a(message).reset());
                            ServerHost.setBroadcastingMessage(message);
                            ServerHost.broadcast();
                        } catch (IOException clientIOE) {
                            System.out.println(Ansi.ansi().fg(Ansi.Color.RED).a("IOE ERROR IN THREAD (readFromClient)"
                                    + "MESSAGE IS " + message+" "+clientIOE).reset());
                            System.exit(500);
                        }
                        catch (Exception e){
                            System.out.println(Ansi.ansi().fg(Ansi.Color.RED).a("ERROR WHILE DECRYPTING THE MESSAGE"+e));
                        }
                    }
                }
            };
            sendToClient = new Runnable() {
                @Override
                public void run() {
                    //TODO encryption
                    try{
                        getClientSocketOutputStream().write(ServerHost.getBroadcastingMessage().getBytes());
                        getClientSocketOutputStream().flush();
                    }
                    catch (IOException clientIOE){
                        System.out.println(Ansi.ansi().fg(Ansi.Color.RED).a("IOE ERROR IN THREAD (sendToClient) "
                                + "MESSAGE IS " + ServerHost.getBroadcastingMessage()+clientIOE).reset());
                        System.exit(500);
                    }
                }
            };
            Thread initialReadingThread = new Thread(getReadFromClient());
            initialReadingThread.setName("initial-Reading");
            initialReadingThread.start();
            try {
                initialReadingThread.join();
                setInitial(false);
            }
            catch (InterruptedException interruptedException){
                System.out.println(Ansi.ansi().fg(Ansi.Color.RED).a("ERROR IN SingleClient CONSTRUCTOR  :"+interruptedException).reset());
                System.exit(500);
            }
            if(!clientSocket.isClosed()){
                Thread readingThread = new Thread(getReadFromClient());
                readingThread.setName("reading-thread");
                readingThread.start();
            }
        } catch (IOException clientIOE) {
            System.out.println(Ansi.ansi().fg(Ansi.Color.RED).a(  "IOE ERROR IN THREAD RUNNING SingleClient CONSTRUCTOR WITH ID"
                    + Thread.currentThread().getId()+" "+clientIOE).reset());
            System.exit(500);
        }
    }

    public Boolean endConnection(){
        Boolean ended = false;
        try{
            clientSocket.close();
            keepRunning = false;
            clientSocketInputStream.close();
            clientSocketOutputStream.close();
            ended = true;
        }catch (IOException clientIOE){
            System.out.println(Ansi.ansi().fg(Ansi.Color.RED).a("IOE ERROR IN THREAD RUNNING SingleClient endConnection() WITH ID"
                    + Thread.currentThread().getId()+" "+clientIOE).reset());
            ended = false;
            System.exit(500);
        }
        return ended;
    }
    public synchronized Socket getClientSocket() {
        return clientSocket;
    }

    public  void setClientSocket(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public Runnable getReadFromClient() {
        return readFromClient;
    }

    public Runnable getSendToClient() {
        return sendToClient;
    }

    public synchronized User getUser() {
        return user;
    }

    public synchronized Boolean getInitial() {
        return isInitial;
    }

    public void setInitial(Boolean initial) {
        this.isInitial = initial;
    }

    public synchronized BufferedInputStream getClientSocketInputStream() {
        return clientSocketInputStream;
    }

    public void setClientSocketInputStream(BufferedInputStream clientSocketInputStream) {
        this.clientSocketInputStream = clientSocketInputStream;
    }

    public synchronized BufferedOutputStream getClientSocketOutputStream() {
        return clientSocketOutputStream;
    }

    public void setClientSocketOutputStream(BufferedOutputStream clientSocketOutputStream) {
        this.clientSocketOutputStream = clientSocketOutputStream;
    }
}
