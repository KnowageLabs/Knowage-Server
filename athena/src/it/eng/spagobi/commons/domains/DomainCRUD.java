/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.domains;

import it.eng.spago.base.Constants;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IDomainDAO;
import it.eng.spagobi.commons.serializer.DomainJSONSerializer;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

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
public class DomainCRUD {

	private static final String DOMAIN_TYPE = "DOMAIN_TYPE";
	private static final String EXT_VERSION = "EXT_VERSION";

	static protected Logger logger = Logger.getLogger(DomainCRUD.class);

	@GET
	@Path("/listValueDescriptionByType")
	@Produces(MediaType.APPLICATION_JSON)
	public String getListDomainsByType(@Context HttpServletRequest req) {
		IDomainDAO domaindao = null;
		List<Domain> domains = null;

		String language = (String) req.getSession().getAttribute(Constants.USER_LANGUAGE);
		String country = (String) req.getSession().getAttribute(Constants.USER_COUNTRY);
		Locale locale = Locale.UK;
		if (language != null) {
			if (country == null && language != null) {
				locale = new Locale(language);
			} else {
				new Locale(language, country);
			}
		}

		JSONArray domainsJSONArray = new JSONArray();
		JSONObject domainsJSONObject = new JSONObject();

		String type = req.getParameter(DOMAIN_TYPE);
		String extVersion = req.getParameter(EXT_VERSION);

		JSONObject datasorcesJSON = new JSONObject();
		String result = null;
		try {
			domaindao = DAOFactory.getDomainDAO();
			domains = domaindao.loadListDomainsByType(type);
			domainsJSONArray = translate(domains, locale);
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

	protected JSONArray translate(List<Domain> domains, Locale locale) throws JSONException {
		JSONArray dialectsJSONArray = new JSONArray();
		if (domains != null) {
			for (int i = 0; i < domains.size(); i++) {
				JSONObject domain = new JSONObject();
				domain.put(DomainJSONSerializer.VALUE_NAME, domains.get(i).getTranslatedValueName(locale));
				domain.put(DomainJSONSerializer.VALUE_DECRIPTION, domains.get(i).getTranslatedValueDescription(locale));
				domain.put(DomainJSONSerializer.VALUE_ID, domains.get(i).getValueId());
				domain.put(DomainJSONSerializer.VALUE_CODE, domains.get(i).getValueCd());
				dialectsJSONArray.put(domain);
			}
		}
		return dialectsJSONArray;
	}
}
