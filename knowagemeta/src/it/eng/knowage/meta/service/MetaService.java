package it.eng.knowage.meta.service;

import it.eng.knowage.meta.generator.jpamapping.JpaMappingJarGenerator;
import it.eng.knowage.meta.initializer.BusinessModelInitializer;
import it.eng.knowage.meta.initializer.PhysicalModelInitializer;
import it.eng.knowage.meta.model.Model;
import it.eng.knowage.meta.model.ModelFactory;
import it.eng.knowage.meta.model.ModelPropertyType;
import it.eng.knowage.meta.model.business.BusinessColumn;
import it.eng.knowage.meta.model.business.BusinessColumnSet;
import it.eng.knowage.meta.model.business.BusinessModel;
import it.eng.knowage.meta.model.business.BusinessModelFactory;
import it.eng.knowage.meta.model.business.BusinessRelationship;
import it.eng.knowage.meta.model.business.BusinessTable;
import it.eng.knowage.meta.model.filter.PhysicalTableFilter;
import it.eng.knowage.meta.model.physical.PhysicalModel;
import it.eng.knowage.meta.model.physical.PhysicalTable;
import it.eng.knowage.meta.model.serializer.EmfXmiSerializer;
import it.eng.knowage.meta.model.serializer.ModelPropertyFactory;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.serialization.JsonConverter;
import it.eng.spagobi.tenant.Tenant;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.tools.catalogue.bo.Content;
import it.eng.spagobi.tools.catalogue.dao.IMetaModelsDAO;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.rest.RestUtilities;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
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
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.flipkart.zjsonpatch.JsonDiff;

@ManageAuthorization
@Path("/1.0/metaWeb")
public class MetaService {
	private static Logger logger = Logger.getLogger(MetaService.class);
	private static final String DEFAULT_MODEL_NAME = "modelName";
	private static final String EMF_MODEL = "EMF_MODEL";

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

			Model model = createEmptyModel(json);
			req.getSession().setAttribute(EMF_MODEL, model);

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

			JSONObject jsonRoot = RestUtilities.readBodyAsJSONObject(req);

			// Assert.assertTrue(json.has("physicalModel"), "physicalModel is mandatory");
			// Assert.assertTrue(json.has("businessModel"), "businessModel is mandatory");

			// Model model = createEmptyModel(json);
			Model model = (Model) req.getSession().getAttribute(EMF_MODEL);

			serializer.serialize(model, new File("c:\\test.sbimodel_old.txt"));

			// JsonNode patch = JsonDiff.asJson(mapper.readTree(emptyModelJson.toString()), actualJson);

			ObjectMapper mapper = new ObjectMapper();
			if (jsonRoot.has("diff")) {
				JsonNode patch = mapper.readTree(jsonRoot.getString("diff"));
				applyPatch(patch, model);
			}

			// TODO Check if this is still needed
			JSONObject jsonModel = createJson(model);
			JsonNode actualJson = mapper.readTree(jsonModel.toString());
			applyRelationships(actualJson, model);

			serializer.serialize(model, new File("c:\\test.sbimodel_new.txt"));
			System.out.println("!!! model generation ended !!!");

