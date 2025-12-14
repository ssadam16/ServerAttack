import lombok.RequiredArgsConstructor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

@RequiredArgsConstructor
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
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        running = true;
        new Thread(this::readLoop).start();
        new Thread(this::writeLoop).start();
    }

    public void disconnect() {
        running = false;
        try {
            socket.close();
        } catch (IOException ignored) {}
    }

    private void readLoop() {
        try {
            while (running && !socket.isClosed()) {
                Message msg = JsonUtils.readMessage(in);
                onMessage.accept(msg);
            }
        } catch (IOException ignored) {
        } finally {
            disconnect();
        }
    }

    private void writeLoop() {
        try {
            while (running && !socket.isClosed()) {
                Message m = sendQueue.take();
                JsonUtils.writeMessage(out, m);
            }
        } catch (Exception ignored) {
        } finally {
            disconnect();
        }
    }

    public void send(Message msg) {
        msg.setSeq(seq.incrementAndGet());
        try {
            sendQueue.put(msg);
        } catch (InterruptedException ignored) {}
    }

}
