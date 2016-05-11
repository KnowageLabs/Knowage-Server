package it.eng.spagobi.tools.table;

import static it.eng.spagobi.tools.glossary.util.Util.getNumberOrNull;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.metadata.dao.ISbiMetaTableDAO;
import it.eng.spagobi.metadata.metadata.SbiMetaTable;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.serialization.JsonConverter;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.json.JSONObject;

import com.mongodb.util.JSON;

@Path("/1.0/table")
@ManageAuthorization
public class tableService {

	@GET
	@Path("/listTable")
	public Response listListener(@Context HttpServletRequest req, @QueryParam("Page") String pageString, @QueryParam("ItemPerPage") String itemPerPageString,
			@QueryParam("label") String search) throws EMFUserError {
		String resp = "";
		Integer page = getNumberOrNull(pageString);
		Integer item_per_page = getNumberOrNull(itemPerPageString);
		search = search != null ? search : "";
		ISbiMetaTableDAO dao = getDao(req);
		List<SbiMetaTable> table;
		try {

			if (page == null || item_per_page == null) {
				table = dao.loadAllTables();
				resp = JsonConverter.objectToJson(table, table.getClass());

			} else {
				table = dao.loadPaginatedTables(page, item_per_page, search);

				JSONObject tmpResp = new JSONObject();
				tmpResp.put("item", JSON.parse(JsonConverter.objectToJson(table, table.getClass())));
				tmpResp.put("itemCount", dao.countSbiMetaTable(search));
				resp = tmpResp.toString();
			}

		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error while getting the list of documents", e);
		}

		return Response.ok(resp).build();
	}

	private ISbiMetaTableDAO getDao(HttpServletRequest req) throws EMFUserError {
		ISbiMetaTableDAO dao = DAOFactory.getSbiMetaTableDAO();
		dao.setUserProfile((IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE));
		return dao;
	}

}
