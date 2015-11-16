/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.datasource.service.rest;

import it.eng.spago.security.IEngUserProfile;

import java.sql.Connection;
import java.sql.DriverManager;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.mongodb.DB;
import com.mongodb.MongoClient;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
@Path("/datasources")
public class TestConnection {

	static private Logger logger = Logger.getLogger(TestConnection.class);

	@POST
	@Path("/test")
	@Produces(MediaType.APPLICATION_JSON)
	public String testDataSource(@javax.ws.rs.core.Context HttpServletRequest req) throws Exception {

		logger.debug("IN");

		String jndi = req.getParameter("JNDI_URL");
		String url = req.getParameter("CONNECTION_URL");
		String user = req.getParameter("USER");
		String pwd = req.getParameter("PASSWORD");
		String driver = req.getParameter("DRIVER");
		String schemaAttr = req.getParameter("CONNECTION_URL");

		IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);

		String schema = (String) profile.getUserAttribute(schemaAttr);
		logger.debug("schema:" + schema);
		Connection connection = null;

		try {
			if (jndi != null && jndi.length() > 0) {
				String jndiName = schema == null ? jndi : jndi + schema;
				logger.debug("Lookup JNDI name:" + jndiName);
				Context ctx = new InitialContext();
				DataSource ds = (DataSource) ctx.lookup(jndiName);
				connection = ds.getConnection();
			} else {

				if (driver.toLowerCase().contains("mongo")) {
					logger.debug("Checking the connection for MONGODB");
					MongoClient mongoClient = null;
					try {
						int databaseNameStart = url.lastIndexOf("/");
						if (databaseNameStart < 0) {
							logger.error("Error connecting to the mongoDB. No database selected");
						}
						String databaseUrl = url.substring(0, databaseNameStart);
						String databaseName = url.substring(databaseNameStart + 1);

						mongoClient = new MongoClient(databaseUrl);
						DB database = mongoClient.getDB(databaseName);
						database.getCollectionNames();

						logger.debug("Connection OK");
						return new JSONObject().toString();
					} catch (Exception e) {
						logger.error("Error connecting to the mongoDB", e);
					} finally {
						if (mongoClient != null) {
							mongoClient.close();
						}
					}
				} else {
					Class.forName(driver);
					connection = DriverManager.getConnection(url, user, pwd);
				}

			}
			if (connection != null) {// test ok
				logger.debug("Connection OK");
				return new JSONObject().toString();
			} else {
				JSONObject toReturn = new JSONObject();
				toReturn.put("error", "");
				return toReturn.toString();
			}
		} catch (Exception ex) {
			logger.error("Error testing datasources", ex);
			JSONObject toReturn = new JSONObject();
			toReturn.put("error", ex.getMessage());
			return toReturn.toString();
		}
	}

}
