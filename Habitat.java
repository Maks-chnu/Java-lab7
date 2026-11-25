package Java_lab7;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Область симуляції (місто) та спільна колекція об'єктів.
 * Колекцію одночасно змінюють потоки руху і читає потік EDT під час малювання,
 * тому вона потокобезпечна (Collections.synchronizedList), а обхід виконується в synchronized (houses).
 */
public class Habitat {

    public static final double SPEED = 70;   // V — швидкість руху, пікселів за секунду

    private final int width;
    private final int height;
    private final List<House> houses = Collections.synchronizedList(new ArrayList<>());
    private final Random rnd = new Random();

    public Habitat(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    /** Спільна колекція. Під час обходу потрібно синхронізуватися на ній: synchronized (getHouses()) {...} */
    public List<House> getHouses() {
        return houses;
    }

    public synchronized House spawnCapital() {
        House h = new CapitalHouse(randomX(), randomY(), width, height, rnd);
        houses.add(h);
        return h;
    }

    public synchronized House spawnWooden() {
        House h = new WoodenHouse(randomX(), randomY(), width, height, rnd);
        houses.add(h);
        return h;
    }

    public synchronized House spawnRandom() {
        return rnd.nextBoolean() ? spawnCapital() : spawnWooden();
    }

    public void clear() {
        houses.clear();
    }

    private double randomX() {
        return 20 + rnd.nextDouble() * (width - 40);
    }

    private double randomY() {
        return 20 + rnd.nextDouble() * (height - 40);
    }

    /** Кількість об'єктів заданого типу: [усього, у русі] */
    public int[] count(Class<? extends House> type) {
        int total = 0;
        int moving = 0;
        synchronized (houses) {
            for (House h : houses) {
                if (type.isInstance(h)) {
                    total++;
                    if (h.isMoving()) {
                        moving++;
                    }
                }
            }
        }
        return new int[] {total, moving};
    }
}
