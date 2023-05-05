import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

public class SingleClient {
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_GREEN = "\u001B[32m";
    private  volatile BufferedInputStream clientSocketInputStream;
    private  volatile BufferedOutputStream clientSocketOutputStream;
    private volatile User user = new User();
    private volatile Boolean isInitial;
    private  Boolean keepRunning = true;
    private Runnable readFromClient;
    private Runnable sendToClient;
    private volatile Socket  clientSocket;
    public SingleClient(){

    }

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
                                message += (char) ascii;
                                if((char) ascii == '\n'){
                                    break;
                                }
                            }
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
                                        System.out.println(ANSI_RED+"VALIDATION NOT PASSED IN INITIAL CONNECTION "+
                                                "IN SingleClient"+getClientSocket().getInetAddress());
                                        getUser().setNickname("UNKNOWN SPIRIT");
                                        connectionStatus = false;
                                        break;
                                    }
                                    switch (lineKeyValue[0]){
                                        case "nickname": {
                                            getUser().setNickname(lineKeyValue[1]);
                                            break;
                                        }
                                        default:
                                            break;
                                    }
                                }
                                System.out.println(ANSI_BLUE+user.getNickname()+" JOINED THE SERVER "+ANSI_BLUE);
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
                                        " LEFT THE SERVER");
                                ServerHost.endConnection(getClientSocket(),getUser());
                                endConnection();
                            }
                            ServerHost.broadcast(message);
                        } catch (IOException clientIOE) {
                            System.out.println(ANSI_RED + "IOE ERROR IN THREAD (readFromClient)"
                                    + "MESSAGE IS " + message+" "+clientIOE+ANSI_BLUE);
                        }
                    }
                }
            };
            sendToClient = new Runnable() {
                @Override
                public void run() {
                    //TODO encryption
                    try{
                        getClientSocketOutputStream().write((ServerHost.getBroadcastingMessage()+'\n').getBytes());
                        getClientSocketOutputStream().flush();
                    }
                    catch (IOException clientIOE){
                        System.out.println(ANSI_RED + "IOE ERROR IN THREAD (sendToClient) "
                                + "MESSAGE IS " + ServerHost.getBroadcastingMessage()+clientIOE+ANSI_BLUE);
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
                System.out.println(ANSI_RED+"ERROR IN SingleClient CONSTRUCTOR  :"+interruptedException+ANSI_RED);
            }
            if(!clientSocket.isClosed()){
                Thread readingThread = new Thread(getReadFromClient());
                readingThread.setName("reading-thread");
                readingThread.start();
            }
        } catch (IOException clientIOE) {
            System.out.println(ANSI_RED + "IOE ERROR IN THREAD RUNNING SingleClient CONSTRUCTOR WITH ID"
                    + Thread.currentThread().getId()+" "+clientIOE+ANSI_BLUE);
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
            System.out.println(ANSI_RED + "IOE ERROR IN THREAD RUNNING SingleClient endConnection() WITH ID"
                    + Thread.currentThread().getId()+" "+clientIOE+ANSI_BLUE);
            ended = false;
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
