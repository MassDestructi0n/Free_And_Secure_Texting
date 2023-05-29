import java.io.*;
import java.util.Scanner;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
public class Main {

    private  static User user;
    private static volatile boolean loadingThreadLoop = true;

    private static SecretKey secretKey;



    private static final Runnable loadingRunnable = new Runnable() {
        @Override
        public void run() {
            while (loadingThreadLoop && !choice.equals("EXIT")) {
                try {
                    System.out.print(Ansi.ansi().fg(Ansi.Color.BLUE).a("|").reset());
                    Thread.sleep(100);
                    System.out.print(Ansi.ansi().fg(Ansi.Color.BLUE).a("\b").reset());
                    System.out.print(Ansi.ansi().fg(Ansi.Color.BLUE).a("/").reset());
                    Thread.sleep(100);
                    System.out.print(Ansi.ansi().fg(Ansi.Color.BLUE).a("\b").reset());
                    System.out.print(Ansi.ansi().fg(Ansi.Color.BLUE).a("-").reset());
                    Thread.sleep(100);
                    System.out.print(Ansi.ansi().fg(Ansi.Color.BLUE).a("\b").reset());
                    System.out.print(Ansi.ansi().fg(Ansi.Color.BLUE).a("\\").reset());
                    Thread.sleep(100);
                    System.out.print(Ansi.ansi().fg(Ansi.Color.BLUE).a("\b").reset());
                }
                catch (InterruptedException interruptedException){
                    System.out.println(Ansi.ansi().fg(Ansi.Color.RED).a(
                            "ERROR WHILE WAITING IN LOADING "+interruptedException).reset());
                    System.exit(500);
                }
            }
        }
    };
    //https://github.com/fusesource/jansi\
    private static volatile String choice = "";

    public static void main(String[] args) {

        AnsiConsole.systemInstall();
        System.out.println(Ansi.ansi().fg(Ansi.Color.MAGENTA).a("    ______                  ___              __   _____                              ______          __  _            \n" +
                "   / ____/_______  ___     /   |  ____  ____/ /  / ___/___  _______  __________     /_  __/__  _  __/ /_(_)___  ____ _\n" +
                "  / /_  / ___/ _ \\/ _ \\   / /| | / __ \\/ __  /   \\__ \\/ _ \\/ ___/ / / / ___/ _ \\     / / / _ \\| |/_/ __/ / __ \\/ __ `/\n" +
                " / __/ / /  /  __/  __/  / ___ |/ / / / /_/ /   ___/ /  __/ /__/ /_/ / /  /  __/    / / /  __/>  </ /_/ / / / / /_/ / \n" +
                "/_/   /_/   \\___/\\___/  /_/  |_/_/ /_/\\__,_/   /____/\\___/\\___/\\__,_/_/   \\___/    /_/  \\___/_/|_|\\__/_/_/ /_/\\__, /  \n" +
                "                                                                                                             /____/   ").reset());
        System.out.println(Ansi.ansi().fg(Ansi.Color.MAGENTA).a(" -- Enter EXIT anytime to exit or ctrl+c--").reset());
        System.out.println(Ansi.ansi().fg(Ansi.Color.MAGENTA).a("Enter your nickname :").reset());
        Scanner scanner = new Scanner(System.in);
        System.out.print(Ansi.ansi().fg(Ansi.Color.MAGENTA).a(">> ").reset());
        System.out.print(Ansi.ansi().fg(Ansi.Color.GREEN).a(""));
        choice = scanner.nextLine();
        setUser(new User(choice));
        System.out.print(Ansi.ansi().fg(Ansi.Color.GREEN).a("").reset());

        label:
        while (!choice.equals("EXIT")) {
            System.out.println(Ansi.ansi().fg(Ansi.Color.MAGENTA).a("please enter:\n1 : to create a chat room\n2 : to join existing chat room\n").reset());
            System.out.print(Ansi.ansi().fg(Ansi.Color.MAGENTA).a(">> ").reset());
            System.out.print(Ansi.ansi().fg(Ansi.Color.GREEN).a(""));
            choice = scanner.nextLine().trim();
            System.out.println(Ansi.ansi().fg(Ansi.Color.GREEN).a("").reset());
            String message = "";
            switch (choice) {
                case "EXIT":
                    break label;
                case "1":

                    try {
                        secretKey = generateSecretKey();
                    } catch (Exception e) {
                        System.out.println(Ansi.ansi().fg(Ansi.Color.MAGENTA).a("ERROR WHILE" +
                                "GENERATING THE KEY "+e).reset());
                    }

                    String secretKeyString = keyToString(secretKey);

                    System.out.println(Ansi.ansi().fg(Ansi.Color.MAGENTA).a("COPY THE SECRETKEY FROM" +
                            "THE FILE SECRETKEY.TXT").reset());

                    writeSecretKeyOnFile(secretKeyString);

                    user.setSecretKey(secretKeyString);
                    //creating a room
                    System.out.print(Ansi.ansi().fg(Ansi.Color.MAGENTA).a("by default" +
                            " port is : 5000 , ip is 127.0.0.1 , Creating a room .. ").reset());
                    ServerHost serverHost = new ServerHost(5000);

                    while (true) {
                        System.out.println(Ansi.ansi().fg(Ansi.Color.GREEN).a(""));
                        message = scanner.nextLine();
                        if (message.equals("EXIT")) {
                            choice = "EXIT";
                            serverHost.setCommand("EXIT");
                            break;
                        }
                        System.out.println();
                        System.out.println(Ansi.ansi().fg(Ansi.Color.GREEN).a(
                                "\b"+user.getNickname()+":"+message+"\n").reset());
                        ServerHost.setBroadcastingMessage(user.getNickname()+":"+message+"\n");
                        ServerHost.broadcast();
                    }
                    System.out.println(Ansi.ansi().fg(Ansi.Color.MAGENTA).a("END").reset());
                    break label;
                case "2":
                    System.out.println(Ansi.ansi().fg(Ansi.Color.MAGENTA).a("<><>please enter the secret " +
                            "in a file in the jar directory , name it \"SECRETKEY.txt\"\n" +
                            "and paste in it only the secret key which is obtained from server\nafter that" +
                            "press type anything and press enter <><>").reset());
                    System.out.print(Ansi.ansi().fg(Ansi.Color.MAGENTA).a(">> ").reset());
                    System.out.print(Ansi.ansi().fg(Ansi.Color.GREEN).a(""));
                    choice = scanner.nextLine();

                    user.setSecretKey(choice);

                    String key = readSecretKeyFromFile();
                    user.setSecretKey(key);
                    //joining a room
                    System.out.println(Ansi.ansi().fg(Ansi.Color.MAGENTA).a("please enter room number :").reset());
                    System.out.print(Ansi.ansi().fg(Ansi.Color.MAGENTA).a(">> ").reset());
                    System.out.println(Ansi.ansi().fg(Ansi.Color.GREEN).a(""));
                    String room = scanner.nextLine().trim();
                    System.out.print(Ansi.ansi().fg(Ansi.Color.GREEN).a("").reset());
                    if (room.equals("EXIT")) {
                        choice = "EXIT";
                        break label;
                    } else
                        user.setRoom(room);
                    System.out.println(Ansi.ansi().fg(Ansi.Color.MAGENTA).a("Connecting .. ").reset());
                    ClientConnection clientConnection = new ClientConnection(user);
                    while (true) {
                        System.out.println(Ansi.ansi().fg(Ansi.Color.GREEN).a("\b"));
                        message = scanner.nextLine();
                        System.out.print(Ansi.ansi().fg(Ansi.Color.GREEN).a("").reset());
                        if (message.trim().equals("EXIT")) {
                            ClientConnection.setKeepRunning(false);
                            choice = "EXIT";
                            break;
                        }
                        ClientConnection.setMessageToSend(clientConnection.getUser().getNickname()
                                +":"+message+ '\n');
                        clientConnection.sendMessage();
                    }
                    System.out.println(Ansi.ansi().fg(Ansi.Color.MAGENTA).a(choice).reset());
                    break;
                default:
                    System.out.println(Ansi.ansi().fg(Ansi.Color.RED).a("(ERROR) PLEASE ENTER 1 OR 2 : ").reset());
                    continue;
            }
        }
        AnsiConsole.systemUninstall();
    }

