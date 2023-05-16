
import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class EllipseDraw extends JPanel {
    public void paint(Graphics g) {
        int x0 = 100; // 椭圆中心横坐标
        int y0 = 100; // 椭圆中心纵坐标
        int a = 50; // 长轴
        int b = 30; // 短轴

        int x = 0;
        int y = b;

        double d1 = b * b + a * a * (-b + 0.25);
        while (b * b * (x + 1) < a * a * (y - 0.5)) {
            drawPixel(x0, y0, x, y, g);
            if (d1 < 0) {
                d1 += b * b * (2 * x + 3);
            } else {
                d1 += b * b * (2 * x + 3) + a * a * (-2 * y + 2);
                y--;
            }
            x++;
        }

        double d2 = b * b * (x + 0.5) * (x + 0.5) + a * a * (y - 1) * (y - 1) - a * a * b * b;
        while (y >= 0) {
            drawPixel(x0, y0, x, y, g);
            if (d2 > 0) {
                d2 += a * a * (-2 * y + 3);
            } else {
                d2 += b * b * (2 * x + 2) + a * a * (-2 * y + 3);
                x++;
            }
            y--;
        }

        drawOval(g, 100, 100, 40, 90, 0, 270);
    }

    private void drawPixel(int x0, int y0, int x, int y, Graphics g) {
        g.setColor(Color.RED);
        g.drawLine(x + x0, -y + y0, -x + x0, -y + y0);
        g.drawLine(x + x0, y + y0, -x + x0, y + y0);
    }

    public void drawOval(Graphics g, int x0, int y0, int a, int b, int startAngle, int endAngle) {
        for (int i = 0; i < 360; i++) {
            if (i >= startAngle && i <= endAngle) {
                double x = x0 + a * Math.cos(i * Math.PI / 180);
                double y = y0 + b * Math.sin(i * Math.PI / 180);
                g.drawLine((int) x, (int) y, (int) x, (int) y);
            }
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.getContentPane().add(new EllipseDraw());
        frame.setSize(300, 300);
        frame.setVisible(true);
    }
}