			return Response.ok().build();
		} catch (IOException | JSONException e) {
			logger.error(e);
		} catch (SpagoBIException e) {
			throw new SpagoBIServiceException(req.getPathInfo(), e);
		}
		return Response.serverError().build();
	}

	@POST
	@Path("/addBusinessModel")
	public Response addBusinessModel(@Context HttpServletRequest req) {
		try {
			EmfXmiSerializer serializer = new EmfXmiSerializer();

			String jsonString = RestUtilities.readBody(req);
			JSONObject json = new JSONObject(jsonString);

			Model model = (Model) req.getSession().getAttribute(EMF_MODEL);

			if (json.has("diff")) {
				ObjectMapper mapper = new ObjectMapper();
				JsonNode patch = mapper.readTree(json.getString("diff"));
				applyPatch(patch, model);
			}

			JSONObject emptyModelJson = createJson(model);

			// TODO
		} catch (IOException | JSONException e) {
			logger.error(e);
		} catch (SpagoBIException e) {
			throw new SpagoBIServiceException(req.getPathInfo(), e);
		}
		return Response.serverError().build();
	}

	@POST
	@Path("/addBusinessRelation")
	public Response addBusinessRelation(@Context HttpServletRequest req) {
		try {
			JSONObject jsonRoot = RestUtilities.readBodyAsJSONObject(req);

			Model model = (Model) req.getSession().getAttribute(EMF_MODEL);

			JSONObject oldJsonModel = createJson(model);

			ObjectMapper mapper = new ObjectMapper();
			if (jsonRoot.has("diff")) {
				JsonNode patch = mapper.readTree(jsonRoot.getString("diff"));
				applyPatch(patch, model);
			}

			JSONObject json = jsonRoot.getJSONObject("data");
			String name = json.getString("name");

			// TODO remove this
			json.put("uniqueName", name);

			// String destTableName = json.getString("destinationTable");
			// String sourceTableName = json.getString("sourceTable");
			// JSONArray destCols = json.getJSONArray("destinationColumns");
			// JSONArray sourceCols = json.getJSONArray("sourceColumns");

			BusinessModel bm = model.getBusinessModels().get(0);

			JsonNode rel = mapper.readTree(json.toString());
			applyRelationships(rel, bm);

			// BusinessRelationship rel = BusinessModelFactory.eINSTANCE.createBusinessRelationship();
			// rel.setModel(bm);
			// rel.setName(name);
			// rel.getProperties();
			// rel.getPropertyTypes();
			// rel.getPhysicalForeignKey();
			// BusinessColumnSet destColumnSet = BusinessModelFactory.eINSTANCE.createBusinessColumnSet();
			// destColumnSet.setName(destTableName);
			// rel.setDestinationTable(destColumnSet);
			// BusinessColumnSet sourceColumnSet = BusinessModelFactory.eINSTANCE.createBusinessColumnSet();
			// sourceColumnSet.setName(sourceTableName);
			// rel.setSourceTable(sourceColumnSet);
			//
			// for (int i = 0; i < destCols.length(); i++) {
			// String colName = destCols.getString(i);
			// BusinessColumn bc = BusinessModelFactory.eINSTANCE.createBusinessColumn();
			// // bc.setUniqueName(colName);
			//
			// rel.getDestinationColumns().add(bc);
			// }
			//
			// for (int i = 0; i < sourceCols.length(); i++) {
			// String colName = sourceCols.getString(i);
			// BusinessColumn bc = BusinessModelFactory.eINSTANCE.createBusinessColumn();
			// bc.setUniqueName(colName);
			// rel.getSourceColumns().add(bc);
			// }
			// bm.getRelationships().add(rel);

			JSONObject jsonModel = createJson(model);
			JsonNode patch = JsonDiff.asJson(mapper.readTree(oldJsonModel.toString()), mapper.readTree(jsonModel.toString()));

			return Response.ok(patch.toString()).build();
		} catch (IOException | JSONException e) {
			logger.error(e);
		} catch (SpagoBIException e) {
			throw new SpagoBIServiceException(req.getPathInfo(), e);
		}
		return Response.serverError().build();
	}

	@GET
	@Path("/buildModel")
	public Response buildModel(@QueryParam("id") Integer id, @Context HttpServletRequest req) {
		Model model = getModel(id);
		JpaMappingJarGenerator jpaMappingJarGenerator = new JpaMappingJarGenerator();
		// JpaMappingJarGenerator.defaultTemplateFolderPath =
		// "D:/Sviluppo/Athena/knowagemeta-unit-test/workspaces/metadata/it.eng.knowage.meta.generator/templates";
		// File projectRootFolder = new File("D:/Sviluppo/Athena/knowagemeta-unit-test/workspaces/metadata/it.eng.knowage.meta.generator");
		// jpaMappingJarGenerator.setLibDir(new File(projectRootFolder, "libs/eclipselink"));
		jpaMappingJarGenerator.setLibs(new String[] { "org.eclipse.persistence.core_2.1.2.jar", "javax.persistence_2.0.1.jar" });
		try {
			// java.nio.file.Path outFile = Files.createTempFile("model_", "_tmp");
			java.nio.file.Path outDir = Files.createTempDirectory("model_");
			jpaMappingJarGenerator.generate(model.getBusinessModels().get(0), outDir.toString());
			IMetaModelsDAO dao = DAOFactory.getMetaModelsDAO();
			dao.setUserProfile((IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE));
			Content content = new Content();
			// convert the uploaded file to input stream
			// InputStream inputStream = inputPart.getBody(InputStream.class, null);
			// bytes = IOUtils.toByteArray(inputStream);
			// content.setContent(bytes);
			// content.setCreationDate(new Date());
			// content.setCreationUser(getUserProfile().getUserName().toString());
			// dao.insertMetaModelContent(id, content );
		} catch (IOException e) {
			// TODO
			e.printStackTrace();
		}
		return Response.serverError().build();
	}

	private Model getModel(Integer id) {
		// TODO get file from db
		File f = new File("c:\\test.sbimodel_new.txt");
		EmfXmiSerializer serializer = new EmfXmiSerializer();
		return serializer.deserialize(f);
	}

	private void applyRelationships(JsonNode actualJson, Model model) {
		Iterator<JsonNode> btIterator = actualJson.get("businessModel").elements();
		BusinessModel bm = model.getBusinessModels().get(0);
		EList<BusinessRelationship> relationships = bm.getRelationships();
		relationships.clear();
		Map<String, Boolean> relationshipMap = new HashMap<>();
		while (btIterator.hasNext()) {
			JsonNode bt = btIterator.next();
			JsonNode rels = bt.get("relationships");
			if (rels != null && !rels.isNull()) {
				Iterator<JsonNode> relIndex = rels.elements();
				while (relIndex.hasNext()) {
					JsonNode rel = relIndex.next();
					String uniqueName = rel.get("uniqueName").textValue();
					if (!Boolean.TRUE.equals(relationshipMap.get(uniqueName))) {
						relationshipMap.put(uniqueName, Boolean.TRUE);
						applyRelationships(rel, bm);
					}
				}
				System.out.println("relationships: " + relationships.size());
			}
		}
		bm.getBusinessTables().iterator();
	}

	private void applyRelationships(JsonNode rel, BusinessModel bm) {
		BusinessRelationship br = BusinessModelFactory.eINSTANCE.createBusinessRelationship();
		bm.getRelationships().add(br);
		String uniqueName = rel.get("uniqueName").textValue();

		String sourceTableName = rel.get("sourceTableName").asText().toLowerCase();
		String destinationTableName = rel.get("destinationTableName").asText().toLowerCase();

		BusinessColumnSet sourceBcs = bm.getBusinessTableByUniqueName(sourceTableName);
		br.setSourceTable(sourceBcs);

		BusinessColumnSet destBcs = bm.getBusinessTableByUniqueName(destinationTableName);
		br.setDestinationTable(destBcs);

		if (rel.has("id"))
			br.setId(rel.get("id").textValue());
		if (rel.has("description"))
			br.setDescription(rel.get("description").textValue());
		if (rel.has("name"))
			br.setName(rel.get("name").textValue());

		br.setUniqueName(uniqueName);

		Iterator<JsonNode> sourceColsIterator = rel.get("sourceColumns").elements();
		while (sourceColsIterator.hasNext()) {
			JsonNode jsonNode = sourceColsIterator.next();
			BusinessColumn bc;
			if (jsonNode.isTextual()) {
				bc = sourceBcs.getSimpleBusinessColumnByUniqueName(jsonNode.asText());
			} else {
				bc = sourceBcs.getSimpleBusinessColumnByUniqueName(jsonNode.get("uniqueName").asText());
			}
			br.getSourceColumns().add(bc);
		}
		Iterator<JsonNode> destColsIterator = rel.get("destinationColumns").elements();
		while (destColsIterator.hasNext()) {
			JsonNode jsonNode = destColsIterator.next();
			BusinessColumn bc;
			if (jsonNode.isTextual()) {
				bc = destBcs.getSimpleBusinessColumnByUniqueName(jsonNode.asText());
			} else {
				bc = destBcs.getSimpleBusinessColumnByUniqueName(jsonNode.get("uniqueName").asText());
			}
			br.getDestinationColumns().add(bc);
		}
		// setting multiplicity and other properties
		if (rel.has("properties")) {
			Iterator<JsonNode> propIterator = rel.get("properties").elements();
			while (propIterator.hasNext()) {
				JsonNode jsonNode = propIterator.next();
				ModelPropertyType propType = bm.getPropertyType(jsonNode.get("key").textValue());
				br.setProperty(propType, jsonNode.get("value").textValue());
			}
		}
		// This field should be null (in relationship)
		// br.setPhysicalForeignKey();
	}

	private void applyPatch(JsonNode patch, Model model) throws SpagoBIException {
		System.out.println(patch.toString());
		Iterator<JsonNode> elements = patch.elements();
		JXPathContext context = JXPathContext.newContext(model);
		context.setFactory(new ModelPropertyFactory());
		while (elements.hasNext()) {
			JsonNode jsonNode = elements.next();
			String operation = jsonNode.get("op").textValue();
			String path = jsonNode.get("path").textValue();

			if (toSkip(path)) {
				System.out.println("skipping " + path);
				continue;
			}

			path = cleanPath(path);

			switch (operation.toUpperCase()) {
			case "ADD":
				JsonNode node = jsonNode.get("value");
				System.out.println("add " + node.asText());
				addJson(node, path, context);
				break;
			case "REMOVE":
				remove(path, context);
				break;
			case "REPLACE":
				String value = jsonNode.get("value").textValue();
				replace(value, path, context);
				break;
			case "MOVE":
				String from = cleanPath(jsonNode.get("from").textValue());
				System.out.println("move from [" + from + "] to [" + path + "]");
				Object obj = context.getPointer(from).getNode();
				remove(from, context);
				// addValue(obj, path, context);
				replace(obj, path, context);
				break;
			default:
				throw new SpagoBIException("invalid json patch operation [" + operation + "]");
			}
		}
	}

	private boolean toSkip(String path) {
		if (path.equals("/datasourceId") || path.equals("/modelName") || path.equals("/physicalModels") || path.equals("/businessModels")
				|| path.contains("/physicalColumn/") || path.contains("/simpleBusinessColumns") || path.contains("relationships")) {
			System.out.println("skipping " + path);
			return true;
		}
		return false;
	}

	private void replace(Object value, String path, JXPathContext context) {
		try {
			if (value instanceof TextNode)
				value = ((TextNode) value).asText();
			if (value == null || value.equals("null") || value instanceof NullNode)
				value = null;
			context.createPathAndSetValue(path, value);
		} catch (Throwable t) {
			// TODO
			t.printStackTrace();
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
			// TODO
			// throw new SpagoBIException("TODO operation \"remove\" not supported for single element");
		}
	}

	private void addJson(JsonNode obj, String topath, JXPathContext context) throws SpagoBIException {
		if (toSkip(topath)) {
			System.out.println("skipping " + topath);
			return;
		}

		if (obj.isArray()) {
			System.out.println("isArray! " + obj);
			int i = 1;
			for (JsonNode jsonNode : obj) {
				addJson(jsonNode, topath + "[" + i + "]", context);
			}
		} else if (obj.isObject()) {

			// setting all key-values from json to eObject
			Iterator<Entry<String, JsonNode>> elementIterator = obj.fields();
			while (elementIterator.hasNext()) {
				Entry<String, JsonNode> ele = elementIterator.next();
				addJson(ele.getValue(), topath + "/" + ele.getKey(), context);
			}
		} else {
			addValue(obj.textValue(), topath, context);
		}

	}

	private void addValue(String obj, String topath, JXPathContext context) throws SpagoBIException {
		System.out.println("ADD > " + obj + " > " + topath);
		if (topath.endsWith("]")) {
			int i = topath.lastIndexOf("[");
			int j = topath.lastIndexOf("]");
			Pointer toColl = context.getPointer(topath.substring(0, i));
			if (toColl.getNode() instanceof List) {
				if (!((List) toColl.getNode()).contains(obj)) {
					((List) toColl.getNode()).add(obj);
				}
				// ((List) toColl.getNode()).add(Integer.parseInt(topath.substring(i + 1, j)) - 1, obj);
			}
		} else {
			replace(obj, topath, context);
		}
	}

	private Field getField(Class clazz, String fieldName) {
		Field f = null;
		if (clazz != null) {
			try {
				f = clazz.getDeclaredField(fieldName);
			} catch (NoSuchFieldException | SecurityException e) {
			}
			if (f == null) {
				return getField(clazz.getSuperclass(), fieldName);
			}
		}
		return f;
	}

	private String cleanPath(String path) {
		// path = path.replaceAll("^/physicalModel/", "/physicalModels/0/tables/").replaceAll("^/businessModel/", "/businessModels/0/businessTables/");
		path = "/businessModels" + path;
		Pattern p = Pattern.compile("(/)(\\d)");
		Matcher m = p.matcher(path);
		// StringBuffer s = new StringBuffer("/businessTables");
		StringBuffer s = new StringBuffer();
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

	private Model createEmptyModel(JSONObject json) throws JSONException {
		Assert.assertTrue(json.has("datasourceId"), "datasourceId is mandatory");
		Assert.assertTrue(json.has("physicalModels"), "physicalModels is mandatory");
		Assert.assertTrue(json.has("businessModels"), "businessModels is mandatory");

		Integer dsId = json.getInt("datasourceId");
		String modelName = json.optString("modelName", DEFAULT_MODEL_NAME);
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

	public static void main2(String[] args) {
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
