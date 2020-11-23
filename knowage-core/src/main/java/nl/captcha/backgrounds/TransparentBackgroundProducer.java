package nl.captcha.backgrounds;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Generates a transparent background.
 * 
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 *
 */
public class TransparentBackgroundProducer implements BackgroundProducer {

	public BufferedImage addBackground(BufferedImage image) {
		return getBackground(image.getWidth(), image.getHeight());
	}

	public BufferedImage getBackground(int width, int height) {
		BufferedImage bg = new BufferedImage(width, height, BufferedImage.TRANSLUCENT);
		Graphics2D g = bg.createGraphics();

		g.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f));
		g.fillRect(0, 0, width, height);
		
		return bg;
	}

}
