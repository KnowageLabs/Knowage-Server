/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
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
package it.eng.spagobi.api.v2;

import java.net.URI;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONObject;

import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.i18n.dao.I18NMessagesDAO;
import it.eng.spagobi.i18n.metadata.SbiI18NMessageBody;
import it.eng.spagobi.i18n.metadata.SbiI18NMessages;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;

/**
 * @author Giulio Gavardi (giulio.gavardi@eng.it)
 */
@Path("/2.0/i18nMessages")
@ManageAuthorization
public class I18nResource extends AbstractSpagoBIResource {
	private final String charset = "; charset=UTF-8";

	@GET
	@Path("/") // i18nmessages/
	@Produces(MediaType.APPLICATION_JSON)
	public String loadI18NFromDB(@QueryParam("currLanguage") String currLanguage, @QueryParam("currCountry") String currCountry) {

		JSONObject toReturn = new JSONObject();
		if (currLanguage.endsWith("/")) {
			currLanguage = currLanguage.substring(0, currLanguage.lastIndexOf("/") - 1);
		}
		try {
			Locale locale = null;
			if (currLanguage != null && currCountry != null) {
				locale = new Locale(currLanguage, currCountry);
			} else {
				locale = Locale.ENGLISH;
			}
			Map<String, String> map = DAOFactory.getI18NMessageDAO().getAllI18NMessages(locale);

			// convert map to JSON Object
			for (Iterator iterator = map.keySet().iterator(); iterator.hasNext();) {
				String lab = (String) iterator.next();
				String val = map.get(lab);
				toReturn.put(lab, val);
			}

			return toReturn.toString();

		} catch (Exception e) {
			String errorString = "Error in getting translations";
			logger.error(errorString, e);
			throw new SpagoBIRestServiceException(errorString, buildLocaleFromSession(), e);
		}

	}

	/*
	 * Manage Internationalization Functionality
	 */
	@GET
	@Path("/internationalization/") // i18nmessages/internationalization/
	@Produces(MediaType.APPLICATION_JSON + charset)
	public Response getAllI18NMessages(@QueryParam("currLanguage") String currLanguage) {
		List<SbiI18NMessages> toReturn = null;
		try {
			toReturn = DAOFactory.getI18NMessageDAO().getI18NMessages(currLanguage);
			return Response.ok(toReturn).build();
		} catch (Exception e) {
			String errorString = "Error has occurred while getting Internationalization Message translations";
			logger.error(errorString, e);
			throw new SpagoBIRestServiceException(errorString, buildLocaleFromSession(), e);
		}
	}

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response saveI18NMessage(@Valid I18NMessageBodyDTO message) {
		I18NMessagesDAO I18NMessagesDAO = null;
		try {
			I18NMessagesDAO = DAOFactory.getI18NMessageDAO();
			I18NMessagesDAO.insertI18NMessage(toSbiI18NMessageBody(message));
			return Response.ok().build();
		} catch (Exception e) {
			logger.error("Error while saving I18NMessage", e);
			throw new SpagoBIRestServiceException("Error while saving new I18NMessage", buildLocaleFromSession(), e);
		}
	}

	private SbiI18NMessageBody toSbiI18NMessageBody(I18NMessageBodyDTO message) {
		return new SbiI18NMessageBody(message.getLabel(), message.getMessage(), message.getLanguage());
	}

	@PUT
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response modifyI18NMessage(@Valid I18NMessageDTO messageDTO) {
		SbiI18NMessages message = toSbiI18NMessages(messageDTO);
		I18NMessagesDAO I18NMessagesDAO = null;
		try {
			I18NMessagesDAO = DAOFactory.getI18NMessageDAO();
			// If updating Default Message Label, find others with particular Label and update them as well
			SbiI18NMessages messageBeforeUpdate = I18NMessagesDAO.getSbiI18NMessageById(message.getId());
			if (!message.getLabel().equals(messageBeforeUpdate.getLabel())) {
				I18NMessagesDAO.updateNonDefaultI18NMessagesLabel(messageBeforeUpdate, message);
			}
			I18NMessagesDAO.updateI18NMessage(message);
			String encodedI18NMessage = URLEncoder.encode("" + message.getId(), "UTF-8");
			return Response.created(new URI("/2.0/i18nMessages/" + encodedI18NMessage)).entity(encodedI18NMessage).build();
		} catch (Exception e) {
			logger.error("Error while updating I18NMessage", e);
			throw new SpagoBIRestServiceException("Error while updating I18NMessage", buildLocaleFromSession(), e);
		}
	}

	private SbiI18NMessages toSbiI18NMessages(I18NMessageDTO m) {
		return new SbiI18NMessages(m.getId(), m.getLanguageCd(), m.getLabel(), m.getMessage());
	}

	/*
	 * Deleting Non-Default i18nMessages
	 */
	@DELETE
	@Path("/{id}")
	public Response deleteI18NMessage(@PathParam("id") Integer id) {
		I18NMessagesDAO I18NMessagesDAO = null;
		try {
			I18NMessagesDAO = DAOFactory.getI18NMessageDAO();
			I18NMessagesDAO.deleteI18NMessage(id);
			String encodedI18NMessage = URLEncoder.encode("" + id, "UTF-8");
			return Response.ok().entity(encodedI18NMessage).build();
		} catch (Exception e) {
			logger.error("Error has occurred while deleting I18NMessage", e);
			throw new SpagoBIRestServiceException("Error while deleting I18NMessage", buildLocaleFromSession(), e);
		}
	}

	/*
	 * Deleting Default i18nMessage, and all other i18nMessages for that particular Label
	 */
	@DELETE
	@Path("/deletedefault/{id}")
	public Response deleteDefaultI18NMessage(@PathParam("id") Integer id) {
		I18NMessagesDAO I18NMessagesDAO = null;
		try {
			I18NMessagesDAO = DAOFactory.getI18NMessageDAO();
			SbiI18NMessages message = I18NMessagesDAO.getSbiI18NMessageById(id);
			I18NMessagesDAO.deleteNonDefaultI18NMessages(message);
			I18NMessagesDAO.deleteI18NMessage(id);
			String encodedI18NMessage = URLEncoder.encode("" + id, "UTF-8");
			return Response.ok().entity(encodedI18NMessage).build();
		} catch (Exception e) {
			logger.error("Error has occurred while deleting Default-Language I18NMessage", e);
			throw new SpagoBIRestServiceException("Error while deleting Default-Language I18NMessage", buildLocaleFromSession(), e);
		}
	}

}
