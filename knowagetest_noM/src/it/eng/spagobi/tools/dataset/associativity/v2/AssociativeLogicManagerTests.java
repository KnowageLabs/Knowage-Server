package it.eng.spagobi.tools.dataset.associativity.v2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import it.eng.knowage.tools.dataset.associativity.strategy.OuterAssociativityManager;
import it.eng.spagobi.UtilitiesForTest;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.UtilitiesDAOForTest;
import it.eng.spagobi.tenant.Tenant;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.tools.dataset.associativity.strategy.AssociativeStrategyFactory;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.cache.ICache;
import it.eng.spagobi.tools.dataset.cache.SpagoBICacheManager;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.tools.dataset.graph.EdgeGroup;
import it.eng.spagobi.tools.dataset.graph.LabeledEdge;
import it.eng.spagobi.tools.dataset.graph.associativity.Config;
import it.eng.spagobi.tools.dataset.graph.associativity.utils.AssociativeLogicUtils;
import it.eng.spagobi.user.UserProfileManager;
import it.eng.spagobi.utilities.locks.DistributedLockFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.Pseudograph;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hazelcast.config.MulticastConfig;
import com.hazelcast.config.TcpIpConfig;

/*
 * For class under test please use:
 * [NameOfTheClassUnderTestTests]
 *
 * For unit under test please use:
 * [MethodName__StateUnderTest__ExpectedBehavior]
 */

public class AssociativeLogicManagerTests {

	private static final String DATASET_FOODMART_STORE = "store";
	private static final String DATASET_FOODMART_PRODUCT = "product";
	private static final String DATASET_FOODMART_PRODUCTCLASS = "class";
	private static final String DATASET_FOODMART_CUSTOMER = "customer";

	private static final String COLUMN_CUSTOMER_ID = "customer_id";
	private static final String COLUMN_PRODUCT_ID = "product_id";
	private static final String COLUMN_PRODUCT_CLASS_ID = "product_class_id";
	private static final String COLUMN_STORE_CITY = "store_city";
	private static final String COLUMN_CITY = "city";
	private static final String COLUMN_STORE_COUNTRY = "store_country";
	private static final String COLUMN_COUNTRY = "country";

	private static ICache cache;
	private static IDataSetDAO dataSetDAO;

	@BeforeClass
	public static void setUp() {
		try {
			UtilitiesForTest.setUpMasterConfiguration();
			UtilitiesDAOForTest.setUpDatabaseTestJNDI();
			TenantManager.setTenant(new Tenant("DEFAULT_TENANT"));
			UserProfileManager.setProfile(new UserProfile("biadmin", "DEFAULT_TENANT"));
			setHazelcastDefaultConfig();
			cache = SpagoBICacheManager.getCache();
			dataSetDAO = DAOFactory.getDataSetDAO();
		} catch (Exception e) {
			fail(e.toString());
		}

		loadDataSetInCache(DATASET_FOODMART_STORE);
		loadDataSetInCache(DATASET_FOODMART_PRODUCTCLASS);
		loadDataSetInCache(DATASET_FOODMART_PRODUCT);
		loadDataSetInCache(DATASET_FOODMART_CUSTOMER);
	}

	private static void setHazelcastDefaultConfig() {
		com.hazelcast.config.Config cfg = new com.hazelcast.config.Config();

		cfg.getNetworkConfig().setPort(5702);
		cfg.getNetworkConfig().setPortAutoIncrement(true);
		cfg.getNetworkConfig().setPortCount(100);
		MulticastConfig multicastConfig = new MulticastConfig();
		multicastConfig.setEnabled(false);
		cfg.getNetworkConfig().getJoin().setMulticastConfig(multicastConfig);

		TcpIpConfig tcpIpConfig = new TcpIpConfig();
		tcpIpConfig.setEnabled(true);
		List<String> members = new ArrayList<String>();
		members.add("127.0.0.1");
		tcpIpConfig.setMembers(members);
		cfg.getNetworkConfig().getJoin().setTcpIpConfig(tcpIpConfig);

		cfg.setProperty("hazelcast.socket.bind.any", "false");
		cfg.setProperty("hazelcast.logging.type", "log4j");

		DistributedLockFactory.setDefaultConfig(cfg);
	}

