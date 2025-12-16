import minigames.CoolingMinigame;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ZonePanel extends JPanel {
    private String zoneName;
    private LoadGraph loadGraph;
    private JTextArea logArea;
    private JList<String> playerList;
    private CoolingMinigame coolingMinigame;

    public ZonePanel(String zoneName) {
        this.zoneName = zoneName;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 1. Заголовок зоны
        JLabel titleLabel = new JLabel(zoneName, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        add(titleLabel, BorderLayout.NORTH);

        // 2. Левая часть: График нагрузки и игроки
        JPanel leftPanel = new JPanel(new BorderLayout());

        // График нагрузки (заглушка)
        loadGraph = new LoadGraph();
        leftPanel.add(loadGraph, BorderLayout.NORTH);

        // Список игроков в зоне
        JPanel playersPanel = new JPanel(new BorderLayout());
        playersPanel.setBorder(BorderFactory.createTitledBorder("Игроки в зоне"));
        playerList = new JList<>(new String[]{"Никого нет"});
        playersPanel.add(new JScrollPane(playerList), BorderLayout.CENTER);
        leftPanel.add(playersPanel, BorderLayout.CENTER);

        // 3. Правая часть: Логи
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("Логи зоны"));

        logArea = new JTextArea(10, 30);
        logArea.setEditable(false);
        logArea.setText("=== Логи зоны " + zoneName + " ===\n");
        rightPanel.add(new JScrollPane(logArea), BorderLayout.CENTER);

        // 4. Центр: Мини-игра (заглушка)
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder("Задания"));

        if (zoneName.contains("Cooling")) {
            // Для Cooling Station - наша мини-игра
            coolingMinigame = new CoolingMinigame();
            centerPanel.add(coolingMinigame, BorderLayout.CENTER);
        } else {
            // Для других зон - временная заглушка
            JButton testButton = new JButton("Тест: выполнить задание в " + zoneName);
            testButton.addActionListener(e -> {
                logArea.append("Выполнено тестовое задание в " + zoneName + "\n");
                // Обновляем график
                double newLoad = 20 + Math.random() * 60;
                loadGraph.updateLoad(newLoad);
            });
            centerPanel.add(testButton, BorderLayout.CENTER);
        }

        // 5. Собираем всё вместе
        add(leftPanel, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
    }

    // Методы для обновления данных (будут вызываться из handleMessage)
    public void updateLoad(double load) {
        loadGraph.updateLoad(load);
    }

    public void updatePlayers(List<String> players) {
        playerList.setListData(players.toArray(new String[0]));
    }

    public void addLog(String logEntry) {
        logArea.append(logEntry + "\n");
    }
}