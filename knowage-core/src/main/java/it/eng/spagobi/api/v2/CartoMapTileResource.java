/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2026 Engineering Ingegneria Informatica S.p.A.
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
package it.eng.spagobi.api.v2;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.eng.spagobi.services.rest.annotations.ManageAuthorization;

/**
 * Proxies CARTO basemap tiles so the API key remains on the Knowage server.
 */
@Path("/2.0/map-tiles/carto")
@ManageAuthorization
public class CartoMapTileResource {

	private static final Logger LOGGER = LogManager.getLogger(CartoMapTileResource.class);

	private static final String CARTO_API_KEY_PROPERTY = "KNOWAGE_CARTO_API_KEY";
	private static final String CARTO_TILE_URL = "https://basemaps.cartocdn.com/%s/%d/%d/%d.png?key=%s";
	private static final int MAX_ZOOM = 20;
	private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10))
			.followRedirects(HttpClient.Redirect.NEVER).build();

	@GET
	@Path("/{style}/{z}/{x}/{y}.png")
	@Produces("image/png")
	public Response getTile(@PathParam("style") String style, @PathParam("z") int zoom, @PathParam("x") int x,
			@PathParam("y") int y) {
		String cartoStyle = getCartoStyle(style);
		if (cartoStyle == null || !hasValidCoordinates(zoom, x, y)) {
			return Response.status(Status.BAD_REQUEST).build();
		}

		String apiKey = System.getProperty(CARTO_API_KEY_PROPERTY, System.getenv(CARTO_API_KEY_PROPERTY));
		if (apiKey == null || apiKey.trim().isEmpty()) {
			LOGGER.error("CARTO tile requested but {} is not configured", CARTO_API_KEY_PROPERTY);
			return Response.status(Status.SERVICE_UNAVAILABLE).build();
		}

		URI tileUri = URI.create(String.format(CARTO_TILE_URL, cartoStyle, zoom, x, y,
				URLEncoder.encode(apiKey, StandardCharsets.UTF_8)));
		HttpRequest request = HttpRequest.newBuilder(tileUri).GET().timeout(Duration.ofSeconds(15)).build();

		try {
			HttpResponse<byte[]> cartoResponse = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofByteArray());
			if (cartoResponse.statusCode() != 200) {
				LOGGER.warn("CARTO tile request failed with status {} for style {}, zoom {}, x {}, y {}",
						cartoResponse.statusCode(), style, zoom, x, y);
				return Response.status(Status.BAD_GATEWAY).build();
			}

			return Response.ok(cartoResponse.body(), MediaType.valueOf("image/png"))
					.header("Cache-Control", "private, max-age=86400").build();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			LOGGER.error("Interrupted while retrieving a CARTO tile");
			return Response.status(Status.BAD_GATEWAY).build();
		} catch (IOException e) {
			LOGGER.error(
					"Unable to retrieve CARTO tile from host {} for style {}, zoom {}, x {}, y {}: {}",
					tileUri.getHost(), style, zoom, x, y, e.getMessage(), e
			);
			return Response.status(Status.BAD_GATEWAY).build();
		}
	}

	private String getCartoStyle(String style) {
		if ("light".equals(style)) {
			return "light_all";
		}
		if ("dark".equals(style)) {
			return "dark_all";
		}
		return null;
	}

	private boolean hasValidCoordinates(int zoom, int x, int y) {
		if (zoom < 0 || zoom > MAX_ZOOM) {
			return false;
		}

		int tilesPerAxis = 1 << zoom;
		return x >= 0 && x < tilesPerAxis && y >= 0 && y < tilesPerAxis;
	}
}
