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
    private  Boolean keepRunning = true;
    private Runnable readFromClient;
    private Runnable sendToClient;
    private Socket clientSocket;

    public SingleClient(Socket clientSocket) {
        this.clientSocket = clientSocket;
        try {
            clientSocketInputStream = new BufferedInputStream(clientSocket.getInputStream());
            clientSocketOutputStream = new BufferedOutputStream(clientSocket.getOutputStream());
            readFromClient = new Runnable() {
                @Override
                public void run() {
                    //TODO decryption
                    while (keepRunning) {
                        int ascii;
                        String message = "";
                        try {
                            while ((ascii = clientSocketInputStream.read()) != -1){
                                message += (char) ascii;
                                if((char) ascii == '\n'){
                                    ServerHost.broadcast(message);
                                    break;
                                }
                            }
                            if(getClientSocket().isClosed() || message.trim().equals("EXIT")){
                                //todo add client ip address or someway to make the exiting message generic
                                keepRunning = false;
                                System.out.println(ANSI_BLUE+getClientSocket().getInetAddress()+
                                        " LEFT THE SERVER"+ANSI_BLUE);
                            }
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
                        clientSocketOutputStream.write((ServerHost.getBroadcastingMessage()+'\n').getBytes());
                        clientSocketOutputStream.flush();
                    }
                    catch (IOException clientIOE){
                        System.out.println(ANSI_RED + "IOE ERROR IN THREAD (sendToClient) "
                                + "MESSAGE IS " + ServerHost.getBroadcastingMessage()+clientIOE+ANSI_BLUE);
                    }
                }
            };
            Thread readingThread = new Thread(readFromClient);
            readingThread.start();
        } catch (IOException clientIOE) {
            System.out.println(ANSI_RED + "IOE ERROR IN THREAD RUNNING SingleClient CONSTRUCTOR WITH ID"
                    + Thread.currentThread().getId()+" "+clientIOE+ANSI_BLUE);
        }
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    public void setClientSocket(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public Runnable getReadFromClient() {
        return readFromClient;
    }

    public Runnable getSendToClient() {
        return sendToClient;
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
}
