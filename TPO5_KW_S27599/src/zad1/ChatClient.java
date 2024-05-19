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
    private ByteBuffer outBuf;
    private Thread messageReceiver;
    private List<String> chatViev;
    CharBuffer decoded;
//    private StringBuilder cache = new StringBuilder();


    public ChatClient(String host, int port, String id) {
        this.host = host;
        this.port = port;
        this.id = id;
        this.inBuf = ByteBuffer.allocateDirect(1024);
        this.outBuf = ByteBuffer.allocateDirect(1024);
        this.chatViev = new ArrayList<>();
        chatViev.add("=== " + id + " chat view");
        try {
            socketChannel = SocketChannel.open();
        } catch (IOException e) {
            e.printStackTrace();
        }

        messageReceiver = new Thread(() -> {
            while (!messageReceiver.isInterrupted()) {
                StringBuilder message = new StringBuilder();
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


//                        while (decoded.hasRemaining()) {
//                            char ch = decoded.get();
//                            if (Character.toString(ch).equals("\u0004")) {
////                                System.out.println(id + "---- " + message);
//                                if(message.toString().contains(id+" logged out")){
//                                    messageReceiver.interrupt();
//                                }
//                                chatViev.add(message.toString());
//                                message.setLength(0);
//                            } else {
//                                message.append(ch);
//                            }
//
//                        }

                    }
                } catch (IOException e) {
                    chatViev.add("***"+e.toString());
                }
//                System.out.println("chatViev from client " + id + ":");
//                System.out.println(chatViev);
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
    }

    public void send(String req) {
        try {
            outBuf.put(req.getBytes());
            outBuf.put((byte) '\u0004');
            outBuf.flip();
            socketChannel.write(outBuf);
            outBuf.clear();
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
