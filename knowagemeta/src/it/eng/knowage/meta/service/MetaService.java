package it.eng.knowage.meta.service;

import it.eng.knowage.meta.initializer.BusinessModelInitializer;
import it.eng.knowage.meta.initializer.PhysicalModelInitializer;
import it.eng.knowage.meta.model.Model;
import it.eng.knowage.meta.model.ModelFactory;
import it.eng.knowage.meta.model.business.BusinessModel;
import it.eng.knowage.meta.model.business.BusinessTable;
import it.eng.knowage.meta.model.filter.PhysicalTableFilter;
import it.eng.knowage.meta.model.physical.PhysicalModel;
import it.eng.knowage.meta.model.physical.PhysicalTable;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.services.serialization.JsonConverter;
import it.eng.spagobi.tenant.Tenant;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.rest.RestUtilities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@Path("/1.0/metaWeb")
public class MetaService {
	private static Logger logger = Logger.getLogger(MetaService.class);
	private static final String MODEL_NAME = "modelName";

	/**
	 * Gets a json like this {datasourceId: 'xxx', physicalModels: ['name1', 'name2', ...], businessModels: ['name1', 'name2', ...]}
	 * 
	 * @param dsId
	 * @return
	 */

	@POST
	@Path("/create")
	public Response createModels(@Context HttpServletRequest req) {
		try {
			JSONObject json = RestUtilities.readBodyAsJSONObject(req);

			IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			if (profile != null) {
				UserProfile userProfile = (UserProfile) profile;
				TenantManager.setTenant(new Tenant(userProfile.getOrganization()));
			}

			Assert.assertTrue(json.has("datasourceId"), "datasourceId is mandatory");
			Assert.assertTrue(json.has("physicalModels"), "physicalModels is mandatory");
			Assert.assertTrue(json.has("businessModels"), "businessModels is mandatory");
			Integer dsId = json.getInt("datasourceId");
			List<String> physicalModels = (List<String>) JsonConverter.jsonToObject(json.getString("physicalModels"), List.class);
			List<String> businessModels = (List<String>) JsonConverter.jsonToObject(json.getString("businessModels"), List.class);

			Model model;

			String name = MODEL_NAME;

			model = ModelFactory.eINSTANCE.createModel();
			model.setName(name);

			PhysicalModelInitializer physicalModelInitializer = new PhysicalModelInitializer();
			physicalModelInitializer.setRootModel(model);
			PhysicalModel physicalModel = physicalModelInitializer.initialize(dsId, physicalModels);

			List<PhysicalTable> physicalTableToIncludeInBusinessModel = new ArrayList<PhysicalTable>();
			for (int i = 0; i < businessModels.size(); i++) {
				physicalTableToIncludeInBusinessModel.add(physicalModel.getTable(businessModels.get(i)));
			}
			PhysicalTableFilter physicalTableFilter = new PhysicalTableFilter(physicalTableToIncludeInBusinessModel);
			BusinessModelInitializer businessModelInitializer = new BusinessModelInitializer();
			BusinessModel businessModel = businessModelInitializer.initialize("pippoBusiness", physicalTableFilter, physicalModel);

			JSONObject translatedModel = new JSONObject();
			Iterator<PhysicalTable> physicalModelsIterator = physicalModel.getTables().iterator();
			JSONArray physicalModelJson = new JSONArray();
			while (physicalModelsIterator.hasNext()) {
				physicalModelJson.put(new JSONObject(physicalModelsIterator.next()));
			}

			JSONArray businessModelJson = new JSONArray();
			Iterator<BusinessTable> businessModelsIterator = businessModel.getBusinessTables().iterator();
			while (businessModelsIterator.hasNext()) {
				BusinessTable curr = businessModelsIterator.next();
				String tabelName = curr.getPhysicalTable().getName();
				curr.setPhysicalTable(null);
				JSONObject bmJson = new JSONObject(curr);
				bmJson.put("physicalTable", new JSONObject().put("name", tabelName));
				businessModelJson.put(bmJson);
			}
			translatedModel.put("physicalModel", physicalModelJson);
			translatedModel.put("businessModel", businessModelJson);

			return Response.ok(translatedModel.toString()).build();

		} catch (IOException | JSONException e) {
			logger.error(e);
		}
		return Response.serverError().build();
	}
}
