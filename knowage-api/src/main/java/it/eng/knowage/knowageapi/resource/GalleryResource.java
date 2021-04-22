package it.eng.knowage.knowageapi.resource;

import java.rmi.RemoteException;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
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
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import it.eng.knowage.knowageapi.error.KnowageRuntimeException;
import it.eng.knowage.knowageapi.resource.dto.WidgetGalleryDTO;
import it.eng.knowage.knowageapi.service.WidgetGalleryAPI;
import it.eng.knowage.knowageapi.utils.StringUtilities;
import it.eng.spagobi.services.security.SecurityServiceService;
import it.eng.spagobi.services.security.SpagoBIUserProfile;

@Path("/1.0/widgetgallery")
@Component
public class GalleryResource {

	static private Logger logger = Logger.getLogger(GalleryResource.class);

	@Autowired
	WidgetGalleryAPI widgetGalleryService;

	@Autowired
	@Lazy
	SecurityServiceService securityServiceService;

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public List<WidgetGalleryDTO> widgetList(@HeaderParam("Authorization") String token) {
		SpagoBIUserProfile profile = null;
		List<WidgetGalleryDTO> widgetGalleryDTOs = null;
		try {
			profile = getUserProfile(token);
			widgetGalleryDTOs = widgetGalleryService.getWidgetsByTenant(profile);

		} catch (Exception e) {
			throw new KnowageRuntimeException(e.getMessage(), e);
		}

		return widgetGalleryDTOs;

	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public WidgetGalleryDTO widget(@HeaderParam("Authorization") String token, @PathParam("id") String widgetId) {
		SpagoBIUserProfile profile = null;
		WidgetGalleryDTO widgetGalleryDTO = null;
		try {
			profile = getUserProfile(token);
			widgetGalleryDTO = widgetGalleryService.getWidgetsById(widgetId, profile);
		} catch (Throwable e) {
			throw new KnowageRuntimeException(e.getMessage(), e);
		}

		return widgetGalleryDTO;

	}

	@GET
	@Path("/widgets/{type}")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public List<WidgetGalleryDTO> widgetType(@HeaderParam("Authorization") String token, @PathParam("type") String type) {
		SpagoBIUserProfile profile = null;
		List<WidgetGalleryDTO> widgetGalleryDTOs = null;
		try {
			profile = getUserProfile(token);
			widgetGalleryDTOs = widgetGalleryService.getWidgetsByTenantType(profile, type);
		} catch (Throwable e) {
			throw new KnowageRuntimeException(e.getMessage(), e);
		}
		return widgetGalleryDTOs;

	}

	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public WidgetGalleryDTO widgetCreate(String body, @HeaderParam("Authorization") String token) {
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
		String outputType = "";
		String userId = jwtToken2userId(token.replace("Bearer ", ""));
		WidgetGalleryDTO newSbiWidgetGallery = null;
		if (StringUtilities.isNotEmpty(body)) {
			try {
				JSONObject jsonBody = new JSONObject(body);
				type = jsonBody.getString("type");
				name = jsonBody.getString("name");
				if (jsonBody.has("description"))
					description = jsonBody.getString("description");
				if (jsonBody.has("tags") && !jsonBody.getString("tags").equals("[]"))
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
				if (jsonBody.has("outputType")) {
					outputType = jsonBody.getString("outputType");
				}
				newSbiWidgetGallery = widgetGalleryService.createNewGallery(name, type, userId, description, image, "", body, profile, tags, outputType);

			} catch (Exception e) {
				throw new KnowageRuntimeException(e.getMessage(), e);
			}

		}

		return newSbiWidgetGallery;

	}

	@POST
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public WidgetGalleryDTO widgetUpdate(String body, @HeaderParam("Authorization") String token, @PathParam("id") String widgetId) {
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
		String outputType = "";
		WidgetGalleryDTO newSbiWidgetGallery = null;
		if (StringUtilities.isNotEmpty(body)) {
			try {
				JSONObject jsonBody = new JSONObject(body);
				type = jsonBody.getString("type");
				label = jsonBody.getString("name");
				if (jsonBody.has("description"))
					description = jsonBody.getString("description");
				if (jsonBody.has("tags") && !jsonBody.getString("tags").equals("[]"))
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
				newSbiWidgetGallery = widgetGalleryService.getWidgetsById(widgetId, profile);
				if (jsonBody.has("outputType")) {
					outputType = jsonBody.getString("outputType");
				}
				if (newSbiWidgetGallery != null) {

					widgetGalleryService.updateGallery(newSbiWidgetGallery.getId(), label, type, userId, description, image, "", body, profile, tags,
							outputType);
				}

			} catch (Exception e) {
				throw new KnowageRuntimeException(e.getMessage(), e);
			}
		}
		return newSbiWidgetGallery;

	}

	@DELETE
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response widgetDelete(String body, @HeaderParam("Authorization") String token, @PathParam("id") String widgetId) {
		Response response = null;
		SpagoBIUserProfile profile = null;
		try {
			profile = getUserProfile(token);
			int success = widgetGalleryService.deleteGallery(widgetId, profile);
			if (success == 1)
				response = Response.status(Response.Status.OK).build();
			else {
				response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
			}
		} catch (Exception e) {
			throw new KnowageRuntimeException(e.getMessage(), e);
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
			throw new KnowageRuntimeException(e.getMessage(), e);
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
		} catch (Exception e) {
			throw new KnowageRuntimeException(e.getMessage(), e);
		}
		return userId;
	}

	public SpagoBIUserProfile getUserProfile(String userToken) throws RemoteException {
		SpagoBIUserProfile profile = null;
		userToken = userToken.replace("Bearer ", "");
		String technicalToken = getTechnicalToken();
		try {
			profile = securityServiceService.getUserProfile(technicalToken, userToken);
		} catch (Exception e) {
			throw new KnowageRuntimeException("Impossible to get UserProfile from SOAP security service", e);
		}
		return profile;
	}
}