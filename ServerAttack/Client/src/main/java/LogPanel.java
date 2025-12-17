import lombok.Getter;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.List;

public class LogPanel extends JPanel {
    private JTextArea logArea;
    private JComboBox<String> zoneFilter;
    private JComboBox<String> typeFilter;
    @Getter
    private JButton refreshButton;
    private JLabel statusLabel;

    public LogPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),  // сначала рамка
                "Системные логи",                           // затем заголовок
                TitledBorder.LEFT,                         // выравнивание
                TitledBorder.TOP,                          // положение заголовка
                new Font("Arial", Font.BOLD, 12),         // шрифт
                Color.DARK_GRAY                            // цвет текста
        ));

        // Панель фильтров
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        filterPanel.add(new JLabel("Зона:"));
        zoneFilter = new JComboBox<>(new String[]{"Все", "BackupStorage", "FirewallControl", "RouterBay", "CoolingStation", "MonitoringCenter"});
        filterPanel.add(zoneFilter);

        filterPanel.add(new JLabel("Тип:"));
        typeFilter = new JComboBox<>(new String[]{"Все", "INFO", "WARNING", "ALERT", "ИЗМЕНЕНЫЕ", "УДАЛЕННЫЕ"});
        filterPanel.add(typeFilter);

        refreshButton = new JButton("Обновить");
        filterPanel.add(refreshButton);

        statusLabel = new JLabel("Логи загружены");
        filterPanel.add(statusLabel);

        add(filterPanel, BorderLayout.NORTH);

        // Область логов
        logArea = new JTextArea(15, 50);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logArea.setBackground(new Color(240, 240, 240));

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        add(scrollPane, BorderLayout.CENTER);

        // Кнопка очистки
        JButton clearButton = new JButton("Очистить логи");
        clearButton.addActionListener(e -> logArea.setText(""));

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(clearButton);
        add(bottomPanel, BorderLayout.SOUTH);

        // Настраиваем цвета для разных типов логов
        logArea.setForeground(Color.BLACK);
    }

    public void addLogEntry(String logEntry, String logType) {
        Color color = switch (logType) {
            case "WARNING" -> new Color(200, 100, 0); // Оранжевый
            case "ALERT" -> Color.RED;
            case "ИЗМЕНЕНЫЕ" -> new Color(128, 0, 128); // Фиолетовый
            case "УДАЛЕННЫЕ" -> new Color(139, 0, 0); // Темно-красный
            default -> Color.BLACK;
        };

        // Используем HTML для цветного текста
        String coloredText = String.format("<font color='#%02x%02x%02x'>%s</font><br>",
                color.getRed(), color.getGreen(), color.getBlue(),
                logEntry.replace("<", "&lt;").replace(">", "&gt;"));

        // Обновляем текст с сохранением формата
        String currentText = logArea.getText();
        if (currentText.isEmpty()) {
            logArea.setText("<html><body>" + coloredText + "</body></html>");
        } else {
            // Удаляем закрывающие теги, добавляем новую запись, снова добавляем теги
            String newText = currentText.replace("</body></html>", "")
                    + coloredText + "</body></html>";
            logArea.setText(newText);
        }

        // Прокручиваем вниз
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    public void clearLogs() {
        logArea.setText("");
        statusLabel.setText("Логи очищены");
    }

    public void updateStatus(String status) {
        statusLabel.setText(status);
    }

    public String getSelectedZone() {
        String selected = (String) zoneFilter.getSelectedItem();
        return "Все".equals(selected) ? null : selected;
    }

    public String getSelectedType() {
        return (String) typeFilter.getSelectedItem();
    }

}