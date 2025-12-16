import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
@ToString
public class NetworkClient {

    private final String host;
    private final int port;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private final BlockingQueue<Message> sendQueue = new LinkedBlockingQueue<>();
    private final AtomicLong seq = new AtomicLong(0);
    private volatile boolean running = false;
    private final Consumer<Message> onMessage;

    public void connect() throws IOException {
        socket = new Socket(host, port);
        log.info("Socket {}", socket);
        log.info("Client connected successfully");
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        log.info("In {}, out {}", in, out);
        running = true;
        log.info("Running: {}", running);
        new Thread(this::readLoop).start();
        new Thread(this::writeLoop).start();
    }

    public void disconnect() {
        log.info("Client has disconnected");
        running = false;
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }

    private void readLoop() {
        log.info("Client start reading thread");

        while (running && !socket.isClosed()) {
            Message msg = JsonUtils.readMessage(in);
            onMessage.accept(msg);
        }

        log.error("Client has disconnected");
        disconnect();

    }

    private void writeLoop() {
        log.info("Client start writing thread");
        try {
            while (running && !socket.isClosed()) {
                Message m = sendQueue.take();
                log.info("Client received message {}", m);
                JsonUtils.writeMessage(out, m);
                log.info("Client write message");
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            disconnect();
        }

    }

    public void send(Message msg) {
        log.info("Received message {}", msg);
        msg.setSeq(seq.incrementAndGet());
        try {
            sendQueue.put(msg);
            log.info("Put message in queue {}", msg);
        } catch (InterruptedException ignored) {
        }
    }

}
