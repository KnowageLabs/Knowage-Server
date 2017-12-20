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

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.json.JSONObject;

import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.dao.DAOFactory;
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

}
