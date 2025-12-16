import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class ClientHandler {

    private final Socket socket;
    private final MainServer server;
    @Getter
    private final String id;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final DataInputStream in;
    private final DataOutputStream out;

    private volatile boolean running = true;

    public ClientHandler(Socket socket, MainServer server) throws IOException {
        this.socket = socket;
        this.server = server;
        this.id = UUID.randomUUID().toString();
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
    }

    public void start() {
        executor.submit(this::readLoop);
    }

    private void readLoop() {
        log.info("ClientHandler start reading messages from client with id {}", id);
        try {
            while (running && !socket.isClosed()) {
                log.info("socket is running and not closed");
                Message msg = JsonUtils.readMessage(in);
                log.info("Server received message {}", msg);
                server.getActionProcessor().process(msg, this);
                log.info("Server has processed message {}", msg);
            }
        } finally {
            close();
        }
    }

    public void send(Message m) throws IOException {
        JsonUtils.writeMessage(out, m);
    }

    private void close() {
        running = false;
        server.remove(this);
        try {
            socket.close();
        } catch (IOException ignored) {}
        executor.shutdownNow();
    }

}
