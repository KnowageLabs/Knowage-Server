/*
 * SpagoBI, the Open Source Business Intelligence suite
 * © 2005-2015 Engineering Group
 *
 * This file is part of SpagoBI. SpagoBI is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 2.1 of the License, or any later version. 
 * SpagoBI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details. You should have received
 * a copy of the GNU Lesser General Public License along with SpagoBI. If not, see: http://www.gnu.org/licenses/.
 * The complete text of SpagoBI license is included in the COPYING.LESSER file. 
 */
package it.eng.spagobi.tools.dataset.service;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanAttribute;
import it.eng.spago.base.SourceBeanException;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.Config;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOConfig;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.FileUtilities;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.tools.dataset.bo.FileDataSet;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.constants.DataSetConstants;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.tools.dataset.utils.DatasetMetadataParser;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.SpagoBIAccessUtils;
import it.eng.spagobi.utilities.assertion.Assert;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/*
 DB UPDATES TO DO to apply patch

 insert into SBI_CONFIG (ID,LABEL,NAME,DESCRIPTION,IS_ACTIVE,VALUE_CHECK, VALUE_TYPE_ID, USER_IN, TIME_IN, CATEGORY)
 values(
 (select NEXT_VAL from hibernate_sequences where SEQUENCE_NAME = 'SBI_CONFIG')
 , 'remoteUrl.protocol' , ' Remote Url Protocol', ' Remote Url Protocol', 1, 'http' 
 , (select VALUE_ID from SBI_DOMAINS where DOMAIN_CD = 'PAR_TYPE ' AND VALUE_CD = 'STRING')
 , 'spagobi'
 , current_timestamp
 , 'REMOTE_URL'
 );
 update hibernate_sequences set NEXT_VAL = NEXT_VAL+1 where SEQUENCE_NAME = 'SBI_CONFIG';

 insert into SBI_CONFIG (ID,LABEL,NAME,DESCRIPTION,IS_ACTIVE,VALUE_CHECK, VALUE_TYPE_ID, USER_IN, TIME_IN, CATEGORY)
 values(
 (select NEXT_VAL from hibernate_sequences where SEQUENCE_NAME = 'SBI_CONFIG')
 , 'remoteUrl.host' , ' Remote Url Host', ' Remote Url Host', 1, 'HOST_TO_CALL' 
 , (select VALUE_ID from SBI_DOMAINS where DOMAIN_CD = 'PAR_TYPE ' AND VALUE_CD = 'STRING')
 , 'spagobi'
 , current_timestamp
 , 'REMOTE_URL'
 );
 update hibernate_sequences set NEXT_VAL = NEXT_VAL+1 where SEQUENCE_NAME = 'SBI_CONFIG';


 insert into SBI_CONFIG (ID,LABEL,NAME,DESCRIPTION,IS_ACTIVE,VALUE_CHECK, VALUE_TYPE_ID, USER_IN, TIME_IN, CATEGORY)
 values(
 (select NEXT_VAL from hibernate_sequences where SEQUENCE_NAME = 'SBI_CONFIG')
 , 'remoteUrl.port' , ' Remote Url Port', ' Remote Url Host', 1, 'PORT_TO_CALL' 
 , (select VALUE_ID from SBI_DOMAINS where DOMAIN_CD = 'PAR_TYPE ' AND VALUE_CD = 'NUM')
 , 'spagobi'
 , current_timestamp
 , 'REMOTE_URL'
 );
 update hibernate_sequences set NEXT_VAL = NEXT_VAL+1 where SEQUENCE_NAME = 'SBI_CONFIG';


 insert into SBI_CONFIG (ID,LABEL,NAME,DESCRIPTION,IS_ACTIVE,VALUE_CHECK, VALUE_TYPE_ID, USER_IN, TIME_IN, CATEGORY)
 values(
 (select NEXT_VAL from hibernate_sequences where SEQUENCE_NAME = 'SBI_CONFIG')
 , 'remoteUrl.domain' , ' Remote Url DOMAIN', ' Remote Url Domain', 1, 'DOMAIN_TO_CALL' 
 , (select VALUE_ID from SBI_DOMAINS where DOMAIN_CD = 'PAR_TYPE ' AND VALUE_CD = 'STRING')
 , 'spagobi'
 , current_timestamp
 , 'REMOTE_URL'	
 );
 update hibernate_sequences set NEXT_VAL = NEXT_VAL+1 where SEQUENCE_NAME = 'SBI_CONFIG';
 commit



 commit

 // "http://sira2.hyperborea.com/h2cube/elaborazioni/elaboracsvmetadata?sourceName=MUD.Q1A&idFonte=1013&dbName=h2cube&of=h&at=&s=ANNO&s=KCER&s=PRODOTTO_IN_UL&sg=&sg=&sg=SUM({0})&w=ANNO&wo==&wa=2000";

 */
