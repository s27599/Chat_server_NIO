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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ChatServer implements Runnable {

    private String host;
    private int port;
    private ServerSocketChannel serverSocketChannel = null;
    private Selector selector = null;
    private Thread thread;
    private ByteBuffer inBuffer = ByteBuffer.allocate(1024);
    private StringBuilder request = new StringBuilder();
    private Map<SocketChannel, String> clients;


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

            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        thread = new Thread(this);
        thread.start();
    }

    public void stopServer() {
        thread.interrupt();
    }

    public String getServerLog() {
        return "";
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
        request.setLength(0);
        inBuffer.clear();
        try {
            int n = socketChannel.read(inBuffer);
            if (n > 0) {
                inBuffer.flip();
                CharBuffer decoded = StandardCharsets.UTF_8.decode(inBuffer);
                while (decoded.hasRemaining()) {
                    char ch = decoded.get();
                    request.append(ch);
                    if (Character.toString(ch).equals("\u0004")) {
                        processRequest(request.toString(), socketChannel);
//                        response = response + "\u0004";
                        //BRODCAST
                        brodcast(request.toString(), socketChannel);

//                        ByteBuffer responseBuffer = ByteBuffer.wrap(response.getBytes());
//                        socketChannel.write(responseBuffer);
                        request.setLength(0);
                    }

                }

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


    private String processRequest(String request, SocketChannel socketChannel) throws IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
        request = request.replace("\u0004", "");
        String s = socketChannel.getRemoteAddress().toString();
        if (request.startsWith("login")) {
            String user = request.split(" ")[1];
            clients.put(socketChannel, user);
            brodcast(user + " logged in",socketChannel);
//            user = request.split(" ")[1];
//            usernamesMap.put(s, user);
//            clientLogs.put(s, new ArrayList<>());
//            clientLogs.get(s).add("=== " + usernamesMap.get(s) + " log start ===");
//            clientLogs.get(s).add("logged in");
//            serverLog.add(usernamesMap.get(s) + " logged in at " + LocalDateTime.now().format(formatter));
            return "logged in";
        } else if (request.equals("bye")) {
            clients.remove(socketChannel);
            socketChannel.close();
//            clientLogs.get(s).add("logged out");
//            clientLogs.get(s).add("=== " + usernamesMap.get(s) + " log end ===");
//            serverLog.add(usernamesMap.get(s) + " logged out at " + LocalDateTime.now().format(formatter));
            return "logged out";
        }

        //garbage
        return request;


    }

    private void brodcast(String msg, SocketChannel senderSocketChannel) throws IOException {
        for (Map.Entry<SocketChannel, String> entry : clients.entrySet()) {
            if (!entry.getKey().equals(senderSocketChannel)) {
                ByteBuffer responseBuffer = ByteBuffer.wrap(msg.getBytes());
                entry.getKey().write(responseBuffer);
            }
        }
    }

}
