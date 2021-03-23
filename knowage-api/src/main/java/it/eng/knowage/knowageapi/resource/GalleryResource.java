package it.eng.knowage.knowageapi.resource;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestHeader;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import it.eng.knowage.knowageapi.resource.dto.WidgetGalleryDTO;
import it.eng.knowage.knowageapi.service.WidgetGalleryService;

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

//		List<WidgetGalleryDTO> widgetGalleryDTOs = widgetGalleryService.getWidgets();

		String userId = jwtToken2userId(token.replace("Bearer ", ""));

		List<WidgetGalleryDTO> widgetGalleryDTOs = mockGallery();

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
	public Response widget(@RequestHeader(name = "Authorization") String token, @PathParam("id") String widgetId) {
		Response response = null;

		List<WidgetGalleryDTO> widgetGalleryDTOs = mockGallery();

		if (widgetGalleryDTOs != null)
			response = Response.status(Response.Status.OK).entity(mockMap.get(widgetId)).build();
		else {
			response = Response.status(Response.Status.NO_CONTENT).build();
		}

		return response;

	}

	@POST
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response widgetCreateOrUpdate(@RequestHeader(name = "Authorization") String token, @PathParam("id") String widgetId) {
		Response response = null;

		List<WidgetGalleryDTO> widgetGalleryDTOs = mockGallery();

		if (widgetGalleryDTOs != null)
			response = Response.status(Response.Status.OK).entity(mockMap.get(widgetId)).build();
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

	public List<WidgetGalleryDTO> mockGallery() {
		List<WidgetGalleryDTO> widgetGalleryDTOs = new ArrayList<WidgetGalleryDTO>();
		String id1 = "908d9674-ff77-43bd-90e6-fa11eef06c99";
		String id2 = "b206bf60-5622-4432-9e6c-fd4a66bab811";
		String id3 = "b160c219-801e-4030-afa9-b52583a9094f";
		String id4 = "27f46bee-442b-4c65-a6ff-4e55a1caa93f";
		String id5 = "84d99a09-5d07-4fa5-85f3-e0c19c78d508";
		String id6 = "cba22aa7-444f-4dfb-9d16-ca7df7965360";
		String id7 = "0e5c80b8-8308-48fe-8943-274d7bfa5dfb";
		String id8 = "833c2694-7873-4308-956d-f0a4ccddae08";
		String id9 = "c0f50788-866e-11eb-8dcd-0242ac536748";
		String tag1 = "html";
		String tag2 = "card";
		String tag3 = "chart";
		String tag4 = "highchart";
		String tag5 = "MARE";
		String tag6 = "python";
		String tag7 = "multiple";
		String tag8 = "header";
		String tag9 = "function";
		String tag10 = "python";
		String tag11 = "ai";
		List<String> tagsList1 = new ArrayList<String>();
		tagsList1.add(tag1);
		tagsList1.add(tag2);
		List<String> tagsList2 = new ArrayList<String>();
		tagsList2.add(tag3);
		tagsList2.add(tag4);
		List<String> tagsList3 = new ArrayList<String>();
		tagsList3.add(tag5);
		tagsList3.add(tag6);
		List<String> tagsList4 = new ArrayList<String>();
		tagsList4.add(tag7);
		tagsList4.add(tag8);
		List<String> tagsList5 = new ArrayList<String>();
		tagsList1.add(tag9);
		tagsList5.add(tag10);
		tagsList5.add(tag11);
		List<String> tagsList6 = new ArrayList<String>();
		tagsList6.add(tag1);
		tagsList6.add(tag2);
		List<String> tagsList7 = new ArrayList<String>();
		tagsList7.add(tag7);
		tagsList7.add(tag8);
		List<String> tagsList8 = new ArrayList<String>();
		tagsList8.add(tag9);
		tagsList8.add(tag10);
		List<String> tagsList9 = new ArrayList<String>();
		tagsList9.add(tag10);
		tagsList9.add(tag11);
		String coloredCard = "colored card";
		String advancedLineChart = "advanced line chart";
		String hierarchy = "hierarchy";
		String doubleCards = "double cards";

		String pythonExampleCode = "# Python program to swap two variables\r\n" + "\r\n" + "x = 5\r\n" + "y = 10\r\n" + "\r\n"
				+ "# To take inputs from the user\r\n" + "#x = input('Enter value of x: ')\r\n" + "#y = input('Enter value of y: ')\r\n" + "\r\n"
				+ "# create a temporary variable and swap the values\r\n" + "temp = x\r\n" + "x = y\r\n" + "y = temp\r\n" + "\r\n"
				+ "print('The value of x after swapping: {}'.format(x))\r\n" + "print('The value of y after swapping: {}'.format(y));";

		String htmlExampleCode = "<html><div>Hello world!</div></html>";

		String javaScriptExampleCode = "function myFunction() {\r\n" + "  document.getElementById(\"demo\").innerHTML = \"Paragraph changed.\";\r\n" + "}";

		String cssExampleCode = "# @id: %s \r\n body {\r\n" + "  background-color: lightblue;\r\n" + "}\r\n" + "\r\n" + "h1 {\r\n" + "  color: navy;\r\n"
				+ "  margin-left: 20px;\r\n" + "}";

		String base64Image = "data:image/png;base64,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";

		WidgetGalleryDTO wg1 = new WidgetGalleryDTO(UUID.fromString(id1), "davide.vernassa@eng.it", coloredCard, "html", tagsList1, htmlExampleCode, "", "", "",
				null);
		WidgetGalleryDTO wg2 = new WidgetGalleryDTO(UUID.fromString(id2), "davide.vernassa@eng.it", advancedLineChart, "chart", tagsList2, "",
				javaScriptExampleCode, "", "", null);
		WidgetGalleryDTO wg3 = new WidgetGalleryDTO(UUID.fromString(id3), "matteo.massarotto@eng.it", hierarchy, "chart", tagsList3, htmlExampleCode,
				javaScriptExampleCode, "", "", null);
		WidgetGalleryDTO wg4 = new WidgetGalleryDTO(UUID.fromString(id4), "davide.vernassa@eng.it", doubleCards, "html", tagsList4, "", "", "",
				String.format(cssExampleCode, id4), null);
		WidgetGalleryDTO wg5 = new WidgetGalleryDTO(UUID.fromString(id5), "alberto.nale@eng.it", "progression chart", "python", tagsList5, "", "",
				pythonExampleCode, "", null);
		WidgetGalleryDTO wg6 = new WidgetGalleryDTO(UUID.fromString(id6), "davide.vernassa@eng.it", "multicard", "html", tagsList6, "", "", "",
				String.format(cssExampleCode, id6), null);
		WidgetGalleryDTO wg7 = new WidgetGalleryDTO(UUID.fromString(id7), "alberto.nale@eng.it", "header_light", "html", tagsList7, "", "", "", "", null);
		WidgetGalleryDTO wg8 = new WidgetGalleryDTO(UUID.fromString(id8), "alberto.nale@eng.it", "header_dark", "html", tagsList8, htmlExampleCode, "", "",
				String.format(cssExampleCode, id8), null);
		WidgetGalleryDTO wg9 = new WidgetGalleryDTO(UUID.fromString(id9), "alberto.nale@eng.it", "header_dark_dark", "html", tagsList9, htmlExampleCode, "", "",
				String.format(cssExampleCode, id9), base64Image);
		widgetGalleryDTOs.add(wg1);
		widgetGalleryDTOs.add(wg2);
		widgetGalleryDTOs.add(wg3);
		widgetGalleryDTOs.add(wg4);
		widgetGalleryDTOs.add(wg5);
		widgetGalleryDTOs.add(wg6);
		widgetGalleryDTOs.add(wg7);
		widgetGalleryDTOs.add(wg8);
		widgetGalleryDTOs.add(wg9);

		mockMap.put(id1, wg1);
		mockMap.put(id2, wg2);
		mockMap.put(id3, wg3);
		mockMap.put(id4, wg4);
		mockMap.put(id5, wg5);
		mockMap.put(id6, wg6);
		mockMap.put(id7, wg7);
		mockMap.put(id8, wg8);
		mockMap.put(id9, wg9);
		return widgetGalleryDTOs;
	}

}