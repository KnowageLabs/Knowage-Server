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
package it.eng.spagobi.tools.datasource.service;


import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.dispatching.action.AbstractHttpAction;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.utilities.UserUtilities;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

/**
 * 
 * @author Chiarelli (chiara.chiarelli@eng.it)
 *
 */
public class TestConnectionAction extends AbstractHttpAction {

	static private Logger logger = Logger.getLogger(TestConnectionAction.class);
	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.commons.services.BaseProfileAction#service(it.eng.spago.base.SourceBean, it.eng.spago.base.SourceBean)
	 */
	public void service(SourceBean serviceRequest, SourceBean serviceResponse)
			throws Exception {
		
		logger.debug("IN");
		String message = null;
		freezeHttpResponse();
		HttpServletResponse httResponse = getHttpResponse();
		
		String url = (String) serviceRequest.getAttribute("urlc");
		String jndi = (String) serviceRequest.getAttribute("jndi");
		String isjndi = (String) serviceRequest.getAttribute("isjndi");
		String user = (String) serviceRequest.getAttribute("user");
		String pwd = (String) serviceRequest.getAttribute("pwd");
		String driver = (String) serviceRequest.getAttribute("driver");
		String multischema = (String) serviceRequest.getAttribute("multischema");
		Boolean isMultischema = false;
		if(multischema!=null && !multischema.equals("") && multischema.equalsIgnoreCase("true")){
			isMultischema = true;
		}
		RequestContainer requestContainer = this.getRequestContainer();	
		SessionContainer session = requestContainer.getSessionContainer();
		SessionContainer permanentSession = session.getPermanentContainer();
		IEngUserProfile profile = (IEngUserProfile)permanentSession.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		logger.debug("user profile:"+ profile);
		String schemaattr = (String) serviceRequest.getAttribute("schemaattr");
		logger.debug("User Attribute:"+ schemaattr);
		
		String schema=(String)profile.getUserAttribute(schemaattr);
		logger.debug("schema:"+ schema);
		Connection connection = null;
		Context ctx;
		try {
			if (isjndi.equals("true")){
					String jndiName = schema == null ? jndi : jndi + schema;
					logger.debug("Lookup JNDI name:"+ jndiName);
				    ctx = new InitialContext();
				    DataSource ds = (DataSource) ctx.lookup(jndiName);
				    connection = ds.getConnection();
			}else if (isjndi.equals("false")){			
				    Class.forName(driver);
				    connection = DriverManager.getConnection(url, user, pwd);
			}
		 if (connection != null){
			    	message = "sbi.connTestOk";
			    	logger.debug("connection is ok");
			   }
		} catch (NamingException ne) {
		    logger.error("JNDI error", ne);
		    message = "sbi.connTestError";
		} catch (SQLException sqle) {
		    logger.error("Cannot retrive connection", sqle);
		    message = "sbi.connTestError";
		} catch (ClassNotFoundException e) {
			    logger.error("Driver not found", e);
			    message = "sbi.connTestError";	
		}finally {
			httResponse.getOutputStream().write(message.getBytes());
			httResponse.getOutputStream().flush();
			if ((connection != null) && (!connection.isClosed())) connection.close();
			logger.debug("OUT "+message);
		}
	}	
}
