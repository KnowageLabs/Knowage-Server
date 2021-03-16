package it.eng.knowage.knowageapi.resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.eng.knowage.knowageapi.resource.dto.WidgetGalleryDTO;
import it.eng.knowage.knowageapi.service.WidgetGalleryService;

@Path("/1.0/widgetgallery")
@Component
public class GalleryResource {

	@Autowired
	WidgetGalleryService widgetGalleryService;

	private HashMap<String, WidgetGalleryDTO> mockMap = new HashMap<String, WidgetGalleryDTO>();

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response widgetList() {
		Response response = null;

//		List<WidgetGalleryDTO> widgetGalleryDTOs = widgetGalleryService.getWidgets();

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
	public Response widget(@PathParam("id") String widgetId) {
		Response response = null;

		List<WidgetGalleryDTO> widgetGalleryDTOs = mockGallery();

		if (widgetGalleryDTOs != null)
			response = Response.status(Response.Status.OK).entity(mockMap.get(widgetId)).build();
		else {
			response = Response.status(Response.Status.NO_CONTENT).build();
		}

		return response;

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

		WidgetGalleryDTO wg1 = new WidgetGalleryDTO(UUID.fromString(id1), "davide.vernassa@eng.it", coloredCard, "html", tagsList1, htmlExampleCode, null, null,
				null);
		WidgetGalleryDTO wg2 = new WidgetGalleryDTO(UUID.fromString(id2), "davide.vernassa@eng.it", advancedLineChart, "chart", tagsList2, null,
				javaScriptExampleCode, null, null);
		WidgetGalleryDTO wg3 = new WidgetGalleryDTO(UUID.fromString(id3), "matteo.massarotto@eng.it", hierarchy, "chart", tagsList3, htmlExampleCode,
				javaScriptExampleCode, null, null);
		WidgetGalleryDTO wg4 = new WidgetGalleryDTO(UUID.fromString(id4), "davide.vernassa@eng.it", doubleCards, "html", tagsList4, null, null, null,
				String.format(cssExampleCode, id4));
		WidgetGalleryDTO wg5 = new WidgetGalleryDTO(UUID.fromString(id5), "alberto.nale@eng.it", "progression chart", "python", tagsList5, null, null,
				pythonExampleCode, null);
		WidgetGalleryDTO wg6 = new WidgetGalleryDTO(UUID.fromString(id6), "davide.vernassa@eng.it", "multicard", "html", tagsList6, null, null, null,
				String.format(cssExampleCode, id6));
		WidgetGalleryDTO wg7 = new WidgetGalleryDTO(UUID.fromString(id7), "alberto.nale@eng.it", "header_light", "html", tagsList7, null, null, null, null);
		WidgetGalleryDTO wg8 = new WidgetGalleryDTO(UUID.fromString(id8), "alberto.nale@eng.it", "header_dark", "html", tagsList8, htmlExampleCode, null, null,
				String.format(cssExampleCode, id8));
		WidgetGalleryDTO wg9 = new WidgetGalleryDTO(UUID.fromString(id9), "alberto.nale@eng.it", "header_dark_dark", "html", tagsList9, htmlExampleCode, null,
				null, String.format(cssExampleCode, id9));
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