package it.eng.knowage.knowageapi.resource;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import it.eng.knowage.knowageapi.resource.dto.WidgetGalleryDTO;
import it.eng.knowage.knowageapi.service.WidgetGalleryService;
import it.eng.knowage.knowageapi.utils.StringUtilities;

@Path("/1.0/widgetgallery")
@Component
public class GalleryResource {

	@Autowired
	WidgetGalleryService widgetGalleryService;

	private HashMap<String, WidgetGalleryDTO> mockMap = new HashMap<String, WidgetGalleryDTO>();

	// TODO: put authorization with token into an interceptor
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response widgetList(@HeaderParam("Authorization") String token) {
		Response response = null;

		List<WidgetGalleryDTO> widgetGalleryDTOs = widgetGalleryService.getWidgets();

		String userId = jwtToken2userId(token.replace("Bearer ", ""));

		if (widgetGalleryDTOs != null)
			response = Response.status(Response.Status.OK).entity(widgetGalleryDTOs).build();
		else {
			response = Response.status(Response.Status.NO_CONTENT).build();
		}

		return response;

	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response widget(@HeaderParam("Authorization") String token, @PathParam("id") String widgetId) {
		Response response = null;

		WidgetGalleryDTO widgetGalleryDTO = widgetGalleryService.getWidgetsById(widgetId);

		if (widgetGalleryDTO != null)
			response = Response.status(Response.Status.OK).entity(widgetGalleryDTO).build();
		else {
			response = Response.status(Response.Status.NO_CONTENT).build();
		}

		return response;

	}

	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response widgetCreate(String body, @HeaderParam("Authorization") String token) {
		Response response = null;

		String image = "";
		String code = "";
		String name = "";
		String description = "";
		String css = "";
		String tags = "";
		String type = "";
		String html = "";
		String javascript = "";
		String python = "";
		String userId = jwtToken2userId(token.replace("Bearer ", ""));
		WidgetGalleryDTO newSbiWidgetGallery = null;
		if (StringUtilities.isNotEmpty(body)) {
			try {
				JSONObject jsonBody = new JSONObject(body);
				type = jsonBody.getString("type");
				name = jsonBody.getString("name");
				if (jsonBody.has("description"))
					description = jsonBody.getString("description");
				if (jsonBody.has("tags"))
					tags = jsonBody.getString("tags");
				if (jsonBody.has("image"))
					image = jsonBody.getString("image");
				JSONObject jsonCode = jsonBody.optJSONObject("code");
				code = jsonCode != null ? jsonCode.toString() : null;
				html = jsonCode.getString("html");
				javascript = jsonCode.getString("javascript");
				python = jsonCode.getString("python");
				css = jsonCode.getString("css");

				newSbiWidgetGallery = widgetGalleryService.createNewGallery(name, type, userId, description, "licenseText", "licenseName", "tenant", image,
						"sbiversion", body, userId, tags);

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (newSbiWidgetGallery != null)
			response = Response.status(Response.Status.OK).entity(newSbiWidgetGallery.getId()).build();
		else {
			// TODO: error handling!!
			response = Response.status(Response.Status.NO_CONTENT).build();
		}

		return response;

	}

	@POST
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response widgetUpdate(String body, @HeaderParam("Authorization") String token, @PathParam("id") String widgetId) {
		Response response = null;

		String image = "";
		String code = "";
		String label = "";
		String description = "";
		String css = "";
		String tags = "";
		String type = "";
		String html = "";
		String javascript = "";
		String python = "";
		String userId = jwtToken2userId(token.replace("Bearer ", ""));
		WidgetGalleryDTO newSbiWidgetGallery = null;
		if (StringUtilities.isNotEmpty(body)) {
			try {
				JSONObject jsonBody = new JSONObject(body);
				type = jsonBody.getString("type");
				label = jsonBody.getString("name");
				if (jsonBody.has("description"))
					description = jsonBody.getString("description");
				if (jsonBody.has("tags"))
					tags = jsonBody.getString("tags");
				if (jsonBody.has("image"))
					image = jsonBody.getString("image");
				JSONObject jsonCode = jsonBody.optJSONObject("code");
				code = jsonCode != null ? jsonCode.toString() : null;
				html = jsonCode.getString("html");
				javascript = jsonCode.getString("javascript");
				python = jsonCode.getString("python");
				css = jsonCode.getString("css");

				newSbiWidgetGallery = widgetGalleryService.getWidgetsById(widgetId);
				if (newSbiWidgetGallery != null) {

					widgetGalleryService.updateGallery(newSbiWidgetGallery.getId(), label, type, userId, description, "licenseText", "licenseName", "tenant",
							image, "sbiversion", body, userId, tags);
				}

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (newSbiWidgetGallery != null)
			response = Response.status(Response.Status.OK).entity(newSbiWidgetGallery.getId()).build();
		else {
			response = Response.status(Response.Status.NO_CONTENT).build();
		}

		return response;

	}

	@DELETE
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response widgetDelete(String body, @HeaderParam("Authorization") String token, @PathParam("id") String widgetId) {
		Response response = null;

		String userId = jwtToken2userId(token.replace("Bearer ", ""));

		int success = widgetGalleryService.deleteGallery(widgetId);

		if (success == 1)
			response = Response.status(Response.Status.OK).build();
		else {
			response = Response.status(Response.Status.NO_CONTENT).build();
		}

		return response;

	}

	public static String jwtToken2userId(String jwtToken) throws JWTVerificationException {
		String userId = null;
		Context ctx;
		try {
			ctx = new InitialContext();

			String key = (String) ctx.lookup("java:/comp/env/hmacKey");
			Algorithm algorithm = Algorithm.HMAC256(key);
			JWTVerifier verifier = JWT.require(algorithm).build();
			DecodedJWT decodedJWT = verifier.verify(jwtToken);
			Claim userIdClaim = decodedJWT.getClaim("user_id");
			userId = userIdClaim.asString();
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return userId;
	}

}