/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2021 Engineering Ingegneria Informatica S.p.A.
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
package it.eng.spagobi.engines.qbe.services.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Workbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONObjectDeserializator;

import it.eng.qbe.datasource.transaction.ITransaction;
import it.eng.qbe.model.accessmodality.IModelAccessModality;
import it.eng.qbe.query.ISelectField;
import it.eng.qbe.query.Query;
import it.eng.qbe.query.SimpleSelectField;
import it.eng.qbe.query.serializer.SerializerFactory;
import it.eng.qbe.serializer.SerializationException;
import it.eng.qbe.statement.IStatement;
import it.eng.qbe.statement.QbeDatasetFactory;
import it.eng.spago.base.SourceBean;
import it.eng.spago.error.EMFInternalError;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.engines.qbe.QbeEngineConfig;
import it.eng.spagobi.engines.qbe.exporter.QbeCSVExporter;
import it.eng.spagobi.engines.qbe.exporter.QbeXLSXExporter;
import it.eng.spagobi.engines.qbe.query.Field;
import it.eng.spagobi.engines.qbe.query.ReportRunner;
import it.eng.spagobi.engines.qbe.query.SQLFieldsReader;
import it.eng.spagobi.engines.qbe.query.TemplateBuilder;
import it.eng.spagobi.engines.qbe.services.formviewer.ExecuteDetailQueryAction;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.JDBCDataSet;
import it.eng.spagobi.tools.dataset.common.behaviour.UserProfileUtils;
import it.eng.spagobi.tools.dataset.common.iterator.DataIterator;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceExceptionHandler;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.mime.MimeUtils;

/**
 * The Class ExecuteQueryAction.
 */
public class ExportResultAction extends AbstractQbeEngineAction {

	// INPUT PARAMETERS
	public static final String MIME_TYPE = "MIME_TYPE";
	public static final String QUERY = "query";
	public static final String RESPONSE_TYPE = "RESPONSE_TYPE";
	public static final String PARS = "pars";
	public static final String LIMIT = "limit";

	// misc
	public static final String RESPONSE_TYPE_INLINE = "RESPONSE_TYPE_INLINE";
	public static final String RESPONSE_TYPE_ATTACHMENT = "RESPONSE_TYPE_ATTACHMENT";

	public static final String DRIVERS = "DRIVERS";

	/** Logger component. */
	public static transient Logger logger = Logger.getLogger(ExportResultAction.class);

