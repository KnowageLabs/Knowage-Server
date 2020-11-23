package nl.captcha.servlet;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

public final class CaptchaServletUtil {

    public static void writeImage(HttpServletResponse response, BufferedImage bi) {
        response.setHeader("Cache-Control", "private,no-cache,no-store");
        response.setContentType("image/png");	// PNGs allow for transparency. JPGs do not.
        try {
            writeImage(response.getOutputStream(), bi);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeImage(OutputStream os, BufferedImage bi) {
    	try {
			ImageIO.write(bi, "png", os);
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
