package it.eng.spagobi.tools.metaBusinessClass;

import static it.eng.spagobi.tools.glossary.util.Util.getNumberOrNull;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.metadata.dao.ISbiMetaBCDAO;
import it.eng.spagobi.metadata.metadata.SbiMetaBc;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@Path("/1.0/metaBC")
@ManageAuthorization
public class MetaBusinessClassService {

	@GET
	@Path("/listMetaBC")
	public Response listMetaBC(@Context HttpServletRequest req, @QueryParam("Page") String pageString, @QueryParam("ItemPerPage") String itemPerPageString,
			@QueryParam("label") String search) throws EMFUserError {
		String resp = "";
		Integer page = getNumberOrNull(pageString);
		Integer item_per_page = getNumberOrNull(itemPerPageString);
		search = search != null ? search : "";
		ISbiMetaBCDAO dao = getDao(req);
		List<SbiMetaBc> businessClass;
		try {

			if (page == null || item_per_page == null) {
				businessClass = dao.loadAllBCs();
				resp = listSbiMetaBcToLigthJson(businessClass).toString();

			} else {
				businessClass = dao.loadPaginatedMetaBC(page, item_per_page, search);

				JSONObject tmpResp = new JSONObject();
				tmpResp.put("item", listSbiMetaBcToLigthJson(businessClass));
				tmpResp.put("itemCount", dao.countSbiMetaBC(search));
				resp = tmpResp.toString();
			}

		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error while getting the list of businessClass", e);
		}

		return Response.ok(resp).build();
	}

	private JSONArray listSbiMetaBcToLigthJson(List<SbiMetaBc> bcList) throws JSONException {
		JSONArray tr = new JSONArray();
		for (SbiMetaBc smBC : bcList) {
			JSONObject jo = new JSONObject();
			jo.put("id", smBC.getBcId());
			jo.put("name", smBC.getName());
			tr.put(jo);
		}
		return tr;
	}

	private ISbiMetaBCDAO getDao(HttpServletRequest req) throws EMFUserError {
		ISbiMetaBCDAO dao = DAOFactory.getSbiMetaBCDAO();
		dao.setUserProfile((IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE));
		return dao;
	}

}
