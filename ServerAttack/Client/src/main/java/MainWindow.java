import com.fasterxml.jackson.databind.JsonNode;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class MainWindow extends JFrame {
    private final JTextField hostField = new JTextField("127.0.0.1");
    private final JTextField portField = new JTextField("8080");
    private final JButton connectButton = new JButton("Подключиться");
    private final JLabel statusLabel = new JLabel("не подключено");
    private final JLabel roleLabel = new JLabel("Роль: не определено");
    private NetworkClient client;

    private JTabbedPane tabbedPane;
    private ZonePanel backupPanel;
    private ZonePanel firewallPanel;
    private ZonePanel routerPanel;
    private ZonePanel coolingPanel;
    private ZonePanel monitorPanel;

    private final AtomicReference<String> playerId = new AtomicReference<>();
    private final AtomicReference<Role> playerRole = new AtomicReference<>();

    public MainWindow() {
        setTitle("ServerAttack Client");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel topPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        topPanel.add(new JLabel("Хост:"));
        topPanel.add(hostField);
        topPanel.add(new JLabel("Порт:"));
        topPanel.add(portField);
        topPanel.add(new JLabel("Статус:"));
        topPanel.add(statusLabel);

        JPanel rolePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rolePanel.add(new JLabel("Ваша роль:"));
        rolePanel.add(roleLabel);
        roleLabel.setFont(new Font("Arial", Font.BOLD, 12));

        tabbedPane = new JTabbedPane();

        backupPanel = new ZonePanel("Backup Storage");
        firewallPanel = new ZonePanel("Firewall Control");
        routerPanel = new ZonePanel("Router Bay");
        coolingPanel = new ZonePanel("Cooling Station");
        monitorPanel = new ZonePanel("Monitoring Center");

        tabbedPane.addTab("Backup", backupPanel);
        tabbedPane.addTab("Firewall", firewallPanel);
        tabbedPane.addTab("Router", routerPanel);
        tabbedPane.addTab("Cooling", coolingPanel);
        tabbedPane.addTab("Monitor", monitorPanel);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(connectButton, BorderLayout.WEST);
        bottomPanel.add(rolePanel, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        connectButton.addActionListener(this::onConnect);
    }

    private void onConnect(ActionEvent e) {
        if (client != null) {
            client.disconnect();
            client = null;
            connectButton.setText("Подключиться");
            statusLabel.setText("не подключено");
            roleLabel.setText("Роль: не определено");
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
        if (msg.getTyped() == MessageType.JOIN_ACK) {
            JsonNode data = msg.getData();
            String id = data.get("playerId").asText();
            String roleStr = data.get("role").asText();
            Role role = Role.valueOf(roleStr);

            playerId.set(id);
            playerRole.set(role);

            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("Подключено! ID: " + id);
                roleLabel.setText("Роль: " + (role == Role.HACKER ? "ХАКЕР" : "ИНЖЕНЕР"));
                roleLabel.setForeground(role == Role.HACKER ? Color.RED : Color.GREEN);

                // Обновляем роль во всех панелях
                backupPanel.setPlayerRole(role);
                firewallPanel.setPlayerRole(role);
                routerPanel.setPlayerRole(role);
                coolingPanel.setPlayerRole(role);
                monitorPanel.setPlayerRole(role);
            });
        }

        if (msg.getTyped() == MessageType.STATE_UPDATE) {
            JsonNode data = msg.getData();
            if (data != null) {
                // Обновляем графики нагрузки
                if (data.has("zones")) {
                    for (JsonNode zone : data.get("zones")) {
                        String zoneId = zone.get("id").asText();
                        double load = zone.get("load").asDouble();

                        SwingUtilities.invokeLater(() -> {
                            switch (zoneId) {
                                case "CoolingStation": coolingPanel.updateLoad(load); break;
                                case "FirewallControl": firewallPanel.updateLoad(load); break;
                                case "BackupStorage": backupPanel.updateLoad(load); break;
                                case "RouterBay": routerPanel.updateLoad(load); break;
                                case "MonitoringCenter": monitorPanel.updateLoad(load); break;
                            }
                        });
                    }
                }

                // Обновляем прогресс
                if (data.has("progress")) {
                    int progress = data.get("progress").asInt();
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("Прогресс: " + progress + "%");
                    });
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainWindow mw = new MainWindow();
            mw.setVisible(true);
        });
    }
}