import java.util.Scanner;

public class Main {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    private static volatile boolean keepRunning = true;

    private static final Thread loadingThread = new Thread(new Runnable() {
        @Override
        public void run() {
            while (keepRunning) {
                System.out.print("|" + ANSI_BLUE);
                System.out.print("\b");
                System.out.print("/" + ANSI_BLUE);
                System.out.print("\b");
                System.out.print("-" + ANSI_BLUE);
                System.out.print("\b");
                System.out.print("\\" + ANSI_BLUE);
                System.out.print("\b");
            }
        }
    });
    public static void main(String[] args) {
        System.out.println(ANSI_PURPLE + "    ______                  ___              __   _____                              ______          __  _            \n" +
                "   / ____/_______  ___     /   |  ____  ____/ /  / ___/___  _______  __________     /_  __/__  _  __/ /_(_)___  ____ _\n" +
                "  / /_  / ___/ _ \\/ _ \\   / /| | / __ \\/ __  /   \\__ \\/ _ \\/ ___/ / / / ___/ _ \\     / / / _ \\| |/_/ __/ / __ \\/ __ `/\n" +
                " / __/ / /  /  __/  __/  / ___ |/ / / / /_/ /   ___/ /  __/ /__/ /_/ / /  /  __/    / / /  __/>  </ /_/ / / / / /_/ / \n" +
                "/_/   /_/   \\___/\\___/  /_/  |_/_/ /_/\\__,_/   /____/\\___/\\___/\\__,_/_/   \\___/    /_/  \\___/_/|_|\\__/_/_/ /_/\\__, /  \n" +
                "                                                                                                             /____/   ");
        System.out.println(ANSI_BLUE + "Enter your nickname :");
        Scanner scanner = new Scanner(System.in);
        System.out.print(ANSI_PURPLE + ">> ");
        User user = new User(scanner.nextLine());
        while (true) {
            System.out.println(ANSI_BLUE + "please enter:\n1 : to create a chat room\n2 : to join existing chat room\n");
            System.out.print(ANSI_PURPLE + ">> ");
            String choice = scanner.nextLine().trim();
            if (!choice.equals("1") && !choice.equals("2")) {

                System.out.println(ANSI_RED + "(ERROR) PLEASE ENTER 1 OR 2 : ");

                continue;
            }

            if (choice.equals("1")) {
                //creating a room
                System.out.print(ANSI_BLUE+"Creating a room .. ");
                loadingThread.start();
                ServerHost serverHost = new ServerHost(3000);
                String message = "";
                while (true){
                    message = scanner.nextLine();
                    if(message.equals("EXIT")){
                        serverHost.setCommand("EXIT");
                        break;
                    }
                    System.out.println(message);
                }
                System.out.println("END");
                break;
            } else {
                //joining a room
                System.out.println(ANSI_BLUE+"please enter room number :");
                System.out.print(ANSI_PURPLE + ">> ");
                user.setRoom(scanner.nextLine().trim());
                System.out.print(ANSI_BLUE+"Connecting .. ");

                loadingThread.start();
                try{
                    //making the connection to the server
                    Thread.sleep(5000);
                }
                catch (InterruptedException interruptedException){
                    System.out.println(interruptedException);
                }
                keepRunning = false;
                System.out.println();
            }
        }
    }

    public static void setKeepRunning(boolean keepRunning) {
        Main.keepRunning = keepRunning;
    }
}