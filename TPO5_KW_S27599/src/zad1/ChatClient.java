/**
 *
 *  @author Kaczor Wiktor S27599
 *
 */

package zad1;


public class ChatClient {
    
    private String host;
    private int port;
    private String id;
    
    public ChatClient(String host, int port, String id) {
        this.host = host;
        this.port = port;
        this.id = id;
    }


    public void login(){

    }
    public void login(String id) {
    }
    public void logout(){

    }
    public void send(String req){

    }

    public String getChatView() {
        return " ";
    }

    public String getId() {
        return id;
    }


}
