/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.api;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.services.serialization.JsonConverter;
import it.eng.spagobi.tools.udp.bo.Udp;
import it.eng.spagobi.tools.udp.dao.IUdpDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @authors Giovanni Luca Ulivo (GiovanniLuca.Ulivo@eng.it)
 *
 */
@Path("/1.0/udp")
public class UdpResource {

	@GET
	@Path("/loadUdp")
	@Produces(MediaType.APPLICATION_JSON)
	public String loadUDPGlossaryLikeLabel(@Context HttpServletRequest req) {
		try {
			IUdpDAO dao = DAOFactory.getUdpDAO();
			IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			// TODO check if profile is null
			dao.setUserProfile(profile);
			String lab = req.getParameter("LABEL");
			String fam = req.getParameter("FAMILY");
			// if(lab.trim().isEmpty()){
			// throw new SpagoBIServiceException(req.getPathInfo(),
			// "An unexpected error occured while executing service. Empty search label");
			// }
			if (fam.trim().isEmpty()) {
				throw new SpagoBIServiceException(req.getPathInfo(), "An unexpected error occured while executing service. Empty family");
			}
			List<Udp> lst = null;
			if (lab != null && !lab.trim().isEmpty()) {
				lst = dao.loadByFamilyAndLikeLabel(fam, lab);
			} else {
				lst = dao.loadAllByFamily(fam);
			}
			if (lst == null) {
				throw new SpagoBIServiceException(req.getPathInfo(), "An unexpected error occured while executing service. Null list");
			}

			JSONArray jarr = new JSONArray();
			for (Udp o : lst) {
				if (fam.compareTo("Glossary") == 0) {
					jarr.put(fromUdpLight(o));
				} else {
					jarr.put(JsonConverter.objectToJson(o, Udp.class));
				}

			}
			return jarr.toString();
		} catch (Throwable t) {
			throw new SpagoBIServiceException(req.getPathInfo(), "An unexpected error occured while executing service", t);
		}
	}

	private static JSONObject fromUdpLight(Udp SbiUdp) throws JSONException {
		JSONObject jobj = new JSONObject();
		jobj.put("ATTRIBUTE_ID", SbiUdp.getUdpId());
		jobj.put("ATTRIBUTE_NM", SbiUdp.getLabel());
		return jobj;
	}
}
