import java.util.Scanner;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
public class Main {
    private static volatile boolean loadingThreadLoop = true;

    public static String getChoice() {
        return choice;
    }

    public static void setChoice(String choice) {
        Main.choice = choice;
    }

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
        User user = new User(choice);
        System.out.print(Ansi.ansi().fg(Ansi.Color.GREEN).a("").reset());
        label:
        while (!choice.equals("EXIT")) {

            System.out.println(Ansi.ansi().fg(Ansi.Color.MAGENTA).a("please enter:\n1 : to create a chat room\n2 : to join existing chat room\n").reset());
            System.out.print(Ansi.ansi().fg(Ansi.Color.MAGENTA).a(">> ").reset());
            System.out.print(Ansi.ansi().fg(Ansi.Color.GREEN).a(""));
            choice = scanner.nextLine().trim();
            System.out.print(Ansi.ansi().fg(Ansi.Color.GREEN).a("").reset());
            String message = "";
            switch (choice) {
                case "EXIT":
                    break label;
                case "1":
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
                        ServerHost.broadcast(ServerHost.getBroadcastingMessage());
                    }
                    System.out.println(Ansi.ansi().fg(Ansi.Color.MAGENTA).a("END").reset());
                    break label;
                case "2":
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


}