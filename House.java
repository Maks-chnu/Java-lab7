package Java_lab7;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Random;

/**
 * Базовий клас будинку (мобільного модуля міста майбутнього).
 * Після появи будинок рухається по прямій до випадкової цільової точки у своїй зоні
 * (чверті області симуляції). Якщо будинок з'явився вже в цій зоні або досяг цілі — зупиняється.
 */
public abstract class House {

    private static final double MARGIN = 18;
    private static int counter = 0;

    protected final int id;
    protected double x;        // поточні координати центру
    protected double y;
    protected final double startX;
    protected final double startY;
    protected final double targetX;  // цільова точка
    protected final double targetY;
    protected boolean moving;

    protected House(double x, double y, Rectangle2D zone, Random rnd) {
        this.id = nextId();
        this.x = x;
        this.y = y;
        this.startX = x;
        this.startY = y;
        if (zone.contains(x, y)) {
            // Об'єкт одразу згенерований у своїй зоні — він нерухомий
            targetX = x;
            targetY = y;
            moving = false;
        } else {
            // Випадкова цільова точка всередині зони (з відступом MARGIN, щоб будинок не виходив за край)
            targetX = zone.getX() + MARGIN + rnd.nextDouble() * (zone.getWidth() - 2 * MARGIN);
            targetY = zone.getY() + MARGIN + rnd.nextDouble() * (zone.getHeight() - 2 * MARGIN);
            moving = true;
        }
    }

    private static synchronized int nextId() {
        return ++counter;
    }

    /**
     * Переміщує будинок до цілі на відстань speed·dt.
     * @return true, якщо будинок саме зараз досяг цілі
     */
    public boolean moveTowardsTarget(double speed, double dt) {
        if (!moving) {
            return false;
        }
        double dx = targetX - x;
        double dy = targetY - y;
        double distance = Math.hypot(dx, dy);
        double step = speed * dt;
        if (step >= distance) {
            x = targetX;          // не "проскакуємо" ціль
            y = targetY;
            moving = false;
            return true;
        }
        x += dx / distance * step;  // рух уздовж одиничного вектора напрямку
        y += dy / distance * step;
        return false;
    }

    public boolean isMoving() {
        return moving;
    }

    public int getId() {
        return id;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public abstract String typeName();

    public abstract void draw(Graphics2D g);

    @Override
    public String toString() {
        return String.format("%s #%d (%.0f; %.0f)", typeName(), id, x, y);
    }
}
