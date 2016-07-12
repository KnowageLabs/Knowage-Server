package it.eng.knowage.meta.service;

import it.eng.knowage.meta.generator.jpamapping.JpaMappingJarGenerator;
import it.eng.knowage.meta.initializer.BusinessModelInitializer;
import it.eng.knowage.meta.initializer.PhysicalModelInitializer;
import it.eng.knowage.meta.initializer.descriptor.BusinessRelationshipDescriptor;
import it.eng.knowage.meta.initializer.descriptor.BusinessViewInnerJoinRelationshipDescriptor;
import it.eng.knowage.meta.initializer.descriptor.CalculatedFieldDescriptor;
import it.eng.knowage.meta.initializer.properties.BusinessModelPropertiesFromFileInitializer;
import it.eng.knowage.meta.initializer.properties.PhysicalModelPropertiesFromFileInitializer;
import it.eng.knowage.meta.model.Model;
import it.eng.knowage.meta.model.ModelFactory;
import it.eng.knowage.meta.model.ModelPropertyType;
import it.eng.knowage.meta.model.business.BusinessColumn;
import it.eng.knowage.meta.model.business.BusinessColumnSet;
import it.eng.knowage.meta.model.business.BusinessIdentifier;
import it.eng.knowage.meta.model.business.BusinessModel;
import it.eng.knowage.meta.model.business.BusinessModelFactory;
import it.eng.knowage.meta.model.business.BusinessRelationship;
import it.eng.knowage.meta.model.business.BusinessTable;
import it.eng.knowage.meta.model.business.BusinessView;
import it.eng.knowage.meta.model.business.BusinessViewInnerJoinRelationship;
import it.eng.knowage.meta.model.business.CalculatedBusinessColumn;
import it.eng.knowage.meta.model.business.SimpleBusinessColumn;
import it.eng.knowage.meta.model.filter.PhysicalTableFilter;
import it.eng.knowage.meta.model.physical.PhysicalColumn;
import it.eng.knowage.meta.model.physical.PhysicalModel;
import it.eng.knowage.meta.model.physical.PhysicalTable;
import it.eng.knowage.meta.model.serializer.EmfXmiSerializer;
import it.eng.knowage.meta.model.serializer.ModelPropertyFactory;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.serialization.JsonConverter;
import it.eng.spagobi.tenant.Tenant;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.tools.catalogue.bo.Content;
import it.eng.spagobi.tools.catalogue.dao.IMetaModelsDAO;
import it.eng.spagobi.tools.datasource.bo.DataSource;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.rest.RestUtilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.EList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.flipkart.zjsonpatch.JsonDiff;

@ManageAuthorization
@Path("/1.0/metaWeb")
public class MetaService extends AbstractSpagoBIResource {
	private static Logger logger = Logger.getLogger(MetaService.class);
	private static final String DEFAULT_MODEL_NAME = "modelName";
	public static final String EMF_MODEL = "EMF_MODEL";

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

	@GET
	@Path("/loadSbiModel/{bmId}")
	public Response loadSbiModel(@PathParam("bmId") Integer bmId, @Context HttpServletRequest req) {
		try {
			IMetaModelsDAO businessModelsDAO = DAOFactory.getMetaModelsDAO();
			businessModelsDAO.setUserProfile((IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE));

			EmfXmiSerializer serializer = new EmfXmiSerializer();
			Content lastFileModelContent = businessModelsDAO.lastFileModelMeta(bmId);
			InputStream is = new ByteArrayInputStream(lastFileModelContent.getFileModel());
			Model model = serializer.deserialize(is);
			req.getSession().setAttribute(EMF_MODEL, model);

			JSONObject translatedModel = createJson(model);

			return Response.ok(translatedModel.toString()).build();

		} catch (Throwable t) {
			throw new SpagoBIServiceException(req.getPathInfo(), t);
		}
	}

