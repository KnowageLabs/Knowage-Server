package it.eng.spagobi.mapcatalogue.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;

import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.dao.IObjTemplateDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.utilities.rest.RestUtilities;

@Path("/template")
@ManageAuthorization
public class TemplateCRUD {
	static private Logger logger = Logger.getLogger(TemplateCRUD.class);

	@GET
	public List loadTemplates(@Context HttpServletRequest req) throws JSONException, UnsupportedEncodingException, EMFInternalError, ParseException {
		Object data = null;
		List templates = new ArrayList();
		try {
			req.toString();

			data = req.getParameter("data");

			IObjTemplateDAO dao = DAOFactory.getObjTemplateDAO();
			templates = dao.getAllTemplateWithoutActive((String) data);
		} catch (EMFUserError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return templates;

	}

	@POST
	@Path("/deleteTemplate")
	public void deleteTemp(@Context HttpServletRequest req) throws EMFUserError, EMFInternalError, JSONException {
		JSONArray request = new JSONArray();

		try {
			request = RestUtilities.readBodyAsJSONArray(req);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		IObjTemplateDAO dao = DAOFactory.getObjTemplateDAO();
		dao.removeTemplates(request);

	}
}