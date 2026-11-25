package Java_lab7;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Лабораторна робота №7, завдання 2, варіант 7.
 * Симуляція руху будинків двома потоками-спадкоємцями BaseAI
 * з паузою/відновленням (wait/notifyAll) та зміною пріоритетів потоків.
 */
public class Task2Simulation extends JFrame {

    private static final long serialVersionUID = 1L;

    static final int SPAWN_INTERVAL_MS = 2000; // N — інтервал появи нових будинків

    // Пункти випадаючого списку пріоритетів і відповідні константи Thread
    static final String[] PRIORITY_NAMES = {"MIN_PRIORITY (1)", "NORM_PRIORITY (5)", "MAX_PRIORITY (10)"};
    static final int[] PRIORITY_VALUES = {Thread.MIN_PRIORITY, Thread.NORM_PRIORITY, Thread.MAX_PRIORITY};

    final transient Habitat habitat = new Habitat(640, 460);
    final SimulationPanel panel = new SimulationPanel(habitat);
    final transient CapitalHouseAI capitalAI = new CapitalHouseAI(habitat);
    final transient WoodenHouseAI woodenAI = new WoodenHouseAI(habitat);

    final ThreadControls capitalControls = new ThreadControls("Капітальні будинки", capitalAI, CapitalHouse.class);
    final ThreadControls woodenControls = new ThreadControls("Дерев'яні будинки", woodenAI, WoodenHouse.class);
    final JCheckBox autoSpawn = new JCheckBox("Автопоява кожні " + SPAWN_INTERVAL_MS / 1000 + " с", true);

    public Task2Simulation() {
        super("ЛР7, завдання 2: потоки BaseAI (варіант 7)");

        // ===== Панель керування праворуч =====
        JPanel controls = new JPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        controls.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        controls.add(capitalControls);
        controls.add(Box.createVerticalStrut(8));
        controls.add(woodenControls);
        controls.add(Box.createVerticalStrut(8));

        JPanel common = new JPanel(new GridLayout(0, 1, 4, 4));
        common.setBorder(BorderFactory.createTitledBorder("Об'єкти"));
        JButton add = new JButton("Додати 3 + 3 будинки");
        add.addActionListener(e -> {
            for (int i = 0; i < 3; i++) {
                habitat.spawnCapital();
                habitat.spawnWooden();
            }
        });
        JButton clear = new JButton("Очистити область");
        clear.addActionListener(e -> habitat.clear());
        common.add(add);
        common.add(clear);
        common.add(autoSpawn);
        controls.add(common);
        controls.add(Box.createVerticalGlue());
        // Фіксована ширина панелі керування, щоб зміна написів не стискала область симуляції
        controls.setPreferredSize(new Dimension(320, habitat.getHeight()));

        add(panel, BorderLayout.CENTER);
        add(controls, BorderLayout.EAST);

        // Коректне завершення потоків під час закриття вікна
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                capitalAI.shutdown();
                woodenAI.shutdown();
                System.exit(0);
            }
        });
        pack();
        setLocationRelativeTo(null);

        for (int i = 0; i < 4; i++) {
            habitat.spawnCapital();
            habitat.spawnWooden();
        }

        // Усі таймери Swing виконуються в EDT
        new Timer(SPAWN_INTERVAL_MS, e -> {
            if (autoSpawn.isSelected()) {
                habitat.spawnRandom();
            }
        }).start();
        new Timer(30, e -> {
            panel.repaint();            // малювання — лише в EDT через paintComponent()
            capitalControls.refresh();
            woodenControls.refresh();
        }).start();
    }

    void startThreads() {
        capitalAI.start();
        woodenAI.start();
        System.out.println("Запущено потоки " + capitalAI.getName() + " і " + woodenAI.getName());
    }

    /** Блок керування одним потоком: стан, пауза/продовження, пріоритет. */
    static class ThreadControls extends JPanel {
        private static final long serialVersionUID = 1L;

        final transient BaseAI ai;
        final Class<? extends House> type;
        final JLabel stateLabel = new JLabel();
        final JLabel threadLabel = new JLabel();
        final JLabel countLabel = new JLabel();
        final JButton pauseButton = new JButton("Пауза");
        final JButton resumeButton = new JButton("Продовжити");
        final JComboBox<String> priorityBox = new JComboBox<>(PRIORITY_NAMES);

        ThreadControls(String title, BaseAI ai, Class<? extends House> type) {
            this.ai = ai;
            this.type = type;
            setLayout(new GridLayout(0, 1, 4, 4));
            setBorder(BorderFactory.createTitledBorder(title));

            stateLabel.setFont(stateLabel.getFont().deriveFont(Font.BOLD));
            pauseButton.addActionListener(e -> ai.pauseAI());
            resumeButton.addActionListener(e -> ai.resumeAI());

            priorityBox.setSelectedIndex(1); // NORM_PRIORITY за замовчуванням
            priorityBox.addActionListener(e -> {
                int priority = PRIORITY_VALUES[priorityBox.getSelectedIndex()];
                ai.setPriority(priority);    // динамічна зміна пріоритету потоку
                System.out.println("Пріоритет потоку " + ai.getName() + " = " + ai.getPriority());
            });

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
            buttons.add(pauseButton);
            buttons.add(resumeButton);
            JPanel priorityRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
            priorityRow.add(new JLabel("Пріоритет:"));
            priorityRow.add(priorityBox);

            add(stateLabel);
            add(threadLabel);
            add(countLabel);
            add(buttons);
            add(priorityRow);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, getPreferredSize().height));
        }

        /** Оновлення написів (викликається з таймера в EDT). */
        void refresh() {
            boolean paused = ai.isPausedAI();
            stateLabel.setText(paused ? "ПАУЗА" : "ПРАЦЮЄ");
            threadLabel.setText(ai.getName() + ": " + ai.getState() + ", пріоритет " + ai.getPriority());
            stateLabel.setForeground(paused ? new Color(180, 0, 0) : new Color(0, 120, 0));
            int[] c = habitatCount();
            countLabel.setText("Об'єктів: " + c[0] + ", у русі: " + c[1]);
            pauseButton.setEnabled(!paused);
            resumeButton.setEnabled(paused);
        }

        private int[] habitatCount() {
            return ai.habitat.count(type);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Task2Simulation frame = new Task2Simulation();
            frame.setVisible(true);
            frame.startThreads();
        });
    }
}
