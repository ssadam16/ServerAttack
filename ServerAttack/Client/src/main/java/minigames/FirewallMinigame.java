package minigames;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FirewallMinigame extends JPanel {
    private JLabel statusLabel;
    private JButton[] ruleButtons;
    private List<String> rules;
    private List<String> correctOrder = Arrays.asList("BLOCK", "ALLOW", "LOG", "ALERT");

    public FirewallMinigame() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("Настройка фаервола"));

        JLabel instruction = new JLabel("<html><center>Расставьте правила в правильном порядке<br>BLOCK → ALLOW → LOG → ALERT</center></html>");
        instruction.setHorizontalAlignment(SwingConstants.CENTER);
        add(instruction, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(2, 2, 10, 10));

        rules = Arrays.asList(
                "ALLOW admin 192.168.1.1",
                "BLOCK hacker 10.0.0.5",
                "LOG all *.*.*.*",
                "ALERT suspicious activity"
        );
        Collections.shuffle(rules);

        ruleButtons = new JButton[4];
        for (int i = 0; i < 4; i++) {
            ruleButtons[i] = new JButton(rules.get(i));
            int index = i;
            ruleButtons[i].addActionListener(e -> checkRule(index));
            centerPanel.add(ruleButtons[i]);
        }

        add(centerPanel, BorderLayout.CENTER);

        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        JButton startButton = new JButton("Начать настройку");
        startButton.addActionListener(e -> startGame());
        add(startButton, BorderLayout.NORTH);
    }

    private void startGame() {
        Collections.shuffle(rules);
        for (int i = 0; i < 4; i++) {
            ruleButtons[i].setText(rules.get(i));
            ruleButtons[i].setEnabled(true);
            ruleButtons[i].setBackground(null);
        }
        statusLabel.setText("Соберите правила в правильном порядке!");
    }

    private void checkRule(int buttonIndex) {
        String rule = ruleButtons[buttonIndex].getText();
        String firstWord = rule.split(" ")[0];

        if (firstWord.equals(correctOrder.get(0))) {
            ruleButtons[buttonIndex].setBackground(Color.GREEN);
            ruleButtons[buttonIndex].setEnabled(false);
            correctOrder.remove(0);

            if (correctOrder.isEmpty()) {
                statusLabel.setText("УСПЕХ! Фаервол настроен!");
                JOptionPane.showMessageDialog(this, "Фаервол успешно настроен!", "Успех", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            ruleButtons[buttonIndex].setBackground(Color.RED);
            statusLabel.setText("Неправильный порядок! Попробуйте еще раз.");
        }
    }
}