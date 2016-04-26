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
package it.eng.spagobi.api.v2;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.HibernateSessionManager;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.meta.model.Model;
import it.eng.spagobi.meta.model.ModelProperty;
import it.eng.spagobi.meta.model.business.BusinessColumn;
import it.eng.spagobi.meta.model.business.BusinessModel;
import it.eng.spagobi.meta.model.business.BusinessTable;
import it.eng.spagobi.meta.model.business.SimpleBusinessColumn;
import it.eng.spagobi.meta.model.physical.PhysicalColumn;
import it.eng.spagobi.meta.model.physical.PhysicalModel;
import it.eng.spagobi.meta.model.physical.PhysicalTable;
import it.eng.spagobi.metadata.cwm.CWMImplType;
import it.eng.spagobi.metadata.cwm.CWMMapperFactory;
import it.eng.spagobi.metadata.cwm.ICWM;
import it.eng.spagobi.metadata.cwm.ICWMMapper;
import it.eng.spagobi.metadata.dao.ImportMetadata;
import it.eng.spagobi.metadata.etl.ETLMetadata;
import it.eng.spagobi.metadata.etl.ETLParser;
import it.eng.spagobi.metadata.metadata.SbiMetaBc;
import it.eng.spagobi.metadata.metadata.SbiMetaBcAttribute;
import it.eng.spagobi.metadata.metadata.SbiMetaSource;
import it.eng.spagobi.metadata.metadata.SbiMetaTable;
import it.eng.spagobi.metadata.metadata.SbiMetaTableColumn;
import it.eng.spagobi.metamodel.MetaModelLoader;
import it.eng.spagobi.services.common.EnginConf;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.tools.catalogue.bo.Content;
import it.eng.spagobi.tools.catalogue.bo.MetaModel;
import it.eng.spagobi.tools.catalogue.dao.IMetaModelsDAO;
import it.eng.spagobi.tools.catalogue.metadata.SbiMetaModel;
import it.eng.spagobi.tools.datasource.metadata.SbiDataSource;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
@Path("/2.0/metadata")
@ManageAuthorization
public class MetadataResource extends AbstractSpagoBIResource {
	static private Logger logger = Logger.getLogger(MetadataResource.class);

	public static final String TENANT_FILTER_NAME = "tenantFilter";
	public static final String CONNECTION_URL = "connection.url";
	public static final String CONNECTION_DATABASENAME = "connection.databasename";
	public static final String BUSINESS_COLUMN_TYPE = "structural.columntype";

	public static final String DATABASE_SOURCE_TYPE = "database";

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String testService() {
		return "{\"result\":\"ok\"}";
	}

