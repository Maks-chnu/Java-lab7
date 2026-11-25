package Java_lab7;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Random;

/** Дерев'яний будинок: рухається до випадкової точки в правій нижній чверті [w/2, h/2] розміром w/2 × h/2. */
public class WoodenHouse extends House {

    public WoodenHouse(double x, double y, double width, double height, Random rnd) {
        super(x, y, zone(width, height), rnd);
    }

    public static Rectangle2D zone(double width, double height) {
        return new Rectangle2D.Double(width / 2, height / 2, width / 2, height / 2);
    }

    @Override
    public String typeName() {
        return "Дерев'яний будинок";
    }

    @Override
    public void draw(Graphics2D g) {
        int cx = (int) Math.round(x);
        int cy = (int) Math.round(y);
        // Дерев'яна хатинка з трикутним дахом
        g.setColor(new Color(180, 130, 70));
        g.fillRect(cx - 11, cy - 4, 22, 16);
        g.setColor(new Color(110, 60, 25));
        g.fillPolygon(new int[] {cx - 14, cx, cx + 14}, new int[] {cy - 4, cy - 16, cy - 4}, 3);
        g.setColor(new Color(90, 50, 20));
        g.fillRect(cx - 3, cy + 3, 6, 9);   // двері
        g.setStroke(new BasicStroke(1.2f));
        g.drawRect(cx - 11, cy - 4, 22, 16);
    }
}
