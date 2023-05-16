import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class EllipseFill extends JPanel {
    public void paint(Graphics g) {
        int x0 = 100, y0 = 100, a = 80, b = 50;
        int x = 0, y = b;
        int d1 = b * b - a * a * b + a * a / 4;
        int dx = 2 * b * b * x;
        int dy = 2 * a * a * y;
        while (dx < dy) {
            g.setColor(Color.black);
            g.fillRect(x0 + x, y0 + y, 1, 1);
            g.fillRect(x0 - x, y0 + y, 1, 1);
            g.fillRect(x0 + x, y0 - y, 1, 1);
            g.fillRect(x0 - x, y0 - y, 1, 1);
            if (d1 < 0) {
                x++;
                dx += 2 * b * b;
                d1 += dx + b * b;
            } else {
                x++;
                y--;
                dx += 2 * b * b;
                dy -= 2 * a * a;
                d1 += dx - dy + b * b;
            }
        }
        int d2 = (int) (b * b * (x + 0.5) * (x + 0.5) + a * a * (y - 1) * (y - 1) - a * a * b * b);
        while (y >= 0) {
            g.setColor(Color.black);
            g.fillRect(x0 + x, y0 + y, 1, 1);
            g.fillRect(x0 - x, y0 + y, 1, 1);
            g.fillRect(x0 + x, y0 - y, 1, 1);
            g.fillRect(x0 - x, y0 - y, 1, 1);
            if (d2 > 0) {
                y--;
                dy -= 2 * a * a;
                d2 += a * a - dy;
            } else {
                y--;
                x++;
                dx += 2 * b * b;
                dy -= 2 * a * a;
                d2 += dx - dy + a * a;
            }
        }

        g.fillArc(100, 150, 80, 40, 90, 315);
        g.setColor(Color.pink);
        g.drawArc(100, 150, 80, 40, 90, 315);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.getContentPane().add(new EllipseFill());
        frame.setSize(300,400);
        frame.setVisible(true);
    }
}
