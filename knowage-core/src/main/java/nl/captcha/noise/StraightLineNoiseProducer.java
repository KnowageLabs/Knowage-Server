package nl.captcha.noise;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.security.SecureRandom;

/**
 * Draws a 4-pixel thick straight red line through the given image.
 * 
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 * 
 */
public class StraightLineNoiseProducer implements NoiseProducer {

    private final Color _color;
    private final int _thickness;
    private final SecureRandom _gen = new SecureRandom();

    /**
     * Default constructor creates a 4-pixel wide red line.
     */
    public StraightLineNoiseProducer() {
        this(Color.RED, 4);
    }

    public StraightLineNoiseProducer(Color color, int thickness) {
        _color = color;
        _thickness = thickness;
    }

    public void makeNoise(BufferedImage image) {
        Graphics2D graphics = image.createGraphics();
        int height = image.getHeight();
        int width = image.getWidth();
        int y1 = _gen.nextInt(height) + 1;
        int y2 = _gen.nextInt(height) + 1;
        drawLine(graphics, y1, width, y2);
    }

    private void drawLine(Graphics g, int y1, int x2, int y2) {
        int X1 = 0;

        // The thick line is in fact a filled polygon
        g.setColor(_color);
        int dX = x2 - X1;
        int dY = y2 - y1;
        // line length
        double lineLength = Math.sqrt(dX * dX + dY * dY);

        double scale = _thickness / (2 * lineLength);

        // The x and y increments from an endpoint needed to create a
        // rectangle...
        double ddx = -scale * dY;
        double ddy = scale * dX;
        ddx += (ddx > 0) ? 0.5 : -0.5;
        ddy += (ddy > 0) ? 0.5 : -0.5;
        int dx = (int) ddx;
        int dy = (int) ddy;

        // Now we can compute the corner points...
        int xPoints[] = new int[4];
        int yPoints[] = new int[4];

        xPoints[0] = X1 + dx;
        yPoints[0] = y1 + dy;
        xPoints[1] = X1 - dx;
        yPoints[1] = y1 - dy;
        xPoints[2] = x2 - dx;
        yPoints[2] = y2 - dy;
        xPoints[3] = x2 + dx;
        yPoints[3] = y2 + dy;

        g.fillPolygon(xPoints, yPoints, 4);
    }
}