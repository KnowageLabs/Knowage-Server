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
import it.eng.spagobi.services.security.SecurityServiceServiceProxy;
import it.eng.spagobi.services.security.SpagoBIUserProfile;

@Path("/1.0/widgetgallery")
@Component
public class GalleryResource {

	static private Logger logger = Logger.getLogger(GalleryResource.class);

	@Autowired
	WidgetGalleryAPI widgetGalleryService;

	// TODO: set FE roles and decide the BE roles management behaviour

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public List<WidgetGalleryDTO> widgetList(@HeaderParam("Authorization") String token) {
		SpagoBIUserProfile profile = null;
		List<WidgetGalleryDTO> widgetGalleryDTOs = null;
		try {
			profile = getUserProfile(token);
			if (widgetGalleryService.canSeeGallery(profile)) {
				widgetGalleryDTOs = widgetGalleryService.getWidgetsByTenant(profile.getOrganization());
			}
		} catch (Throwable e) {
			throw new KnowageRuntimeException(e.getMessage());
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
			widgetGalleryDTO = widgetGalleryService.getWidgetsById(widgetId, profile.getOrganization());
		} catch (Throwable e) {
			throw new KnowageRuntimeException(e.getMessage());
		}

		return widgetGalleryDTO;

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
		String userId = jwtToken2userId(token.replace("Bearer ", ""));
		String licenseText = "";
		String licenseName = "";
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
				if (jsonBody.has("licenseText"))
					licenseText = jsonCode.optString("licenseText");
				if (jsonBody.has("licenseName"))
					licenseName = jsonCode.optString("licenseName");
				profile = getUserProfile(token);

				newSbiWidgetGallery = widgetGalleryService.createNewGallery(name, type, userId, description, profile.getOrganization(), image, "", body, userId,
						tags);

			} catch (Exception e) {
				throw new KnowageRuntimeException(e.getMessage());
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
		String licenseText = "";
		String licenseName = "";
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
				if (jsonBody.has("licenseText"))
					licenseText = jsonCode.optString("licenseText");
				if (jsonBody.has("licenseName"))
					licenseName = jsonCode.optString("licenseName");
				newSbiWidgetGallery = widgetGalleryService.getWidgetsById(widgetId, profile.getOrganization());
				if (newSbiWidgetGallery != null) {

					widgetGalleryService.updateGallery(newSbiWidgetGallery.getId(), label, type, userId, description, profile.getOrganization(), image, "",
							body, userId, tags);
				}

			} catch (Exception e) {
				throw new KnowageRuntimeException(e.getMessage());
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
			int success = widgetGalleryService.deleteGallery(widgetId, profile.getOrganization());
			if (success == 1)
				response = Response.status(Response.Status.OK).build();
			else {
				response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
			}
		} catch (Exception e) {
			throw new KnowageRuntimeException(e.getMessage());
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
			throw new KnowageRuntimeException(e.getMessage());
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
			throw new KnowageRuntimeException(e.getMessage());
		}
		return userId;
	}

	public SpagoBIUserProfile getUserProfile(String userToken) throws RemoteException {
		SpagoBIUserProfile profile = null;
		userToken = userToken.replace("Bearer ", "");
		String technicalToken = getTechnicalToken();
		Context ctx;
		try {
			ctx = new InitialContext();
			String serviceUrl = (String) ctx.lookup("java:/comp/env/service_url");
			SecurityServiceServiceProxy proxy = new SecurityServiceServiceProxy(serviceUrl + "/services/SecurityService");
			profile = proxy.getUserProfile(technicalToken, userToken);
		} catch (Exception e) {
			throw new KnowageRuntimeException(e.getMessage());
		}
		return profile;
	}
}