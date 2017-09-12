package it.eng.spagobi.dataset.cache.impl.sqldbcache.test.perf;

import java.util.ArrayList;
import java.util.List;

import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spago.configuration.FileCreatorConfiguration;
import it.eng.spagobi.commons.utilities.UtilitiesDAOForTest;
import it.eng.spagobi.dataset.cache.impl.sqldbcache.DataType;
import it.eng.spagobi.dataset.cache.impl.sqldbcache.test.AbstractCacheTest;
import it.eng.spagobi.dataset.cache.test.TestConstants;
import it.eng.spagobi.tenant.Tenant;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.tools.dataset.bo.JDBCDataSet;
import it.eng.spagobi.tools.dataset.cache.CacheFactory;
import it.eng.spagobi.tools.dataset.cache.ICache;
import it.eng.spagobi.tools.dataset.cache.impl.sqldbcache.SQLDBCacheConfiguration;
import it.eng.spagobi.tools.datasource.bo.DataSource;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import junit.framework.TestCase;

import org.apache.log4j.Logger;

public class AbstractCachePerfTest extends TestCase {
	
	protected static List<ICache> caches = new ArrayList<ICache>();
	protected JDBCDataSet[] sqlDatasets;
	protected List<DataSource> dataSourceReadings;
	protected List<DataSource> dataSourceWritings;
	private static final String query = "select * from store";
	
	static private Logger logger = Logger.getLogger(AbstractCacheTest.class);

	/*
	 * (non-Javadoc)
	 *
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		System.setProperty("AF_CONFIG_FILE", TestConstants.AF_CONFIG_FILE);
		ConfigSingleton.setConfigurationCreation(new FileCreatorConfiguration(TestConstants.WEBCONTENT_PATH));

		TenantManager.setTenant(new Tenant("SPAGOBI"));
		
		UtilitiesDAOForTest.setUpDatabaseTestJNDI();
		createDataSources();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		for(int i=0; i<caches.size();i++){
			caches.get(i).deleteAll();
		}
	}



	/*
	 * Initialization Methods
	 */

	public void createDataSources() {
		// Must be overridden by specific implementation
		// dataSourceReading = TestDataSourceFactory.createDataSource(TestConstants.DatabaseType.MYSQL, false);
		// dataSourceWriting = TestDataSourceFactory.createDataSource(TestConstants.DatabaseType.MYSQL, true);
		logger.error("Specific DataSource must be specified in specialized Test");
	}




	public JDBCDataSet createJDBCDataset(String sqlQuery, IDataSource datasourceForReading) {

			JDBCDataSet sqlDataset = new JDBCDataSet();
			sqlDataset.setQuery(sqlQuery);
			sqlDataset.setQueryScript("");
			sqlDataset.setQueryScriptLanguage("");
			sqlDataset.setDataSource(datasourceForReading);
			sqlDataset.setLabel("test_jdbcDataset"+System.currentTimeMillis());
			return sqlDataset;

	}

	

}
