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

import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.dao.DAOFactory;
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
import it.eng.spagobi.metamodel.MetaModelLoader;
import it.eng.spagobi.services.common.EnginConf;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.tools.catalogue.bo.Content;
import it.eng.spagobi.tools.catalogue.dao.IMetaModelsDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
@Path("/2.0/metadata")
@ManageAuthorization
public class MetadataResource extends AbstractSpagoBIResource {
	static private Logger logger = Logger.getLogger(MetadataResource.class);

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

			List<PhysicalTable> physicalTables = physicalModel.getTables();
			for (PhysicalTable physicalTable : physicalTables) {
				// TODO: 4 - Insert the columns into the db, Insert the Physical Table to the db
				// For the table get Name and Source Name
				String physicalTableName = physicalTable.getName();
				// For the columns get Name and Type
				List<PhysicalColumn> physicalColumns = physicalTable.getColumns();
				for (PhysicalColumn physicalColumn : physicalColumns) {
					String physicalColumnName = physicalColumn.getName();
					String physicalColumnTypeName = physicalColumn.getTypeName();
				}

			}

			// Extract information from Business Model
			BusinessModel businessModel = model.getBusinessModels().get(0);
			String BusinessModelName = businessModel.getName(); // used to retrieve model in SBI_META_MODELS
			List<BusinessTable> businessTables = businessModel.getBusinessTables();
			// TODO: to remove
			StringBuilder sb = new StringBuilder();
			for (BusinessTable businessTable : businessTables) {
				// TODO: 5 - Insert the business columns into the db, Insert the Business Table to the db
				// For the Business Table get Name, Model and Physical Table
				String businessTableName = businessTable.getName();
				String businessTablePhysicalTable = businessTable.getPhysicalTable().getName();
				// For the business columns get Name and Type (attribute/measure)
				List<BusinessColumn> businessColumns = businessTable.getColumns();
				for (BusinessColumn businessColumn : businessColumns) {
					String businessColumnName = businessColumn.getName();
					String businessColumnType = getProperty(businessColumn, BUSINESS_COLUMN_TYPE);
					if (businessColumn instanceof SimpleBusinessColumn) {
						SimpleBusinessColumn simpleBusinessColumn = ((SimpleBusinessColumn) businessColumn);
						String businessColumnPhysicalColumn = simpleBusinessColumn.getPhysicalColumn().getName();
					} else {
						// TODO: what to do if isn't a Simple Business Column ???
					}
				}
				// TODO: to remove
				sb.append(businessTable.getName() + "\n");
			}

			return Response.ok(sb).build();

		} catch (Exception e) {
			logger.error("An error occurred while trying to extract metadata information from model with id:" + businessModelId, e);
			throw new SpagoBIRestServiceException("An error occurred while trying to extract metadata information from model with id:" + businessModelId,
					buildLocaleFromSession(), e);

		} finally {
			logger.debug("OUT");
		}

	}

	/**
	 * POST: Extract and insert new ETL metadata informations with specified id
	 **/
	@POST
	@Path("/{etlId}/ETLExtract")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response extractETLMetadataInformation(@PathParam("etlId") int etlId) {
		return null;
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

	@JsonIgnore
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

	private String getProperty(PhysicalModel physicalModel, String propertyName) {
		ModelProperty property = physicalModel.getProperties().get(propertyName);
		return property != null ? property.getValue() : null;
	}

	private String getProperty(BusinessColumn businessColumn, String propertyName) {
		ModelProperty property = businessColumn.getProperties().get(propertyName);
		return property != null ? property.getValue() : null;
	}

}