/**
 * 
 * @author gavardi
 * 
 */
@Path("/fileservicedataset")
public class FileServiceDataSetCRUD {

	static private Logger logger = Logger.getLogger(it.eng.spagobi.tools.dataset.service.FileServiceDataSetCRUD.class);

	// Distinguish if has to be opened with QBE or worksheet
	static final private String OPEN_WITH = "openWith";
	static final private String QBE = "qbe";
	static final private String WORKSHEET = "worksheet";

	//
	static final private String PARAMETERS_URL = "parametersUrl";

	// the name of the data file expected to download inside the zip
	static private String DATA_FILE_NAME = "data.csv";
	// the name of the metadata file expected to download inside the zip
	static private String METADATA_FILE_NAME = "metadata.json";

	IEngUserProfile profile = null;

	/**
	 * this service calls an url, retrieve the zip, opens and write a dataset base on metadata file and data file; then opens it with a qbe or worksheet
	 * 
	 * @param req
	 * @return
	 * @throws Exception
	 */
	@GET
	@Path("/createdataset")
	public Response createDataSet(@Context HttpServletRequest req) throws Exception {
		logger.debug("IN");

		Object openWithO = req.getParameter(OPEN_WITH);
		boolean openWorksheet = openWithO == null ? true : openWithO.toString().equalsIgnoreCase(WORKSHEET);
		logger.debug("Open with " + (openWorksheet == true ? "Worksheet Engine" : "QBE Engine"));

		Object parsRemoteUrlO = req.getParameter(PARAMETERS_URL);
		String parsRemoteUrl = parsRemoteUrlO != null ? parsRemoteUrlO.toString() : "";

		logger.debug("Patrameters of remote call are " + parsRemoteUrl);

		String remoteUrl = getRemoteUrl(parsRemoteUrl);

		logger.debug("Remote URL to call is " + remoteUrl);

		profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);

		byte[] response = null;

		try {
			response = getCSVFile(remoteUrl);
		} catch (Exception e) {
			logger.error("Error in retrieving CSV file from server ", e);
			throw e;
		}

		logger.debug("Files retrieved from remote url, decompress zip");

		File tempDir = null;
		try {
			tempDir = decompressByteArray(response);
		} catch (Exception e) {
			logger.error("Error in decompressing zip file", e);
			throw e;
		}

		logger.debug("Archive decompressed");

		String jsonContent = null;
		try {

			File metadataFile = new File(tempDir.getAbsolutePath() + "/" + METADATA_FILE_NAME);
			jsonContent = deserializeString(metadataFile);
		} catch (IOException e) {
			logger.error("Error in reading JSON from metadata file", e);
			throw e;
		}

		logger.debug("Metadata Json file read " + jsonContent);

		JSONObject jsonObject = null;

		// read dataset info and save it if label not
		FileDataSet dataSet = null;
		try {
			jsonObject = new JSONObject(jsonContent);
			dataSet = readMetadataAndSaveDataset(jsonObject);
		} catch (Exception e) {
			logger.error("Error in retrieving dataset metadata and saving metadata", e);
			throw e;
		}

		// copy data file to resources
		File dataFile = new File(tempDir.getAbsolutePath() + "/" + DATA_FILE_NAME);
		copyDataFileToResources(dataFile, dataSet);

		// FIND DATASOURCE_FOR_WRITING
		IDataSource idataSource = DAOFactory.getDataSourceDAO().loadDataSourceWriteDefault();
		if (idataSource == null) {
			logger.error("No write default datasource defined");
			throw new Exception("No write default datasource defined");
		}
		String dataSourceLabel = idataSource.getLabel();

		String url = null;

		String host = req.getServerName();
		String context = req.getContextPath();
		String protocol = req.getProtocol();
		if (protocol.contains("/")) {
			protocol = protocol.substring(0, protocol.indexOf("/")).toLowerCase();
		}

		String port = Integer.valueOf(req.getServerPort()).toString();

