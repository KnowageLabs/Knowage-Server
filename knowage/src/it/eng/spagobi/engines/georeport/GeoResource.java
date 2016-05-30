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

import static it.eng.spagobi.engines.georeport.utils.geoUtils.getFileLayerAction;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.engines.georeport.utils.geoUtils;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.utilities.rest.RestUtilities;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @authors Giovanni Luca Ulivo (GiovanniLuca.Ulivo@eng.it)
 *
 */

@Path("1.0/geo")
@ManageAuthorization
public class GeoResource {

	@Path("/getWMSlayer")
	@GET
	public Response getWMSlayer(@Context HttpServletRequest req) throws IOException, JSONException {

		String layerUrl = req.getParameter("layerURL");
		String reqString = req.getQueryString();

		String finalWMSUrl = layerUrl + "?" + reqString.replaceAll("layerURL[^&]*&", "");

		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			@Override
			public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
			}

			@Override
			public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
			}
		} };

		// Install the all-trusting trust manager
		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {
		}

		// Now you can access an https URL without having the certificate in the truststore

		URL url = new URL(finalWMSUrl);
		if (req.getParameter("REQUEST").equals("GetFeatureInfo")) {

			URLConnection conn = url.openConnection();

			// open the stream and put it into BufferedReader
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuilder stringBuilder = new StringBuilder();

			String line = null;
			while ((line = br.readLine()) != null) {
				stringBuilder.append(line + "\n");
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
				System.out.println("getWMSlayer return a null image");
			}
			return Response.ok(new ByteArrayInputStream(imageData)).build();
		}
	}

	@Path("/getFileLayer")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public String getFileLayer(@Context HttpServletRequest req) throws IOException, JSONException, EMFUserError {
		JSONObject requestVal = RestUtilities.readBodyAsJSONObject(req);
		String layerUrl = requestVal.getString(geoUtils.LAYER_URL);
		String result = getFileLayerAction(layerUrl);

		return result;
	}

}