	@Test
	public void testProcessTwoDatasetsOneSimpleAssociation() {
		Pseudograph<String, LabeledEdge<String>> graph = new Pseudograph<String, LabeledEdge<String>>(new ClassBasedEdgeFactory<String, LabeledEdge<String>>(
				(Class<LabeledEdge<String>>) (Object) LabeledEdge.class));
		graph.addVertex(DATASET_FOODMART_STORE);
		graph.addVertex(DATASET_FOODMART_CUSTOMER);
		LabeledEdge<String> labeledEdge = new LabeledEdge<String>(DATASET_FOODMART_STORE, DATASET_FOODMART_CUSTOMER, "A1");
		graph.addEdge(DATASET_FOODMART_STORE, DATASET_FOODMART_CUSTOMER, labeledEdge);

		Map<String, Map<String, String>> datasetToAssociations = new HashMap<String, Map<String, String>>();
		Map<String, String> associationToColumns = new HashMap<String, String>(1);
		associationToColumns.put("A1", COLUMN_STORE_CITY);
		datasetToAssociations.put(DATASET_FOODMART_STORE, associationToColumns);
		associationToColumns = new HashMap<String, String>(1);
		associationToColumns.put("A1", COLUMN_CITY);
		datasetToAssociations.put(DATASET_FOODMART_CUSTOMER, associationToColumns);

		Map<String, String> selections = new HashMap<String, String>(1);
		selections.put(DATASET_FOODMART_CUSTOMER, "customer_id = '4'");

		Set<String> realtimeDatasets = new HashSet<String>(0);
		Map<String, Map<String, String>> datasetParameters = new HashMap<String, Map<String, String>>();
		Set<String> documents = new HashSet<String>(0);

		Map<EdgeGroup, Set<String>> edgeGroupToValues = null;

		Config config = AssociativeLogicUtils.buildConfig(AssociativeStrategyFactory.OUTER_STRATEGY, graph, datasetToAssociations, selections,
				realtimeDatasets, datasetParameters, documents);
		try {
			OuterAssociativityManager manager = new OuterAssociativityManager(config, UserProfileManager.getProfile());
			edgeGroupToValues = manager.process().getEdgeGroupValues();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.toString());
		}

		assertNotNull(edgeGroupToValues);

		Set<LabeledEdge<String>> labeledEdges = new HashSet<LabeledEdge<String>>();
		labeledEdges.add(labeledEdge);
		EdgeGroup edgeGroup = new EdgeGroup(labeledEdges);
		assertTrue(edgeGroupToValues.containsKey(edgeGroup));

