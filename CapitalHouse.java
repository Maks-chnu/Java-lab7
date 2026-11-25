package Java_lab7;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Random;

/** Капітальний будинок: рухається до випадкової точки в лівій верхній чверті [0,0] розміром w/2 × h/2. */
public class CapitalHouse extends House {

    public CapitalHouse(double x, double y, double width, double height, Random rnd) {
        super(x, y, zone(width, height), rnd);
    }

    public static Rectangle2D zone(double width, double height) {
        return new Rectangle2D.Double(0, 0, width / 2, height / 2);
    }

    @Override
    public String typeName() {
        return "Капітальний будинок";
    }

    @Override
    public void draw(Graphics2D g) {
        int cx = (int) Math.round(x);
        int cy = (int) Math.round(y);
        // Багатоповерховий цегляний будинок з вікнами
        g.setColor(new Color(170, 70, 50));
        g.fillRect(cx - 10, cy - 14, 20, 28);
        g.setColor(new Color(255, 230, 140));
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 2; col++) {
                g.fillRect(cx - 7 + col * 9, cy - 11 + row * 8, 5, 5);
            }
        }
        g.setColor(new Color(90, 30, 20));
        g.setStroke(new BasicStroke(1.5f));
        g.drawRect(cx - 10, cy - 14, 20, 28);
    }
}
