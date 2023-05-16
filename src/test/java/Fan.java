import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Fan extends JPanel {
    Graphics g;

    public void paint(Graphics g) {
        this.g = g;

//        int x0 = 100; // 圆心横坐标
//        int y0 = 100; // 圆心纵坐标
//        int r = 50; // 半径
//        int startAngle = 0; // 起始角度
//        int endAngle = 90; // 终止角度
//
//        double radianStart = Math.toRadians(startAngle); // 起始角度转换为弧度
//        double radianEnd = Math.toRadians(endAngle); // 终止角度转换为弧度
//
//        int x = r;
//        int y = 0;
//        int d = 3 - 2 * r;
//
//        while (x >= y) {
//            drawPixel(x0, y0, x, y, g);
//            if (d < 0) {
//                d = d + 4 * y + 6;
//            } else {
//                d = d + 4 * (y - x) + 10;
//                x--;
//            }
//            y++;
//        }
//
//        x = (int) (r * Math.cos(radianStart));
//        y = (int) (r * Math.sin(radianStart));
//        int x1 = x;
//        int y1 = y;
//
//        while (x >= y) {
//            drawPixel(x0, y0, x, y, g);
//            if (d < 0) {
//                d = d + 4 * y + 6;
//            } else {
//                d = d + 4 * (y - x) + 10;
//                x--;
//            }
//            y++;
//        }
//
//        x = (int) (r * Math.cos(radianEnd));
//        y = (int) (r * Math.sin(radianEnd));
//        int x2 = x;
//        int y2 = y;
//
//        while (x >= y) {
//            drawPixel(x0, y0, x, y, g);
//            if (d < 0) {
//                d = d + 4 * y + 6;
//            } else {
//                d = d + 4 * (y - x) + 10;
//                x--;
//            }
//            y++;
//        }
//
//        g.setColor(Color.RED);
//        g.drawLine(x1 + x0, -y1 + y0, x2 + x0, -y2 + y0);
        g.setColor(Color.BLACK);
        g.drawRect(100, 100, 40, 80);
        g.setColor(Color.RED);
        drawEllipse(100, 100, 40, 80, 0, 90);
        g.setColor(Color.BLACK);
        g.drawRect(150, 100, 40, 80);
        g.setColor(Color.GREEN);
        g.drawArc(150, 100, 40, 80, 0, 270);
//        drawEllipse(100, 100, 40, 80);

        //drawFan1(g, 150, 150, 30, 0, 270);
    }

    private void drawPixel(int x0, int y0, int x, int y, Graphics g) {
        g.setColor(Color.RED);
        g.drawLine(x + x0, -y + y0, -x + x0, -y + y0);
        g.drawLine(y + x0, -x + y0, -y + x0, -x + y0);
    }


    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.getContentPane().add(new Fan());
        frame.setSize(300, 300);
        frame.setVisible(true);
    }


    public void drawEllipse(int x0, int y0, int width, int height, int startAngle, int endAngle) {
        double a = width / 2.0;
        double b = height / 2.0;
        double x = 0;
        double y = b;
        double d1 = b * b + a * a * (-b + 0.25);
        double dx = 2 * b * b * x;
        double dy = 2 * a * a * y;
        while (dx < dy) {
            if (startAngle <= Math.toDegrees(Math.atan2(y, x))) {
                fillSymmetricPoints(x0, y0, x, y);
            }
            if (endAngle <= Math.toDegrees(Math.atan2(y, x))) {
                return;
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
        double d2 = b * b * (x + 0.5) * (x + 0.5) + a * a * (y - 1) * (y - 1) - a * a * b * b;
        while (y >= 0) {
            if (startAngle <= Math.toDegrees(Math.atan2(y, x))) {
                fillSymmetricPoints(x0, y0, x, y);
            }
            if (endAngle <= Math.toDegrees(Math.atan2(y, x))) {
                return;
            }
            if (d2 > 0) {
                y--;
                dy -= 2 * a * a;
                d2 += a * a - dy;
            } else {
                x++;
                y--;
                dx += 2 * b * b;
                dy -= 2 * a * a;
                d2 += dx - dy + a * a;
            }
        }
    }

    private void fillSymmetricPoints(int x0, int y0, double x, double y) {
        fillPixel(x0 + (int) Math.round(x), y0 + (int) Math.round(y));
        fillPixel(x0 - (int) Math.round(x), y0 + (int) Math.round(y));
        fillPixel(x0 - (int) Math.round(x), y0 - (int) Math.round(y));
        fillPixel(x0 + (int) Math.round(x), y0 - (int) Math.round(y));
    }


    public void drawEllipse1(int x0, int y0, int width, int height, int startAngle, int arcAngle) {

        startAngle = startAngle % 360;
        int endAngle = startAngle + arcAngle;

        float startRadian = (float) (Math.toRadians(startAngle) % (Math.PI * 2));
        float endRadian = (float) (Math.toRadians(endAngle) % (Math.PI * 2));
        int a = width / 2;
        int b = height / 2;
        int x = 0;
        int y = b;
        int d1 = (int) (b * b + a * a * (-b + 0.25f));
        int dx = 2 * b * b * x;
        int dy = 2 * a * a * y;
        while (dx < dy) {
            fillSymmetricPoints(x0, y0, x, y, startRadian, endRadian, a, b);
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
        int d2 = (int) (b * b * (x + 0.5f) * (x + 0.5f) + a * a * (y - 1) * (y - 1) - a * a * b * b);
        while (y >= 0) {
            fillSymmetricPoints(x0, y0, x, y, startRadian, endRadian, a, b);
            if (d2 > 0) {
                y--;
                dy -= 2 * a * a;
                d2 += a * a - dy;
            } else {
                x++;
                y--;
                dx += 2 * b * b;
                dy -= 2 * a * a;
                d2 += dx - dy + a * a;
            }
        }
    }

    private void fillSymmetricPoints(int x0, int y0, int x, int y, double startRadian, double endRadian, int a, int b) {
        if (startRadian == endRadian) {
            fillPixel(x0 + x, y0 + y);
            fillPixel(x0 - x, y0 + y);
            fillPixel(x0 - x, y0 - y);
            fillPixel(x0 + x, y0 - y);
            return;
        }
        double s = Math.toDegrees(startRadian);
        double e = Math.toDegrees(endRadian);
        double angle1 = Math.atan2(y / b, x / a);
        double a1 = Math.toDegrees(angle1);
        double angle2 = Math.atan2(-y / b, -x / a);
        double a2 = Math.toDegrees(angle2);
        if ((angle1 >= startRadian && angle1 <= endRadian)) {
            fillPixel(x0 + x, y0 + y);
            fillPixel(x0 - x, y0 + y);
        }
        if ((angle2 >= startRadian && angle2 <= endRadian)) {
            fillPixel(x0 - x, y0 - y);
            fillPixel(x0 + x, y0 - y);
        }
    }

    private void fillPixel(int x, int y) {
        // your code to fill pixel
        g.drawLine(x, y, x, y);
    }

    public void fillFan(Graphics g, int x, int y, int radius, int startAngle, int endAngle) {
        int[] xPoints = new int[3];
        int[] yPoints = new int[3];

        xPoints[0] = x;
        yPoints[0] = y;

        for (int i = startAngle; i <= endAngle; i++) {
            double angle = Math.toRadians(i);
            xPoints[1] = (int) (x + radius * Math.cos(angle));
            yPoints[1] = (int) (y + radius * Math.sin(angle));

            angle = Math.toRadians(i + 1);
            xPoints[2] = (int) (x + radius * Math.cos(angle));
            yPoints[2] = (int) (y + radius * Math.sin(angle));

            g.fillPolygon(xPoints, yPoints, 3);
        }
    }

    public void drawFan1(Graphics g, int x, int y, int radius, int startAngle, int endAngle) {
        for (int i = startAngle; i <= endAngle; i++) {
            double angle = Math.toRadians(i);
            int x1 = (int) (x + radius * Math.cos(angle));
            int y1 = (int) (y + radius * Math.sin(angle));

            angle = Math.toRadians(i + 1);
            int x2 = (int) (x + radius * Math.cos(angle));
            int y2 = (int) (y + radius * Math.sin(angle));

            g.drawLine(x1, y1, x2, y2);
            //System.out.println(x1 + ", " + y1 + "    " + x2 + ", " + y2);
        }
    }


//    public void drawEllipse(int x, int y, int width, int height, int startAngle, int arcAngle) {
//        if (width <= 0 || height <= 0) return;
//
//        int a = width / 2;
//        int b = height / 2;
//        int x0 = x + a;
//        int y0 = y + b;
//
//        startAngle = startAngle % 360;
//        arcAngle = arcAngle % 360;
//        int endAngle = startAngle + arcAngle;
//        endAngle = endAngle % 360;
//
//        if (startAngle > endAngle) {
//            int t = startAngle;
//            startAngle = endAngle;
//            endAngle = t;
//        }
//        double t = Math.toRadians(startAngle);
//        double dt = 1.0 / Math.max(a, b);
//        int i = 0;
//        while (t < Math.toRadians(endAngle)) {
//            int dx = (int) (x0 + a * Math.cos(t));
//            int dy = (int) (y0 - b * Math.sin(t));
//            g.drawLine(dx, dy, dx, dy);
//            System.out.println(i + ":   " + dx + ", " + dy);
//            t += dt;
//            i++;
//        }
//    }
}
