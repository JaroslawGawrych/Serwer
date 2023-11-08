import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    //pola klasy
    private DatabaseNode databaseNode;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String s;
    //konstruktor
    public ClientHandler(DatabaseNode databaseNode, Socket socket, BufferedReader input, PrintWriter out, String s) {
        this.databaseNode = databaseNode;
        this.socket = socket;
        this.in = input;
        this.out = out;
        this.s = s;
    }
    //wysłanie wiadomości z wypisaniem jej na konsoli
    public void out(String msg){
        System.out.println("K " + msg);
        out.println(msg);
    }
    //obsluga komunikacji klient - węzeł
    @Override
    public void run() {
        String[] task = s.split(" ");
        switch (task[0]){
            case "set-value": {
                String[] pair = task[1].split(":");
                String tmp = "ERROR";
                if (databaseNode.getKey() == Integer.parseInt(pair[0])) {
                    databaseNode.setValue(Integer.parseInt(pair[1]));
                    tmp = "ok";
                }
                databaseNode.sendOG(s);

                for (String ans : databaseNode.getReturns()) {
                    if (!ans.equals("ERROR")) {
                        tmp = ans;
                    }
                }
                out(tmp);
                break;
            }
            case "get-value" : {
                if(databaseNode.getKey() == Integer.parseInt(task[1])){
                    out(databaseNode.getKey() + ":" + databaseNode.getValue());
                } else {
                    String tmp = "ERROR";
                    databaseNode.sendOG(s);
                    for(String ans: databaseNode.getReturns()){
                        if(!ans.equals("ERROR")){
                            tmp = ans;
                        }
                    }
                    out(tmp);
                }
                break;
            }
            case "find-key" : {
                if(databaseNode.getKey() == Integer.valueOf(task[1])){
                    out(socket.getRemoteSocketAddress() + ":" + databaseNode.getTcpport());
                }else{
                    String tmp = "ERROR";
                    databaseNode.sendOG(s);
                    for(String ans: databaseNode.getReturns()){
                        if(!ans.equals("ERROR")){
                            tmp = ans;
                        }
                    }
                    out(tmp);
                }
                break;
            }
            case "get-max" : {
                databaseNode.sendOG(s);
                String tmp = databaseNode.getKey() +":"+databaseNode.getValue();
                for (String ans: databaseNode.getReturns()) {
                    if(!ans.equals("ERROR")){
                        if(Integer.parseInt(ans.split(":")[1])>Integer.parseInt(tmp.split(":")[1])){
                            tmp = ans;
                        }
                    }
                }
                out(tmp);
                break;
            }
            case "get-min" : {
                databaseNode.sendOG(s);
                String tmp = databaseNode.getKey() +":"+databaseNode.getValue();
                for (String ans: databaseNode.getReturns()) {
                    if(!ans.equals("ERROR")){
                        if(Integer.parseInt(ans.split(":")[1])<Integer.parseInt(tmp.split(":")[1])){
                            tmp = ans;
                        }
                    }
                }
                out(tmp);
                break;
            }
            case "new-record" : {
                String[] record = task[1].split(":");
                databaseNode.setKey(Integer.valueOf(record[0]));
                databaseNode.setValue(Integer.valueOf(record[1]));
                out("ok");
                break;
            }
            case "terminate" : {
                out("ok");
                databaseNode.terminate();
                System.out.println("returning");
                break;
            }
        }
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}