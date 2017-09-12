/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 * 
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.images.dao.util;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;
import org.imgscalr.Scalr.Mode;

public class ImageUtil {

	synchronized private static String getFormatName(String filename) throws IOException {
		String ext = filename.substring(filename.lastIndexOf(".") + 1);
		String[] arr = ImageIO.getWriterFormatNames();
		for (int i = 0; i < arr.length; i++) {
			if (arr[i].equals(ext))
				return ext;
		}
		throw new IOException("file [" + filename + "] has a format not supported [" + ext + "]");
	}

	synchronized public static void resize(String filename, InputStream orig, OutputStream dest, int width, int height) throws IOException {
		if (width > 0 || height > 0) {

			String formatName = getFormatName(filename);

			Mode mode = Mode.AUTOMATIC;
			if (height == 0)
				mode = Mode.FIT_TO_WIDTH;
			else if (width == 0)
				mode = Mode.FIT_TO_HEIGHT;

			BufferedImage src = ImageIO.read(orig);
			BufferedImage thumbnail = null;
			if (src.getHeight() < height && src.getWidth() < width) {
				thumbnail = src;
			} else {
				thumbnail = Scalr.resize(src, Method.ULTRA_QUALITY, mode, width, height);
			}
			if (!ImageIO.write(thumbnail, formatName, dest)) {
				throw new IOException("ImageIO.write error");
			}
		}
	}
}
