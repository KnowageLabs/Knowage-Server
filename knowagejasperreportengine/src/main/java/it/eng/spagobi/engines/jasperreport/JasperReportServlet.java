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
package it.eng.spagobi.engines.jasperreport;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

import it.eng.spago.error.EMFInternalError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.services.common.EnginConf;
import it.eng.spagobi.services.proxy.DataSourceServiceProxy;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.ParametersDecoder;
import it.eng.spagobi.utilities.callbacks.audit.AuditAccessUtils;

/**
 * 
 * @deprecated use JasperReportEngineStartAction instead (for any questions contact andrea gioia)
 */
public class JasperReportServlet extends HttpServlet {

   

    /**
     * Logger component
     */
    private static transient Logger logger = Logger.getLogger(JasperReportServlet.class);
    private static String CONNECTION_NAME="connectionName";
    private static String PARAM_OUTPUT_FORMAT="outputType";

    /**
     * Initialize the engine.
     * 
     * @param config the config
     * 
     * @throws ServletException the servlet exception
     */
    public void init(ServletConfig config) throws ServletException {
	super.init(config);
	logger.debug("Initializing SpagoBI JasperReport Engine...");
    }

    /**
     * process jasper report execution requests.
     * 
     * @param request the request
     * @param response the response
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ServletException the servlet exception
     */
    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    	
	logger.debug("Start processing a new request...");
    Monitor monitor =MonitorFactory.start("JasperReportServlet.service");
	HttpSession session = request.getSession();
	logger.debug("documentId IN Session:"+(String)session.getAttribute("document"));
	// USER PROFILE
	String documentId = (String) request.getParameter("document");
	if (documentId==null){
	    documentId=(String)session.getAttribute("document");
	    logger.debug("documentId From Session:"+documentId);
	}
	logger.debug("documentId:"+documentId);
	
	String requestConnectionName = (String) request.getParameter(CONNECTION_NAME);
	if (requestConnectionName==null) logger.debug("requestConnectionName is NULL");
	else logger.debug("requestConnectionName:"+requestConnectionName);
	
	//  operazioni fatte dal filtro OUT
	IEngUserProfile profile = (IEngUserProfile) session.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
	logger.debug("profile from session: " + profile);
	Map params = new HashMap();
	Enumeration enumer = request.getParameterNames();
	String parName = null;
	String parValue = null;
	logger.debug("Reading request parameters...");
	while (enumer.hasMoreElements()) {
	    parName = (String) enumer.nextElement();
	    parValue = request.getParameter(parName);
	    addParToParMap(params, parName, parValue);
	    logger.debug("Read parameter [" + parName + "] with value ["+ parValue + "] from request");
	}
	logger.debug("Request parameters read sucesfully" + params);
	
	// AUDIT UPDATE
	String auditId = request.getParameter("SPAGOBI_AUDIT_ID");
	AuditAccessUtils auditAccessUtils = (AuditAccessUtils) request.getSession().getAttribute("SPAGOBI_AUDIT_UTILS");
	if (auditAccessUtils != null)
	    auditAccessUtils.updateAudit(session,(String) profile.getUserUniqueIdentifier(), auditId, new Long(System
		    .currentTimeMillis()), null, "EXECUTION_STARTED", null, null);
	
	logger.debug("GetConnection...");
	JasperReportRunner jasperReportRunner = new JasperReportRunner(session);
	logger.debug("GetConnection...");
	Connection con = getConnection(requestConnectionName,session,profile,documentId);

