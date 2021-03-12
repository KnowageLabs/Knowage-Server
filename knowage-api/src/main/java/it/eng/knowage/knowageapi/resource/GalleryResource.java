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
		widgetGalleryDTOs.add(new WidgetGalleryDTO(UUID.fromString(id1), "davide.vernassa@eng.it", "colored card", "html", tagsList1));
		widgetGalleryDTOs.add(new WidgetGalleryDTO(UUID.fromString(id2), "davide.vernassa@eng.it", "advanced line chart", "html", tagsList2));
		widgetGalleryDTOs.add(new WidgetGalleryDTO(UUID.fromString(id3), "matteo.massarotto@eng.it", "hierarchy", "html", tagsList3));
		widgetGalleryDTOs.add(new WidgetGalleryDTO(UUID.fromString(id4), "davide.vernassa@eng.it", "double cards", "html", tagsList4));
		widgetGalleryDTOs.add(new WidgetGalleryDTO(UUID.fromString(id5), "alberto.nale@eng.it", "progression chart", "html", tagsList5));
		widgetGalleryDTOs.add(new WidgetGalleryDTO(UUID.fromString(id6), "davide.vernassa@eng.it", "multicard", "html", tagsList6));
		widgetGalleryDTOs.add(new WidgetGalleryDTO(UUID.fromString(id7), "alberto.nale@eng.it", "header_light", "html", tagsList7));
		widgetGalleryDTOs.add(new WidgetGalleryDTO(UUID.fromString(id8), "alberto.nale@eng.it", "header_dark", "html", tagsList8));
		mockMap.put(id1, new WidgetGalleryDTO(UUID.fromString(id1), "davide.vernassa@eng.it", "colored card", "html", tagsList1));
		mockMap.put(id2, new WidgetGalleryDTO(UUID.fromString(id2), "davide.vernassa@eng.it", "colored card", "html", tagsList2));
		mockMap.put(id3, new WidgetGalleryDTO(UUID.fromString(id3), "davide.vernassa@eng.it", "colored card", "html", tagsList3));
		mockMap.put(id4, new WidgetGalleryDTO(UUID.fromString(id4), "davide.vernassa@eng.it", "colored card", "html", tagsList4));
		mockMap.put(id5, new WidgetGalleryDTO(UUID.fromString(id5), "davide.vernassa@eng.it", "colored card", "html", tagsList5));
		mockMap.put(id6, new WidgetGalleryDTO(UUID.fromString(id6), "davide.vernassa@eng.it", "colored card", "html", tagsList6));
		mockMap.put(id7, new WidgetGalleryDTO(UUID.fromString(id7), "davide.vernassa@eng.it", "colored card", "html", tagsList7));
		mockMap.put(id8, new WidgetGalleryDTO(UUID.fromString(id8), "davide.vernassa@eng.it", "colored card", "html", tagsList8));
		return widgetGalleryDTOs;
	}

}