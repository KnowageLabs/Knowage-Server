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
package it.eng.spagobi.commons.domains;

import it.eng.spago.base.Constants;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IDomainDAO;
import it.eng.spagobi.commons.serializer.DomainJSONSerializer;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
@Path("/domains")
public class DomainCRUD extends AbstractSpagoBIResource {

	private static final String DOMAIN_TYPE = "DOMAIN_TYPE";
	private static final String EXT_VERSION = "EXT_VERSION";
	protected final String charset = "; charset=UTF-8";

	static protected Logger logger = Logger.getLogger(DomainCRUD.class);

	@GET
	@Path("/listValueDescriptionByType")
	@Produces(MediaType.APPLICATION_JSON)
	public String getListDomainsByType(@Context HttpServletRequest req) {
		IDomainDAO domaindao = null;
		List<Domain> domains = null;

		JSONArray domainsJSONArray = new JSONArray();
		JSONObject domainsJSONObject = new JSONObject();

		String type = req.getParameter(DOMAIN_TYPE);
		String extVersion = req.getParameter(EXT_VERSION);

		JSONObject datasorcesJSON = new JSONObject();
		String result = null;
		try {
			domaindao = DAOFactory.getDomainDAO();
			domains = domaindao.loadListDomainsByType(type);
			domainsJSONArray = translate(domains, getLocale(req));
			domainsJSONObject.put("domains", domainsJSONArray);

			if ((extVersion != null) && (extVersion.equals("3"))) {
				result = domainsJSONObject.toString();

			} else {
				result = domainsJSONArray.toString();
			}

		} catch (Throwable t) {
			throw new SpagoBIServiceException("An unexpected error occured while instatiating the dao", t);
		}

		logger.debug("------------------------ " + result);

		return result;

	}

	protected JSONArray translate(Collection<Domain> domains, Locale locale) throws JSONException {
		JSONArray dialectsJSONArray = new JSONArray();
		if (domains != null) {
			for (Domain domainObject : domains) {
				JSONObject domain = new JSONObject();
				domain.put(DomainJSONSerializer.VALUE_NAME, domainObject.getTranslatedValueName(locale));
				domain.put(DomainJSONSerializer.VALUE_DECRIPTION, domainObject.getTranslatedValueDescription(locale));
				domain.put(DomainJSONSerializer.VALUE_ID, domainObject.getValueId());
				domain.put(DomainJSONSerializer.VALUE_CODE, domainObject.getValueCd());
				dialectsJSONArray.put(domain);
			}
		}
		return dialectsJSONArray;
	}

	protected Locale getLocale(@Context HttpServletRequest req) {
		String language = (String) req.getSession().getAttribute(Constants.USER_LANGUAGE);
		String country = (String) req.getSession().getAttribute(Constants.USER_COUNTRY);
		Locale locale = Locale.UK;
		if (language != null) {
			if (country == null && language != null) {
				locale = new Locale(language);
			} else {
				locale = new Locale(language, country);
			}
		}
		return locale;
	}
}
