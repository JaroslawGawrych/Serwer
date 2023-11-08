import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class DatabaseNode {
    //pola klasy
    private ArrayList<ConnectionHandler> connectionHandlers;
    private int tcpport;
    private int key;
    private int value;
    private ExecutorService executor;
    private ClientHandler klient;
    private boolean isDone;
    private ServerSocket server;
    private ArrayList<String> returns;

    //gettery i settery
    public void setDone(boolean done) {
        isDone = done;
    }

    public boolean isDone() {
        return isDone;
    }

    public ArrayList<String> getReturns() {
        return returns;
    }

    public ArrayList<ConnectionHandler> getConnectionHandlers() {
        return connectionHandlers;
    }


    public int getKey() {
        return key;
    }

    public int getValue() {
        return value;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getTcpport() {
        return tcpport;
    }
    //konsturktor
    public DatabaseNode(int tcpport, int key, int value) {
        returns = new ArrayList<>();
        this.tcpport = tcpport;
        this.key = key;
        this.value = value;
        this.connectionHandlers = new ArrayList<>();
        this.executor = Executors.newCachedThreadPool();
        isDone = false;
        System.out.println("hej mam na imie: " + tcpport);
    }

    public static void main(String[] args) {
        int tcpport = 0;
        int key = 0;
        int value = 0;
        ArrayList<String> connections = new ArrayList<>();
        ArrayList<Integer> ports = new ArrayList<>();

        //oblsługa args
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-tcpport":
                    tcpport = Integer.parseInt(args[++i]);
                    break;
                case "-record":
                    String[] record = args[++i].split(":");
                    key = Integer.parseInt(record[0]);
                    value = Integer.parseInt(record[1]);
                    break;
                case "-connect":
                    String[] connect = args[++i].split(":");
                    String address = connect[0];
                    int port = Integer.parseInt(connect[1]);
                    connections.add(address);
                    ports.add(port);
                    break;
                default:
                    System.err.println("wrong argument: " + args[i]);
                    System.exit(1);
            }
        }

        //utworzenie node
        DatabaseNode node = new DatabaseNode(tcpport, key, value);

        //połączenie z węzłami z args
        for (int i =0; i<connections.size(); i++) {
            node.connect(connections.get(i), ports.get(i));
        }
        //rozpoczecie pracy node
        node.start();
    }

    //metoda tworząca połączenia z węzłami podanymi w args
    public void connect(String address, Integer port) {

        try {
            Socket socket = new Socket(address, port);
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output.println("node");
            String odp = input.readLine();
            if(!odp.equals("node")){
                System.out.println("node nie odpowiedzial na przywitanie");
                return;
            }
            System.out.println("new node connected");
            String connection = address + ":" + port;
            System.out.println(connection);
            connectionHandlers.add(new ConnectionHandler(this, socket, input, output));
            executor.execute(connectionHandlers.get(connectionHandlers.size()-1));
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
    //metoda odbierająca połączenia od klientów i węzłów
    public void start() {

        try{
            server = new ServerSocket(tcpport);
            while (true) {
                System.out.println("waiting...");
                Socket socket = server.accept();
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String s = in.readLine();
                if(s.equals("node")){
                    out.println("node");
                    System.out.println("new node connected");
                    String connection = socket.getRemoteSocketAddress() + ":" + socket.getPort();
                    System.out.println(connection);
                    connectionHandlers.add(new ConnectionHandler(this, socket, in, out));
                    executor.execute(connectionHandlers.get(connectionHandlers.size()-1));
                }else {
                    klient = new ClientHandler(this, socket, in, out, s);
                    executor.execute(klient);
                }

            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        System.out.println("ended start");

    }
    //metoda rozsyłająca wiadomość do wszyskich sąsiadujących węzłów i oczekująca na odpowiedzi
    public void send(String task, ConnectionHandler sender) {
        returns = new ArrayList<>();
        for (ConnectionHandler ch : connectionHandlers) {
            if(ch!= sender)ch.send(task);
        }
        for (ConnectionHandler ch : connectionHandlers) {
            while(ch.isWaiting()) {
                try {
                    TimeUnit.MILLISECONDS.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("Odpowiedzi od nodes gotowe");
    }
    //metoda rozsyłająca wiadomość do wszyskich sąsiadujących węzłów i oczekująca na odpowiedzi. metoda dotyczy tylko węzła połączonego z klientem
    public void sendOG(String task) {
        returns = new ArrayList<>();
        isDone = true;
        for (ConnectionHandler ch : connectionHandlers) {
            ch.sendOG(task);
        }
        for (ConnectionHandler ch : connectionHandlers) {
            while(ch.isOG()) {
                try {
                    TimeUnit.MILLISECONDS.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("odpowiedzi dla klienta gotowe");
        isDone = false;
        unDone();

    }
    //metoda przygotowująca wszyskie nody na nowego klienta
    public void unDone(){
        for (ConnectionHandler ch : connectionHandlers) {
            ch.out("UNDONE");
        }
    }
    //metoda zamykająca dany node
    public void terminate(){
        for (ConnectionHandler ch : connectionHandlers) {
            ch.out("terminate");
            try {
                ch.setTerminated(true);
                ch.getSocket().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            server.close();
            System.out.println("closed");
        } catch (IOException e) {
            e.printStackTrace();
        }
        executor.shutdown();
    }
}
