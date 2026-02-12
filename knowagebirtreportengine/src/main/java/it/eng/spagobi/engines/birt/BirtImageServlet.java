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
package it.eng.spagobi.engines.birt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import it.eng.knowage.commons.security.PathTraversalChecker;
import it.eng.spagobi.utilities.mime.MimeUtils;

public class BirtImageServlet extends HttpServlet {

	private static final Logger logger = Logger.getLogger(BirtImageServlet.class);

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) {

		ServletOutputStream ouputStream = null;
		InputStream fis = null;
		File imageFile = null;
		String completeImageFileName = null;
		String mimeType = null;

		String imageFileName = request.getParameter("imageID");
		if (imageFileName == null) {
			logger.error("Image directory or image file name missing.");
			throw new RuntimeException("Image file name missing.");
		}

		imageFile = PathTraversalChecker.get(System.getProperty("java.io.tmpdir"), "birt", imageFileName);

		if (!imageFile.exists()) {
			logger.error("File " + imageFile.getPath() + " not found");
			return;
		}

		try {
			fis = new FileInputStream(imageFile);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Image file [" + completeImageFileName + "] not found.", e);
		}

		mimeType = MimeUtils.getMimeType(imageFileName);

		try {

			ouputStream = response.getOutputStream();

			logger.debug("Mime type is = " + mimeType);
			response.setContentType(mimeType);
			response.setHeader("Content-Type", mimeType);

			byte[] buffer = new byte[1024];
			int len;
			while ((len = fis.read(buffer)) >= 0) {
				ouputStream.write(buffer, 0, len);
			}

		} catch (Exception e) {
			logger.error("Error writing image into servlet output stream", e);
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					logger.error("Error while closing FileInputStream on file " + completeImageFileName, e);
				}
			}
			if (ouputStream != null) {
				try {
					ouputStream.flush();
					ouputStream.close();
				} catch (IOException e) {
					logger.error("Error flushing servlet output stream", e);
				}
			}
			if (imageFile != null && imageFile.exists() && imageFile.isFile()) {
				// keep SVG needed to be displayed
				if (!imageFile.getName().endsWith("svg")) {
					imageFile.delete();
				} else {
					final File myFile = imageFile;

					new java.util.Timer().schedule(new java.util.TimerTask() {
						@Override
						public void run() {
							myFile.delete();
						}
					}, 5000);

				}
			}
		}

	}


	/*
	 * private Map getMapParameters(Map allParams){ Map toReturn = new HashMap(); String[] strArParams = (String[])allParams.get("params"); String strParams =
	 * strArParams[0];
	 *
	 * try{ strParams = strParams.replace("{", ""); strParams = strParams.replace("}", ""); String[] arParamsImage= strParams.split(","); for (int i=0; i<
	 * arParamsImage.length; i++){ String name = arParamsImage[i].substring(0,arParamsImage[i].indexOf("=")); String value =
	 * arParamsImage[i].substring(arParamsImage[i].indexOf("=")+1); if (value != null && !value.equals("")) toReturn.put(name.trim(), value.trim()); }
	 * }catch(Exception e){ logger.error("Error while parsing chart's parameter map. Error: " + e ); } return toReturn; }
	 */

}
