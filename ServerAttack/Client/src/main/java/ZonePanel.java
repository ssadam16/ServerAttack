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
    private JLabel roleLabel;  // Новое поле для отображения роли

    public ZonePanel(String zoneName) {
        this.zoneName = zoneName;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Панель для заголовка и роли
        JPanel headerPanel = new JPanel(new BorderLayout());

        // 1. Заголовок зоны
        JLabel titleLabel = new JLabel(zoneName, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));

        // 2. Метка роли (пока пустая, будет обновляться из MainWindow)
        roleLabel = new JLabel("", SwingConstants.RIGHT);
        roleLabel.setFont(new Font("Arial", Font.BOLD, 14));

        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(roleLabel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // 2. Левая часть: График нагрузки и игроки
        JPanel leftPanel = new JPanel(new BorderLayout());

        // График нагрузки
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

        // 4. Центр: Мини-игра
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder("Задания"));

        if (zoneName.contains("Cooling")) {
            coolingMinigame = new CoolingMinigame();
            centerPanel.add(coolingMinigame, BorderLayout.CENTER);
        } else {
            // Для других зон - временная заглушка
            JPanel testPanel = new JPanel(new GridLayout(2, 1, 5, 5));

            JButton engineerBtn = new JButton("Инженер: Выполнить задание");
            engineerBtn.addActionListener(e -> {
                logArea.append("Инженер выполнил задание в " + zoneName + "\n");
                loadGraph.updateLoad(20 + Math.random() * 60);
            });

            JButton hackerBtn = new JButton("Хакер: Саботировать");
            hackerBtn.addActionListener(e -> {
                logArea.append("ВНИМАНИЕ! Обнаружена попытка саботажа!\n");
                loadGraph.updateLoad(80 + Math.random() * 20);
                // Меняем цвет графика на красный при саботаже
                loadGraph.setGraphColor(Color.RED);
            });

            testPanel.add(engineerBtn);
            testPanel.add(hackerBtn);
            centerPanel.add(testPanel, BorderLayout.CENTER);
        }

        // 5. Собираем всё вместе
        add(leftPanel, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
    }

    // Метод для обновления роли игрока
    public void setPlayerRole(Role role) {
        String roleText = role == Role.HACKER ? "Вы: ХАКЕР" : "Вы: ИНЖЕНЕР";
        Color color = role == Role.HACKER ? Color.RED : Color.GREEN;
        roleLabel.setText(roleText);
        roleLabel.setForeground(color);
    }

    public void updateLoad(double load) {
        loadGraph.updateLoad(load);
        // Возвращаем нормальный цвет графика, если нагрузка не критическая
        if (load < 70) {
            loadGraph.setGraphColor(new Color(0, 150, 255));
        }
    }

    public void updatePlayers(List<String> players) {
        playerList.setListData(players.toArray(new String[0]));
    }

    public void addLog(String logEntry) {
        logArea.append(logEntry + "\n");
        // Прокручиваем до конца
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}