		// get Qbe context
		Engine qbeEngine = DAOFactory.getEngineDAO().loadEngineByDriver("it.eng.spagobi.engines.drivers.qbe.QbeDriver");
		String qbeContext = qbeEngine.getUrl();
		if (qbeContext.startsWith("/") || qbeContext.startsWith("\\")) {
			qbeContext = qbeContext.substring(1);
		}

		// Build locale (try taking in request otherwse in session)

		Object languageO = req.getParameter(SpagoBIConstants.AF_LANGUAGE);
		Object countryO = req.getParameter(SpagoBIConstants.AF_COUNTRY);

		if (languageO == null || countryO == null) {
			languageO = req.getSession().getAttribute(SpagoBIConstants.AF_LANGUAGE);
			countryO = req.getSession().getAttribute(SpagoBIConstants.AF_COUNTRY);
		}
		String sbiLanguage = languageO != null ? languageO.toString() : "en";
		String sbiCountry = countryO != null ? countryO.toString() : "En";

		logger.debug("Language retrieved: [" + sbiLanguage + "]; country retrieved: [" + sbiCountry + "]");

		url = protocol + "://" + host + ":" + port + "/" + qbeContext;

		if (openWorksheet) {
			logger.debug("Open with worksheet case");

			url += "?ACTION_NAME=WORKSHEET_WITH_DATASET_START_EDIT_ACTION";
			url += "&user_id=" + profile.getUserUniqueIdentifier();
			url += "&dataset_label=" + dataSet.getLabel();
			url += "&ENGINE_DATASOURCE_LABEL=" + dataSourceLabel;
			url += "&NEW_SESSION=TRUE";
			url += "&SBI_LANGUAGE=" + sbiLanguage;
			url += "&SBI_COUNTRY=" + sbiCountry;

		} else {

			logger.debug("Open with QBE case");

			url += "?ACTION_NAME=QBE_ENGINE_FROM_DATASET_START_ACTION";
			url += "&user_id=" + profile.getUserUniqueIdentifier();
			url += "&dataset_label=" + dataSet.getLabel();
			url += "&datasource_label=" + dataSourceLabel;
			url += "&NEW_SESSION=TRUE";
			url += "&SBI_LANGUAGE=" + sbiLanguage;
			url += "&SBI_COUNTRY=" + sbiCountry;
		}

		logger.debug("URL to call");
		java.net.URI location = new java.net.URI(url);

