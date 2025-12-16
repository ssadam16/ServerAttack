import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Ellipse2D;
import java.util.LinkedList;

public class LoadGraph extends JPanel {
    private LinkedList<Double> loadHistory;
    private double currentLoad;
    private Color graphColor;

    public LoadGraph() {
        this.loadHistory = new LinkedList<>();
        this.currentLoad = 50.0;
        this.graphColor = new Color(0, 150, 255);

        for (int i = 0; i < 50; i++) {
            loadHistory.add(50.0);
        }

        setPreferredSize(new Dimension(300, 150));
        setBorder(BorderFactory.createTitledBorder("График нагрузки"));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int padding = 20;

        // 1. Фон
        g2d.setColor(new Color(240, 240, 240));
        g2d.fillRect(0, 0, width, height);

        // 2. Сетка
        g2d.setColor(new Color(200, 200, 200));
        for (int i = 1; i < 5; i++) {
            int y = height - padding - (i * (height - 2 * padding) / 5);
            g2d.drawLine(padding, y, width - padding, y);
        }

        // 3. Оси
        g2d.setColor(Color.BLACK);
        g2d.drawLine(padding, height - padding, width - padding, height - padding);
        g2d.drawLine(padding, padding, padding, height - padding);

        // 4. Подписи
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        g2d.drawString("0%", padding - 15, height - padding + 15);
        g2d.drawString("100%", padding - 20, padding - 5);
        g2d.drawString("Время →", width - 40, height - padding + 15);

        // 5. Рисуем график и ЗАПОМИНАЕМ последнюю точку
        int lastX = 0;
        int lastY = 0;

        if (loadHistory.size() > 1) {
            g2d.setColor(graphColor);
            g2d.setStroke(new BasicStroke(2.0f));

            int stepX = (width - 2 * padding) / (loadHistory.size() - 1);

            for (int i = 0; i < loadHistory.size() - 1; i++) {
                double load1 = loadHistory.get(i);
                double load2 = loadHistory.get(i + 1);

                int x1 = padding + i * stepX;
                int y1 = height - padding - (int)((load1 / 100.0) * (height - 2 * padding));

                int x2 = padding + (i + 1) * stepX;
                int y2 = height - padding - (int)((load2 / 100.0) * (height - 2 * padding));

                g2d.draw(new Line2D.Double(x1, y1, x2, y2));

                // Запоминаем последнюю точку
                if (i == loadHistory.size() - 2) {
                    lastX = x2;
                    lastY = y2;
                }
            }
        } else if (loadHistory.size() == 1) {
            // Если только одна точка
            lastX = width - padding;
            lastY = height - padding - (int)((loadHistory.getLast() / 100.0) * (height - 2 * padding));
        }

        // 6. Рисуем круг индикатора НА ПОСЛЕДНЕЙ ТОЧКЕ ГРАФИКА
        if (!loadHistory.isEmpty()) {
            // Цвет круга в зависимости от нагрузки
            double lastLoad = loadHistory.getLast();
            if (lastLoad >= 80) {
                g2d.setColor(Color.RED);
            } else if (lastLoad > 50) {
                g2d.setColor(Color.ORANGE);
            } else {
                g2d.setColor(Color.GREEN);
            }

            // Рисуем круг на последней точке
            g2d.fill(new Ellipse2D.Double(lastX - 6, lastY - 6, 12, 12));
            g2d.setColor(Color.BLACK);
            g2d.draw(new Ellipse2D.Double(lastX - 6, lastY - 6, 12, 12));
        }

        // 7. Текстовое значение (оставляем в правом верхнем углу)
        g2d.setColor(Color.RED);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        String loadText = String.format("Текущая: %.1f%%", currentLoad);
        g2d.drawString(loadText, width - 100, padding + 15);
    }

    public void updateLoad(double newLoad) {
        this.currentLoad = newLoad;
        loadHistory.add(newLoad);

        if (loadHistory.size() > 50) {
            loadHistory.removeFirst();
        }

        repaint();
    }

    public void setGraphColor(Color color) {
        this.graphColor = color;
        repaint();
    }
}