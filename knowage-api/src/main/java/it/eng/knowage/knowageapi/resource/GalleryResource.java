package it.eng.knowage.knowageapi.resource;

import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

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

import it.eng.knowage.knowageapi.dao.dto.SbiWidgetGallery;
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
		SbiWidgetGallery newSbiWidgetGallery = null;
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

				newSbiWidgetGallery = createNewGallery(name, type, userId, description, "licenseText", "licenseName", "tenant", image, "sbiversion", body,
						userId);

				widgetGalleryService.createNewGallery(newSbiWidgetGallery);

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (newSbiWidgetGallery != null)
			response = Response.status(Response.Status.OK).entity(newSbiWidgetGallery.getUuid()).build();
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

					SbiWidgetGallery newSbiWidgetGalleryToUpdate = widgetGalleryService.updateGallery(newSbiWidgetGallery, label, type, userId, description,
							"licenseText", "licenseName", "tenant", image, "sbiversion", body, userId);
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

	public SbiWidgetGallery createNewGallery(String name, String type, String author, String description, String licenseText, String licenseName,
			String organization, String image, String sbiversion, String template, String userid) {

		UUID uuidGenerated = generateType1UUID();
		image = image.substring(image.indexOf(",") + 1);
		byte[] byteArrray = image.getBytes();
		SbiWidgetGallery newSbiWidgetGallery = new SbiWidgetGallery();
		newSbiWidgetGallery.setUuid(uuidGenerated.toString());
		newSbiWidgetGallery.setAuthor(author);
		newSbiWidgetGallery.setDescription(description);
		newSbiWidgetGallery.setLicenseText(licenseText);
		newSbiWidgetGallery.setLicenseName(licenseName);
		newSbiWidgetGallery.setName(name);
		newSbiWidgetGallery.setOrganization(organization);
		newSbiWidgetGallery.setPreviewImage(byteArrray);
		newSbiWidgetGallery.setSbiVersionIn(sbiversion);
		newSbiWidgetGallery.setTemplate(template);
		newSbiWidgetGallery.setTimeIn(Timestamp.from(Instant.now()));
		newSbiWidgetGallery.setType(type);
		newSbiWidgetGallery.setUserIn(userid);

		return newSbiWidgetGallery;

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

	public static UUID generateType1UUID() {

		long most64SigBits = get64MostSignificantBitsForVersion1();
		long least64SigBits = get64LeastSignificantBitsForVersion1();

		return new UUID(most64SigBits, least64SigBits);
	}

	private static long get64LeastSignificantBitsForVersion1() {
		Random random = new Random();
		long random63BitLong = random.nextLong() & 0x3FFFFFFFFFFFFFFFL;
		long variant3BitFlag = 0x8000000000000000L;
		return random63BitLong + variant3BitFlag;
	}

	private static long get64MostSignificantBitsForVersion1() {
		LocalDateTime start = LocalDateTime.of(1582, 10, 15, 0, 0, 0);
		Duration duration = Duration.between(start, LocalDateTime.now());
		long seconds = duration.getSeconds();
		long nanos = duration.getNano();
		long timeForUuidIn100Nanos = seconds * 10000000 + nanos * 100;
		long least12SignificatBitOfTime = (timeForUuidIn100Nanos & 0x000000000000FFFFL) >> 4;
		long version = 1 << 12;
		return (timeForUuidIn100Nanos & 0xFFFFFFFFFFFF0000L) + version + least12SignificatBitOfTime;
	}
}