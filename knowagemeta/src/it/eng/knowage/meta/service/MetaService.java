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
import it.eng.knowage.meta.model.serializer.EmfXmiSerializer;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.serialization.JsonConverter;
import it.eng.spagobi.tenant.Tenant;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.rest.RestUtilities;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.EList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.zjsonpatch.JsonDiff;

@ManageAuthorization
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

			Model model = createModel(json);

			JSONObject translatedModel = createJson(model);

			return Response.ok(translatedModel.toString()).build();

		} catch (IOException | JSONException e) {
			logger.error(e);
		}
		return Response.serverError().build();
	}

	@POST
	@Path("/generateModel")
	public Response generateModel(@Context HttpServletRequest req) {
		try {
			EmfXmiSerializer serializer = new EmfXmiSerializer();

			String jsonString = RestUtilities.readBody(req);
			JSONObject json = new JSONObject(jsonString);

			ObjectMapper mapper = new ObjectMapper();
			JsonNode actualJson = mapper.readTree(jsonString);

			Assert.assertTrue(json.has("physicalModel"), "physicalModel is mandatory");
			Assert.assertTrue(json.has("businessModel"), "businessModel is mandatory");

			Model model = createModel(json);
			serializer.serialize(model, new File("c:\\test.sbimodel_old.txt"));

			JSONObject jsonOld = createJson(model);

			// JsonNode patch = JsonDiff.asJson(mapper.readTree(jsonOld.toString()), actualJson);
			jsonOld.remove("physicalModel");
			JsonNode patch = JsonDiff.asJson(mapper.readTree(jsonOld.getJSONArray("businessModel").toString()), actualJson.get("businessModel"));

			try {
				applyPatch(patch, model.getBusinessModels().get(0));
			} catch (SpagoBIException e) {
				throw new SpagoBIServiceException(req.getPathInfo(), e);
			}

			serializer.serialize(model, new File("c:\\test.sbimodel_new.txt"));
			/*
			 * JSONArray physicalModels = json.getJSONArray("physicalModel"); Map<String, JSONObject> pmMap = new HashMap<>(); for (int i = 0; i <
			 * physicalModels.length(); i++) { JSONObject physicalModel = physicalModels.getJSONObject(i); pmMap.put(physicalModel.getString("name"),
			 * physicalModel); }
			 * 
			 * JSONArray businessTablesJson = json.getJSONArray("businessModel"); Map<String, JSONObject> bmMap = new HashMap<>(); for (int i = 0; i <
			 * businessTablesJson.length(); i++) { JSONObject businessTableJson = businessTablesJson.getJSONObject(i);
			 * bmMap.put(businessTableJson.getString("uniqueName"), businessTableJson); }
			 * 
			 * Iterator<BusinessTable> businessTableIterator = model.getBusinessModels().get(0).getBusinessTables().iterator(); while
			 * (businessTableIterator.hasNext()) { BusinessTable businessTable = businessTableIterator.next(); Iterator<BusinessColumn> columnIterator =
			 * businessTable.getColumns().iterator(); JSONObject businessTableJson = bmMap.get(businessTable.getName()); while (columnIterator.hasNext()) {
			 * BusinessColumn businessColumn = columnIterator.next(); businessColumn.getProperties().clear(); } }
			 */

			return Response.ok().build();
		} catch (IOException | JSONException e) {
			logger.error(e);
		}
		return Response.serverError().build();
	}

	private void applyPatch(JsonNode patch, BusinessModel model) throws SpagoBIException {
		System.out.println(patch.toString());
		Iterator<JsonNode> elements = patch.elements();
		JXPathContext context = JXPathContext.newContext(model);
		while (elements.hasNext()) {
			JsonNode jsonNode = elements.next();
			String operation = jsonNode.get("op").textValue();
			String path = jsonNode.get("path").textValue();

			path = cleanPath(path);

			System.out.println(operation + " > " + path);

			switch (operation.toUpperCase()) {
			case "ADD":
				System.out.println("ADD - todo");
				System.out.println(path);
				break;
			case "REMOVE":
				remove(path, context);
				break;
			case "REPLACE":
				String value = jsonNode.get("value").textValue();
				Pointer empPtr = context.getPointer(path);
				empPtr.setValue(value);
				break;
			case "MOVE":
				if (path.matches(".*\\]$")) {
					String from = cleanPath(jsonNode.get("from").textValue());
					Object obj = context.getPointer(from).getNode();
					remove(from, context);
					add(obj, path, context);
				} else {
					throw new SpagoBIException("TODO operation [" + operation + "] not supported for single element");
				}
				break;
			default:
				throw new SpagoBIException("invalid json patch operation [" + operation + "]");
			}
		}

	}

	private void remove(String path, JXPathContext context) throws SpagoBIException {
		if (path.matches(".*\\]$")) {
			int i = path.lastIndexOf("[");
			Pointer obj = context.getPointer(path);
			System.out.println("REMOVE > " + path + " > " + obj.getNode());
			Pointer coll = context.getPointer(path.substring(0, i));
			((List<?>) coll.getNode()).remove(obj.getNode());
		} else {
			throw new SpagoBIException("TODO operation \"remove\" not supported for single element");
		}
	}

	private void add(Object obj, String topath, JXPathContext context) throws SpagoBIException {
		System.out.println("ADD > " + obj + " > " + topath);
		int i = topath.lastIndexOf("[");
		int j = topath.lastIndexOf("]");
		Pointer toColl = context.getPointer(topath.substring(0, i));
		((List) toColl.getNode()).add(Integer.parseInt(topath.substring(i + 1, j)) - 1, obj);
	}

	private String cleanPath(String path) {
		// path.replaceAll("^/physicalModel/", "/physicalModels/0/tables/").replaceAll("^/businessModel/", "businessModels/0/businessTables/");
		Pattern p = Pattern.compile("(/)(\\d)");
		Matcher m = p.matcher(path);
		StringBuffer s = new StringBuffer("/businessTables");
		while (m.find()) {
			m.appendReplacement(s, "[" + String.valueOf(1 + Integer.parseInt(m.group(2))) + "]");
		}
		m.appendTail(s);
		return s.toString();
	}

	private JSONObject createJson(Model model) throws JSONException {
		JSONObject translatedModel = new JSONObject();
		JSONArray physicalModelJson = new JSONArray();
		Map<String, Integer> physicalTableMap = new HashMap<>();
		EList<PhysicalTable> physicalTables = model.getPhysicalModels().get(0).getTables();
		JSONArray businessModelJson = new JSONArray();
		Iterator<BusinessTable> businessModelsIterator = model.getBusinessModels().get(0).getBusinessTables().iterator();
		while (businessModelsIterator.hasNext()) {
			BusinessTable curr = businessModelsIterator.next();
			String tabelName = curr.getPhysicalTable().getName();
			JSONObject bmJson = new JSONObject(JsonConverter.objectToJson(curr, curr.getClass()));
			bmJson.put("physicalTable", new JSONObject().put("physicalTableIndex", physicalTableMap.get(tabelName)));
			businessModelJson.put(bmJson);
		}
		for (int j = 0; j < physicalTables.size(); j++) {
			PhysicalTable physicalTable = physicalTables.get(j);
			physicalModelJson.put(new JSONObject(JsonConverter.objectToJson(physicalTable, physicalTable.getClass())));
			physicalTableMap.put(physicalTable.getName(), j);
		}
		translatedModel.put("physicalModel", physicalModelJson);
		translatedModel.put("businessModel", businessModelJson);
		return translatedModel;
	}

	private Model createModel(JSONObject json) throws JSONException {
		Assert.assertTrue(json.has("datasourceId"), "datasourceId is mandatory");
		Assert.assertTrue(json.has("modelName"), "modelName is mandatory");
		Assert.assertTrue(json.has("physicalModels"), "physicalModels is mandatory");
		Assert.assertTrue(json.has("businessModels"), "businessModels is mandatory");

		Integer dsId = json.getInt("datasourceId");
		String modelName = json.getString("modelName");
		List<String> physicalModels = (List<String>) JsonConverter.jsonToObject(json.getString("physicalModels"), List.class);
		List<String> businessModels = (List<String>) JsonConverter.jsonToObject(json.getString("businessModels"), List.class);

		Model model = ModelFactory.eINSTANCE.createModel();
		model.setName(modelName);

		PhysicalModelInitializer physicalModelInitializer = new PhysicalModelInitializer();
		physicalModelInitializer.setRootModel(model);
		PhysicalModel physicalModel = physicalModelInitializer.initialize(dsId, physicalModels);

		List<PhysicalTable> physicalTableToIncludeInBusinessModel = new ArrayList<PhysicalTable>();
		for (int i = 0; i < businessModels.size(); i++) {
			physicalTableToIncludeInBusinessModel.add(physicalModel.getTable(businessModels.get(i)));
		}
		PhysicalTableFilter physicalTableFilter = new PhysicalTableFilter(physicalTableToIncludeInBusinessModel);
		BusinessModelInitializer businessModelInitializer = new BusinessModelInitializer();
		businessModelInitializer.initialize("pippoBusiness", physicalTableFilter, physicalModel);

		return model;
	}

	public static void main(String[] args) {
		Pattern p = Pattern.compile("(/)(\\d)(/)");
		String input = "/physicalModel/0/columns/0/name";
		Matcher m = p.matcher(input);
		StringBuffer s = new StringBuffer();
		while (m.find()) {
			System.out.println(m.group(2));
			m.appendReplacement(s, "[" + String.valueOf(1 + Integer.parseInt(m.group(2))) + "]/");
		}
		System.out.println(">" + s.toString());
		System.exit(0);
	}
}
