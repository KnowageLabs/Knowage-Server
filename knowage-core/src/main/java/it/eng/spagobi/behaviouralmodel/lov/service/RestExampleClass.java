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

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@Path("/restExample")
public class RestExampleClass {

	static protected Logger logger = Logger.getLogger(RestExampleClass.class);

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String get() {

		IModalitiesValueDAO modalitiesValueDAO = null;
		List<ModalitiesValue> modalitiesValues;
		JSONObject modalitiesValuesJSON = new JSONObject();

		try {
			modalitiesValueDAO = DAOFactory.getModalitiesValueDAO();
			logger.debug("1:" + modalitiesValueDAO.toString());
			modalitiesValues = modalitiesValueDAO.loadAllModalitiesValue();
			logger.debug("2:" + modalitiesValues.toString());

			modalitiesValuesJSON = serializeModalitiesValues(modalitiesValues);
			// logger.debug("5:" + modalitiesValuesJSON);
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
				logger.debug("Saved");
			}

			else {

				moddao.modifyModalitiesValue(mod);
				logger.debug("Updated");

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

			logger.debug("6:" + modalitiesValuesJSONArray);

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
