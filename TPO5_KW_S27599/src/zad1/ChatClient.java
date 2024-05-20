/**
 * @author Kaczor Wiktor S27599
 */

package zad1;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


public class ChatClient {

    private String host;
    private int port;
    private String id;
    private SocketChannel socketChannel;
    private ByteBuffer inBuf;
    private Thread messageReceiver;
    private List<String> chatViev;
    CharBuffer decoded;


    public ChatClient(String host, int port, String id) {
        this.host = host;
        this.port = port;
        this.id = id;
        this.inBuf = ByteBuffer.allocateDirect(1024);
        this.chatViev = new ArrayList<>();
        chatViev.add("=== " + id + " chat view");
        try {
            socketChannel = SocketChannel.open();
        } catch (IOException e) {
            e.printStackTrace();
        }

        messageReceiver = new Thread(() -> {
            while (!messageReceiver.isInterrupted()) {
                try {
                    int n = socketChannel.read(inBuf);
                    if (n > 0) {
                        inBuf.flip();
                        decoded = StandardCharsets.UTF_8.decode(inBuf);

                        int fullMessages = countChar(decoded.toString(), '\u0004');
                        String[] messages = decoded.toString().split(String.valueOf("\u0004"));
                        for (int a = 0; a < fullMessages; a++) {
                            if(messages[a].contains(id+" logged out")){
                                messageReceiver.interrupt();
                            }
                            chatViev.add(messages[a]);
                        }

                                            }
                } catch (IOException e) {
                    chatViev.add("***"+e.toString());
                }
//                System.out.println("chatViev from client " + id + ":");
//                System.out.println(chatViev);
//                System.out.println("123");
                inBuf.clear();


            }

        });
    }

    public void login() {
        connect();
        send("login " + id);
        messageReceiver.start();
        messageReceiver.setName("Client " + id + " message receiver");
    }

    public void logout() {
        send("bye");
//        messageReceiver.interrupt();
//        try {
//            socketChannel.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    public void send(String req) {
        try {
            ByteBuffer encoded = StandardCharsets.UTF_8.encode(CharBuffer.wrap(req+"\u0004"));
            socketChannel.write(encoded);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getId() {
        return id;
    }

    public String getChatView() {
        StringBuilder sb = new StringBuilder();
        for (String str : chatViev) {
            sb.append(str).append("\n");
        }
        return sb.toString();
    }



    private void connect() {
        try {
            if (!socketChannel.isOpen())
                socketChannel = SocketChannel.open();
            socketChannel.connect(new InetSocketAddress(host, port));
            socketChannel.configureBlocking(false);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private int countChar(String str, char c){
        int count=0;
        for(int i=0;i<str.length();i++){
            if(str.charAt(i)==c){
                count++;
            }
        }
        return count;

    }
}
