import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class MainWindow extends JFrame {

    private final JTextField hostField = new JTextField("127.0.0.1");
    private final JTextField portField = new JTextField("8080");
    private final JButton connectButton = new JButton("Подключиться");
    private final JLabel timeLabel = new JLabel("не подключено");
    private NetworkClient client;

    private JTabbedPane tabbedPane; // Панель с вкладками
    private ZonePanel backupPanel;
    private ZonePanel firewallPanel;
    private ZonePanel routerPanel;
    private ZonePanel coolingPanel;
    private ZonePanel monitorPanel;

    private final AtomicReference<String> playerId = new AtomicReference<>();

    public MainWindow() {
        setTitle("ServerAttack Client");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel topPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        setLayout(new BorderLayout());
        topPanel.add(new JLabel("Хост:"));
        topPanel.add(hostField);
        topPanel.add(new JLabel("Порт:"));
        topPanel.add(portField);

        tabbedPane = new JTabbedPane();

        backupPanel = new ZonePanel("Backup Storage");
        firewallPanel = new ZonePanel("Firewall Control");
        routerPanel = new ZonePanel("Router Bay");
        coolingPanel = new ZonePanel("Cooling Station");
        monitorPanel = new ZonePanel("Monitoring Center");

        // Добавляем вкладки
        tabbedPane.addTab("Backup", backupPanel);
        tabbedPane.addTab("Firewall", firewallPanel);
        tabbedPane.addTab("Router", routerPanel);
        tabbedPane.addTab("Cooling", coolingPanel);
        tabbedPane.addTab("Monitor", monitorPanel);


        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(connectButton, BorderLayout.WEST);
        bottomPanel.add(timeLabel, BorderLayout.CENTER);


        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        JButton testGuiButton = new JButton("Тест GUI");
        testGuiButton.addActionListener(e -> testGuiFunctionality());
        bottomPanel.add(testGuiButton, BorderLayout.EAST);

        connectButton.addActionListener(this::onConnect);
    }

    private void testGuiFunctionality() {
        // Тестируем все панели
        backupPanel.updateLoad(25.5);
        backupPanel.updatePlayers(java.util.List.of("Игрок1", "Игрок2"));
        backupPanel.addLog("Тест: система Backup работает");

        firewallPanel.updateLoad(80.0);
        firewallPanel.updatePlayers(java.util.List.of("Хакер?"));
        firewallPanel.addLog("ВНИМАНИЕ: подозрительная активность!");

        coolingPanel.updateLoad(45.0);
        coolingPanel.addLog("Температура в норме");

        timeLabel.setText("Тест GUI выполнен");
    }

    private void onConnect(ActionEvent e) {
        log.info("Client try to connect");
        if (client != null) {
            log.info("Client try to disconnect");
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
            Thread.sleep(500);
        } catch (InterruptedException ignored) {

        }

        try {
            log.info("client: {}", client);
            client.connect();
            Message join = Message.of(
                    MessageType.JOIN,
                    0,
                    System.currentTimeMillis(),
                    JsonUtils.toJsonNode(Map.of("name", "player"))
            );
            client.send(join);
            connectButton.setText("Отключиться");
        } catch (Exception ex) {
            client = null;
            JOptionPane.showMessageDialog(this, "Не удалось подключиться: " + ex.getMessage());
        }
    }

    private void handleMessage(Message msg) {
        log.info("Client received from server {}", msg);

        if (msg.getTyped() == MessageType.JOIN_ACK) {
            String playerId = msg.getData().get("playerId").asText();
            this.playerId.set(playerId);

            SwingUtilities.invokeLater(() -> {
                timeLabel.setText("Подключено! ID: " + playerId);
                JOptionPane.showMessageDialog(this,
                        "Успешно подключились!\nВаш ID: " + playerId,
                        "Подключено",
                        JOptionPane.INFORMATION_MESSAGE
                );
            });
        }

        if (msg.getTyped() == MessageType.STATE_UPDATE) {
            JsonNode data = msg.getData();
            if (data != null && data.has("time")) {
                long time = data.get("time").asLong();
                SwingUtilities.invokeLater(() -> timeLabel.setText("время сервера: " + time));
            }
        }
    }

}