	/**
	 * POST: Extract and insert new business model metadata information with specified id
	 **/
	@POST
	@Path("/{bmId}/bmExtract")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response extractBusinessModelMetadataInformation(@PathParam("bmId") int businessModelId) {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			// 1 - Retrieve Metamodel file from datamart.jar
			IMetaModelsDAO businessModelsDAO = DAOFactory.getMetaModelsDAO();
			Content modelContent = businessModelsDAO.loadActiveMetaModelContentById(businessModelId);
			byte[] metamodelTemplateBytes = getModelFileFromJar(modelContent);
			if (metamodelTemplateBytes == null) {
				return Response.serverError().build();
			}

			// 2 - Read the metamodel and convert to object
			ByteArrayInputStream bis = new ByteArrayInputStream(metamodelTemplateBytes);
			Model model = MetaModelLoader.load(bis);

			// 3 - Scan the metamodel
			// Extract information from Physical Model
			PhysicalModel physicalModel = model.getPhysicalModels().get(0);
			String physicalModelCatalog = physicalModel.getCatalog();
			String physicalModelSchema = physicalModel.getSchema();
			String url = getProperty(physicalModel, CONNECTION_URL);
			String databaseName = getProperty(physicalModel, CONNECTION_DATABASENAME);
			String sourceType = DATABASE_SOURCE_TYPE;

			// Objects Definition
			SbiMetaSource smSource = new SbiMetaSource();
			SbiMetaTable smTable = new SbiMetaTable();
			SbiMetaTableColumn smTableColumn = new SbiMetaTableColumn();
			SbiMetaBcAttribute smAttribute = new SbiMetaBcAttribute();
			SbiMetaBc smBC = new SbiMetaBc();
			List<SbiMetaBc> bcList = new ArrayList<SbiMetaBc>();

			// SBI_META_SOURCE
			smSource.setSourceSchema(physicalModelSchema);
			smSource.setSourceCatalogue(physicalModelCatalog);
			smSource.setUrl(url);
			smSource.setType(sourceType);
			smSource.setName(databaseName);

			List<PhysicalTable> physicalTables = physicalModel.getTables();
			// HashMap<String, SbiMetaTable> newTables = new HashMap<String, SbiMetaTable>();
			HashMap<String, SbiMetaTableColumn> newTableColumns = new HashMap<String, SbiMetaTableColumn>();
			Set<SbiMetaTable> sourceTableSet = new HashSet<SbiMetaTable>();

			for (PhysicalTable physicalTable : physicalTables) {
				// 4 - Insert the columns into the db, Insert the Physical Table to the db
				// For the table get Name and Source Name
				// SBI_META_TABLE
				Set<SbiMetaTableColumn> tableColumnSet = new HashSet<SbiMetaTableColumn>();
				smTable = new SbiMetaTable();
				String physicalTableName = physicalTable.getName();
				smTable.setSbiMetaSource(smSource);
				smTable.setName(physicalTableName);

				// For the columns get Name and Type
				List<PhysicalColumn> physicalColumns = physicalTable.getColumns();
				for (PhysicalColumn physicalColumn : physicalColumns) {
					// SBI_META_TABLE_COLUMN
					smTableColumn = new SbiMetaTableColumn();
					String physicalColumnName = physicalColumn.getName();
					String physicalColumnTypeName = physicalColumn.getTypeName();
					smTableColumn.setName(physicalColumnName);
					smTableColumn.setType(physicalColumnTypeName);
					smTableColumn.setSbiMetaTable(smTable);
					newTableColumns.put(physicalColumnName, smTableColumn);
					tableColumnSet.add(smTableColumn);
				}
				smTable.setSbiMetaTableColumns(tableColumnSet);
				sourceTableSet.add(smTable);
				// newTables.put(smTable.getName(), smTable);
			}
			smSource.setSbiMetaTables(sourceTableSet);

			// Extract information from Business Model
			BusinessModel businessModel = model.getBusinessModels().get(0);
			String BusinessModelName = businessModel.getName(); // used to retrieve model in SBI_META_MODELS

			SbiMetaModel smBM = getSbiMetaModel(BusinessModelName);

			List<BusinessTable> businessTables = businessModel.getBusinessTables();

			for (BusinessTable businessTable : businessTables) {
				// TODO: 5 - Insert the business columns into the db, Insert the Business Table to the db
				// For the Business Table get Name, Model and Physical Table
				// SBI_META_BC
				smBC = new SbiMetaBc();
				Set<SbiMetaBcAttribute> bcAttributeSet = new HashSet<SbiMetaBcAttribute>();

				String businessTableName = businessTable.getName();
				String businessTablePhysicalTable = businessTable.getPhysicalTable().getName();

				// For the business columns get Name and Type (attribute/measure)
				List<BusinessColumn> businessColumns = businessTable.getColumns();

				for (BusinessColumn businessColumn : businessColumns) {
					// SBI_META_BC_ATTRIBUTE
					smAttribute = new SbiMetaBcAttribute();
					String businessColumnName = businessColumn.getName();
					String businessColumnType = getProperty(businessColumn, BUSINESS_COLUMN_TYPE);
					smAttribute.setName(businessColumnName);
					smAttribute.setType(businessColumnType);
					if (businessColumn instanceof SimpleBusinessColumn) {
						SimpleBusinessColumn simpleBusinessColumn = ((SimpleBusinessColumn) businessColumn);
						String businessColumnPhysicalColumn = simpleBusinessColumn.getPhysicalColumn().getName();
						SbiMetaTableColumn smTcBc = new SbiMetaTableColumn();
						smAttribute.setSbiMetaTableColumn(newTableColumns.get(businessColumnPhysicalColumn));
						bcAttributeSet.add(smAttribute);

					} else {
						// TODO: what to do if isn't a Simple Business Column ???
					}
				}
				smBC.setName(businessTablePhysicalTable + "|" + businessTableName); // link the logical table name with the phisical table name
				smBC.setSbiMetaBcAttributes(bcAttributeSet);
				smBC.setSbiMetaModel(smBM);

				bcList.add(smBC);
			}
			// call the import data method
			ImportMetadata im = new ImportMetadata();
			im.importBusinessModel(businessModelId, smSource, bcList);

			return Response.ok().build();

		} catch (Exception e) {
			logger.error("An error occurred while trying to extract metadata information from model with id:" + businessModelId, e);
			throw new SpagoBIRestServiceException("An error occurred while trying to extract metadata information from model with id:" + businessModelId,
					buildLocaleFromSession(), e);

		} finally {
			logger.debug("OUT");
		}

	}

	/**
	 * POST: get ETL Contexts from the uploaded file
	 **/
	@POST
	@Path("/getETLContexts")
	@Consumes({ MediaType.MULTIPART_FORM_DATA, MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response getETLContexts(@MultipartForm MultipartFormDataInput input) {
		logger.debug("IN");

		try {

			// 1- Retrieve uploaded file data
			Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
			InputStream inputStream = null;
			String fileName = null;
			for (String key : uploadForm.keySet()) {
				List<InputPart> inputParts = uploadForm.get(key);
				for (InputPart inputPart : inputParts) {
					MultivaluedMap<String, String> header = inputPart.getHeaders();
					if (getFileName(header) != null) {
						fileName = getFileName(header);
						inputStream = inputPart.getBody(InputStream.class, null);
					}
				}
			}

			// save to temporary folder
			File tmpFile = new File(System.getProperty("java.io.tmpdir"), fileName + ".tmp");
			OutputStream outStream = new FileOutputStream(tmpFile);

			byte[] buffer = new byte[8 * 1024];
			int bytesRead;
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outStream.write(buffer, 0, bytesRead);
			}

			// 2 - Parse xml file
			// Document xmlDocument = inputStreamToDocument(inputStream);
			Document xmlDocument = xmlToDocument(tmpFile);
			ETLParser etlParser = new ETLParser(xmlDocument);
			Set<String> contexts = etlParser.getContextNames();

			IOUtils.closeQuietly(inputStream);
			IOUtils.closeQuietly(outStream);
			return Response.ok(contexts).build();

		} catch (Exception e) {
			logger.error("An error occurred while trying to extract metadata information from file:", e);
			throw new SpagoBIRestServiceException("An error occurred while trying to extract metadata information from file:", buildLocaleFromSession(), e);

		} finally {

			logger.debug("OUT");
		}
	}

	/**
	 * GET: Extract and insert new ETL metadata informations from uploaded file
	 **/
	@GET
	@Path("/{fileName}/{contextName}/ETLExtract")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response extractETLMetadataInformation(@PathParam("fileName") String fileName, @PathParam("contextName") String contextName) {
		logger.debug("IN");

		try {

			// 1- Retrieve uploaded file data
			// Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
			// InputStream inputStream = null;
			//
			// for (String key : uploadForm.keySet()) {
			// List<InputPart> inputParts = uploadForm.get(key);
			// for (InputPart inputPart : inputParts) {
			// MultivaluedMap<String, String> header = inputPart.getHeaders();
			// if (getFileName(header) != null) {
			// inputStream = inputPart.getBody(InputStream.class, null);
			// }
			// }
			// }
			File tmpFile = new File(System.getProperty("java.io.tmpdir"), fileName + ".tmp");

			// TODO: 2 - Parse xml inputStream
			Document xmlDocument = xmlToDocument(tmpFile);
			ETLParser etlParser = new ETLParser(xmlDocument);
			ETLMetadata etlMetadata = etlParser.getETLMetadata(contextName);
			// etlParser.extractAll();

			// TODO: 3 - Write informations on db

			// erase temp file
			tmpFile.delete();

			return Response.ok(etlMetadata).build();

		} catch (Exception e) {
			logger.error("An error occurred while trying to extract metadata information from file:", e);
			throw new SpagoBIRestServiceException("An error occurred while trying to extract metadata information from file:", buildLocaleFromSession(), e);

		} finally {
			logger.debug("OUT");
		}
	}

	/**
	 * Update existing business model metadata information with specified id PUT
	 **/
	@PUT
	@Path("/{bmId}/bmExtract")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response updateBusinessModelMetadataInformation(@PathParam("bmId") int businessModelId) {
		// TODO:
		return null;
	}

	/**
	 * Update existing ETL metadata information with specified id PUT
	 **/
	@POST
	@Path("/{etlId}/ETLExtract")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response updateETLMetadataInformation(@PathParam("etlId") int etlId) {
		return null;
	}

	/**
	 * Export Knowage Metamodel with the specified id in CWM Format
	 **/
	@GET
	@Path("/{bmId}/exportCWM")
	public Response exportMetamodelToCWM(@PathParam("bmId") int businessModelId) {
		logger.debug("IN");

		try {
			// 1 - Retrieve Metamodel file from datamart.jar
			IMetaModelsDAO businessModelsDAO = DAOFactory.getMetaModelsDAO();
			Content modelContent = businessModelsDAO.loadActiveMetaModelContentById(businessModelId);
			byte[] metamodelTemplateBytes = getModelFileFromJar(modelContent);
			if (metamodelTemplateBytes == null) {
				return Response.serverError().build();
			}

			// 2 - Read the metamodel and convert to object
			ByteArrayInputStream bis = new ByteArrayInputStream(metamodelTemplateBytes);
			Model model = MetaModelLoader.load(bis);

			// 3- Convert from Knowage Metamodel to CWM Metamodel
			ICWMMapper modelMapper;
			ICWM cwm;
			PhysicalModel physicalModel = model.getPhysicalModels().get(0);
			modelMapper = CWMMapperFactory.getMapper(CWMImplType.JMI);
			cwm = modelMapper.encodeICWM(physicalModel);

			// 4- Return CWM Metamodel as a downloadable file
			ByteArrayOutputStream byteOutputStream = cwm.exportStreamToXMI();
			byte[] b = byteOutputStream.toByteArray();

			ResponseBuilder response = Response.ok(b);
			response.header("Content-Disposition", "attachment; filename=exportCWM.xmi");
			return response.build();

		} catch (Exception e) {
			logger.error("An error occurred while trying to export metamodel with id " + businessModelId + "to CWM", e);
			throw new SpagoBIRestServiceException("An error occurred while trying to export metamodel with id " + businessModelId + "to CWM",
					buildLocaleFromSession(), e);

		} finally {
			logger.debug("OUT");
		}
	}

	/**
	 * -------------------------------------------------------------------------------------
	 *
	 * Utility methods
	 *
	 * -------------------------------------------------------------------------------------
	 */

	private byte[] getModelFileFromJar(Content content) {
		logger.debug("IN");

		// read jar
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
		logger.debug("the model file could not be taken by datamart.jar");
		return null;
	}

	public String getResourcePath() {
		String resPath;
		try {
			String jndiName = SingletonConfig.getInstance().getConfigValue("SPAGOBI.RESOURCE_PATH_JNDI_NAME");
			resPath = SpagoBIUtilities.readJndiResource(jndiName);
		} catch (Throwable t) {
			logger.debug(t);
			resPath = EnginConf.getInstance().getResourcePath();
		}

		if (resPath == null) {
			throw new SpagoBIRuntimeException("Resource path not found!!!");
		}
		return resPath;
	}

	private String getFileName(MultivaluedMap<String, String> header) {
		String[] contentDisposition = header.getFirst("Content-Disposition").split(";");

		for (String filename : contentDisposition) {
			if ((filename.trim().startsWith("filename"))) {

				String[] name = filename.split("=");

				String finalFileName = name[1].trim().replaceAll("\"", "");
				return finalFileName;
			}
		}
		return null;
	}

	private String getProperty(PhysicalModel physicalModel, String propertyName) {
		ModelProperty property = physicalModel.getProperties().get(propertyName);
		return property != null ? property.getValue() : null;
	}

	private String getProperty(BusinessColumn businessColumn, String propertyName) {
		ModelProperty property = businessColumn.getProperties().get(propertyName);
		return property != null ? property.getValue() : null;
	}

	public static Document xmlToDocument(File xmlfile) {
		Document doc = null;
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder;
			dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(xmlfile);
			// optional, but recommended
			doc.getDocumentElement().normalize();

		} catch (ParserConfigurationException e) {
			logger.error("ParserConfigurationException for " + xmlfile.getName());
		} catch (SAXException e) {
			logger.error("SAXException for " + xmlfile.getName());
		} catch (IOException e) {
			logger.error("IOException for " + xmlfile.getName());
		}
		return doc;

	}

	public static Document inputStreamToDocument(InputStream inputStream) {
		Document doc = null;
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder;
			dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(inputStream);
			// optional, but recommended
			doc.getDocumentElement().normalize();

		} catch (ParserConfigurationException e) {
			logger.error("ParserConfigurationException for " + inputStream);
		} catch (SAXException e) {
			logger.error("SAXException for " + inputStream);
		} catch (IOException e) {
			logger.error("IOException for " + inputStream);
		}
		return doc;

	}

	/**
	 * Gets tre current session.
	 *
	 * @return The current session object.
	 */
	private Session getSession() {
		Session session = HibernateSessionManager.getCurrentSession();
		String tenantId = null;
		IEngUserProfile profile = this.getUserProfile();

		if (profile != null) {
			UserProfile userProfile = (UserProfile) profile;
			tenantId = userProfile.getOrganization();
			logger.debug("User profile tenant = [{0}]" + tenantId);
		}

		if (tenantId != null) {
			// if tenant is set, enable tenant filter and put filter's value
			this.enableTenantFilter(session, tenantId);
		}
		return session;
	}

	protected void enableTenantFilter(Session session, String tenantId) {
		Filter filter = session.enableFilter(TENANT_FILTER_NAME);
		filter.setParameter("tenant", tenantId);
	}

	private SbiMetaModel getSbiMetaModel(String BusinessModelName) {
		SbiMetaModel toReturn = new SbiMetaModel();
		SbiDataSource smDS = new SbiDataSource();

		MetaModel boBM = DAOFactory.getMetaModelsDAO().loadMetaModelByName(BusinessModelName);
		// Convert into the sbi obj
		toReturn.setId(boBM.getId());
		toReturn.setName(boBM.getName());
		// TODO check if its necessary to set all infos
		// toReturn.setCategory(boBM.getCategory());
		// toReturn.setDescription(boBM.getDescription());
		// toReturn.setModelLocked(boBM.getModelLocked());
		// toReturn.setModelLocker(boBM.getModelLocker());
		//
		// IDataSource boDS = DAOFactory.getDataSourceDAO().loadDataSourceByLabel(boBM.getDataSourceLabel());
		// smDS.setDsId(boDS.getDsId());
		// smDS.setLabel(boDS.getLabel());

		// toReturn.setDataSource(smDS);

		return toReturn;
	}

}
