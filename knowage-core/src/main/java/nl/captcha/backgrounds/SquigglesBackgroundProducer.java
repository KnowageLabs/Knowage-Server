package nl.captcha.backgrounds;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.awt.image.BufferedImage;

/**
 * Adds a black and white squiggly background to the image.
 * 
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 *
 */
public class SquigglesBackgroundProducer implements BackgroundProducer {

    public BufferedImage addBackground(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        return getBackground(width, height);
    }
    
    public BufferedImage getBackground(int width, int height) {
        BufferedImage result = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = result.createGraphics();

        BasicStroke bs = new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 2.0f, new float[] { 2.0f, 2.0f }, 0.0f);
        graphics.setStroke(bs);
        AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                0.75f);
        graphics.setComposite(ac);

        graphics.translate(width * -1.0, 0.0);
        double delta = 5.0;
        double xt;
        double ts = 0.0;
        for (xt = 0.0; xt < (2.0 * width); xt += delta) {
            Arc2D arc = new Arc2D.Double(0, 0, width, height, 0.0, 360.0,
                    Arc2D.OPEN);
            graphics.draw(arc);
            graphics.translate(delta, 0.0);
            ts += delta;
        }
        graphics.dispose();
        return result;
    }
}
