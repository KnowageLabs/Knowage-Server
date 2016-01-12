package it.eng.spagobi.api.v2;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.tools.datasource.bo.DataSource;
import it.eng.spagobi.tools.datasource.dao.IDataSourceDAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.mongodb.DB;
import com.mongodb.MongoClient;

@Path("/2.0/datasources")
@ManageAuthorization
public class TestConnectionResource extends AbstractSpagoBIResource {

	static protected Logger logger = Logger.getLogger(DataSourceResource.class);

	IDataSourceDAO dataSourceDAO;
	DataSource dataSource;
	List<DataSource> dataSourceList;

	@POST
	@Path("/test")
	@Consumes(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.DATASOURCE_MANAGEMENT })
	public String postDataSource(@Valid DataSource dataSource) throws Exception {

		logger.debug("IN");

		String url = dataSource.getUrlConnection();
		String user = dataSource.getUser();
		String pwd = dataSource.getPwd();
		String driver = dataSource.getDriver();
		String schemaAttr = dataSource.getSchemaAttribute();
		String jndi = dataSource.getJndi();

		IEngUserProfile profile = getUserProfile();

		String schema = (String) profile.getUserAttribute(schemaAttr);
		logger.debug("schema:" + schema);
		Connection connection = null;

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

		return new JSONObject().toString();

	}

}
