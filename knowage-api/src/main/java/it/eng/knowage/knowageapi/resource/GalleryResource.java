/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2021 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.knowage.knowageapi.resource;

import java.util.List;
import java.util.Optional;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import it.eng.knowage.boot.error.KnowageRuntimeException;
import it.eng.knowage.boot.utils.JsonConverter;
import it.eng.knowage.knowageapi.resource.dto.WidgetGalleryDTO;
import it.eng.knowage.knowageapi.service.WidgetGalleryAPI;
import it.eng.spagobi.services.security.SecurityServiceService;
import it.eng.spagobi.services.security.SpagoBIUserProfile;

@Path("/1.0/widgetgallery")
@Component
@Validated
public class GalleryResource {

	private static final Logger LOGGER = Logger.getLogger(GalleryResource.class);

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
			throw new KnowageRuntimeException(e);
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
		} catch (Exception e) {
			throw new KnowageRuntimeException("Error getting widget with id " + Optional.ofNullable(widgetId).orElse("null"), e);
		}
		return widgetGalleryDTO;

	}

	@GET
	@Path("/image/{widgetId}")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String widgetImageByWidgetId(@PathParam("widgetId") String widgetId) {
		WidgetGalleryDTO widgetGalleryDTO = null;
		try {
			SpagoBIUserProfile profile = getUserProfile();
			widgetGalleryDTO = widgetGalleryService.getWidgetsById(widgetId, profile);
		} catch (Exception e) {
			throw new KnowageRuntimeException("Error getting widget with id " + String.valueOf(widgetId), e);
		}
		return widgetGalleryDTO.getImage();

	}

	@GET
	@Path("/widgets/{type}")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public List<WidgetGalleryDTO> widgetType(@PathParam("type") String type) {
		List<WidgetGalleryDTO> widgetGalleryDTOs = null;
		try {
			SpagoBIUserProfile profile = getUserProfile();
			widgetGalleryDTOs = widgetGalleryService.getWidgetsByTenantType(profile, type);
		} catch (Exception e) {
			throw new KnowageRuntimeException("Error getting widget of type " + String.valueOf(type), e);
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
			throw new KnowageRuntimeException("Cannot create widget " + Optional.ofNullable(newSbiWidgetGallery).map(WidgetGalleryDTO::getName).orElse("null"), e);
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
			throw new KnowageRuntimeException("Error updating widget with id " + String.valueOf(widgetId), e);
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
			throw new KnowageRuntimeException("Error deleting widget with id " + String.valueOf(widgetId), e);
		}
		return response;

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

		} catch (Exception e) {
			throw new KnowageRuntimeException("Cannot import widget " + Optional.ofNullable(newSbiWidgetGallery).map(WidgetGalleryDTO::getName).orElse("null"), e);
		}

		return newSbiWidgetGallery;
	}

}