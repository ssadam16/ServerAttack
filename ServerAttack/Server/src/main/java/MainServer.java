import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

@RequiredArgsConstructor
@Slf4j
public class MainServer {

    private final int port;
    private final ConcurrentHashMap<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong(0);
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
    private final GameState gameState;
    @Getter
    private final ActionProcessor actionProcessor;


    public MainServer(int port) {
        this.port = port;
        this.gameState = new GameState();
        this.actionProcessor = new ActionProcessor(gameState, this);
    }


    public void start()  {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            executor.scheduleAtFixedRate(this::gameLoop, 0, 2, TimeUnit.SECONDS);

            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                log.info("Client has connected!");
                ClientHandler handler = new ClientHandler(socket, this);
                clients.put(handler.getId(), handler);
                handler.start();
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void remove(ClientHandler handler) {
        clients.remove(handler.getId());
    }

    private void gameLoop() {
        try {
            // Используем ActionProcessor для создания снапшота
            JsonNode snapshot = actionProcessor.createSnapshot();
            Message msg = Message.of(
                    MessageType.STATE_UPDATE,
                    seq.incrementAndGet(),
                    System.currentTimeMillis(),
                    snapshot
            );
            broadcast(msg);
            log.info("Broadcasted STATE_UPDATE to {} clients", clients.size());
        } catch (Exception e) {
            log.error("Error in gameLoop: {}", e.getMessage());
        }
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
