import java.awt.Graphics;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class FanShape extends JPanel {

    private int centerX, centerY, radius;
    private double startAngle, endAngle;

    public FanShape(int centerX, int centerY, int radius, double startAngle, double endAngle) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.radius = radius;
        this.startAngle = Math.toRadians(startAngle);
        this.endAngle = Math.toRadians(endAngle);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawFan(g);
    }

    private void drawFan(Graphics g) {
        int numLines = 100; // Number of lines to approximate the curve
        double angleStep = (endAngle - startAngle) / numLines;

        // Draw the two radii
        int startX = centerX + (int) (radius * Math.cos(startAngle));
        int startY = centerY + (int) (radius * Math.sin(startAngle));
        g.drawLine(centerX, centerY, startX, startY);

        int endX = centerX + (int) (radius * Math.cos(endAngle));
        int endY = centerY + (int) (radius * Math.sin(endAngle));
        g.drawLine(centerX, centerY, endX, endY);

        // Draw the arc and fill the fan
        for (int i = 0; i <= numLines; i++) {
            double currentAngle = startAngle + i * angleStep;
            int x = centerX + (int) (radius * Math.cos(currentAngle));
            int y = centerY + (int) (radius * Math.sin(currentAngle));

            if (i > 0) {
                g.drawLine(prevX, prevY, x, y);
                g.drawLine(centerX, centerY, x, y);
            }
            prevX = x;
            prevY = y;
        }
    }

    private int prevX, prevY;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Fan Shape");
        FanShape fanShape = new FanShape(200, 200, 100, 0, 90);
        frame.add(fanShape);
        frame.setSize(400, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
