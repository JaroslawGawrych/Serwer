import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class ConnectionHandler implements Runnable {
    //pola klasy
    private DatabaseNode databaseNode;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private boolean isOG;
    private boolean isWaiting;
    private boolean terminated;
    //gettery i settery
    public boolean isOG() {
        return isOG;
    }

    public boolean isWaiting() {
        return isWaiting;
    }

    public void setTerminated(boolean terminated) {
        this.terminated = terminated;
    }

    public Socket getSocket() {
        return socket;
    }
    //konstruktor
    public ConnectionHandler(DatabaseNode databaseNode, Socket socket, BufferedReader in, PrintWriter output) {
        this.databaseNode = databaseNode;
        this.socket = socket;
        this.in = in;
        this.out = output;
        isOG = false;
        isWaiting = false;
        terminated = false;
    }
    //wysłanie wiadomości z wypisaniem jej na konsoli
    void out(String msg){
        System.out.println("Wysylam " + msg);
        out.println(msg);
    }
    //obsługa przesyłanych wiadmości między węzłami
    @Override
    public void run() {
        while (!terminated) {
            try {
                String msg = "";
                while (msg.equals("")) {
                    msg = in.readLine();
                }
                System.out.println("Odebralem " + msg);
                System.out.println("Status done: " + databaseNode.isDone());
                String[] task = msg.split(" ");
                if(task[0].equals("UNDONE")){
                    if(databaseNode.isDone())databaseNode.unDone();
                    databaseNode.setDone(false);
                }
                else if(task[0].equals("terminate")){
                    databaseNode.getConnectionHandlers().remove(this);
                    socket.close();
                    return;
                }
                else if(task[0].equals("return")){
                    if(isOG || isWaiting) {
                        databaseNode.getReturns().add(task[1]);
                        isWaiting = false;
                        isOG = false;
                    }else{
                        System.out.println("======kurwa co?======");
                    }
                }else if(!databaseNode.isDone()){
                    databaseNode.setDone(true);
                    switch (task[0]) {
                        case "set-value": {
                            String[] pair = task[1].split(":");
                            if (databaseNode.getKey() == Integer.parseInt(pair[0])) {
                                databaseNode.setValue(Integer.parseInt(pair[1]));
                                out("return OK");
                            } else {
                                databaseNode.send(msg, this);
                                String tmp = "ERROR";
                                for (String ans: databaseNode.getReturns()) {
                                    if(!ans.equals("ERROR")){
                                        tmp = ans;
                                    }
                                }
                                out("return " + tmp);
                            }
                            break;
                        }
                        case "get-value": {
                            if (databaseNode.getKey() == Integer.parseInt(task[1])) {
                                out("return " + databaseNode.getKey() + ":" + databaseNode.getValue());
                            } else {
                                databaseNode.send(msg, this);
                                String tmp = "ERROR";
                                for (String ans: databaseNode.getReturns()) {
                                    if(!ans.equals("ERROR")){
                                        tmp = ans;
                                    }
                                }
                                out("return " + tmp);
                            }
                            break;
                        }
                        case "find-key": {
                            if (databaseNode.getKey() == Integer.parseInt(task[1])) {
                                out("return " + socket.getInetAddress().getHostAddress()+ ":" + databaseNode.getTcpport());
                            } else {
                                databaseNode.send(msg, this);
                                String tmp = "ERROR";
                                for (String ans: databaseNode.getReturns()) {
                                    if(!ans.equals("ERROR")){
                                        tmp = ans;
                                    }
                                }
                                out("return " + tmp);
                            }
                            break;
                        }
                        case "get-max": {
                            databaseNode.send(msg, this);
                            String tmp = databaseNode.getKey() +":"+databaseNode.getValue();
                            for (String ans: databaseNode.getReturns()) {
                                if(!ans.equals("ERROR")){
                                    if(Integer.parseInt(ans.split(":")[1])>Integer.parseInt(tmp.split(":")[1])){
                                        tmp = ans;
                                    }
                                }
                            }
                            out("return " + tmp);
                            break;
                        }
                        case "get-min": {
                            databaseNode.send(msg, this);
                            String tmp = databaseNode.getKey() + ":" + databaseNode.getValue();
                            for (String ans : databaseNode.getReturns()) {
                                if (!ans.equals("ERROR")) {
                                    if (Integer.parseInt(ans.split(":")[1]) < Integer.parseInt(tmp.split(":")[1])) {
                                        tmp = ans;
                                    }
                                }
                            }
                            out("return " + tmp);
                            break;
                        }
                    }
                }else{
                    out("return ERROR");
                    System.out.println("juz to zrobilem");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("klient closed");
    }
    //metoda rozsylajaca zadanie.
    public void send(String task){
        isWaiting = true;
        out(task);
    }
    //metoda rozsylajaca zadanie. dotyczy tylko połączeń z węzłem odpowiedzialnym za komunikacje z klientem
    public void sendOG(String task){
        isOG = true;
        out(task);
    }

}