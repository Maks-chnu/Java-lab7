package Java_lab7;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Лабораторна робота №7, завдання 1, варіант 7.
 * Два окремі потоки керують рухом капітальних і дерев'яних будинків.
 * Потік капітальних будинків створено успадкуванням від Thread,
 * потік дерев'яних — реалізацією інтерфейсу Runnable.
 */
public class Task1HouseThreads extends JFrame {

    private static final long serialVersionUID = 1L;

    static final int SPAWN_INTERVAL_MS = 2000; // N — інтервал появи нових будинків
    static final int FRAME_MS = 20;            // період оновлення координат у потоках

    final transient Habitat habitat = new Habitat(640, 440);
    final SimulationPanel panel = new SimulationPanel(habitat);
    final JLabel info = new JLabel();
    final transient CapitalHouseThread capitalThread = new CapitalHouseThread(habitat);
    final transient Thread woodenThread = new Thread(new WoodenHouseMover(habitat), "Потік-Дерев'яні");

    public Task1HouseThreads() {
        super("ЛР7, завдання 1: потоки руху будинків");

        JButton addButton = new JButton("Додати будинки");
        addButton.addActionListener(e -> {
            for (int i = 0; i < 3; i++) {
                habitat.spawnCapital();
                habitat.spawnWooden();
            }
        });
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.add(addButton);
        bottom.add(info);

        add(panel, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);

        // Початкові об'єкти
        for (int i = 0; i < 4; i++) {
            habitat.spawnCapital();
            habitat.spawnWooden();
        }

        // Таймер Swing (виконується в EDT): періодична поява нового будинку раз на N мс
        new Timer(SPAWN_INTERVAL_MS, e -> habitat.spawnRandom()).start();
        // Таймер перемальовування: малювання виконується лише в EDT
        new Timer(30, e -> {
            panel.repaint();
            int[] c = habitat.count(CapitalHouse.class);
            int[] w = habitat.count(WoodenHouse.class);
            info.setText(String.format("Капітальні: %d (у русі %d)   Дерев'яні: %d (у русі %d)",
                    c[0], c[1], w[0], w[1]));
        }).start();
    }

    void startThreads() {
        woodenThread.setDaemon(true);
        woodenThread.setPriority(Thread.NORM_PRIORITY);
        capitalThread.start(); // start() створює новий потік, який виконає run()
        woodenThread.start();
        System.out.println("Запущено потоки: " + capitalThread.getName() + ", " + woodenThread.getName());
    }

    /** Спосіб 1: підклас Thread з перевизначеним методом run(). */
    static class CapitalHouseThread extends Thread {
        private final Habitat habitat;

        CapitalHouseThread(Habitat habitat) {
            super("Потік-Капітальні");
            this.habitat = habitat;
            setDaemon(true); // потік-демон завершиться разом з програмою
            setPriority(Thread.NORM_PRIORITY);
        }

        @Override
        public void run() {
            moveLoop(habitat, CapitalHouse.class);
        }
    }

    /** Спосіб 2: клас, що реалізує Runnable; об'єкт передається в конструктор Thread. */
    static class WoodenHouseMover implements Runnable {
        private final Habitat habitat;

        WoodenHouseMover(Habitat habitat) {
            this.habitat = habitat;
        }

        @Override
        public void run() {
            moveLoop(habitat, WoodenHouse.class);
        }
    }

    /** Цикл потоку: кожні FRAME_MS мс зсуває всі будинки свого типу на V·dt. */
    static void moveLoop(Habitat habitat, Class<? extends House> type) {
        String name = Thread.currentThread().getName();
        long last = System.nanoTime();
        while (!Thread.currentThread().isInterrupted()) {
            long now = System.nanoTime();
            double dt = (now - last) / 1e9; // реальний час з попереднього кроку, с
            last = now;
            List<House> arrived = new ArrayList<>();
            synchronized (habitat.getHouses()) { // монопольний доступ до спільної колекції
                for (House house : habitat.getHouses()) {
                    if (type.isInstance(house) && house.moveTowardsTarget(Habitat.SPEED, dt)) {
                        arrived.add(house);
                    }
                }
            }
            for (House house : arrived) {
                System.out.println("[" + name + "] " + house + " досяг цільової точки і зупинився");
            }
            try {
                Thread.sleep(FRAME_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // відновлюємо прапорець переривання -> вихід з циклу
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Task1HouseThreads frame = new Task1HouseThreads();
            frame.setVisible(true);
            frame.startThreads();
        });
    }
}
