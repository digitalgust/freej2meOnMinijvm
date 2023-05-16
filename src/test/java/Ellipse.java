import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Ellipse extends JPanel {
    public void paint(Graphics g) {
        g.setColor(new Color(0xff808080));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.red);

        int x0 = 100, y0 = 100, a = 80, b = 50;
        int x = 0, y = b;
        int d1 = b * b - a * a * b + a * a / 4;
        int dx = 2 * b * b * x;
        int dy = 2 * a * a * y;
        double startRadian = Math.toRadians(0);// Math.PI /4; // 起始角度
        double endRadian = Math.toRadians(800); // 终止角度
        while (dx < dy) {

            double radian;
            radian = Math.atan2(y, x);
            if (radian >= startRadian && radian <= endRadian) {
                g.drawLine(x0 + x, y0 - y, x0 + x, y0 - y);
            }
            radian = Math.atan2(y, -x);
            if (radian >= startRadian && radian <= endRadian) {
                g.drawLine(x0 - x, y0 - y, x0 - x, y0 - y);
            }
            radian = Math.atan2(-y, x) + Math.PI * 2;
            if (radian >= startRadian && radian <= endRadian) {
                g.drawLine(x0 + x, y0 + y, x0 + x, y0 + y);
            }
            radian = Math.atan2(-y, -x) + Math.PI * 2;
            if (radian >= startRadian && radian <= endRadian) {
                g.drawLine(x0 - x, y0 + y, x0 - x, y0 + y);
            }


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
            double radian;
            radian = Math.atan2(y, x);
            if (radian >= startRadian && radian <= endRadian) {
                g.drawLine(x0 + x, y0 - y, x0 + x, y0 - y);
            }
            radian = Math.atan2(y, -x);
            if (radian >= startRadian && radian <= endRadian) {
                g.drawLine(x0 - x, y0 - y, x0 - x, y0 - y);
            }
            radian = Math.atan2(-y, x) + Math.PI * 2;
            if (radian >= startRadian && radian <= endRadian) {
                g.drawLine(x0 + x, y0 + y, x0 + x, y0 + y);
            }
            radian = Math.atan2(-y, -x) + Math.PI * 2;
            if (radian >= startRadian && radian <= endRadian) {
                g.drawLine(x0 - x, y0 + y, x0 - x, y0 + y);
            }


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
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.getContentPane().add(new Ellipse());
        frame.setSize(300, 200);
        frame.setVisible(true);
    }
}
