import com.fasterxml.jackson.databind.JsonNode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.concurrent.atomic.AtomicReference;

public class MainWindow extends JFrame {

    private final JTextField hostField = new JTextField("127.0.0.1");
    private final JTextField portField = new JTextField("8080");
    private final JButton connectButton = new JButton("Подключиться");
    private final JLabel timeLabel = new JLabel("не подключено");
    private NetworkClient client;
    private final AtomicReference<String> playerId = new AtomicReference<>();

    public MainWindow() {
        setTitle("ServerAttack Client");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        JPanel top = new JPanel(new GridLayout(2, 2, 5, 5));
        top.add(new JLabel("host"));
        top.add(hostField);
        top.add(new JLabel("port"));
        top.add(portField);
        add(top, BorderLayout.NORTH);
        add(timeLabel, BorderLayout.CENTER);
        add(connectButton, BorderLayout.SOUTH);
        connectButton.addActionListener(this::onConnect);
    }

    private void onConnect(ActionEvent e) {
        if (client != null) {
            client.disconnect();
            client = null;
            connectButton.setText("Подключиться");
            timeLabel.setText("не подключено");
            return;
        }

        String host = hostField.getText().trim();
        int port = Integer.parseInt(portField.getText().trim());
        client = new NetworkClient(host, port, this::handleMessage);

        try {
            client.connect();
            Message join = Message.of(
                    MessageType.JOIN,
                    0,
                    System.currentTimeMillis(),
                    JsonUtils.toJsonNode(java.util.Map.of("name", "player"))
            );
            client.send(join);
            connectButton.setText("Отключиться");
        } catch (Exception ex) {
            client = null;
            JOptionPane.showMessageDialog(this, "Не удалось подключиться: " + ex.getMessage());
        }
    }

    private void handleMessage(Message msg) {
        if (msg.getTyped() == MessageType.STATE_UPDATE) {
            JsonNode data = msg.getData();
            if (data != null && data.has("time")) {
                long time = data.get("time").asLong();
                SwingUtilities.invokeLater(() -> timeLabel.setText("время сервера: " + time));
            }
        }
    }

}
