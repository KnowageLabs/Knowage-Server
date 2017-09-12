package it.eng.spagobi.dataset.cache.impl.sqldbcache.test.perf;

import it.eng.spagobi.dataset.cache.impl.sqldbcache.test.GenericSQLDBCachePersistTableTest;
import it.eng.spagobi.dataset.cache.test.TestXmlFactory;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.JDBCDataSet;
import it.eng.spagobi.tools.dataset.cache.CacheFactory;
import it.eng.spagobi.tools.dataset.cache.ICache;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.datasource.bo.IDataSource;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class CachePerfTest extends AbstractCachePerfTest {

	static private final String XML_FOLDER_PATH = "test/resources/dataset/files";
	static private Logger logger = Logger.getLogger(GenericSQLDBCachePersistTableTest.class);
	private List<String> datasetQueryes;
	StringBuffer buffer = new StringBuffer();
	
	
	private String xmlFileAbsolutePath;

	public CachePerfTest(String xmlFileAbsolutePath) {
		super();
		this.xmlFileAbsolutePath = xmlFileAbsolutePath;
		try {
			setUp();
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Parameters
	public static Collection data() {
		File xmlFolder = new File(XML_FOLDER_PATH);
		File[] xmlFiles = xmlFolder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith("Perf.xml".toLowerCase());
			}
		});
		Object[][] xmlFileAbsolutePaths = new Object[xmlFiles.length][1];
		for (int i = 0; i < xmlFiles.length; i++) {
			xmlFileAbsolutePaths[i][0] = xmlFiles[i].getAbsolutePath();
		}
		return Arrays.asList(xmlFileAbsolutePaths);
	}

	@Override
	public void createDataSources() {
		try {
			dataSourceReadings = TestXmlFactory.createDataSources(xmlFileAbsolutePath, false);
			dataSourceWritings = TestXmlFactory.createDataSources(xmlFileAbsolutePath, true);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}


	
	protected ICache getDefaultCacheConfiguration(IDataSource dataSourceWriting) {
		try {
			CacheFactory cacheFactory = new CacheFactory();
			return cacheFactory.getCache( TestXmlFactory.createCacheConfiguration(xmlFileAbsolutePath, dataSourceWriting));

		} catch (Exception e) {
			e.printStackTrace();
			fail();
			return null;
		}
	}

	@Test
	public void testJDBCDataSetFieldTypes() {

		
		for(int i=0; i<dataSourceReadings.size(); i++){
			for(int j=0; j<dataSourceWritings.size(); j++){
				testOneSourceDest(dataSourceReadings.get(i), dataSourceWritings.get(j));
			}
		}
		
		System.out.println(buffer);
	}
	
	private void testOneSourceDest(IDataSource reading, IDataSource writing){
		buffer.append("\n");
		buffer.append("----------------------------------------------");
		buffer.append("\n");
		buffer.append("Reading from "+reading.getDriver());
		buffer.append("   "+reading.getUrlConnection());
		buffer.append("\n");
		buffer.append("Writing into "+writing.getDriver());
		buffer.append("   "+writing.getUrlConnection());

		ICache aCacahe = this.getDefaultCacheConfiguration(writing);
		caches.add(aCacahe);
		
		datasetQueryes = new ArrayList<String>();
		datasetQueryes.add("select * from sales_fact_1998");
		
		List<String> tableNames = new ArrayList<String>();
		
		for(int i=0; i<datasetQueryes.size(); i++){
			buffer.append("\n");
			buffer.append("-----QUERY------");
			buffer.append("\n");
			buffer.append(datasetQueryes.get(i));
			buffer.append("\n");
			
			IDataSet ds = createJDBCDataset(datasetQueryes.get(i), reading);
			ds.loadData();
			IDataStore dataStore = ds.getDataStore();
			long start = System.currentTimeMillis();
			long persistTime = aCacahe.put(ds, dataStore);
			dataStore = aCacahe.get(ds.getSignature());
			long end = System.currentTimeMillis();
			tableNames.add(aCacahe.getMetadata().getCacheItem(ds.getSignature()).getTable());
			
			buffer.append("PUT: " + (end-start));
			buffer.append("\n");
			buffer.append("JUST PERSIST: " + persistTime);
			buffer.append("\n");

		}
		long start = System.currentTimeMillis();
		//build a test query 
		IDataSet ds = createJDBCDataset("select * from "+tableNames.get(0), reading);
		ds.loadData();
		ds = createJDBCDataset("select * from "+tableNames.get(1), reading);
		ds.loadData();
		ds = createJDBCDataset("select * from "+tableNames.get(2), reading);
		ds.loadData();
		ds = createJDBCDataset("select * from "+tableNames.get(0)+" sal join "+tableNames.get(1)+" prod on (sal.product_id = prod.product_id) join "+tableNames.get(2)+" cust on (sal.customer_id = cust.customer_id)", reading);
		ds.loadData();
		long end = System.currentTimeMillis();
		buffer.append("QUERY: " + end);
		buffer.append("\n");
		aCacahe.deleteAll();
	}


}
