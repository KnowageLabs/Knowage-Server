package it.eng.spagobi.commons.services;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//import com.esotericsoftware.minlog.Log.Logger;
import org.apache.log4j.Logger;



import net.logicsquad.nanocaptcha.image.ImageCaptcha;
import net.logicsquad.nanocaptcha.image.filter.FishEyeImageFilter;

public class KnowageCaptchaServlet extends HttpServlet {

	/**
	 *
	 */
	private static final long serialVersionUID = -1268155968745374787L;
	private static final Logger LOGGER = Logger.getLogger(KnowageCaptchaServlet.class);
	@Override
	public void doGet(HttpServletRequest httpservletrequest, HttpServletResponse httpservletresponse) throws ServletException, IOException {
		int width = 200;
		int height = 50;

		if (getInitParameter("height") != null) {
			try {
				height = Integer.parseInt(getInitParameter("height"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (getInitParameter("width") != null) {
			try {
				width = Integer.parseInt(getInitParameter("width"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		ImageCaptcha imageCaptcha = new ImageCaptcha.Builder(width, height).addContent().addBackground().addFilter(new FishEyeImageFilter()).addBackground()
				.build();
		writeImage(httpservletresponse, imageCaptcha.getImage());
		httpservletrequest.getSession().setAttribute("simpleCaptcha", imageCaptcha);
	}

	private void writeImage(HttpServletResponse httpservletresponse, BufferedImage bufferedimage) {
		httpservletresponse.setHeader("Cache-Control", "private,no-cache,no-store");
		httpservletresponse.setContentType("image/png");
		try {
			writeImage(((httpservletresponse.getOutputStream())), bufferedimage);
		} catch (IOException ioexception) {
			LOGGER.error("writeImage",ioexception);
		}
	}

	private void writeImage(OutputStream outputstream, BufferedImage bufferedimage) {
		try {
			ImageIO.write(bufferedimage, "png", outputstream);
			outputstream.close();
		} catch (IOException ioexception) {
			LOGGER.error("writeImage",ioexception);
		}
	}

}