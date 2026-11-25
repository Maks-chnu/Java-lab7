package Java_lab7;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import javax.swing.JPanel;

/** Панель відображення області симуляції. paintComponent() завжди виконується в потоці EDT. */
public class SimulationPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final transient Habitat habitat;

    public SimulationPanel(Habitat habitat) {
        this.habitat = habitat;
        setPreferredSize(new Dimension(habitat.getWidth(), habitat.getHeight()));
        setBackground(new Color(235, 245, 235));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = habitat.getWidth();
        int h = habitat.getHeight();

        // Цільові зони (чверті)
        Rectangle2D capitalZone = CapitalHouse.zone(w, h);
        Rectangle2D woodenZone = WoodenHouse.zone(w, h);
        g2.setColor(new Color(230, 200, 190));
        g2.fill(capitalZone);
        g2.setColor(new Color(225, 210, 175));
        g2.fill(woodenZone);
        g2.setColor(new Color(150, 150, 150));
        g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[] {6f, 6f}, 0f));
        g2.drawLine(w / 2, 0, w / 2, h);
        g2.drawLine(0, h / 2, w, h / 2);
        g2.setColor(Color.DARK_GRAY);
        g2.setFont(getFont().deriveFont(Font.BOLD, 12f));
        g2.drawString("Зона капітальних будинків", 8, 16);
        g2.drawString("Зона дерев'яних будинків", w / 2 + 8, h / 2 + 16);

        // Об'єкти: синхронізований доступ до спільної колекції
        g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[] {3f, 4f}, 0f));
        synchronized (habitat.getHouses()) {
            for (House house : habitat.getHouses()) {
                if (house.isMoving()) {
                    // Пунктир до цільової точки та хрестик цілі
                    g2.setColor(new Color(120, 120, 120, 150));
                    g2.drawLine((int) house.x, (int) house.y, (int) house.targetX, (int) house.targetY);
                    g2.drawLine((int) house.targetX - 4, (int) house.targetY - 4, (int) house.targetX + 4, (int) house.targetY + 4);
                    g2.drawLine((int) house.targetX - 4, (int) house.targetY + 4, (int) house.targetX + 4, (int) house.targetY - 4);
                }
            }
            for (House house : habitat.getHouses()) {
                house.draw(g2);
            }
        }
        g2.dispose();
    }
}
