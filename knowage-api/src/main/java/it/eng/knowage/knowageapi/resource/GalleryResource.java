package it.eng.knowage.knowageapi.resource;

import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
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
import it.eng.knowage.knowageapi.utils.JsonConverter;
import it.eng.spagobi.services.security.SecurityServiceService;
import it.eng.spagobi.services.security.SpagoBIUserProfile;

@Path("/1.0/widgetgallery")
@Component
@Validated
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
			SpagoBIUserProfile profile = getUserProfile();
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
			SpagoBIUserProfile profile = getUserProfile();
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
			SpagoBIUserProfile profile = getUserProfile();
			widgetGalleryDTOs = widgetGalleryService.getWidgetsByTenantType(profile, type);
		} catch (Throwable e) {
			throw new KnowageRuntimeException(e.getMessage(), e);
		}
		return widgetGalleryDTOs;
	}

	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public WidgetGalleryDTO widgetCreate(@Valid WidgetGalleryDTO newSbiWidgetGallery) {
		try {
			SpagoBIUserProfile profile = getUserProfile();
			String template = JsonConverter.objectToJson(newSbiWidgetGallery, WidgetGalleryDTO.class);
			newSbiWidgetGallery.setTemplate(template);
			newSbiWidgetGallery = widgetGalleryService.makeNewWidget(newSbiWidgetGallery, profile, true);
		} catch (Exception e) {
			throw new KnowageRuntimeException(e.getMessage(), e);
		}
		return newSbiWidgetGallery;

	}

	@POST
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public WidgetGalleryDTO widgetUpdate(@Valid WidgetGalleryDTO newSbiWidgetGallery, @PathParam("id") String widgetId) {

		WidgetGalleryDTO newSbiWidgetGalleryToUpdate = null;
		try {
			SpagoBIUserProfile profile = getUserProfile();

			String template = JsonConverter.objectToJson(newSbiWidgetGallery, WidgetGalleryDTO.class);
			newSbiWidgetGallery.setTemplate(template);
			newSbiWidgetGalleryToUpdate = widgetGalleryService.getWidgetsById(widgetId, profile);
			if (newSbiWidgetGalleryToUpdate != null) {

				newSbiWidgetGalleryToUpdate = widgetGalleryService.updateWidget(newSbiWidgetGallery, profile);
			}
		} catch (Exception e) {
			throw new KnowageRuntimeException(e.getMessage(), e);
		}

		return newSbiWidgetGalleryToUpdate;

	}

	private SpagoBIUserProfile getUserProfile() {
		SpagoBIUserProfile profile = (SpagoBIUserProfile) RequestContextHolder.currentRequestAttributes().getAttribute("userProfile",
				RequestAttributes.SCOPE_REQUEST);
		return profile;
	}

	@DELETE
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response widgetDelete(@PathParam("id") String widgetId) {
		Response response = null;
		try {
			SpagoBIUserProfile profile = getUserProfile();
			int success = widgetGalleryService.deleteGallery(widgetId, profile);
			if (success == 1)
				response = Response.status(Response.Status.OK).build();
			else {
				throw new KnowageRuntimeException("Cannot delete object with id: " + widgetId);
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

	@POST
	@Path("/import")
	@Consumes(MediaType.APPLICATION_JSON)
	public WidgetGalleryDTO importSingleWidget(@Valid WidgetGalleryDTO newSbiWidgetGallery) {

		SpagoBIUserProfile profile = getUserProfile();
		try {
			String template = JsonConverter.objectToJson(newSbiWidgetGallery, WidgetGalleryDTO.class);
			newSbiWidgetGallery.setTemplate(template);
			newSbiWidgetGallery = widgetGalleryService.importOrUpdateWidget(newSbiWidgetGallery, profile);

		} catch (JSONException e) {
			throw new KnowageRuntimeException(e.getMessage(), e);
		}

		return newSbiWidgetGallery;
	}

}