	@Override
	public void service(SourceBean request, SourceBean response) {

		String responseType = null;
		boolean writeBackResponseInline = false;
		String mimeType = null;
		Integer exportLimit = null;
		JSONObject queryJSON = null;
		String fileExtension = null;
		IStatement statement = null;
		ITransaction transaction = null;
		Connection connection = null;
		// HQL2SQLStatementRewriter queryRewriter = null;
		String jpaQueryStr = null;
		String sqlQuery = null;
		SQLFieldsReader fieldsReader = null;
		List<?> extractedFields = null;
		Map params = null;
		TemplateBuilder templateBuilder = null;
		String templateContent = null;
		File reportFile = null;
		ReportRunner runner = null;
		boolean isFormEngineInstance = false;
		Query query = null;

		logger.debug("IN");

		try {
			super.service(request, response);

			mimeType = getAttributeAsString(MIME_TYPE);
			logger.debug(MIME_TYPE + ": " + mimeType);

			exportLimit = parseExportLimit(getAttributeAsString(LIMIT));
			logger.debug(LIMIT + ": " + exportLimit);

			responseType = getAttributeAsString(RESPONSE_TYPE);
			logger.debug(RESPONSE_TYPE + ": " + responseType);

			queryJSON = getAttributeAsJSONObject(QUERY);
			logger.debug(QUERY + ": " + queryJSON);
			JSONArray parameters = getAttributeAsJSONArray(PARS);
			addParameters(parameters);
			Assert.assertNotNull(getEngineInstance(),
					"It's not possible to execute " + this.getActionName() + " service before having properly created an instance of EngineInstance class");

			transaction = (getEngineInstance().getDataSource()).getTransaction();
			transaction.open();

			fileExtension = MimeUtils.getFileExtension(mimeType);
			writeBackResponseInline = RESPONSE_TYPE_INLINE.equalsIgnoreCase(responseType);

			isFormEngineInstance = getEngineInstance().getTemplate().getProperty("formJSONTemplate") != null;
			if (!isFormEngineInstance) {
				// case of standard QBE
				query = deserializeQuery(queryJSON);
				Assert.assertNotNull(query, "Query object cannot be null in oder to execute " + this.getActionName() + " service");
				Assert.assertTrue(query.isEmpty() == false, "Query object cannot be empty in oder to execute " + this.getActionName() + " service");
				Assert.assertNotNull(mimeType, "Input parameter [" + MIME_TYPE + "] cannot be null in oder to execute " + this.getActionName() + " service");
				Assert.assertTrue(MimeUtils.isValidMimeType(mimeType) == true, "[" + mimeType + "] is not a valid value for " + MIME_TYPE + " parameter");

				// Assert.assertNotNull(responseType,
				// "Input parameter [" + RESPONSE_TYPE + "] cannot be null in oder to execute " + this.getActionName() + " service");
				// Assert.assertTrue(RESPONSE_TYPE_INLINE.equalsIgnoreCase(responseType) || RESPONSE_TYPE_ATTACHMENT.equalsIgnoreCase(responseType),
				// "[" + responseType + "] is not a valid value for " + RESPONSE_TYPE + " parameter");
				UserProfile userProfile = (UserProfile) getEnv().get(EngineConstants.ENV_USER_PROFILE);
				IModelAccessModality accessModality = getEngineInstance().getDataSource().getModelAccessModality();

				query = accessModality.getFilteredStatement(query, this.getEngineInstance().getDataSource(), userProfile.getUserAttributes());

				statement = getEngineInstance().getDataSource().createStatement(query);

				statement.setParameters(getEnv());
				statement.setProfileAttributes(getUserProfileAttributes());

				jpaQueryStr = statement.getQueryString();

				logger.debug("Executable HQL/JPQL query: [" + jpaQueryStr + "]");

				sqlQuery = statement.getSqlQueryString();
				Assert.assertNotNull(sqlQuery, "The SQL query is needed while exporting results.");

			} else {
				// case of FormEngine

				sqlQuery = this.getAttributeFromSessionAsString(ExecuteDetailQueryAction.LAST_DETAIL_QUERY);
				Assert.assertNotNull(sqlQuery, "The detail query was not found, maybe you have not execute the detail query yet.");
			}
			logger.debug("Executable SQL query: [" + sqlQuery + "]");

			logger.debug("Exctracting fields ...");

			IDataSource dataSource = (IDataSource) getEngineInstance().getDataSource().getConfiguration().loadDataSourceProperties().get("datasource");
			connection = dataSource.getConnection();

			fieldsReader = new SQLFieldsReader(sqlQuery, connection);

			try {
				extractedFields = fieldsReader.readFields();
			} catch (Exception e) {
				logger.debug("Impossible to extract fields from query");
				throw new SpagoBIEngineException("Impossible to extract fields from query: " + jpaQueryStr, e);
			}

			logger.debug("Fields extracted succesfully");

			Assert.assertTrue(query.getSimpleSelectFields(true).size() + query.getInLineCalculatedSelectFields(true).size() == extractedFields.size(),
					"The number of fields extracted from query resultset cannot be different from the number of fields specified into the query select clause");

			decorateExtractedFields(extractedFields, query);

			params = new HashMap();
			params.put("pagination", getPaginationParamVaue(mimeType));

			if (isXlsx(mimeType)) {
				exportIntoXLSX(writeBackResponseInline, mimeType, statement, sqlQuery, extractedFields, exportLimit);
			} else if (isCsv(mimeType)) {
				exportIntoCSV(writeBackResponseInline, mimeType, fileExtension, transaction, sqlQuery);
			}

		} catch (Throwable t) {
			throw SpagoBIEngineServiceExceptionHandler.getInstance().getWrappedException(getActionName(), getEngineInstance(), t);
		} finally {

			try {
				// closing session will close also all connection created into this section
				transaction.close();
			} catch (Exception e) {
				logger.warn("Impossible to close the connection used to execute the report in " + getActionName() + " service", e);
			}

			if (reportFile != null && reportFile.exists()) {
				try {
					reportFile.delete();
				} catch (Exception e) {
					logger.warn("Impossible to delete temporary file " + reportFile, e);
				}
			}
		}

		logger.debug("OUT");
	}

	/**
	 * @return
	 * @throws EMFInternalError
	 */
	private Map getUserProfileAttributes() throws EMFInternalError {
		UserProfile userProfile = (UserProfile) getEnv().get(EngineConstants.ENV_USER_PROFILE);
		Map userAttributes = new HashMap();
		Iterator it = userProfile.getUserAttributeNames().iterator();
		while (it.hasNext()) {
			String attributeName = (String) it.next();
			Object attributeValue = userProfile.getUserAttribute(attributeName);
			userAttributes.put(attributeName, attributeValue);
		}
		return userAttributes;
	}

	private void exportIntoCSV(boolean writeBackResponseInline, String mimeType, String fileExtension, ITransaction transaction, String sqlQuery)
			throws IOException, SpagoBIEngineException {
		File csvFile = null;
		try {
			csvFile = File.createTempFile("csv", ".csv");
			QbeCSVExporter exporter = new QbeCSVExporter();
			Connection connection = null;
			try {

				IDataSource dataSource = (IDataSource) getEngineInstance().getDataSource().getConfiguration().loadDataSourceProperties().get("datasource");

				connection = dataSource.getConnection();
			} catch (Exception e) {
				logger.debug("Query execution aborted because of an internal exception");

			}
			exporter.export(csvFile, connection, sqlQuery);
			try {
				writeBackToClient(csvFile, null, writeBackResponseInline, "report." + fileExtension, mimeType);
			} catch (IOException ioe) {
				throw new SpagoBIEngineException("Impossible to write back the responce to the client", ioe);
			}
		} finally {
			if (csvFile != null) {
				csvFile.delete();
			}
		}
	}

