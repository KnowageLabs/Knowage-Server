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
package it.eng.spagobi.engines.qbe.api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import javax.naming.NamingException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.eng.qbe.datasource.transaction.ITransaction;
import it.eng.qbe.query.ISelectField;
import it.eng.qbe.query.SimpleSelectField;
import it.eng.qbe.statement.IStatement;
import it.eng.qbe.statement.QbeDatasetFactory;
import it.eng.spago.error.EMFInternalError;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.engines.qbe.QbeEngineConfig;
import it.eng.spagobi.engines.qbe.QbeEngineInstance;
import it.eng.spagobi.engines.qbe.exporter.QbeCSVExporter;
import it.eng.spagobi.engines.qbe.exporter.QbeXLSXExporter;
import it.eng.spagobi.engines.qbe.query.Field;
import it.eng.spagobi.engines.qbe.query.SQLFieldsReader;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.JDBCDataSet;
import it.eng.spagobi.tools.dataset.common.behaviour.UserProfileUtils;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineException;

/**
 * REST resource which replaces the old it.eng.spagobi.engines.qbe.services.core.ExportResultAction.
 */
@Path("/1.0/export")
public class ExportResource extends AbstractQbeEngineResource {

	private static final Logger LOGGER = LogManager.getLogger(ExportResource.class);

	@GET
	@Path("/registry/csv")
	public Response registryAsCsv() {
		Response ret = null;
		ITransaction transaction = null;
		IStatement statement = null;
		String jpaQuery = null;
		String sqlQuery = null;
		Connection connection = null;
		List<Field> extractedFields = null;
		QbeEngineInstance engineInstance = getEngineInstance();

		if (Objects.isNull(engineInstance)) {
			LOGGER.warn("No engine instance: this is a statefull service. You must need to start QBE or Registry");
			ret = Response.serverError().build();
		} else {

			try {
				String id = engineInstance.getId();
				transaction = (engineInstance.getDataSource()).getTransaction();
				transaction.open();

				statement = engineInstance.getDataSource().createStatement(engineInstance.getActiveQuery());
				statement.setParameters(getEnv());

				jpaQuery = getJpaQueryStr(statement);
				sqlQuery = getSqlQuery(statement);
				connection = createConnection(engineInstance);
				extractFields(connection, jpaQuery, sqlQuery, extractedFields);
				File tempFile = exportToTempFileAsCsv(connection, sqlQuery);

				InputStream retVal = Files.newInputStream(tempFile.toPath(), StandardOpenOption.DELETE_ON_CLOSE);

			// @formatter:off
			ret = Response.ok(retVal)
					.header("Content-Length", tempFile.length())
					.header("Content-Disposition", String.format("attachment; filename=\"%s.%s\"", id, "csv"))
					.build();
			// @formatter:on
			} catch (ClassNotFoundException | NamingException | SQLException | SpagoBIEngineException | IOException e) {
				LOGGER.warn("Error during export", e);
				ret = Response.serverError().build();
			} finally {
				if (Objects.nonNull(transaction)) {
					transaction.close();
				}
			}
		}

		return ret;
	}

	@GET
	@Path("/registry/spreadsheet")
	public Response registryAsSpreadsheet() {
		Response ret = null;
		ITransaction transaction = null;
		IStatement statement = null;
		String jpaQuery = null;
		String sqlQuery = null;
		Connection connection = null;
		List<Field> extractedFields = null;
		QbeEngineInstance engineInstance = getEngineInstance();

		if (Objects.isNull(engineInstance)) {
			LOGGER.warn("No engine instance: this is a statefull service. You must need to start QBE or Registry");
			ret = Response.serverError().build();
		} else {

			try {
				String id = engineInstance.getId();
				transaction = (engineInstance.getDataSource()).getTransaction();
				transaction.open();

				statement = engineInstance.getDataSource().createStatement(engineInstance.getActiveQuery());
				statement.setParameters(getEnv());

				jpaQuery = getJpaQueryStr(statement);
				sqlQuery = getSqlQuery(statement);
				connection = createConnection(engineInstance);
				extractFields(connection, jpaQuery, sqlQuery, extractedFields);
				File tempFile = exportToTempFileAsXlsx(connection, sqlQuery, statement);

				InputStream retVal = Files.newInputStream(tempFile.toPath(), StandardOpenOption.DELETE_ON_CLOSE);

			// @formatter:off
			ret = Response.ok(retVal)
					.header("Content-Length", tempFile.length())
					.header("Content-Disposition", String.format("attachment; filename=\"%s.%s\"", id, "xslx"))
					.build();
			// @formatter:on
			} catch (ClassNotFoundException | NamingException | SQLException | SpagoBIEngineException | IOException
					| EMFInternalError e) {
				LOGGER.warn("Error during export", e);
				ret = Response.serverError().build();
			} finally {
				if (Objects.nonNull(transaction)) {
					transaction.close();
				}
			}
		}

		return ret;
	}

	private File exportToTempFileAsCsv(Connection connection, String sqlQuery) throws IOException {
		File csvFile = createTempFile();
		QbeCSVExporter exporter = new QbeCSVExporter();

		exporter.export(csvFile, connection, sqlQuery);
		return csvFile;
	}