    public static void setLoadingThreadLoop(boolean keepRunning) {
        Main.loadingThreadLoop = keepRunning;
    }
    public static Runnable getLoadingRunnable(){
        return loadingRunnable;
    }

    public static User getUser() {
        return user;
    }

    public static void setUser(User user) {
        Main.user = user;
    }

    public static SecretKey generateSecretKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256); // Specify key size (e.g., 128, 192, or 256 for AES)
        return keyGenerator.generateKey();
    }

    public static String keyToString(SecretKey secretKey) {
        byte[] keyBytes = secretKey.getEncoded();
        return Base64.getEncoder().encodeToString(keyBytes);
    }

    public static byte[] stringToKey(String secretKeyString) {
        byte[] keyBytes = Base64.getDecoder().decode(secretKeyString);
        return keyBytes;
    }
    public static String encrypt(String plaintext, byte[] secretKeyBytes) throws Exception {
        SecretKey secretKey = new SecretKeySpec(secretKeyBytes, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] ciphertextBytes = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(ciphertextBytes);
    }

    public static String decrypt(String ciphertext, byte[] secretKeyBytes) throws Exception {
        byte[] ciphertextBytes = Base64.getDecoder().decode(ciphertext);
        SecretKey secretKey = new SecretKeySpec(secretKeyBytes, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedBytes = cipher.doFinal(ciphertextBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    private static void writeSecretKeyOnFile(String key){
        try {
            File file = new File("SECRETKEY.txt");
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            bufferedOutputStream.write(key.getBytes());
            bufferedOutputStream.flush();
            fileOutputStream.close();
        }catch (IOException ioException){
            System.out.println(Ansi.ansi().fg(Ansi.Color.GREEN).a("ERROR WHILE " +
                    "WRITING THE SECRETKEY ON THE FILE").reset());
        }
    }
    private static String readSecretKeyFromFile(){
        String key = "";
        try {
            File file = new File("SECRETKEY.txt");
            FileInputStream fileInputStream = new FileInputStream(file);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            int ascii = 0;
            while ((ascii = bufferedInputStream.read())!=-1){
                if((char) ascii == '\n'){
                    break;
                }
                key += (char)ascii;
            }
            fileInputStream.close();

        }catch (IOException ioException){
            System.out.println(Ansi.ansi().fg(Ansi.Color.GREEN).a("ERROR WHILE " +
                    "READING THE SECRETKEY FROM THE FILE").reset());
        }
        return key;
    }
    public static String getChoice() {
        return choice;
    }

    public static void setChoice(String choice) {
        Main.choice = choice;
    }
}