	if (con == null) {
	    logger.error("Cannot obtain" + " connection for engine ["
		    + this.getClass().getName() + "] control document configurations");
	    // AUDIT UPDATE
	    if (auditAccessUtils != null)
		auditAccessUtils.updateAudit(session,(String) profile.getUserUniqueIdentifier(), auditId, null, new Long(System
			.currentTimeMillis()), "EXECUTION_FAILED", "No connection available", null);
	    return;
	}
	try {
	    String outputType = (params.get(PARAM_OUTPUT_FORMAT) == null)?"html":(String) params.get(PARAM_OUTPUT_FORMAT);
	    String tmpdir = (String) EnginConf.getInstance().getConfig().getAttribute("GENERALSETTINGS.tmpdir");
	    if (!tmpdir.startsWith("/")) {
		String contRealPath = getServletContext().getRealPath("/");
		if (contRealPath.endsWith("\\") || contRealPath.endsWith("/")) {
		    contRealPath = contRealPath.substring(0, contRealPath.length() - 1);
		}
		tmpdir = contRealPath + "/" + tmpdir;
	    }
	    tmpdir = tmpdir + System.getProperty("file.separator") + "reports";
	    File dir = new File(tmpdir);
	    dir.mkdirs();
	    File tmpFile = File.createTempFile("report", "." + outputType, dir);
	    OutputStream out = new FileOutputStream(tmpFile);
	    jasperReportRunner.runReport(con, params, out, getServletContext(), response, request);
	    out.flush();
	    out.close();

	   // if (outputType == null)	outputType = "html";
		//outputType = ExporterFactory.getDefaultType();
	    
	    response.setHeader("Content-Disposition", "filename=\"report." + outputType + "\";");
	    // response.setContentType((String)extensions.get(outputType));
	    response.setContentLength((int) tmpFile.length());

	    BufferedInputStream in = new BufferedInputStream(new FileInputStream(tmpFile));
	    int b = -1;
	    while ((b = in.read()) != -1) {
	    	response.getOutputStream().write(b);
	    }
	    response.getOutputStream().flush();
	    in.close();
	    // instant cleaning
	    tmpFile.delete();

	    // AUDIT UPDATE
	    if (auditAccessUtils != null)
		auditAccessUtils.updateAudit(session,(String) profile.getUserUniqueIdentifier(), auditId, null, new Long(System
			.currentTimeMillis()), "EXECUTION_PERFORMED", null, null);
	} catch (Exception e) {
	    logger.error( "Error during report production \n\n " + e);
	    // AUDIT UPDATE
	    if (auditAccessUtils != null)
		auditAccessUtils.updateAudit(session,(String) profile.getUserUniqueIdentifier(), auditId, null, new Long(System
			.currentTimeMillis()), "EXECUTION_FAILED", e.getMessage(), null);
	    return;
	} finally {
		try {			
			if (con != null && !con.isClosed()) 
					con.close();
		} catch (SQLException sqle) {
			logger.error("Error closing connection",sqle);
		}
		monitor.stop();
		logger.debug("OUT: Request processed");
	}


    }

    /**
     * @param params
     * @param parName
     * @param parValue
     */
    private void addParToParMap(Map params, String parName, String parValue) {
	logger.debug("IN.parName:"+parName+" /parValue:"+parValue);
	String newParValue;

	ParametersDecoder decoder = new ParametersDecoder();
	if (decoder.isMultiValues(parValue)) {
	    List values = decoder.decode(parValue);
	    newParValue = "";
	    for (int i = 0; i < values.size(); i++) {
		newParValue += (i > 0 ? "," : "");
		newParValue += values.get(i);
	    }

	} else {
	    newParValue = parValue;
	}

	params.put(parName, newParValue);
	logger.debug("OUT");
    }

    /**
     * This method, based on the data sources table, gets a database connection
     * and return it
     * 
     * @return the database connection
     */
    private Connection getConnection(String requestConnectionName,HttpSession session,IEngUserProfile profile,String documentId) {
	logger.debug("IN.documentId:"+documentId);
	DataSourceServiceProxy proxyDS = new DataSourceServiceProxy((String)profile.getUserUniqueIdentifier(),session);
	IDataSource ds =null;
	if (requestConnectionName!=null){
	    ds = proxyDS.getDataSourceByLabel(requestConnectionName);
	}else{
	    ds = proxyDS.getDataSource(documentId);
	}
	
	String schema=null;
	try {
		if (ds.checkIsMultiSchema()){
			String attrname=ds.getSchemaAttribute();
			if (attrname!=null) schema = (String)profile.getUserAttribute(attrname);
		}
	} catch (EMFInternalError e) {
		logger.error("Cannot retrive ENTE", e);
	}

	if (ds==null) {
	    logger.warn("Data Source IS NULL. There are problems reading DataSource informations");
	    return null;
	}
	// get connection
	Connection conn = null;
	
	try {
		conn = ds.toSpagoBiDataSource().readConnection(schema);
	} catch (Exception e) {
		logger.error("Cannot retrive connection", e);
	} 
	
	return conn;

    }




}