		Set<String> values = edgeGroupToValues.get(edgeGroup);
		assertEquals(1, values.size());
		assertTrue(values.contains("('Burnaby')"));
	}

	@Test
	public void testProcessThreeDatasetsTwoSimpleAssociations() {
		Pseudograph<String, LabeledEdge<String>> graph = new Pseudograph<String, LabeledEdge<String>>(new ClassBasedEdgeFactory<String, LabeledEdge<String>>(
				(Class<LabeledEdge<String>>) (Object) LabeledEdge.class));
		graph.addVertex(DATASET_FOODMART_STORE);
		graph.addVertex(DATASET_FOODMART_CUSTOMER);
		graph.addVertex(DATASET_FOODMART_PRODUCT);
		LabeledEdge<String> labeledEdgeStoreCustomer = new LabeledEdge<String>(DATASET_FOODMART_STORE, DATASET_FOODMART_CUSTOMER, "A1");
		graph.addEdge(DATASET_FOODMART_STORE, DATASET_FOODMART_CUSTOMER, labeledEdgeStoreCustomer);
		LabeledEdge<String> labeledEdgeCustomerProduct = new LabeledEdge<String>(DATASET_FOODMART_CUSTOMER, DATASET_FOODMART_PRODUCT, "A2");
		graph.addEdge(DATASET_FOODMART_CUSTOMER, DATASET_FOODMART_PRODUCT, labeledEdgeCustomerProduct);

		Map<String, String> associationToColumnStore = new HashMap<String, String>();
		associationToColumnStore.put("A1", COLUMN_STORE_CITY);
		Map<String, String> associationToColumnProduct = new HashMap<String, String>();
		associationToColumnProduct.put("A2", COLUMN_PRODUCT_ID);
		Map<String, String> associationToColumnCustomer = new HashMap<String, String>();
		associationToColumnCustomer.put("A1", COLUMN_CITY);
		associationToColumnCustomer.put("A2", COLUMN_CUSTOMER_ID);
		Map<String, Map<String, String>> datasetToAssociations = new HashMap<String, Map<String, String>>();
		datasetToAssociations.put(DATASET_FOODMART_STORE, associationToColumnStore);
		datasetToAssociations.put(DATASET_FOODMART_PRODUCT, associationToColumnProduct);
		datasetToAssociations.put(DATASET_FOODMART_CUSTOMER, associationToColumnCustomer);

		Map<String, String> selections = new HashMap<String, String>();
		selections.put(DATASET_FOODMART_CUSTOMER, "account_num IN ('87500482201','17993524670')");

		Set<String> realtimeDatasets = new HashSet<String>();
		Map<String, Map<String, String>> datasetParameters = new HashMap<String, Map<String, String>>();
		Set<String> documents = new HashSet<String>();

		Map<EdgeGroup, Set<String>> edgeGroupToValues = null;

		Config config = AssociativeLogicUtils.buildConfig(AssociativeStrategyFactory.OUTER_STRATEGY, graph, datasetToAssociations, selections,
				realtimeDatasets, datasetParameters, documents);

		try {
			OuterAssociativityManager manager = new OuterAssociativityManager(config, UserProfileManager.getProfile());
			edgeGroupToValues = manager.process().getEdgeGroupValues();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.toString());
		}

		assertNotNull(edgeGroupToValues);
		assertEquals(2, edgeGroupToValues.size());

		Set<LabeledEdge<String>> labeledEdges = new HashSet<LabeledEdge<String>>();

		labeledEdges.add(labeledEdgeCustomerProduct);
		EdgeGroup edgeGroup = new EdgeGroup(labeledEdges);
		assertTrue(edgeGroupToValues.containsKey(edgeGroup));
		Set<String> values = edgeGroupToValues.get(edgeGroup);
		assertEquals(2, values.size());
		assertTrue(values.contains("('4')"));
		assertTrue(values.contains("('2163')"));
		labeledEdges.clear();

		labeledEdges.add(labeledEdgeStoreCustomer);
		edgeGroup = new EdgeGroup(labeledEdges);
		assertTrue(edgeGroupToValues.containsKey(edgeGroup));
		values = edgeGroupToValues.get(edgeGroup);
		assertTrue(values.contains("('Burnaby')"));
	}

	@Test
	public void testProcessThreeDatasetsTwoSimpleAssociationsTwoSelections() {
		Pseudograph<String, LabeledEdge<String>> graph = new Pseudograph<String, LabeledEdge<String>>(new ClassBasedEdgeFactory<String, LabeledEdge<String>>(
				(Class<LabeledEdge<String>>) (Object) LabeledEdge.class));
		graph.addVertex(DATASET_FOODMART_STORE);
		graph.addVertex(DATASET_FOODMART_CUSTOMER);
		graph.addVertex(DATASET_FOODMART_PRODUCT);
		LabeledEdge<String> labeledEdgeStoreCustomer = new LabeledEdge<String>(DATASET_FOODMART_STORE, DATASET_FOODMART_CUSTOMER, "A1");
		graph.addEdge(DATASET_FOODMART_STORE, DATASET_FOODMART_CUSTOMER, labeledEdgeStoreCustomer);
		LabeledEdge<String> labeledEdgeCustomerProduct = new LabeledEdge<String>(DATASET_FOODMART_CUSTOMER, DATASET_FOODMART_PRODUCT, "A2");
		graph.addEdge(DATASET_FOODMART_CUSTOMER, DATASET_FOODMART_PRODUCT, labeledEdgeCustomerProduct);

		Map<String, String> associationToColumnStore = new HashMap<String, String>();
		associationToColumnStore.put("A1", COLUMN_STORE_CITY);
		Map<String, String> associationToColumnProduct = new HashMap<String, String>();
		associationToColumnProduct.put("A2", COLUMN_PRODUCT_ID);
		Map<String, String> associationToColumnCustomer = new HashMap<String, String>();
		associationToColumnCustomer.put("A1", COLUMN_CITY);
		associationToColumnCustomer.put("A2", COLUMN_CUSTOMER_ID);
		Map<String, Map<String, String>> datasetToAssociations = new HashMap<String, Map<String, String>>();
		datasetToAssociations.put(DATASET_FOODMART_STORE, associationToColumnStore);
		datasetToAssociations.put(DATASET_FOODMART_PRODUCT, associationToColumnProduct);
		datasetToAssociations.put(DATASET_FOODMART_CUSTOMER, associationToColumnCustomer);

		Map<String, String> selections = new LinkedHashMap<String, String>();
		selections.put(DATASET_FOODMART_CUSTOMER, "account_num IN ('87500482201','17993524670')");
		selections.put(DATASET_FOODMART_PRODUCT, "product_id = '4'");

		Set<String> realtimeDatasets = new HashSet<String>();
		Map<String, Map<String, String>> datasetParameters = new HashMap<String, Map<String, String>>();
		Set<String> documents = new HashSet<String>();

		Map<EdgeGroup, Set<String>> edgeGroupToValues = null;

		Config config = AssociativeLogicUtils.buildConfig(AssociativeStrategyFactory.OUTER_STRATEGY, graph, datasetToAssociations, selections,
				realtimeDatasets, datasetParameters, documents);

		try {
			OuterAssociativityManager manager = new OuterAssociativityManager(config, UserProfileManager.getProfile());
			edgeGroupToValues = manager.process().getEdgeGroupValues();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.toString());
		}

		assertNotNull(edgeGroupToValues);
		assertEquals(2, edgeGroupToValues.size());

		Set<LabeledEdge<String>> labeledEdges = new HashSet<LabeledEdge<String>>();

		labeledEdges.add(labeledEdgeCustomerProduct);
		EdgeGroup edgeGroup = new EdgeGroup(labeledEdges);
		assertTrue(edgeGroupToValues.containsKey(edgeGroup));
		Set<String> values = edgeGroupToValues.get(edgeGroup);
		assertEquals(1, values.size());
		assertTrue(values.contains("('4')"));
		assertTrue(!values.contains("('2163')"));
		labeledEdges.clear();

		labeledEdges.add(labeledEdgeStoreCustomer);
		edgeGroup = new EdgeGroup(labeledEdges);
		assertTrue(edgeGroupToValues.containsKey(edgeGroup));
		values = edgeGroupToValues.get(edgeGroup);
		assertTrue(values.contains("('Burnaby')"));
	}

	@Test
	public void testProcessFourDatasetsThreeSimpleAssociations() {
		Pseudograph<String, LabeledEdge<String>> graph = new Pseudograph<String, LabeledEdge<String>>(new ClassBasedEdgeFactory<String, LabeledEdge<String>>(
				(Class<LabeledEdge<String>>) (Object) LabeledEdge.class));
		graph.addVertex(DATASET_FOODMART_STORE);
		graph.addVertex(DATASET_FOODMART_CUSTOMER);
		graph.addVertex(DATASET_FOODMART_PRODUCT);
		graph.addVertex(DATASET_FOODMART_PRODUCTCLASS);
		LabeledEdge<String> labeledEdgeStoreCustomer = new LabeledEdge<String>(DATASET_FOODMART_STORE, DATASET_FOODMART_CUSTOMER, "A1");
		graph.addEdge(DATASET_FOODMART_STORE, DATASET_FOODMART_CUSTOMER, labeledEdgeStoreCustomer);
		LabeledEdge<String> labeledEdgeCustomerProduct = new LabeledEdge<String>(DATASET_FOODMART_CUSTOMER, DATASET_FOODMART_PRODUCT, "A2");
		graph.addEdge(DATASET_FOODMART_CUSTOMER, DATASET_FOODMART_PRODUCT, labeledEdgeCustomerProduct);
		LabeledEdge<String> labeledEdgeProductProductClass = new LabeledEdge<String>(DATASET_FOODMART_PRODUCT, DATASET_FOODMART_PRODUCTCLASS, "A3");
		graph.addEdge(DATASET_FOODMART_PRODUCT, DATASET_FOODMART_PRODUCTCLASS, labeledEdgeProductProductClass);

		Map<String, String> associationToColumnStore = new HashMap<String, String>();
		associationToColumnStore.put("A1", COLUMN_STORE_CITY);
		Map<String, String> associationToColumnProductClass = new HashMap<String, String>();
		associationToColumnProductClass.put("A3", COLUMN_PRODUCT_CLASS_ID);
		Map<String, String> associationToColumnProduct = new HashMap<String, String>();
		associationToColumnProduct.put("A2", COLUMN_PRODUCT_ID);
		associationToColumnProduct.put("A3", COLUMN_PRODUCT_CLASS_ID);
		Map<String, String> associationToColumnCustomer = new HashMap<String, String>();
		associationToColumnCustomer.put("A1", COLUMN_CITY);
		associationToColumnCustomer.put("A2", COLUMN_CUSTOMER_ID);
		Map<String, Map<String, String>> datasetToAssociations = new HashMap<String, Map<String, String>>();
		datasetToAssociations.put(DATASET_FOODMART_STORE, associationToColumnStore);
		datasetToAssociations.put(DATASET_FOODMART_PRODUCT, associationToColumnProduct);
		datasetToAssociations.put(DATASET_FOODMART_CUSTOMER, associationToColumnCustomer);
		datasetToAssociations.put(DATASET_FOODMART_PRODUCTCLASS, associationToColumnProductClass);

		Map<String, String> selections = new HashMap<String, String>();
		selections.put(DATASET_FOODMART_CUSTOMER, "account_num = '17993524670'");

		Set<String> realtimeDatasets = new HashSet<String>();
		Map<String, Map<String, String>> datasetParameters = new HashMap<String, Map<String, String>>();
		Set<String> documents = new HashSet<String>();

		Map<EdgeGroup, Set<String>> edgeGroupToValues = null;

		Config config = AssociativeLogicUtils.buildConfig(AssociativeStrategyFactory.OUTER_STRATEGY, graph, datasetToAssociations, selections,
				realtimeDatasets, datasetParameters, documents);

		try {
			OuterAssociativityManager manager = new OuterAssociativityManager(config, UserProfileManager.getProfile());
			edgeGroupToValues = manager.process().getEdgeGroupValues();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.toString());
		}

		assertNotNull(edgeGroupToValues);
		assertEquals(3, edgeGroupToValues.size());

		Set<LabeledEdge<String>> labeledEdges = new HashSet<LabeledEdge<String>>();

		labeledEdges.add(labeledEdgeCustomerProduct);
		EdgeGroup edgeGroup = new EdgeGroup(labeledEdges);
		assertTrue(edgeGroupToValues.containsKey(edgeGroup));
		Set<String> values = edgeGroupToValues.get(edgeGroup);
		assertEquals(1, values.size());
		assertTrue(values.contains("('2163')"));
		labeledEdges.clear();

		labeledEdges.add(labeledEdgeProductProductClass);
		edgeGroup = new EdgeGroup(labeledEdges);
		assertTrue(edgeGroupToValues.containsKey(edgeGroup));
		values = edgeGroupToValues.get(edgeGroup);
		assertTrue(values.isEmpty());
		labeledEdges.clear();

		labeledEdges.add(labeledEdgeStoreCustomer);
		edgeGroup = new EdgeGroup(labeledEdges);
		assertTrue(edgeGroupToValues.containsKey(edgeGroup));
		values = edgeGroupToValues.get(edgeGroup);
		assertTrue(values.contains("('Burnaby')"));
		labeledEdges.clear();
	}

	@Test
	public void testProcessTwoDatasetsOneComplexAssociation() {
		Pseudograph<String, LabeledEdge<String>> graph = new Pseudograph<String, LabeledEdge<String>>(new ClassBasedEdgeFactory<String, LabeledEdge<String>>(
				(Class<LabeledEdge<String>>) (Object) LabeledEdge.class));
		graph.addVertex(DATASET_FOODMART_STORE);
		graph.addVertex(DATASET_FOODMART_CUSTOMER);
		LabeledEdge<String> labeledEdgeStoreCustomer1 = new LabeledEdge<String>(DATASET_FOODMART_STORE, DATASET_FOODMART_CUSTOMER, "A1");
		graph.addEdge(DATASET_FOODMART_STORE, DATASET_FOODMART_CUSTOMER, labeledEdgeStoreCustomer1);
		LabeledEdge<String> labeledEdgeStoreCustomer2 = new LabeledEdge<String>(DATASET_FOODMART_STORE, DATASET_FOODMART_CUSTOMER, "A2");
		graph.addEdge(DATASET_FOODMART_STORE, DATASET_FOODMART_CUSTOMER, labeledEdgeStoreCustomer2);

		Map<String, String> associationToColumnStore = new HashMap<String, String>();
		associationToColumnStore.put("A1", COLUMN_STORE_COUNTRY);
		associationToColumnStore.put("A2", COLUMN_STORE_CITY);
		Map<String, String> associationToColumnCustomer = new HashMap<String, String>();
		associationToColumnCustomer.put("A1", COLUMN_COUNTRY);
		associationToColumnCustomer.put("A2", COLUMN_CITY);
		Map<String, Map<String, String>> datasetToAssociations = new HashMap<String, Map<String, String>>();
		datasetToAssociations.put(DATASET_FOODMART_STORE, associationToColumnStore);
		datasetToAssociations.put(DATASET_FOODMART_CUSTOMER, associationToColumnCustomer);

		Map<String, String> selections = new HashMap<String, String>();
		selections.put(DATASET_FOODMART_CUSTOMER, "gender = 'F'");

		Set<String> realtimeDatasets = new HashSet<String>();
		Map<String, Map<String, String>> datasetParameters = new HashMap<String, Map<String, String>>();
		Set<String> documents = new HashSet<String>();

		Map<EdgeGroup, Set<String>> edgeGroupToValues = null;

		Config config = AssociativeLogicUtils.buildConfig(AssociativeStrategyFactory.OUTER_STRATEGY, graph, datasetToAssociations, selections,
				realtimeDatasets, datasetParameters, documents);

		try {
			OuterAssociativityManager manager = new OuterAssociativityManager(config, UserProfileManager.getProfile());
			edgeGroupToValues = manager.process().getEdgeGroupValues();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.toString());
		}

		assertNotNull(edgeGroupToValues);
		assertEquals(1, edgeGroupToValues.size());

		Set<LabeledEdge<String>> labeledEdges = new HashSet<LabeledEdge<String>>();
		labeledEdges.add(labeledEdgeStoreCustomer1);
		labeledEdges.add(labeledEdgeStoreCustomer2);
		EdgeGroup edgeGroup = new EdgeGroup(labeledEdges);
		assertTrue(edgeGroupToValues.containsKey(edgeGroup));

		Set<String> values = edgeGroupToValues.get(edgeGroup);
		assertEquals(109, values.size());
	}

	@AfterClass
	public static void tearDown() {
		deleteDataSetFromCache(DATASET_FOODMART_STORE);
		deleteDataSetFromCache(DATASET_FOODMART_PRODUCTCLASS);
		deleteDataSetFromCache(DATASET_FOODMART_PRODUCT);
		deleteDataSetFromCache(DATASET_FOODMART_CUSTOMER);
	}

	private static void loadDataSetInCache(String dataSetLabel) {
		IDataSet dataSet = dataSetDAO.loadDataSetByLabel(dataSetLabel);
		if (!cache.contains(dataSet)) {
			dataSet.loadData();
			cache.put(dataSet, dataSet.getDataStore());
		}
	}

	private static void deleteDataSetFromCache(String dataSetLabel) {
		IDataSet dataSet = dataSetDAO.loadDataSetByLabel(dataSetLabel);
		cache.delete(dataSet);
	}
}
