/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.accessibility.servlet;

import it.eng.spago.error.EMFInternalError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.container.ObjectUtils;
import it.eng.spagobi.engines.accessibility.dao.QueryExecutor;
import it.eng.spagobi.engines.accessibility.xslt.Transformation;
import it.eng.spagobi.services.content.bo.Content;
import it.eng.spagobi.services.datasource.bo.SpagoBiDataSource;
import it.eng.spagobi.services.proxy.ContentServiceProxy;
import it.eng.spagobi.services.proxy.DataSetServiceProxy;
import it.eng.spagobi.services.proxy.DataSourceServiceProxy;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.JDBCDataSet;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.callbacks.audit.AuditAccessUtils;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Iterator;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import sun.misc.BASE64Decoder;

public class AccessibilityServlet extends HttpServlet {

	private static transient Logger logger = Logger.getLogger(AccessibilityServlet.class);
	private static String CONNECTION_NAME = "connectionName";
	private static String QUERY = "query";
	private static String DOCUMENT_ID = "document";
	private static String USER_ID = "user_id";

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		logger.debug("Initializing SpagoBI Accessibility Engine...");
	}

	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		logger.debug("IN");
		// get the document
		HttpSession session = request.getSession();
		logger.debug("documentId IN Session:" + (String) session.getAttribute(DOCUMENT_ID));
		// USER PROFILE
		String documentId = request.getParameter(DOCUMENT_ID);
		if (documentId == null) {
			documentId = (String) session.getAttribute(DOCUMENT_ID);
			logger.debug("documentId From Session:" + documentId);
		}
		logger.debug("documentId:" + documentId);

		// get userprofile
		IEngUserProfile profile = (IEngUserProfile) session.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		logger.debug("profile from session: " + profile);

		// AUDIT UPDATE
		String auditId = request.getParameter("SPAGOBI_AUDIT_ID");
		AuditAccessUtils auditAccessUtils = (AuditAccessUtils) request.getSession().getAttribute("SPAGOBI_AUDIT_UTILS");
		if (auditAccessUtils != null)
			auditAccessUtils.updateAudit(session, (String) profile.getUserUniqueIdentifier(), auditId, new Long(System.currentTimeMillis()), null,
					"EXECUTION_STARTED", null, null);

		// read connection from request
		String requestConnectionName = request.getParameter(CONNECTION_NAME);
		if (requestConnectionName == null)
			logger.debug("requestConnectionName is NULL");
		else
			logger.debug("requestConnectionName:" + requestConnectionName);

		Connection con = null;
		String query = null;

		IDataSet dataset = getDataSet(requestConnectionName, session, profile, documentId);
		if (dataset == null) {
			logger.debug("No dataset query associated to this document");
			logger.debug("Try to get datasource");
			con = getConnection(requestConnectionName, session, profile, documentId);
			if (con == null) {
				logger.error("Document " + documentId + " has no dataset query neither datasource associated!");
				// AUDIT UPDATE
				if (auditAccessUtils != null)
					auditAccessUtils.updateAudit(session, (String) profile.getUserUniqueIdentifier(), auditId, null, new Long(System.currentTimeMillis()),
							"EXECUTION_FAILED", "No connection available", null);
				return;
			} else {
				// get the request query parameter name
				query = request.getParameter(QUERY);

			}
		} else {
			try {
				// get query
				JSONObject jsonConf = ObjectUtils.toJSONObject(dataset.getConfiguration());
				query = jsonConf.getString(QUERY);
				// query = (String)dataset.getQuery();
			} catch (Exception e) {
				logger.error("Error while getting query configuration.  Error: " + e.getMessage());
				if (auditAccessUtils != null)
					auditAccessUtils.updateAudit(session, (String) profile.getUserUniqueIdentifier(), auditId, null, new Long(System.currentTimeMillis()),
							"EXECUTION_FAILED", e.getMessage(), null);
				return;
			}
			try {
				if (dataset instanceof JDBCDataSet) {
					JDBCDataSet jdbcDataset = (JDBCDataSet) dataset;
					SpagoBiDataSource dataSource = jdbcDataset.getDataProxy().getDataSource().toSpagoBiDataSource();
					con = dataSource.readConnection(dataSource.getSchemaAttribute());
				} else {
					throw new SpagoBIRuntimeException("Dataset [" + dataset.getName() + "] is not of type [JDBC]");
				}

			} catch (Exception e) {
				logger.error("Unable to get connection", e);
				if (auditAccessUtils != null)
					auditAccessUtils.updateAudit(session, (String) profile.getUserUniqueIdentifier(), auditId, null, new Long(System.currentTimeMillis()),
							"EXECUTION_FAILED", e.getMessage(), null);
				return;
			}

		}
		// call dao to execute query
		try {
			// gets request parameters to execute query
			HashMap<String, String> parameters = cleanParameters(request);

			String xmlResult = QueryExecutor.executeQuery(con, query, parameters);

			byte[] xsl = getDocumentXSL(requestConnectionName, session, profile, documentId);

			byte[] html = Transformation.tarnsformXSLT(xmlResult, xsl);

			html = Transformation.tarnsformXSLT(xmlResult, xsl);
			html = adjustMetaTag(html);

			String outputFormat = request.getParameter("outputType");
			if (outputFormat != null && outputFormat.equalsIgnoreCase("pdf")) {
				String returnedUrl = generatePdf(html);
				HttpURLConnection connection = null;
				InputStream is = null;
				OutputStream os = null;

				try {
					Authenticator.setDefault(new Authenticator() {

						@Override
						public PasswordAuthentication getPasswordAuthentication() {
							return new PasswordAuthentication(System.getProperty("http.proxyUsername"), System.getProperty("http.proxyPassword").toCharArray());
						}
					});
					URL url = new URL(returnedUrl);
					connection = (HttpURLConnection) url.openConnection();
					connection.setRequestMethod("GET");

					connection.setRequestProperty("Content-Type", "application/pdf");

					response.setContentType("application/pdf");
					response.setHeader("Content-disposition", "inline; filename=report.pdf");
					is = connection.getInputStream();
					os = response.getOutputStream();
					byte[] buffer = new byte[1024];
					int len = is.read(buffer);
					while (len != -1) {
						os.write(buffer, 0, len);
						len = is.read(buffer);
					}
				} catch (Exception e) {
					logger.debug("Error while trying to obtain pdf from robobraille server:\n" + e);
					throw new SpagoBIRuntimeException("Error while trying to obtain pdf from robobraille server:\n" + e);
				} finally {
					if (is != null)
						is.close();

					if (os != null)
						os.close();

					if (connection != null)
						connection.disconnect();
				}
			} else {
				response.setContentType("text/html");
				response.getOutputStream().write(addCss(html));

				response.getOutputStream().flush();
			}
		} catch (Exception e1) {
			logger.error("Unable to output result", e1);
			if (auditAccessUtils != null)
				auditAccessUtils.updateAudit(session, (String) profile.getUserUniqueIdentifier(), auditId, null, new Long(System.currentTimeMillis()),
						"EXECUTION_FAILED", e1.getMessage(), null);
			return;
		}

		logger.debug("OUT");
	}

	// META tag is not closed. This method corrects this error
	private byte[] adjustMetaTag(byte[] html) {
		String htmlString = new String(html);
		htmlString = htmlString.replaceFirst("<META http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">",
				"<META http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"></META>");
		return htmlString.getBytes();
	}

	private byte[] addCss(byte[] html) {
		String htmlString = new String(html);
		htmlString = htmlString.replaceFirst("</head>", "<link rel=\"stylesheet\" type=\"text/css\" href=\"../css/report.css\"></head>");
		return htmlString.getBytes();
	}

	// It returns the url containing the pdf
	private String generatePdf(byte[] html) {
		String returnedUrl = "";
		String boundary = "----WebKitFormBoundaryo1yijFC6BU3ve93m";
		String newLine = "\n";
		HttpURLConnection connection = null;
		DataOutputStream request = null;
		try {
			Authenticator.setDefault(new Authenticator() {

				@Override
				public PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(System.getProperty("http.proxyUsername"), System.getProperty("http.proxyPassword").toCharArray());
				}
			});
			URL url = new URL("http://2.109.50.18:9099/api/htmltopdf");
			connection = (HttpURLConnection) url.openConnection();
			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");

			connection.setRequestProperty("Accept", "*/*");
			connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
			connection.setRequestProperty("Accept-Language", "it-IT,it;q=0.8,en-US;q=0.6,en;q=0.4");
			connection.setRequestProperty("Cache-Control", "no-cache");
			connection.setRequestProperty("Connection", "Keep-Alive");
			connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

			request = new DataOutputStream(connection.getOutputStream());

			request.writeBytes("--" + boundary + newLine);
			request.writeBytes("Content-Disposition: form-data; name=\"pagesize\"" + newLine);
			request.writeBytes(newLine);

			request.writeBytes("a4");
			request.writeBytes(newLine);
			request.writeBytes("--" + boundary + newLine);
			request.writeBytes("Content-Disposition: form-data; name=\"htmlfile\"; filename=\"table.html\"" + newLine);
			request.writeBytes("Content-Type: text/html" + newLine);
			request.writeBytes(newLine);

			request.write(html);

			request.writeBytes(newLine);
			request.writeBytes("--" + boundary + "--");

			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

			returnedUrl = reader.readLine();

			// returnedUrl begins with " and ends with " as well
			returnedUrl = returnedUrl.substring(1, returnedUrl.length() - 1);
		} catch (Exception e) {
			logger.debug("Error while trying to contact robobraille server:\n" + e);
			throw new SpagoBIRuntimeException("Error while trying to contact robobraille server:\n" + e);
		} finally {
			if (request != null) {
				try {
					request.flush();
					request.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if (connection != null)
				connection.disconnect();
		}

		return returnedUrl;
	}

	private HashMap<String, String> cleanParameters(HttpServletRequest request) {
		// gets request parameters to execute query
		HashMap<String, String[]> parameters = new HashMap<String, String[]>(request.getParameterMap());
		HashMap<String, String> parametersCleaned = new HashMap<String, String>();
		if (parameters.containsKey(QUERY)) {
			parameters.remove(QUERY);
		}
		if (parameters.containsKey(DOCUMENT_ID)) {
			parameters.remove(DOCUMENT_ID);
		}
		if (parameters.containsKey(CONNECTION_NAME)) {
			parameters.remove(CONNECTION_NAME);
		}
		if (parameters.containsKey(USER_ID)) {
			parameters.remove(USER_ID);
		}
		if (!parameters.isEmpty()) {
			for (Iterator it = parameters.keySet().iterator(); it.hasNext();) {
				String key = (String) it.next();
				parametersCleaned.put(key, request.getParameter(key));
			}
		}
		return parametersCleaned;
	}

	/**
	 * This method, based on the data sources table, gets a database connection and return it
	 *
	 * @return the database connection
	 */
	private Connection getConnection(String requestConnectionName, HttpSession session, IEngUserProfile profile, String documentId) {
		logger.debug("IN");
		IDataSource ds = null;

		DataSourceServiceProxy proxyDS = new DataSourceServiceProxy((String) profile.getUserUniqueIdentifier(), session);
		if (requestConnectionName == null) {
			// get document's datasource
			ds = proxyDS.getDataSource(documentId);
		} else {
			// get datasource by label
			ds = proxyDS.getDataSourceByLabel(requestConnectionName);
		}
		if (ds != null) {
			String schema = null;
			try {
				if (ds.checkIsMultiSchema()) {
					String attrname = ds.getSchemaAttribute();
					if (attrname != null)
						schema = (String) profile.getUserAttribute(attrname);
				}
			} catch (EMFInternalError e) {
				logger.error("Cannot retrive ENTE", e);
			}

			// get connection
			Connection conn = null;

			try {
				conn = ds.toSpagoBiDataSource().readConnection(schema);
				return conn;
			} catch (Exception e) {
				logger.error("Cannot retrive connection", e);
			}
		} else {
			logger.warn("Data Source IS NULL. There are problems reading DataSource informations");
			return null;
		}

		logger.debug("OUT");
		return null;

	}

	private IDataSet getDataSet(String requestConnectionName, HttpSession session, IEngUserProfile profile, String documentId) {
		logger.debug("IN");
		logger.debug("IN.documentId:" + documentId);
		DataSetServiceProxy proxyDataset = new DataSetServiceProxy((String) profile.getUserUniqueIdentifier(), session);
		// get document's dataset
		IDataSet dataset = proxyDataset.getDataSet(documentId);

		if (dataset == null) {
			logger.warn("Data Set IS NULL. There are problems reading DataSet informations");
			return null;
		}

		logger.debug("OUT");
		return dataset;

	}

	private byte[] getDocumentXSL(String requestConnectionName, HttpSession session, IEngUserProfile profile, String documentId) {
		logger.debug("IN");

		ContentServiceProxy contentProxy = new ContentServiceProxy((String) profile.getUserUniqueIdentifier(), session);

		Content templateContent = contentProxy.readTemplate(documentId, new HashMap());

		InputStream is = null;
		byte[] byteContent = null;
		try {
			BASE64Decoder bASE64Decoder = new BASE64Decoder();
			byteContent = bASE64Decoder.decodeBuffer(templateContent.getContent());
			is = new java.io.ByteArrayInputStream(byteContent);
		} catch (Throwable t) {
			logger.warn("Error on decompile", t);
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				logger.warn("Error on closing inputstream", e);
			}
		}

		logger.debug("OUT");
		return byteContent;

	}
}