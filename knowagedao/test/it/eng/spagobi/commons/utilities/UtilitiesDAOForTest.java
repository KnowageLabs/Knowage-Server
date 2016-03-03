package it.eng.spagobi.commons.utilities;

import it.eng.spagobi.commons.dao.DAOConfig;
import it.eng.spagobi.utilities.MockContext;
import it.eng.spagobi.utilities.MockFactory;

import java.io.File;

import javax.naming.Context;

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;

public class UtilitiesDAOForTest {

	public static void setUpDatabaseTestJNDI() throws Exception {
		DAOConfig.setHibernateConfigurationFileFile(new File("../knowage/src/hibernate.cfg.xml"));

		// Create initial context
		System.setProperty(Context.INITIAL_CONTEXT_FACTORY, MockFactory.class.getName());

		// Construct DataSource
		MysqlConnectionPoolDataSource ds = new MysqlConnectionPoolDataSource();
		ds.setURL("jdbc:mysql://localhost/athenatest");
		ds.setUser("root");
		ds.setPassword("");

		Context ic = new MockContext();
		MockFactory.context = ic;
		ic.bind("java:/comp/env/jdbc/knowage", ds);
	}
}
