import lombok.Getter;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientHandler {

    private final Socket socket;
    private final MainServer server;
    @Getter
    private final String id;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private DataInputStream in;
    private DataOutputStream out;
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
        try {
            while (running && !socket.isClosed()) {
                Message msg = JsonUtils.readMessage(in);
                //лог
            }
        } catch (IOException ignored) {
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
