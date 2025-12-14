import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicLong;

@RequiredArgsConstructor
public class MainServer {

    private final int port;
    private final ConcurrentHashMap<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong(0);
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);

    public void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        while (!serverSocket.isClosed()) {
            Socket socket = serverSocket.accept();
            ClientHandler handler = new ClientHandler(socket, this);
            clients.put(handler.getId(), handler);
            handler.start();
        }
    }

    public void remove(ClientHandler handler) {
        clients.remove(handler.getId());
    }

    private void gameLoop() {
        long now = System.currentTimeMillis();
        JsonNode data = JsonUtils.toJsonNode(Map.of("time", now));
        Message msg = Message.of(MessageType.STATE_UPDATE, seq.incrementAndGet(), now, data);
        broadcast(msg);
    }

    public void broadcast(Message msg) {
        clients.values().forEach(c -> {
            try {
                c.send(msg);
            } catch (Exception ignored) {}
        });
    }

    public static void main(String[] args) throws IOException {
        int port = 8080;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
        MainServer server = new MainServer(port);
        server.start();
    }

}
