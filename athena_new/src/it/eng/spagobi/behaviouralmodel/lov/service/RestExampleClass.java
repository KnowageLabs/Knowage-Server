package it.eng.spagobi.behaviouralmodel.lov.service;

import it.eng.spagobi.behaviouralmodel.lov.bo.ModalitiesValue;
import it.eng.spagobi.behaviouralmodel.lov.dao.IModalitiesValueDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.serializer.SerializationException;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.utilities.rest.RestUtilities;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@Path("/restExample")
public class RestExampleClass {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String get() {

		IModalitiesValueDAO modalitiesValueDAO = null;
		List<ModalitiesValue> modalitiesValues;
		JSONObject modalitiesValuesJSON = new JSONObject();

		try {
			modalitiesValueDAO = DAOFactory.getModalitiesValueDAO();
			System.out.println("1:" + modalitiesValueDAO.toString());
			modalitiesValues = modalitiesValueDAO.loadAllModalitiesValue();
			System.out.println("2:" + modalitiesValues.toString());

			modalitiesValuesJSON = serializeModalitiesValues(modalitiesValues);
			// System.out.println("5:" + modalitiesValuesJSON);
		} catch (Throwable t) {
			// TODO Auto-generated catch block
			t.printStackTrace();
		}

		return modalitiesValuesJSON.toString();

	}

	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	public String delete(@Context HttpServletRequest req) throws SerializationException, JSONException {

		ModalitiesValue mod = null;
		try {

			JSONObject requestBodyJSON = RestUtilities.readBodyAsJSONObject(req);
			String label = (String) requestBodyJSON.opt("LABEL");
			mod = DAOFactory.getModalitiesValueDAO().loadModalitiesValueByLabel(label);

			DAOFactory.getModalitiesValueDAO().eraseModalitiesValue(mod);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "obrisano";

	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public String save(@Context HttpServletRequest req) {

		try {

			JSONObject requestBodyJSON = RestUtilities.readBodyAsJSONObject(req);
			IModalitiesValueDAO moddao = DAOFactory.getModalitiesValueDAO();

			ModalitiesValue mod = recoverModalitiesValueDetails(requestBodyJSON);

			if (mod.getId() == -1) {

				moddao.insertModalitiesValue(mod);
				System.out.println("Saved");
			}

			else {

				moddao.modifyModalitiesValue(mod);
				System.out.println("Updated");

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return "saved";

	}

	private JSONObject serializeModalitiesValues(List<ModalitiesValue> modalitiesValues) throws SerializationException, JSONException {

		JSONObject modalitiesValuesJSON = new JSONObject();
		JSONArray modalitiesValuesJSONArray = new JSONArray();

		if (modalitiesValues != null) {

			modalitiesValuesJSONArray = (JSONArray) SerializerFactory.getSerializer("application/json").serialize(modalitiesValues, null);

			System.out.println("6:" + modalitiesValuesJSONArray);

			modalitiesValuesJSON.put("LOVs", modalitiesValuesJSONArray);

		}

		return modalitiesValuesJSON;
	}

	private ModalitiesValue recoverModalitiesValueDetails(JSONObject requestBodyJSON) {

		ModalitiesValue mod = new ModalitiesValue();
		Integer id = -1;
		String idStr = (String) requestBodyJSON.opt("ID");
		if (idStr != null && !idStr.equals("")) {
			id = new Integer(idStr);
		}

		String modName = (String) requestBodyJSON.opt("NAME");
		String modDesc = (String) requestBodyJSON.opt("DESCRIPTION");
		String modlovProv = (String) requestBodyJSON.opt("LOVPROVIDER");
		String moditypecd = (String) requestBodyJSON.opt("ITYPECD");
		String moditypeid = (String) requestBodyJSON.opt("ITYPEID");
		String modlabel = (String) requestBodyJSON.opt("LABEL");
		String modSelectionType = (String) requestBodyJSON.opt("SELECTIONTYPE");

		mod.setId(id);
		mod.setName(modName);
		mod.setDescription(modDesc);
		mod.setLovProvider(modlovProv);
		mod.setITypeCd(moditypecd);
		mod.setITypeId(moditypeid);
		mod.setLabel(modlabel);
		mod.setSelectionType(modSelectionType);

		return mod;
	}

}
