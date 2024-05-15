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
                        CharBuffer decoded = StandardCharsets.UTF_8.decode(inBuf);
                        while (decoded.hasRemaining()) {
                            char ch = decoded.get();
                            if (Character.toString(ch).equals("\u0004")) {
//                                System.out.println(message);
                                if(message.toString().contains(id+" logged out")){
                                    messageReceiver.interrupt();
                                }
                                chatViev.add(message.toString());
                                message.setLength(0);
                            } else {
                                message.append(ch);
                            }

                        }

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                inBuf.clear();

            }
        });
    }

    public void login() {
        connect();
        messageReceiver.start();
        send("login " + id);
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


}
