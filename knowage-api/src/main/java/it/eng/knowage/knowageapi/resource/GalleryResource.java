package it.eng.knowage.knowageapi.resource;

import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;
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

import org.apache.log4j.Logger;
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
import it.eng.spagobi.services.security.bo.SpagoBIUserProfile;
import spagobisecurity.SecurityServiceProxy;

@Path("/1.0/widgetgallery")
@Component
public class GalleryResource {

	static private Logger logger = Logger.getLogger(GalleryResource.class);

	@Autowired
	WidgetGalleryService widgetGalleryService;

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response widgetList(@HeaderParam("Authorization") String token) {
		Response response = null;
		SpagoBIUserProfile profile = null;
		List<WidgetGalleryDTO> widgetGalleryDTOs = null;
		try {
			profile = getUserProfile(token);
			widgetGalleryDTOs = widgetGalleryService.getWidgetsByTenant(profile.getOrganization());
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
		SpagoBIUserProfile profile = null;
		WidgetGalleryDTO widgetGalleryDTO = null;
		try {
			profile = getUserProfile(token);
			widgetGalleryDTO = widgetGalleryService.getWidgetsById(widgetId, profile.getOrganization());
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		SpagoBIUserProfile profile = null;
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

				profile = getUserProfile(token);

				newSbiWidgetGallery = widgetGalleryService.createNewGallery(name, type, userId, description, "licenseText", "licenseName",
						profile.getOrganization(), image, "", body, userId, tags);

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
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
		SpagoBIUserProfile profile = null;
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
				profile = getUserProfile(token);
				newSbiWidgetGallery = widgetGalleryService.getWidgetsById(widgetId, profile.getOrganization());
				if (newSbiWidgetGallery != null) {

					widgetGalleryService.updateGallery(newSbiWidgetGallery.getId(), label, type, userId, description, "licenseText", "licenseName",
							profile.getOrganization(), image, "", body, userId, tags);
				}

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (RemoteException e) {
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
		SpagoBIUserProfile profile = null;
		try {
			profile = getUserProfile(token);
			int success = widgetGalleryService.deleteGallery(widgetId, profile.getOrganization());
			if (success == 1)
				response = Response.status(Response.Status.OK).build();
			else {
				response = Response.status(Response.Status.NO_CONTENT).build();
			}
		} catch (Exception e) {

		}

		return response;

	}

	public static String getTechnicalToken() {
		String technicalToken = null;
		Context ctx;
		try {
			ctx = new InitialContext();
			// Calendar calendar = Calendar.getInstance();
			// calendar.add(Calendar.MINUTE, 5); // token for services will expire in 5 minutes
			// Date expiresAt = calendar.getTime();
			String key = (String) ctx.lookup("java:/comp/env/hmacKey");
			Algorithm algorithm = Algorithm.HMAC256(key);
			technicalToken = JWT.create().withIssuer("knowage")
					// .withExpiresAt(expiresAt)
					.sign(algorithm);
		} catch (Exception e) {

		}
		return technicalToken;
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

	public SpagoBIUserProfile getUserProfile(String userToken) throws RemoteException {

		SpagoBIUserProfile profile = null;
		userToken = userToken.replace("Bearer ", "");
		// String userId = jwtToken2userId(userToken);
		String technicalToken = getTechnicalToken();

		SecurityServiceProxy proxy = new SecurityServiceProxy("http://localhost:8080/knowage/services/SecurityService");
		profile = proxy.getUserProfile(technicalToken, userToken);

		return profile;
	}
}