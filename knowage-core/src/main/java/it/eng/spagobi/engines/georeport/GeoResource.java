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
package it.eng.spagobi.engines.georeport;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import javax.imageio.ImageIO;
import javax.net.ssl.SSLException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.utilities.rest.RestUtilities;

/**
 * @authors Giovanni Luca Ulivo (GiovanniLuca.Ulivo@eng.it)
 *
 */

@Path("1.0/geo")
@ManageAuthorization
public class GeoResource {
	private static transient Logger logger = Logger.getLogger(GeoResource.class);

	@Path("/getWMSlayer")
	@GET
	public Response getWMSlayer(@Context HttpServletRequest req) throws IOException {
		BufferedReader br = null;
		try {
			String layerUrl = req.getParameter("layerURL");
			String reqString = req.getQueryString();

			String finalWMSUrl = layerUrl + "?" + reqString.replaceAll("layerURL[^&]*&", "");
            
			RestUtilities.checkIfAddressIsInWhitelist(finalWMSUrl);
            
			URL url = new URL(finalWMSUrl);
			if (req.getParameter("REQUEST").equals("GetFeatureInfo")) {
				URLConnection conn = url.openConnection();

				// open the stream and put it into BufferedReader
				br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				StringBuilder stringBuilder = new StringBuilder();

				String line = null;
				while ((line = br.readLine()) != null) {
					stringBuilder.append(line).append("\n");
				}

				return Response.ok(stringBuilder.toString()).build();
			} else {
				BufferedImage image = ImageIO.read(url);

				byte[] imageData = new byte[0];
				if (image != null) {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					ImageIO.write(image, "png", baos);
					imageData = baos.toByteArray();
				} else {
					System.out.println("getWMSlayer returned a null image");
				}
				return Response.ok(new ByteArrayInputStream(imageData)).build();
			}
		} catch (SSLException sslException) {
			logger.error("SSLException occurred while creating socket in GeoResource: ", sslException);
			throw sslException;
		} catch (IOException ioException) {
			logger.error("IOException occurred while creating socket in GeoResource: ", ioException);
			throw ioException;
		} finally {
			if (br != null) {
				br.close();
			}
		}
	}

}