		logger.debug("OUT");
		return javax.ws.rs.core.Response.temporaryRedirect(location).build();

	}

	/**
	 * Build remote url
	 * 
	 * @param parameters
	 * @return
	 * @throws EMFUserError
	 * @throws Exception
	 */
	String getRemoteUrl(String parameters) throws EMFUserError, Exception {
		logger.debug("IN");

		Config protocolC = DAOFactory.getSbiConfigDAO().loadConfigParametersByLabel("remoteUrl.protocol");
		String protocol = protocolC != null ? protocolC.getValueCheck() : null;
		logger.debug(protocol);

		Config hostC = DAOFactory.getSbiConfigDAO().loadConfigParametersByLabel("remoteUrl.host");
		String host = hostC != null ? hostC.getValueCheck() : null;
		logger.debug(host);

		Config domainC = DAOFactory.getSbiConfigDAO().loadConfigParametersByLabel("remoteUrl.domain");
		String domain = domainC != null ? domainC.getValueCheck() : null;
		logger.debug(domain);

		Config portC = DAOFactory.getSbiConfigDAO().loadConfigParametersByLabel("remoteUrl.port");
		String port = portC != null ? portC.getValueCheck() : null;
		logger.debug(port);

		Assert.assertNotNull(protocol, "Protocol not present in sbi configs");
		Assert.assertNotNull(host, "Host not present in sbi configs");
		Assert.assertNotNull(domain, "Domain not present in sbi configs");

		String url = protocol + "://" + host + ((port != null && !port.equalsIgnoreCase("")) ? (":" + port) : "") + domain + "?" + parameters;

		logger.debug("OUT");

		return url;

	}

	/**
	 * get CSV file from remote Url
	 * 
	 * @param url
	 * @return
	 * @throws HttpException
	 * @throws IOException
	 */
	byte[] getCSVFile(String url) throws HttpException, IOException {
		logger.debug("IN");
		HttpClient client = new HttpClient();
		HttpConnectionManager conManager = client.getHttpConnectionManager();

		// Cancel, proxy settings made on server
		// logger.debug("Setting proxy");
		// client.getHostConfiguration().setProxy("192.168.10.1", 3128);
		// HttpState state = new HttpState();
		// state.setProxyCredentials(null, null, new UsernamePasswordCredentials("", ""));
		// client.setState(state);
		// logger.debug("proxy set");
		// cancel

		GetMethod httpGet = new GetMethod(url);
		int statusCode = client.executeMethod(httpGet);
		logger.debug("Status code after request to remote URL is " + statusCode);
		byte[] response = httpGet.getResponseBody();

		if (response == null) {
			logger.warn("Response of remote URL is empty");
		}

		httpGet.releaseConnection();
		logger.debug("OUT");
		return response;
	}

	/**
	 * Decompress downloaded file
	 * 
	 * @param response
	 * @return
	 * @throws IOException
	 */

	File decompressByteArray(byte[] response) throws IOException {
		logger.debug("IN");
		// write byteArray
		File dir = new File(System.getProperty("java.io.tmpdir"));
		Random generator = new Random();
		int randomInt = generator.nextInt();
		String fileName = Integer.valueOf(randomInt).toString();
		File zipFile = File.createTempFile(fileName, ".zip", dir);

		logger.debug("created temporary zip file " + zipFile.getAbsolutePath());

		FileOutputStream fos = new FileOutputStream(zipFile);
		fos.write(response);
		fos.close();

		// create folder to store temporary files
		File temp = new File(System.getProperty("java.io.tmpdir") + "/" + fileName);
		temp.mkdir();

		logger.debug("Unzip file in  " + temp.getAbsolutePath());

		new SpagoBIAccessUtils().unzip(zipFile, temp);
		logger.debug("OUT");
		return temp;
	}

	public static String deserializeString(File file) throws IOException {
		int len;
		char[] chr = new char[4096];
		final StringBuffer buffer = new StringBuffer();
		final FileReader reader = new FileReader(file);
		try {
			while ((len = reader.read(chr)) > 0) {
				buffer.append(chr, 0, len);
			}
		} finally {
			reader.close();
		}
		return buffer.toString();
	}

	/**
	 * Save dataset with metadata info
	 * 
	 * @param jsonObject
	 * @return
	 * @throws JSONException
	 * @throws EMFUserError
	 */

	FileDataSet readMetadataAndSaveDataset(JSONObject jsonObject) throws JSONException, EMFUserError {
		logger.debug("IN");

		FileDataSet dataSet = new FileDataSet();
		dataSet.setResourcePath(DAOConfig.getResourcePath());

		// create configuration
		String encoding = jsonObject.getString("encoding");
		String separator = jsonObject.getString("separator");

		JSONObject configurationObj = new JSONObject();
		configurationObj.put("fileType", "CSV");
		configurationObj.put("csvDelimiter", separator);
		configurationObj.put("csvQuote", "\"");
		configurationObj.put("fileName", DATA_FILE_NAME);
		configurationObj.put("encoding", encoding);
		String confString = configurationObj.toString();
		dataSet.setConfiguration(confString);
		dataSet.setFileName(DATA_FILE_NAME);
		dataSet.setFileType("CSV");
		dataSet.setDsType(DataSetConstants.DS_FILE);

		String label = jsonObject.getString("label");
		dataSet.setLabel(label);
		dataSet.setName(label);
		dataSet.setDescription(label);
		dataSet.setOwner(profile.getUserUniqueIdentifier().toString());

		String cml;
		try {
			cml = writeXMLMetadata(jsonObject);
			dataSet.setDsMetadata(cml);
		} catch (SourceBeanException e) {
			logger.error("Error in retrieving fields metadata in correct format from metadata json");
		}

		IDataSetDAO dataSetDAO = DAOFactory.getDataSetDAO();

		logger.debug("check if dataset with label " + label + " is already present");

		// check label is already present; insert or modify dependengly
		IDataSet iDataSet = DAOFactory.getDataSetDAO().loadDataSetByLabel(label);

		// loadActiveDataSetByLabel(label);
		if (iDataSet != null) {
			logger.debug("a dataset with label " + label + " is already present: modify it");
			dataSet.setId(iDataSet.getId());
			dataSetDAO.modifyDataSet(dataSet);
		} else {
			logger.debug("No dataset with label " + label + " is already present: insert it");
			dataSetDAO.insertDataSet(dataSet);

		}

		logger.debug("OUT");
		return dataSet;
	}

	void copyDataFileToResources(File tempDataFile, FileDataSet dataSet) {
		logger.debug("IN");

		// put csv file inside resources
		String resourcePath = dataSet.getResourcePath();

		String fileResPath = resourcePath + "/dataset/files/";
		File destFile = new File(fileResPath);

		FileUtilities.copyFile(tempDataFile, destFile, true, false);

		logger.debug("OUT");

	}

	String writeXMLMetadata(JSONObject jsonObject) throws JSONException, SourceBeanException {
		logger.debug("IN");
		String toReturn = null;

		SourceBean sb = new SourceBean(DatasetMetadataParser.COLUMNLIST);

		JSONArray fieldsArray = jsonObject.getJSONArray("fields");
		for (int i = 0; i < fieldsArray.length(); i++) {
			JSONObject field = fieldsArray.getJSONObject(i);
			String name = field.getString("nome");
			String labelF = field.getString("label");
			String type = field.getString("tipo");
			String role = field.getString("ruolo");

			final String MEASURE = "MEASURE";
			final String ATTRIBUTE = "ATTRIBUTE";

			if (role != null && role.equalsIgnoreCase("attributo")) {
				role = ATTRIBUTE;
			} else if (role != null && role.equalsIgnoreCase("misura")) {
				role = MEASURE;
			}

			// clean label from not regular characters
			// labelF = labelF.replaceAll("[^a-zA-Z0-9\\_\\s]", "");
			// labelF = labelF.replaceAll("\\s", "_");
			// name = name.replaceAll("[^a-zA-Z0-9\\_\\s]", "");
			// name = name.replaceAll("\\s", "_");

			SourceBean sbMeta = new SourceBean(DatasetMetadataParser.COLUMN);
			SourceBeanAttribute attN = new SourceBeanAttribute(DatasetMetadataParser.NAME, labelF);
			String newType = oracleTypetoJava(type);
			SourceBeanAttribute attT = new SourceBeanAttribute(DatasetMetadataParser.TYPE, newType);
			SourceBeanAttribute attA = name != null ? new SourceBeanAttribute(DatasetMetadataParser.ALIAS, name) : null;
			SourceBeanAttribute attF = role != null ? new SourceBeanAttribute(DatasetMetadataParser.FIELD_TYPE, role) : null;
			sbMeta.setAttribute(attN);
			sbMeta.setAttribute(attT);
			if (attA != null)
				sbMeta.setAttribute(attA);
			if (attF != null)
				sbMeta.setAttribute(attF);
			sb.setAttribute(sbMeta);
		}

		toReturn = sb.toXML(false);
		logger.debug("OUT");
		return toReturn;
	}

	String oracleTypetoJava(String oracle) {
		logger.debug("IN");
		String javaToReturn = null;
		if (oracle.equalsIgnoreCase("VARCHAR") || oracle.equals("VARCHAR2")) {
			javaToReturn = "java.lang.String";
		} else if (oracle.equalsIgnoreCase("Integer")) {
			javaToReturn = "java.lang.Integer";
		} else if (oracle.equalsIgnoreCase("NUMBER")) {
			javaToReturn = "java.math.BigDecimal";
		} else if (oracle.equalsIgnoreCase("DOUBLE")) {
			javaToReturn = "java.lang_double";
		} else if (oracle.equalsIgnoreCase("BOOLEAN")) {
			javaToReturn = "java.math.Boolean";

		}
		logger.debug("OUT");
		return javaToReturn;
	}

	protected Engine getQbeEngine() throws Exception {
		Engine qbeEngine;
		List<Engine> engines;

		qbeEngine = null;
		try {
			Assert.assertNotNull(DAOFactory.getEngineDAO(), "EngineDao cannot be null");
			engines = DAOFactory.getEngineDAO().loadAllEnginesForBIObjectType(SpagoBIConstants.DATAMART_TYPE_CODE);
			if (engines == null || engines.size() == 0) {
				throw new Exception("There are no engines for documents of type [DATAMART] available");
			} else {
				qbeEngine = engines.get(0);
				if (engines.size() > 1) {
					LogMF.warn(logger, "There are more than one engine for document of type [DATAMART]. We will use the one whose label is equal to [{0}]",
							qbeEngine.getLabel());
				} else {
					LogMF.debug(logger, "Using qbe engine with label [{0}]", qbeEngine.getLabel());
				}
			}
		} catch (Throwable t) {
			throw new Exception("Impossible to load a valid engine for document of type [DATAMART]", t);
		} finally {
			logger.debug("OUT");
		}

		return qbeEngine;
	}

}
