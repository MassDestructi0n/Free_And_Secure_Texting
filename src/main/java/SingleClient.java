import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

public class SingleClient {
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_GREEN = "\u001B[32m";
    private  volatile BufferedInputStream clientSocketInputStream;
    private  volatile BufferedOutputStream clientSocketOutputStream;
    private static volatile String messageToSend;
    private  Boolean keepRunning = true;
    private Thread readFromClient;
    private Thread sendToClient;
    private Socket clientSocket;

    public SingleClient(Socket clientSocket) {
        this.clientSocket = clientSocket;
        try {
            clientSocketInputStream = new BufferedInputStream(clientSocket.getInputStream());
            clientSocketOutputStream = new BufferedOutputStream(clientSocket.getOutputStream());
            SingleClient.messageToSend = "";
            readFromClient = new Thread(new Runnable() {
                @Override
                public void run() {
                    //TODO decryption
                    while (keepRunning) {
                        int ascii;
                        String message = "";
                        try {
                            while ((ascii = clientSocketInputStream.read()) != -1)
                                message += (char) ascii;
                            ServerHost.broadcast(message);
                        } catch (IOException clientIOE) {
                            System.out.println(ANSI_RED + "IOE ERROR IN THREAD (readFromClient) WITH ID"
                                    + readFromClient.getId() + "MESSAGE IS " + message+" "+clientIOE);
                        }
                    }
                }
            });
            sendToClient = new Thread(new Runnable() {
                @Override
                public void run() {
                    //TODO encryption
                    try{
                        clientSocketOutputStream.write((SingleClient.getMessageToSend()+'\n').getBytes());
                        clientSocketOutputStream.flush();
                    }
                    catch (IOException clientIOE){
                        System.out.println(ANSI_RED + "IOE ERROR IN THREAD (sendToClient) WITH ID"
                                + readFromClient.getId() + "MESSAGE IS " + messageToSend);
                    }
                }
            });
            readFromClient.start();
            sendToClient.start();
        } catch (IOException clientIOE) {
            System.out.println(ANSI_RED + "IOE ERROR IN THREAD RUNNING SingleClient CONSTRUCTOR WITH ID"
                    + Thread.currentThread().getId()+" "+clientIOE);
        }
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    public void setClientSocket(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public static void setMessageToSend(String messageToSend) {
        SingleClient.messageToSend = messageToSend;
    }

    public static String getMessageToSend() {
        return messageToSend;
    }

    public Thread getReadFromClient() {
        return readFromClient;
    }

    public Thread getSendToClient() {
        return sendToClient;
    }
    public Boolean endConnection(){
        Boolean ended = false;
        try{
            clientSocket.close();
            keepRunning = false;
            clientSocketInputStream.close();
            clientSocketOutputStream.close();
            sendToClient.interrupt();
            readFromClient.interrupt();
            ended = true;
        }catch (IOException clientIOE){
            System.out.println(ANSI_RED + "IOE ERROR IN THREAD RUNNING SingleClient endConnection() WITH ID"
                    + Thread.currentThread().getId()+" "+clientIOE);
            ended = false;
        }
        return ended;
    }
}