	@POST
	@Path("/generateModel")
	public Response generateModel(@Context HttpServletRequest req) {
		try {
			EmfXmiSerializer serializer = new EmfXmiSerializer();
			JSONObject jsonRoot = RestUtilities.readBodyAsJSONObject(req);
			Model model = (Model) req.getSession().getAttribute(EMF_MODEL);

			Assert.assertTrue(jsonRoot.has("data"), "data model is mandatory");
			JSONObject jsonData = jsonRoot.getJSONObject("data");
			String modelName = jsonData.getString("name");
			Integer modelId = jsonData.getInt("id");

			applyDiff(jsonRoot, model);

			ByteArrayOutputStream filee = new ByteArrayOutputStream();
			serializer.serialize(model, filee);

			IMetaModelsDAO businessModelsDAO = DAOFactory.getMetaModelsDAO();
			Content content = new Content();
			byte[] bytes = filee.toByteArray();
			content.setFileName(modelName + ".sbimodel");
			content.setFileModel(bytes);
			content.setCreationDate(new Date());
			UserProfile profile = (UserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			TenantManager.setTenant(new Tenant(profile.getOrganization()));
			content.setCreationUser(profile.getUserId().toString());
			Content metaModelContent = businessModelsDAO.loadActiveMetaModelContentByName(modelName);
			if (metaModelContent == null || metaModelContent.getFileModel() == null
					|| (metaModelContent.getFileModel() != null && metaModelContent.getContent() != null)) {
				businessModelsDAO.insertMetaModelContent(modelId, content);
			} else {
				businessModelsDAO.modifyMetaModelContent(modelId, content, metaModelContent.getId());
			}

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
			String jsonString = RestUtilities.readBody(req);
			JSONObject jsonRoot = new JSONObject(jsonString);

			Model model = (Model) req.getSession().getAttribute(EMF_MODEL);
			JSONObject oldJsonModel = createJson(model);

			applyDiff(jsonRoot, model);

			JSONObject json = jsonRoot.getJSONObject("data");
			ObjectMapper mapper = new ObjectMapper();
			JsonNode newBM = mapper.readTree(json.toString());
			applyBusinessModel(newBM, model);

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

	@POST
	@Path("/addBusinessRelation")
	public Response addBusinessRelation(@Context HttpServletRequest req) {
		try {
			JSONObject jsonRoot = RestUtilities.readBodyAsJSONObject(req);

			Model model = (Model) req.getSession().getAttribute(EMF_MODEL);

			JSONObject oldJsonModel = createJson(model);

			applyDiff(jsonRoot, model);

			JSONObject json = jsonRoot.getJSONObject("data");

			// Setting uniqueName equals to name because front-end sends only a "name" field
			String name = json.getString("name");
			json.put("uniqueName", name);

			BusinessModel bm = model.getBusinessModels().get(0);

			ObjectMapper mapper = new ObjectMapper();
			JsonNode rel = mapper.readTree(json.toString());
			applyRelationships(rel, bm);

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

	@POST
	@Path("/addBusinessView")
	public Response addBusinessView(@Context HttpServletRequest req) {
		try {
			JSONObject jsonRoot = RestUtilities.readBodyAsJSONObject(req);
			Model model = (Model) req.getSession().getAttribute(EMF_MODEL);
			JSONObject oldJsonModel = createJson(model);

			applyDiff(jsonRoot, model);

			JSONObject json = jsonRoot.getJSONObject("data");
			String name = json.getString("name");
			String description = json.optString("description");

			JSONObject relationships = json.getJSONObject("relationships");
			JSONArray physicaltables = json.getJSONArray("physicaltable");

			String sourceBusinessClassName = json.getString("sourceBusinessClass");
			BusinessModelInitializer businessModelInitializer = new BusinessModelInitializer();
			BusinessModel bm = model.getBusinessModels().get(0);
			PhysicalModel physicalModel = model.getPhysicalModels().get(0);

			BusinessTable sourceBusinessClass = bm.getBusinessTableByUniqueName(sourceBusinessClassName);
			BusinessView bw = BusinessModelFactory.eINSTANCE.createBusinessView();
			bw.setName(name);
			bm.addBusinessView(bw);
			bw.setDescription(description);

			// Adding source table only if it has not been selected
			boolean addSourceTable = true;
			for (int i = 0; i < physicaltables.length(); i++) {
				String ptName = physicaltables.getString(i);
				if (ptName.equals(sourceBusinessClass.getPhysicalTable().getName())) {
					addSourceTable = false;
				}
				PhysicalTable pt = physicalModel.getTable(ptName);
				bw.getPhysicalTables().add(pt);

				EList<PhysicalColumn> ptcol = pt.getColumns();
				for (int pci = 0; pci < ptcol.size(); pci++) {
					businessModelInitializer.addColumn(ptcol.get(pci), bw);
				}
			}
			if (addSourceTable) {
				physicaltables.put(sourceBusinessClass.getPhysicalTable().getName());
			}

			List<BusinessViewInnerJoinRelationshipDescriptor> innerJoinRelationshipDescriptors = new ArrayList<>();

			Iterator<String> relationshipsIterator = relationships.keys();
			while (relationshipsIterator.hasNext()) {
				String tableName = relationshipsIterator.next();
				PhysicalTable destinationTable = physicalModel.getTable(tableName);

				JSONObject destinationColumns = relationships.getJSONObject(tableName);
				Iterator<String> destinationColumnsIterator = destinationColumns.keys();
				while (destinationColumnsIterator.hasNext()) {
					String destinationColumnName = destinationColumnsIterator.next();
					PhysicalColumn destinationColumn = destinationTable.getColumn(destinationColumnName);

					JSONObject sourceTables = destinationColumns.getJSONObject(destinationColumnName);
					Iterator<String> sourceTablesIterator = sourceTables.keys();
					while (sourceTablesIterator.hasNext()) {
						String sourceTableName = sourceTablesIterator.next();
						PhysicalTable sourceTable = physicalModel.getTable(sourceTableName);

						BusinessViewInnerJoinRelationshipDescriptor innerJoinRelationshipDescriptor = new BusinessViewInnerJoinRelationshipDescriptor(
								sourceTable, destinationTable);
						int index = innerJoinRelationshipDescriptors.indexOf(innerJoinRelationshipDescriptor);
						if (index < 0) {
							innerJoinRelationshipDescriptors.add(innerJoinRelationshipDescriptor);
						} else {
							innerJoinRelationshipDescriptor = innerJoinRelationshipDescriptors.get(index);
						}
						innerJoinRelationshipDescriptor.getDestinationColumns().add(destinationColumn);

						JSONArray sourceColumns = sourceTables.getJSONArray(sourceTableName);
						for (int x = 0; x < sourceColumns.length(); x++) {
							String sourceColName = sourceColumns.getString(x);
							PhysicalColumn sourceCol = sourceTable.getColumn(sourceColName);

							innerJoinRelationshipDescriptor.getSourceColumns().add(sourceCol);
						}
					}
				}
			}

			// Adding relationships to BusinessView
			for (BusinessViewInnerJoinRelationshipDescriptor businessViewInnerJoinRelationshipDescriptor : innerJoinRelationshipDescriptors) {
				BusinessViewInnerJoinRelationship innerJoinRelationship = businessModelInitializer.addBusinessViewInnerJoinRelationship(bm,
						businessViewInnerJoinRelationshipDescriptor);
				bw.getJoinRelationships().add(innerJoinRelationship);
			}

			// Copy relationships
			Iterator<BusinessRelationship> sourceRelIterator = sourceBusinessClass.getRelationships().iterator();
			BusinessModelFactory.eINSTANCE.createBusinessRelationship();
			while (sourceRelIterator.hasNext()) {
				BusinessRelationship sourceRel = sourceRelIterator.next();
				String relationshipName = sourceRel.getName();
				BusinessColumnSet source = null;
				if (sourceRel.getSourceTable().getUniqueName().equals(sourceBusinessClass.getUniqueName())) {
					source = bw;
				} else {
					source = sourceRel.getSourceTable();
				}
				BusinessColumnSet destination = null;
				if (sourceRel.getDestinationTable().getUniqueName().equals(sourceBusinessClass.getUniqueName())) {
					destination = bw;
				} else {
					destination = sourceRel.getDestinationTable();
				}
				ModelPropertyType cardinalityType = sourceRel.getPropertyType(BusinessModelPropertiesFromFileInitializer.RELATIONSHIP_CARDINALITY);
				String cardinality = sourceRel.getProperties().get(cardinalityType.getId()).getValue();
				List<BusinessColumn> destinationCol = sourceRel.getDestinationColumns();
				List<BusinessColumn> sourceCol = sourceRel.getSourceColumns();
				businessModelInitializer.addRelationship(new BusinessRelationshipDescriptor(source, destination, sourceCol, destinationCol, cardinality,
						relationshipName));
			}

			// Add identifiers
			BusinessIdentifier identifier = sourceBusinessClass.getIdentifier();
			businessModelInitializer.addIdentifier(identifier.getName(), bw, identifier.getColumns());

			JSONObject jsonModel = createJson(model);
			ObjectMapper mapper = new ObjectMapper();
			JsonNode patch = JsonDiff.asJson(mapper.readTree(oldJsonModel.toString()), mapper.readTree(jsonModel.toString()));

			return Response.ok(patch.toString()).build();
		} catch (IOException | JSONException e) {
			logger.error(e);
			throw new SpagoBIServiceException(req.getPathInfo(), e);
		} catch (SpagoBIException e) {
			throw new SpagoBIServiceException(req.getPathInfo(), e);
		}
	}

	@GET
	@Path("/buildModel/{name}/{modelid}")
	public Response buildModel(@PathParam("name") String name, @PathParam("modelid") Integer modelid, @Context HttpServletRequest req) { // ,
		Model model = getModelWeb(name);
		// meta model version (content)

		JpaMappingJarGenerator jpaMappingJarGenerator = new JpaMappingJarGenerator();

		logger.debug(req.getServletContext().getRealPath(File.separator));

		String libDir = req.getServletContext().getRealPath(File.separator) + "WEB-INF" + File.separator + "lib" + File.separator;

		// jpaMappingJarGenerator.setLibs(new String[] { "hibernate-3.6.2.jar", "javax.persistence-2.0.1.jar" });
		String filename = name + ".jar";
		jpaMappingJarGenerator.setJarFileName(filename);
		IMetaModelsDAO dao = DAOFactory.getMetaModelsDAO();
		Content content = dao.lastFileModelMeta(modelid);
		try {
			// java.nio.file.Path outFile = Files.createTempFile("model_", "_tmp");
			java.nio.file.Path outDir = Files.createTempDirectory("model_");

			jpaMappingJarGenerator.generate(model.getBusinessModels().get(0), outDir.toString(), false, new File(libDir), content.getFileModel());

			dao.setUserProfile((IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE));

			String tmpDirJarFile = outDir + File.separator + model.getBusinessModels().get(0).getName() + File.separator + "dist";
			InputStream inputStream = new FileInputStream(tmpDirJarFile + File.separator + filename);
			byte[] bytes = IOUtils.toByteArray(inputStream);
			// Content content = new Content();

			content.setCreationDate(new Date());
			content.setCreationUser(getUserProfile().getUserName().toString());
			content.setContent(bytes);
			content.setFileName(filename);
			// dao.insertMetaModelContent(modelid, content);
			dao.modifyMetaModelContent(modelid, content, content.getId());

		} catch (IOException e) {
			e.printStackTrace();
			return Response.serverError().build();
		}
		return Response.ok().build();
	}

	@POST
	@Path("/setCalculatedField")
	public Response setCalculatedBM(@Context HttpServletRequest req) throws IOException, JSONException, SpagoBIException {

		JSONObject jsonRoot = RestUtilities.readBodyAsJSONObject(req);

		Model model = (Model) req.getSession().getAttribute(EMF_MODEL);
		JSONObject oldJsonModel = createJson(model);

		applyDiff(jsonRoot, model);

		JSONObject jsonData = jsonRoot.getJSONObject("data");
		String name = jsonData.getString("name");
		String expression = jsonData.getString("expression");
		String dataType = jsonData.getString("dataType");
		String sourceTableName = jsonData.getString("sourceTableName");

		BusinessModel bm = model.getBusinessModels().get(0);
		BusinessColumnSet sourceBcs = bm.getBusinessTableByUniqueName(sourceTableName);
		CalculatedFieldDescriptor cfd = new CalculatedFieldDescriptor(name, expression, dataType, sourceBcs);
		BusinessModelInitializer businessModelInitializer = new BusinessModelInitializer();
		businessModelInitializer.addCalculatedColumn(cfd);

		JSONObject jsonModel = createJson(model);
		ObjectMapper mapper = new ObjectMapper();
		JsonNode patch = JsonDiff.asJson(mapper.readTree(oldJsonModel.toString()), mapper.readTree(jsonModel.toString()));
		return Response.ok(patch.toString()).build();

	}

	@POST
	@Path("/deleteCalculatedField")
	public Response deleteCalculatedField(@Context HttpServletRequest req) throws IOException, JSONException, SpagoBIException {

		JSONObject jsonRoot = RestUtilities.readBodyAsJSONObject(req);
		Model model = (Model) req.getSession().getAttribute(EMF_MODEL);
		JSONObject oldJsonModel = createJson(model);

		applyDiff(jsonRoot, model);

		JSONObject json = jsonRoot.getJSONObject("data");
		String name = json.getString("name");
		String sourceTableName = json.getString("sourceTableName");

		BusinessModel bm = model.getBusinessModels().get(0);
		BusinessColumnSet sourceBcs = bm.getBusinessTableByUniqueName(sourceTableName);
		List<BusinessColumn> cbcList = sourceBcs.getColumns();
		Iterator<BusinessColumn> i = cbcList.iterator();
		while (i.hasNext()) {
			BusinessColumn column = i.next();
			if (column instanceof CalculatedBusinessColumn) {
				if (column.getName().equals(name)) {
					i.remove();
				}
			}
		}

		JSONObject jsonModel = createJson(model);
		ObjectMapper mapper = new ObjectMapper();
		JsonNode patch = JsonDiff.asJson(mapper.readTree(oldJsonModel.toString()), mapper.readTree(jsonModel.toString()));
		return Response.ok(patch.toString()).build();

	}

	@POST
	@Path("/deleteBusinessClass")
	public Response deleteBusinessClass(@Context HttpServletRequest req) throws IOException, JSONException, SpagoBIException {

		JSONObject jsonRoot = RestUtilities.readBodyAsJSONObject(req);
		Model model = (Model) req.getSession().getAttribute(EMF_MODEL);
		JSONObject oldJsonModel = createJson(model);

		applyDiff(jsonRoot, model);

		JSONObject json = jsonRoot.getJSONObject("data");
		String bmName = json.getString("name");
		model.getBusinessModels().get(0).deleteBusinessTableByUniqueName(bmName);

		JSONObject jsonModel = createJson(model);
		ObjectMapper mapper = new ObjectMapper();
		JsonNode patch = JsonDiff.asJson(mapper.readTree(oldJsonModel.toString()), mapper.readTree(jsonModel.toString()));

		return Response.ok(patch.toString()).build();

	}

	@POST
	@Path("/deleteBusinessView")
	public Response deleteBusinessView(@Context HttpServletRequest req) throws IOException, JSONException, SpagoBIException {
		JSONObject jsonRoot = RestUtilities.readBodyAsJSONObject(req);
		Model model = (Model) req.getSession().getAttribute(EMF_MODEL);
		JSONObject oldJsonModel = createJson(model);

		applyDiff(jsonRoot, model);

		JSONObject json = jsonRoot.getJSONObject("data");
		String bmName = json.getString("name");
		model.getBusinessModels().get(0).deleteBusinessViewByUniqueName(bmName);

		JSONObject jsonModel = createJson(model);
		ObjectMapper mapper = new ObjectMapper();
		JsonNode patch = JsonDiff.asJson(mapper.readTree(oldJsonModel.toString()), mapper.readTree(jsonModel.toString()));

		return Response.ok(patch.toString()).build();
	}

	@GET
	@Path("/updatePhysicalModel")
	public Response updatePhysicalModel(@Context HttpServletRequest req) throws ClassNotFoundException, NamingException, SQLException, JSONException {
		PhysicalModelInitializer physicalModelInitializer = new PhysicalModelInitializer();
		Model model = (Model) req.getSession().getAttribute(EMF_MODEL);

		PhysicalModel phyMod = model.getPhysicalModels().get(0);
		DataSource dataSource = new DataSource();
		dataSource.setLabel(phyMod.getProperties().get(PhysicalModelPropertiesFromFileInitializer.CONNECTION_NAME).getValue());
		dataSource.setUrlConnection(phyMod.getProperties().get(PhysicalModelPropertiesFromFileInitializer.CONNECTION_URL).getValue());
		dataSource.setDriver(phyMod.getProperties().get(PhysicalModelPropertiesFromFileInitializer.CONNECTION_DRIVER).getValue());
		dataSource.setUser(phyMod.getProperties().get(PhysicalModelPropertiesFromFileInitializer.CONNECTION_USERNAME).getValue());
		dataSource.setPwd(phyMod.getProperties().get(PhysicalModelPropertiesFromFileInitializer.CONNECTION_PASSWORD).getValue());
		dataSource.setHibDialectClass("");
		dataSource.setHibDialectName("");
		List<String> missingTables = physicalModelInitializer.getMissingTablesNames(dataSource.getConnection(), model.getPhysicalModels().get(0));
		List<String> missingColumns = physicalModelInitializer.getMissingColumnsNames(dataSource.getConnection(), model.getPhysicalModels().get(0));
		List<String> removingItems = physicalModelInitializer.getRemovedTablesAndColumnsNames(dataSource.getConnection(), model.getPhysicalModels().get(0));
		JSONObject resp = new JSONObject();
		resp.put("missingTables", new JSONArray(JsonConverter.objectToJson(missingTables, missingTables.getClass())));
		resp.put("missingColumns", new JSONArray(JsonConverter.objectToJson(missingColumns, missingColumns.getClass())));
		resp.put("removingItems", new JSONArray(JsonConverter.objectToJson(removingItems, removingItems.getClass())));
		return Response.ok(resp.toString()).build();
	}

	@POST
	@Path("/updatePhysicalModel")
	@SuppressWarnings("unchecked")
	public Response applyUpdatePhysicalModel(@Context HttpServletRequest req) throws ClassNotFoundException, NamingException, SQLException, JSONException,
			IOException {
		Model model = (Model) req.getSession().getAttribute(EMF_MODEL);
		JSONObject oldJsonModel = createJson(model);

		JSONObject json = RestUtilities.readBodyAsJSONObject(req);
		List<String> tables = (List<String>) JsonConverter.jsonToObject(json.getString("tables"), List.class);
		PhysicalModelInitializer physicalModelInitializer = new PhysicalModelInitializer();
		physicalModelInitializer.setRootModel(model);
		PhysicalModel originalPM = model.getPhysicalModels().get(0);
		List<String> currTables = new ArrayList<String>();
		for (PhysicalTable pt : originalPM.getTables()) {
			currTables.add(pt.getName());
		}
		currTables.addAll(tables);

		PhysicalModel phyMod = physicalModelInitializer.initializeLigth(originalPM.getConnection(), currTables);
		physicalModelInitializer.updateModel(originalPM, phyMod, tables);

		JSONObject jsonModel = createJson(model);
		ObjectMapper mapper = new ObjectMapper();
		JsonNode patch = JsonDiff.asJson(mapper.readTree(oldJsonModel.toString()), mapper.readTree(jsonModel.toString()));

		return Response.ok(patch.toString()).build();
	}

	private void applyBusinessModel(JsonNode newBM, Model model) {
		String name = newBM.get("name").textValue();
		String description = newBM.get("description").textValue();
		String table = newBM.get("physicalModel").textValue();
		Iterator<JsonNode> colIterator = newBM.get("selectedColumns").elements();

		BusinessModel bm = model.getBusinessModels().get(0);

		BusinessTable bt = BusinessModelFactory.eINSTANCE.createBusinessTable();
		PhysicalTable pt = model.getPhysicalModels().get(0).getTable(table);
		bt.setModel(bm);
		bm.getBusinessTables().add(bt);
		bt.setName(name);
		bt.setUniqueName(name.toLowerCase().replaceAll(" ", "_"));
		bt.setDescription(description);
		bt.setPhysicalTable(pt);
		bt.setDescription(description);
		new BusinessModelInitializer().getPropertiesInitializer().addProperties(bt);

		while (colIterator.hasNext()) {
			JsonNode col = colIterator.next();
			String colName = col.textValue();
			BusinessColumn bc = BusinessModelFactory.eINSTANCE.createBusinessColumn();
			SimpleBusinessColumn sbc = BusinessModelFactory.eINSTANCE.createSimpleBusinessColumn();
			bt.getSimpleBusinessColumns().add(sbc);
			sbc.setName(colName);
			sbc.setPhysicalColumn(pt.getColumn(colName));
			sbc.setTable(bt);
			new BusinessModelInitializer().getPropertiesInitializer().addProperties(bc);
			new BusinessModelInitializer().getPropertiesInitializer().addProperties(sbc);
		}

	}

	private Model getModelWeb(String modelName) {
		IMetaModelsDAO businessModelsDAO = DAOFactory.getMetaModelsDAO();
		Content metaModelContent = businessModelsDAO.loadActiveMetaModelWebContentByName(modelName);
		ByteArrayInputStream bis = new ByteArrayInputStream(metaModelContent.getFileModel());
		EmfXmiSerializer serializer = new EmfXmiSerializer();
		return serializer.deserialize(bis);
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
		// This field should be null (in relationship) so it is left unset
		// br.setPhysicalForeignKey(null);
	}

	private void applyDiff(JSONObject jsonRoot, Model model) throws SpagoBIException, JsonProcessingException, IOException, JSONException {
		if (jsonRoot.has("diff")) {
			JsonNode patch = new ObjectMapper().readTree(jsonRoot.getString("diff"));
			applyPatch(patch, model);
		}
	}

	private void applyPatch(JsonNode patch, Model model) throws SpagoBIException {
		logger.debug("applyPatch:" + patch != null ? patch.toString() : "null");
		Iterator<JsonNode> elements = patch.elements();
		JXPathContext context = JXPathContext.newContext(model);
		context.setFactory(new ModelPropertyFactory());
		while (elements.hasNext()) {
			JsonNode jsonNode = elements.next();
			String operation = jsonNode.get("op").textValue();
			String path = jsonNode.get("path").textValue();

			if (toSkip(path)) {
				logger.debug("skipping " + path);
				continue;
			}

			path = cleanPath(path);

			switch (operation.toUpperCase()) {
			case "ADD":
				JsonNode node = jsonNode.get("value");
				logger.debug("add " + node.asText());
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
				logger.debug("moving from [" + from + "] to [" + path + "]");
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
			logger.debug("skipping " + path);
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
			logger.debug("REMOVE > " + path + " > " + obj.getNode());
			Pointer coll = context.getPointer(path.substring(0, i));
			((List<?>) coll.getNode()).remove(obj.getNode());
		} else {
			// TODO
			// throw new SpagoBIException("TODO operation \"remove\" not supported for single element");
		}
	}

	private void addJson(JsonNode obj, String topath, JXPathContext context) throws SpagoBIException {
		if (toSkip(topath)) {
			logger.debug("skipping " + topath);
			return;
		}

		if (obj.isArray()) {
			logger.debug("isArray! " + obj);
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

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void addValue(String obj, String topath, JXPathContext context) throws SpagoBIException {
		logger.debug("ADD > " + obj + " > " + topath);
		if (topath.endsWith("]")) {
			int i = topath.lastIndexOf("[");
			Pointer toColl = context.getPointer(topath.substring(0, i));
			if (toColl.getNode() instanceof List) {
				if (!((List) toColl.getNode()).contains(obj)) {
					((List) toColl.getNode()).add(obj);
				}
			}
		} else {
			replace(obj, topath, context);
		}
	}

	/**
	 * Used to convert a path coming from a frontend jsonDiff in a path as expected by jxpath. Frontend model is something like {physicalModels:[...],
	 * businessModels:[...]} and it has to be converted to backend model structure that is something like
	 * {businessModels:[tables:[...]],businessModels:[businessTables:[...]]} Furthermore jsonDiff is zero-based numbering but jxpath is 1-based numbering
	 * Another difference is that jsonDiff's notation used to select a property of a nth element of a collection is "parent/n/property" but jxpath does same
	 * selection in this way "parent[n]/property"
	 * 
	 * @param path
	 * @return path cleaned
	 */
	private String cleanPath(String path) {
		path = path.replaceAll("^/physicalModels", "/businessModels/0/tables").replaceAll("^/businessModels", "/businessModels/0/businessTables");
		Pattern p = Pattern.compile("(/)(\\d)");
		Matcher m = p.matcher(path);
		StringBuffer s = new StringBuffer();
		while (m.find()) {
			m.appendReplacement(s, "[" + String.valueOf(1 + Integer.parseInt(m.group(2))) + "]");
		}
		m.appendTail(s);
		return s.toString();
	}

	public static JSONObject createJson(Model model) throws JSONException {
		JSONObject translatedModel = new JSONObject();
		Map<String, Integer> physicalTableMap = new HashMap<>();

		JSONArray businessModelJson = new JSONArray();
		Iterator<BusinessTable> businessModelsIterator = model.getBusinessModels().get(0).getBusinessTables().iterator();
		while (businessModelsIterator.hasNext()) {
			BusinessTable curr = businessModelsIterator.next();
			String tabelName = curr.getPhysicalTable().getName();
			JSONObject bmJson = new JSONObject(JsonConverter.objectToJson(curr, curr.getClass()));
			bmJson.put("physicalTable", new JSONObject().put("physicalTableIndex", physicalTableMap.get(tabelName)));
			businessModelJson.put(bmJson);
		}

		JSONArray physicalModelJson = new JSONArray();
		EList<PhysicalTable> physicalTables = model.getPhysicalModels().get(0).getTables();
		for (int j = 0; j < physicalTables.size(); j++) {
			PhysicalTable physicalTable = physicalTables.get(j);
			physicalModelJson.put(new JSONObject(JsonConverter.objectToJson(physicalTable, physicalTable.getClass())));
			physicalTableMap.put(physicalTable.getName(), j);
		}

		JSONArray businessViewJson = new JSONArray();
		List<BusinessView> businessViews = model.getBusinessModels().get(0).getBusinessViews();
		for (int j = 0; j < businessViews.size(); j++) {
			BusinessView businessView = businessViews.get(j);
			businessViewJson.put(new JSONObject(JsonConverter.objectToJson(businessView, businessView.getClass())));
		}
		translatedModel.put("physicalModels", physicalModelJson);
		translatedModel.put("businessModels", businessModelJson);
		translatedModel.put("businessViews", businessViewJson);
		return translatedModel;
	}

	@SuppressWarnings("unchecked")
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
		businessModelInitializer.initialize(modelName, physicalTableFilter, physicalModel);

		return model;
	}

	@SuppressWarnings("rawtypes")
	public static byte[] extractSbiModelFromJar(Content content) {
		byte[] contentBytes = content.getContent();

		JarFile jar = null;
		FileOutputStream output = null;
		java.io.InputStream is = null;

		try {
			UUIDGenerator uuidGen = UUIDGenerator.getInstance();
			UUID uuidObj = uuidGen.generateTimeBasedUUID();
			String idCas = uuidObj.toString().replaceAll("-", "");
			logger.debug("create temp file for jar");
			String path = System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + idCas + ".jar";
			logger.debug("temp file for jar " + path);
			File filee = new File(path);
			output = new FileOutputStream(filee);
			IOUtils.write(contentBytes, output);

			jar = new JarFile(filee);
			logger.debug("jar file created ");

			Enumeration enumEntries = jar.entries();
			while (enumEntries.hasMoreElements()) {
				JarEntry fileEntry = (java.util.jar.JarEntry) enumEntries.nextElement();
				logger.debug("jar content " + fileEntry.getName());

				if (fileEntry.getName().endsWith("sbimodel")) {
					logger.debug("found model file " + fileEntry.getName());
					is = jar.getInputStream(fileEntry);
					byte[] byteContent = SpagoBIUtilities.getByteArrayFromInputStream(is);
					return byteContent;

				}

			}
		} catch (IOException e1) {
			logger.error("the model file could not be taken by datamart.jar due to error ", e1);
			return null;
		} finally {
			try {

				if (jar != null)
					jar.close();
				if (output != null)
					output.close();
				if (is != null)
					is.close();
			} catch (IOException e) {
				logger.error("error in closing streams");
			}
			logger.debug("OUT");
		}
		logger.error("the model file could not be taken by datamart.jar");
		return null;

	}

}
