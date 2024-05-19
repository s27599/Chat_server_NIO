/**
 * @author Kaczor Wiktor S27599
 */

package zad1;


import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ChatServer implements Runnable {

    private String host;
    private int port;
    private ServerSocketChannel serverSocketChannel = null;
    private Selector selector = null;
    private Thread thread;
    private ByteBuffer inBuffer = ByteBuffer.allocate(1024);
    //    private StringBuilder request = new StringBuilder();
//    private String[] requests;
    private static Map<SocketChannel, String> clients;
    private static List<String> serverLog;
    private static StringBuilder cache = new StringBuilder();


    public ChatServer(String host, int port) {
        this.host = host;
        this.port = port;
        clients = new HashMap<>();
    }

    public void startServer() {
        try {
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().bind(new InetSocketAddress(host, port));
            selector = Selector.open();
            serverLog = new ArrayList<>();

            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        thread = new Thread(this);
        thread.start();
        thread.setName("Server thread");
        System.out.println("Server started\n");
    }

    public void stopServer() {
        thread.interrupt();
        System.out.println("Server stopped");
    }

    public String getServerLog() {
        StringBuilder sb = new StringBuilder();
        for (String str : serverLog) {
            sb.append(str).append("\n");
        }
        return sb.toString();
    }

    @Override
    public void run() {
        try {
            while (!thread.isInterrupted()) {
                selector.select();
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> iter = keys.iterator();
                while (iter.hasNext()) {
                    SelectionKey key = iter.next();
                    iter.remove();
                    if (key.isAcceptable()) {
                        SocketChannel socketChannel = serverSocketChannel.accept();
                        socketChannel.configureBlocking(false);
                        socketChannel.register(selector, SelectionKey.OP_READ);
                        continue;
                    }
                    if (key.isReadable()) {
                        SocketChannel socketChannel = (SocketChannel) key.channel();
                        serviceRequest(socketChannel);
                    }
                }


            }
            selector.close();
            serverSocketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void serviceRequest(SocketChannel socketChannel) {
        if (!socketChannel.isOpen()) {
            return;
        }
        inBuffer.clear();
        //receive message from client
        try {
            int n = socketChannel.read(inBuffer);
            if (n > 0) {
                inBuffer.flip();
                CharBuffer decoded = StandardCharsets.UTF_8.decode(inBuffer);

                int fullMessages=countChar(decoded.toString(),'\u0004');
                String[] requests = decoded.toString().split("\u0004");
                for(int i=0;i<fullMessages;i++){
                        processRequest(requests[i], socketChannel);
                }
//                String partialMessage = decoded.toString();
//                int splitIndex;
//                while ((splitIndex = partialMessage.indexOf("\u0004")) != -1) {
//                    String request = partialMessage.substring(0, splitIndex);
//                    processRequest(request, socketChannel);
//                    partialMessage = partialMessage.substring(splitIndex + 1);
//                }
//                inBuffer.position(decoded.position());
//                inBuffer.compact();

//                while (decoded.hasRemaining()) {
//                    String[] requests = decoded.toString().split("\u0004");
//                    for (String request : requests) {
////                        System.out.println(request);
//                        processRequest(request, socketChannel);
//                    }
//                    decoded.compact();
//                    decoded.clear();
//                    decoded.flip();
//                    inBuffer.clear();
//
//                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            try {
                socketChannel.close();
                socketChannel.socket().close();
            } catch (IOException ex) {
                e.printStackTrace();
            }
        }
    }


    private void processRequest(String request, SocketChannel socketChannel) throws IOException {
        if (request.startsWith("login")&&!clients.containsKey(socketChannel)) {
            String user = request.split(" ")[1];
                clients.put(socketChannel, user);
                brodcast(user + " logged in", socketChannel);

        } else if (request.equals("bye")) {
            brodcast(clients.get(socketChannel) + " logged out", socketChannel);
            clients.remove(socketChannel);
            socketChannel.close();

        } else {
            request = clients.get(socketChannel) + ": " + request;
            brodcast(request.toString(), socketChannel);

        }
    }

    private void brodcast(String msg, SocketChannel senderSocketChannel) throws IOException {
        saveToServerLog(msg);
        msg = msg + "\u0004";
        for (Map.Entry<SocketChannel, String> entry : clients.entrySet()) {
//            if (!entry.getKey().equals(senderSocketChannel)) {
            ByteBuffer responseBuffer = ByteBuffer.wrap(msg.getBytes());
            entry.getKey().write(responseBuffer);
//            }
        }
    }

    private void saveToServerLog(String message) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
        serverLog.add(LocalDateTime.now().format(formatter) + " " + message);
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
