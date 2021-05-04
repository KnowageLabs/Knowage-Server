package it.eng.knowage.knowageapi.resource;

import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
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
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

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
	public List<WidgetGalleryDTO> widgetList() {
		List<WidgetGalleryDTO> widgetGalleryDTOs = null;
		try {
			SpagoBIUserProfile profile = (SpagoBIUserProfile) RequestContextHolder.currentRequestAttributes().getAttribute("userProfile",
					RequestAttributes.SCOPE_REQUEST);
			widgetGalleryDTOs = widgetGalleryService.getWidgetsByTenant(profile);

		} catch (Exception e) {
			throw new KnowageRuntimeException(e.getMessage(), e);
		}

		return widgetGalleryDTOs;

	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public WidgetGalleryDTO widget(@PathParam("id") String widgetId) {
		WidgetGalleryDTO widgetGalleryDTO = null;
		try {
			SpagoBIUserProfile profile = (SpagoBIUserProfile) RequestContextHolder.currentRequestAttributes().getAttribute("userProfile",
					RequestAttributes.SCOPE_REQUEST);
			widgetGalleryDTO = widgetGalleryService.getWidgetsById(widgetId, profile);
		} catch (Throwable e) {
			throw new KnowageRuntimeException(e.getMessage(), e);
		}

		return widgetGalleryDTO;

	}

	@GET
	@Path("/widgets/{type}")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public List<WidgetGalleryDTO> widgetType(@PathParam("type") String type) {
		List<WidgetGalleryDTO> widgetGalleryDTOs = null;
		try {
			SpagoBIUserProfile profile = (SpagoBIUserProfile) RequestContextHolder.currentRequestAttributes().getAttribute("userProfile",
					RequestAttributes.SCOPE_REQUEST);
			widgetGalleryDTOs = widgetGalleryService.getWidgetsByTenantType(profile, type);
		} catch (Throwable e) {
			throw new KnowageRuntimeException(e.getMessage(), e);
		}
		return widgetGalleryDTOs;

	}

	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public WidgetGalleryDTO widgetCreate(String body) {
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
		WidgetGalleryDTO newSbiWidgetGallery = null;
		if (StringUtilities.isNotEmpty(body)) {
			try {
				SpagoBIUserProfile profile = (SpagoBIUserProfile) RequestContextHolder.currentRequestAttributes().getAttribute("userProfile",
						RequestAttributes.SCOPE_REQUEST);
				String token = (String) RequestContextHolder.currentRequestAttributes().getAttribute("userToken", RequestAttributes.SCOPE_REQUEST);
				String userId = jwtToken2userId(token.replace("Bearer ", ""));
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
				if (jsonBody.has("outputType")) {
					outputType = jsonBody.getString("outputType");
				}
				newSbiWidgetGallery = widgetGalleryService.createNewWidget(name, type, userId, description, image, "", body, profile, tags, outputType);

			} catch (Exception e) {
				throw new KnowageRuntimeException(e.getMessage(), e);
			}

		}

		return newSbiWidgetGallery;

	}

	@POST
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public WidgetGalleryDTO widgetUpdate(String body, @PathParam("id") String widgetId) {
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

		String outputType = "";
		WidgetGalleryDTO newSbiWidgetGallery = null;
		if (StringUtilities.isNotEmpty(body)) {
			try {
				SpagoBIUserProfile profile = (SpagoBIUserProfile) RequestContextHolder.currentRequestAttributes().getAttribute("userProfile",
						RequestAttributes.SCOPE_REQUEST);
				String token = (String) RequestContextHolder.currentRequestAttributes().getAttribute("userToken", RequestAttributes.SCOPE_REQUEST);
				String userId = jwtToken2userId(token.replace("Bearer ", ""));
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
				newSbiWidgetGallery = widgetGalleryService.getWidgetsById(widgetId, profile);
				if (jsonBody.has("outputType")) {
					outputType = jsonBody.getString("outputType");
				}
				if (newSbiWidgetGallery != null) {

					widgetGalleryService.updateWidget(newSbiWidgetGallery.getId(), label, type, userId, description, image, "", body, profile, tags,
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
	public Response widgetDelete(String body, @PathParam("id") String widgetId) {
		Response response = null;
		try {
			SpagoBIUserProfile profile = (SpagoBIUserProfile) RequestContextHolder.currentRequestAttributes().getAttribute("userProfile",
					RequestAttributes.SCOPE_REQUEST);
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

}