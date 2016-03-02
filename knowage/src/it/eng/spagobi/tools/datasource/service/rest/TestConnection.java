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
package it.eng.spagobi.tools.datasource.service.rest;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.utilities.rest.RestUtilities;

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

	@POST
	@Path("/2.0/test")
	@Produces(MediaType.APPLICATION_JSON)
	public String testDataSourceNew(@javax.ws.rs.core.Context HttpServletRequest req) throws Exception {

		logger.debug("IN");

		JSONObject requestBodyJSON = RestUtilities.readBodyAsJSONObject(req);

		String url = requestBodyJSON.getString("urlConnection");
		String user = requestBodyJSON.getString("user");
		String pwd = requestBodyJSON.getString("pwd");
		String driver = requestBodyJSON.getString("driver");
		String schemaAttr = requestBodyJSON.getString("schemaAttribute");
		String jndi = requestBodyJSON.getString("jndi");

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