	private File exportToTempFileAsXlsx(Connection connection, String sqlQuery, IStatement statement)
			throws IOException, EMFInternalError {
		File csvFile = createTempFile();

		IDataStore dataStore = getDataStore(statement, sqlQuery);
		Locale locale = (Locale) getEngineInstance().getEnv().get(EngineConstants.ENV_LOCALE);

		QbeXLSXExporter exporter = new QbeXLSXExporter(dataStore, locale);

		exporter.export();
		return csvFile;
	}

	private File createTempFile() throws IOException {
		return File.createTempFile("knowage-registry-export", RandomStringUtils.randomAscii(10));
	}

	private void extractFields(Connection connection, String jpaQuery, String sqlQuery, List<Field> extractedFields)
			throws SpagoBIEngineException {
		QbeEngineInstance engineInstance = getEngineInstance();
		SQLFieldsReader fieldsReader;
		LOGGER.debug("Exctracting fields ...");
		fieldsReader = new SQLFieldsReader(sqlQuery, connection);

		try {
			extractedFields = fieldsReader.readFields();
		} catch (Exception e) {
			LOGGER.debug("Impossible to extract fields from query");
			throw new SpagoBIEngineException("Impossible to extract fields from query: " + jpaQuery, e);
		}
		LOGGER.debug("Fields extracted succesfully");

		Assert.assertTrue(
				engineInstance.getActiveQuery().getSimpleSelectFields(true).size() + engineInstance.getActiveQuery()
						.getInLineCalculatedSelectFields(true).size() == extractedFields.size(),
				"The number of fields extracted from query resultset cannot be different from the number of fields specified into the query select clause");

		decorateExtractedFields(extractedFields);
	}

	private Connection createConnection(QbeEngineInstance engineInstance)
			throws NamingException, SQLException, ClassNotFoundException {
		Connection connection;
		IDataSource dataSource = (IDataSource) engineInstance.getDataSource().getConfiguration()
				.loadDataSourceProperties().get("datasource");
		connection = dataSource.getConnection();
		return connection;
	}

	private String getSqlQuery(IStatement statement) {
		String sqlQuery;
		sqlQuery = statement.getSqlQueryString();
		Assert.assertNotNull(sqlQuery, "The SQL query is needed while exporting results.");
		LOGGER.debug("Executable SQL query: [{}]", sqlQuery);
		return sqlQuery;
	}

	private String getJpaQueryStr(IStatement statement) {
		String jpaQueryStr;
		jpaQueryStr = statement.getQueryString();
		LOGGER.debug("Executable HQL/JPQL query: [{}]", jpaQueryStr);
		return jpaQueryStr;
	}

	private IDataStore getDataStore(IStatement statement, String sqlQuery) throws EMFInternalError {
		IDataStore dataStore = null;

		boolean isFormEngineInstance = getEngineInstance().getTemplate().getProperty("formJSONTemplate") != null;
		if (!isFormEngineInstance) {
			// case of standard QBE

			IDataSet dataSet = null;

			Integer limit = 0;
			Integer start = 0;
			Integer maxSize = QbeEngineConfig.getInstance().getResultLimit();
			boolean isMaxResultsLimitBlocking = QbeEngineConfig.getInstance().isMaxResultLimitBlocking();
			dataSet = QbeDatasetFactory.createDataSet(statement);
			dataSet.setAbortOnOverflow(isMaxResultsLimitBlocking);

			Map userAttributes = new HashMap();
			UserProfile profile = (UserProfile) this.getEnv().get(EngineConstants.ENV_USER_PROFILE);
			Iterator it = profile.getUserAttributeNames().iterator();
			while (it.hasNext()) {
				String attributeName = (String) it.next();
				Object attributeValue = profile.getUserAttribute(attributeName);
				userAttributes.put(attributeName, attributeValue);
			}
			dataSet.addBinding("attributes", userAttributes);
			dataSet.addBinding("parameters", this.getEnv());
			LOGGER.debug("Executing query ...");
			dataSet.loadData(start, limit, (maxSize == null ? -1 : maxSize.intValue()));

			dataStore = dataSet.getDataStore();

		} else {
			// case of FormEngine

			JDBCDataSet dataset = new JDBCDataSet();
			IDataSource datasource = (IDataSource) this.getEnv().get(EngineConstants.ENV_DATASOURCE);
			dataset.setDataSource(datasource);
			dataset.setUserProfileAttributes(UserProfileUtils
					.getProfileAttributes((UserProfile) this.getEnv().get(EngineConstants.ENV_USER_PROFILE)));
			dataset.setQuery(sqlQuery);
			LOGGER.debug("Executing query ...");
			dataset.loadData();
			dataStore = dataset.getDataStore();
		}

		return dataStore;
	}

	private void decorateExtractedFields(List<Field> extractedFields) {
		List<ISelectField> selectedFields = getEngineInstance().getActiveQuery().getSelectFields(true);
		Iterator<ISelectField> selectedFieldsIterator = selectedFields.iterator();
		Iterator<Field> extractedFieldsIterator = extractedFields.iterator();
		while (extractedFieldsIterator.hasNext()) {
			Field exctractedField = extractedFieldsIterator.next();
			ISelectField selectedField = selectedFieldsIterator.next();
			exctractedField.setAlias(selectedField.getAlias());
			exctractedField.setVisible(selectedField.isVisible());
			if (selectedField.isSimpleField())
				exctractedField.setPattern(((SimpleSelectField) selectedField).getPattern());
		}
	}

}