	private void exportIntoXLSX(boolean writeBackResponseInline, String mimeType, IStatement statement, String sqlQuery, List<?> extractedFields,
			Integer exportLimit) throws EMFInternalError, IOException, SpagoBIEngineException {
		DataIterator iterator = getDataIterator(statement, sqlQuery, exportLimit);
		Locale locale = (Locale) getEngineInstance().getEnv().get(EngineConstants.ENV_LOCALE);
		QbeXLSXExporter exp = new QbeXLSXExporter(iterator, locale, exportLimit);
		exp.setExtractedFields(extractedFields);

		try (Workbook wb = exp.export()) {

			File file = File.createTempFile("workbook", ".xlsx");

			try (FileOutputStream stream = new FileOutputStream(file)) {
				wb.write(stream);
				stream.flush();
			}

			try {
				writeBackToClient(file, null, writeBackResponseInline, "workbook.xlsx", mimeType);
			} catch (IOException ioe) {
				throw new SpagoBIEngineException("Impossible to write back the responce to the client", ioe);
			} finally {
				if (file != null && file.exists()) {
					try {
						file.delete();
					} catch (Exception e) {
						logger.warn("Impossible to delete temporary file " + file, e);
					}
				}
			}
		}
	}

	private DataIterator getDataIterator(IStatement statement, String sqlQuery, Integer exportLimit) throws EMFInternalError {
		DataIterator iterator = null;

		boolean isFormEngineInstance = getEngineInstance().getTemplate().getProperty("formJSONTemplate") != null;
		if (!isFormEngineInstance) {
			// case of standard QBE

			IDataSet dataSet = null;

			boolean isMaxResultsLimitBlocking = QbeEngineConfig.getInstance().isMaxResultLimitBlocking();
			dataSet = QbeDatasetFactory.createDataSet(statement);
			dataSet.setAbortOnOverflow(isMaxResultsLimitBlocking);

			dataSet.addBinding("attributes", getUserProfileAttributes());
			dataSet.addBinding("parameters", this.getEnv());
			logger.debug("Executing query ...");

			Map<String, Object> envs = getEnv();
			String stringDrivers = envs.get(DRIVERS).toString();
			Map<String, Object> drivers = null;
			try {
				drivers = JSONObjectDeserializator.getHashMapFromString(stringDrivers);
			} catch (Exception e) {
				logger.debug("Drivers cannot be transformed from string to map");
				throw new SpagoBIRuntimeException("Drivers cannot be transformed from string to map", e);
			}
			dataSet.setDrivers(drivers);

			iterator = dataSet.iterator();

		} else {
			// case of FormEngine

			JDBCDataSet dataset = new JDBCDataSet();
			IDataSource datasource = (IDataSource) this.getEnv().get(EngineConstants.ENV_DATASOURCE);
			dataset.setDataSource(datasource);
			dataset.setUserProfileAttributes(UserProfileUtils.getProfileAttributes((UserProfile) this.getEnv().get(EngineConstants.ENV_USER_PROFILE)));
			dataset.setQuery(sqlQuery);
			logger.debug("Executing query ...");
			iterator = dataset.iterator();
		}

		return iterator;
	}

	private Integer parseExportLimit(String exportLimit) {
		Integer limit;
		try {
			limit = Integer.parseInt(exportLimit);
		} catch (NumberFormatException e) {
			String msg = "Export limit cannot be parsed, check if value set for dataset.export.xls.resultsLimit in Configuration Management is numeric";
			logger.error(msg, e);
			limit = 10000;
		}
		return limit;
	}

	private void decorateExtractedFields(List extractedFields, Query query) {
		List selectedFields = query.getSelectFields(true);
		Iterator selectedFieldsIterator = selectedFields.iterator();
		Iterator extractedFieldsIterator = extractedFields.iterator();
		while (extractedFieldsIterator.hasNext()) {
			Field exctractedField = (Field) extractedFieldsIterator.next();
			ISelectField selectedField = (ISelectField) selectedFieldsIterator.next();
			exctractedField.setAlias(selectedField.getAlias());
			exctractedField.setVisible(selectedField.isVisible());
			if (selectedField.isSimpleField())
				exctractedField.setPattern(((SimpleSelectField) selectedField).getPattern());
		}
	}

	private String getPaginationParamVaue(String mimeType) {
		if ("application/pdf".equalsIgnoreCase(mimeType) || "application/rtf".equalsIgnoreCase(mimeType)) {
			return "false";
		}

		return "true";
	}

	private Query deserializeQuery(JSONObject queryJSON) throws SerializationException, JSONException {
		return SerializerFactory.getDeserializer("application/json").deserializeQuery(queryJSON.toString(), getEngineInstance().getDataSource());
	}

	private boolean isCsv(String mimeType) {
		return "text/csv".equalsIgnoreCase(mimeType);
	}

	private boolean isXlsx(String mimeType) {
		return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equalsIgnoreCase(mimeType);
	}

}
