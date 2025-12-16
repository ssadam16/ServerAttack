package minigames;

import lombok.Getter;

import javax.swing.*;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;

public class CoolingMinigame extends JPanel {
    private JSlider temperatureSlider;
    private JLabel temperatureLabel;
    private JLabel statusLabel;
    private JButton stabilizeButton;
    private Timer driftTimer;
    private double driftDirection = 2;
    private int targetMin = 40;
    private int targetMax = 60;
    @Getter
    private boolean isActive = false;

    public CoolingMinigame() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("Стабилизация охлаждения"));

        // 1. Верхняя панель - инструкция
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        JLabel instruction = new JLabel("<html><center>Удержите температуру в зелёной зоне (40°C - 60°C)<br>" +
                "Температура будет медленно дрейфовать!</center></html>");
        instruction.setAlignmentX(CENTER_ALIGNMENT);
        topPanel.add(instruction);

        // 2. Центр - слайдер и индикатор
        JPanel centerPanel = new JPanel(new BorderLayout());

        // Создаём слайдер
        temperatureSlider = new JSlider(0, 100, 50);
        temperatureSlider.setMajorTickSpacing(20);
        temperatureSlider.setMinorTickSpacing(5);
        temperatureSlider.setPaintTicks(true);
        temperatureSlider.setPaintLabels(true);

        // Кастомная отрисовка слайдера
        temperatureSlider.setUI(new BasicSliderUI(temperatureSlider) {
            @Override
            public void paintTrack(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                Rectangle trackBounds = trackRect;

                // Рисуем фон трека
                g2d.setColor(Color.LIGHT_GRAY);
                g2d.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);

                // Рисуем зелёную зону (целевой диапазон)
                int greenStart = (targetMin * trackBounds.width) / 100;
                int greenWidth = ((targetMax - targetMin) * trackBounds.width) / 100;

                g2d.setColor(new Color(144, 238, 144, 150)); // Светло-зелёный с прозрачностью
                g2d.fillRect(trackBounds.x + greenStart, trackBounds.y, greenWidth, trackBounds.height);

                // Обводка трека
                g2d.setColor(Color.DARK_GRAY);
                g2d.drawRect(trackBounds.x, trackBounds.y, trackBounds.width - 1, trackBounds.height - 1);
            }

            @Override
            public void paintThumb(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                Rectangle thumbBounds = thumbRect;

                // Цвет ползунка в зависимости от положения
                int value = slider.getValue();
                if (value >= targetMin && value <= targetMax) {
                    g2d.setColor(Color.GREEN);
                } else if (value < targetMin) {
                    g2d.setColor(Color.BLUE);
                } else {
                    g2d.setColor(Color.RED);
                }

                // Рисуем круглый ползунок
                g2d.fillOval(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height);
                g2d.setColor(Color.BLACK);
                g2d.drawOval(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height);
            }
        });

        // Метка температуры
        temperatureLabel = new JLabel("50°C", SwingConstants.CENTER);
        temperatureLabel.setFont(new Font("Arial", Font.BOLD, 24));

        // Статус
        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        centerPanel.add(temperatureSlider, BorderLayout.CENTER);
        centerPanel.add(temperatureLabel, BorderLayout.NORTH);
        centerPanel.add(statusLabel, BorderLayout.SOUTH);

        // 3. Нижняя панель - кнопки
        JPanel bottomPanel = new JPanel(new FlowLayout());

        JButton startButton = new JButton("Начать стабилизацию");
        stabilizeButton = new JButton("Стабилизировать");
        stabilizeButton.setEnabled(false);

        startButton.addActionListener(e -> startMinigame());
        stabilizeButton.addActionListener(e -> checkStabilization());

        bottomPanel.add(startButton);
        bottomPanel.add(stabilizeButton);

        // 4. Собираем всё
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // Слушатель изменения слайдера
        temperatureSlider.addChangeListener(e -> {
            int temp = temperatureSlider.getValue();
            temperatureLabel.setText(temp + "°C");
            updateStatusColor(temp);
        });

        // Таймер дрейфа
        driftTimer = new Timer(500, e -> driftTemperature());
    }

    private void startMinigame() {
        isActive = true;
        stabilizeButton.setEnabled(true);
        temperatureSlider.setValue(50);
        driftTimer.start();
        statusLabel.setText("Стабилизируйте температуру!");
        statusLabel.setForeground(Color.BLUE);
    }

    private void driftTemperature() {
        if (!isActive) return;

        // Меняем направление дрейфа случайным образом
        if (Math.random() < 0.1) {
            driftDirection = -driftDirection;
        }

        // Добавляем немного случайности
        double drift = driftDirection + (Math.random() - 0.5) * 0.3;

        int newValue = temperatureSlider.getValue() + (int)(drift * 5);

        // Ограничиваем значения
        if (newValue < 0) newValue = 0;
        if (newValue > 100) newValue = 100;

        temperatureSlider.setValue(newValue);
    }

    private void updateStatusColor(int temp) {
        if (!isActive) return;

        if (temp >= targetMin && temp <= targetMax) {
            statusLabel.setText("✓ В целевой зоне");
            statusLabel.setForeground(Color.GREEN);
        } else if (temp < targetMin) {
            statusLabel.setText("⚠ Слишком холодно!");
            statusLabel.setForeground(Color.BLUE);
        } else {
            statusLabel.setText("⚠ Слишком горячо!");
            statusLabel.setForeground(Color.RED);
        }
    }

    private void checkStabilization() {
        if (!isActive) return;

        int temp = temperatureSlider.getValue();

        if (temp >= targetMin && temp <= targetMax) {
            // УСПЕХ!
            driftTimer.stop();
            isActive = false;
            stabilizeButton.setEnabled(false);

            statusLabel.setText("✅ УСПЕХ! Температура стабилизирована!");
            statusLabel.setForeground(new Color(0, 100, 0));

            // Здесь будет отправка на сервер
            JOptionPane.showMessageDialog(this,
                    "Мини-игра 'Охлаждение' пройдена успешно!\n" +
                            "Температура: " + temp + "°C\n" +
                            "Сообщение будет отправлено на сервер.",
                    "Успех!",
                    JOptionPane.INFORMATION_MESSAGE);

        } else {
            // НЕУДАЧА
            statusLabel.setText("❌ Не в целевой зоне! Попробуйте ещё");
            statusLabel.setForeground(Color.RED);
        }
    }

    public void reset() {
        driftTimer.stop();
        isActive = false;
        stabilizeButton.setEnabled(false);
        temperatureSlider.setValue(50);
        statusLabel.setText(" ");
    }
}