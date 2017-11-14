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
package it.eng.spagobi.tools.dataset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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

import edu.emory.mathcs.backport.java.util.Arrays;
import it.eng.spagobi.UtilitiesForTest;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.UtilitiesDAOForTest;
import it.eng.spagobi.tenant.Tenant;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.tools.dataset.associativity.strategy.AssociativeStrategyFactory;
import it.eng.spagobi.tools.dataset.associativity.strategy.InnerAssociativityManager;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.cache.ICache;
import it.eng.spagobi.tools.dataset.cache.SpagoBICacheManager;
import it.eng.spagobi.tools.dataset.cache.query.item.InFilter;
import it.eng.spagobi.tools.dataset.cache.query.item.Projection;
import it.eng.spagobi.tools.dataset.cache.query.item.SimpleFilter;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.tools.dataset.graph.EdgeGroup;
import it.eng.spagobi.tools.dataset.graph.LabeledEdge;
import it.eng.spagobi.tools.dataset.graph.associativity.Config;
import it.eng.spagobi.tools.dataset.graph.associativity.utils.AssociativeLogicUtils;
import it.eng.spagobi.user.UserProfileManager;
import it.eng.spagobi.utilities.locks.DistributedLockFactory;

public class AssociativeLogicManagerTest {

	private static final String STORE = "store";
	private static final String PRODUCT = "product";
	private static final String SALES = "sales";
	private static final String CUSTOMER = "customer";

	private static final String Y = "Y";
	private static final String W = "W";
	private static final String X = "X";
	private static final String Z = "Z";
	private static final String K = "K";

	private static final String R4 = "R4";
	private static final String R1 = "R1";
	private static final String R5 = "R5";
	private static final String R6 = "R6";
	private static final String R3 = "R3";

	private static final String R4_COLUMN = "the_date";
	private static final String R1_COLUMN = "customer_id";
	private static final String R5_COLUMN = "occupation";
	private static final String R6_COLUMN = "city";
	private static final String R3_COLUMN = "product_id";

	private static final String A3 = "A3";
	private static final String A4 = "A4";
	private static final String A7 = "A7";
	private static final String A8 = "A8";

	private static final String A3_COLUMN = "store_id";
	private static final String A4_COLUMN = "product_id";
	private static final String STORE_A7_COLUMN = "store_city";
	private static final String CUSTOMER_A7_COLUMN = "city";
	private static final String STORE_A8_COLUMN = "store_country";
	private static final String CUSTOMER_A8_COLUMN = "country";

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

		loadDataSetInCache(STORE);
		loadDataSetInCache(SALES);
		loadDataSetInCache(PRODUCT);
		loadDataSetInCache(CUSTOMER);
		// loadDataSetInCache(K);
		// loadDataSetInCache(W);
		// loadDataSetInCache(X);
		// loadDataSetInCache(Y);
		// loadDataSetInCache(Z);
	}

	private static void setHazelcastDefaultConfig() {
		com.hazelcast.config.Config cfg = new com.hazelcast.config.Config();

		cfg.getNetworkConfig().setPort(5701);
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
		Pseudograph<String, LabeledEdge<String>> graph = new Pseudograph<String, LabeledEdge<String>>(
				new ClassBasedEdgeFactory<String, LabeledEdge<String>>((Class<LabeledEdge<String>>) (Object) LabeledEdge.class));
		graph.addVertex(STORE);
		graph.addVertex(SALES);
		LabeledEdge<String> labeledEdge = new LabeledEdge<String>(STORE, SALES, A3);
		graph.addEdge(STORE, SALES, labeledEdge);

		Map<String, Map<String, String>> datasetToAssociations = new HashMap<String, Map<String, String>>();
		Map<String, String> associationToColumns = new HashMap<String, String>();
		associationToColumns.put(A3, A3_COLUMN);
		datasetToAssociations.put(STORE, associationToColumns);
		datasetToAssociations.put(SALES, associationToColumns);

		List<SimpleFilter> selections = new ArrayList<>(1);
		IDataSet dataSet = dataSetDAO.loadDataSetByLabel(STORE);
		selections.add(new InFilter(new Projection(dataSet, "store_type"), Arrays.asList(("Small Grocery".split("")))));

		// TODO: Manage real time here!
		Set<String> realtimeDatasets = new HashSet<String>();
		Map<String, Map<String, String>> datasetParameters = new HashMap<String, Map<String, String>>();
		Set<String> documents = new HashSet<String>();

		Map<EdgeGroup, Set<String>> edgeGroupToValues = null;

		Config config = AssociativeLogicUtils.buildConfig(AssociativeStrategyFactory.INNER_STRATEGY, graph, datasetToAssociations, selections, realtimeDatasets,
				datasetParameters, documents);

		try {
			InnerAssociativityManager manager = new InnerAssociativityManager(config, UserProfileManager.getProfile());
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
		assertEquals(4, values.size());
		assertTrue(values.contains("('2')"));
		assertTrue(values.contains("('5')"));
		assertTrue(values.contains("('14')"));
		assertTrue(values.contains("('22')"));
	}

	@Test
	public void testProcessThreeDatasetsTwoSimpleAssociations() {
		Pseudograph<String, LabeledEdge<String>> graph = new Pseudograph<String, LabeledEdge<String>>(
				new ClassBasedEdgeFactory<String, LabeledEdge<String>>((Class<LabeledEdge<String>>) (Object) LabeledEdge.class));
		graph.addVertex(STORE);
		graph.addVertex(SALES);
		graph.addVertex(PRODUCT);
		LabeledEdge<String> labeledEdgeStoreSales = new LabeledEdge<String>(STORE, SALES, A3);
		graph.addEdge(STORE, SALES, labeledEdgeStoreSales);
		LabeledEdge<String> labeledEdgeSalesProduct = new LabeledEdge<String>(SALES, PRODUCT, A4);
		graph.addEdge(SALES, PRODUCT, labeledEdgeSalesProduct);

		Map<String, String> associationToColumnStore = new HashMap<String, String>();
		associationToColumnStore.put(A3, A3_COLUMN);
		Map<String, String> associationToColumnProduct = new HashMap<String, String>();
		associationToColumnProduct.put(A4, A4_COLUMN);
		Map<String, String> associationToColumnSales = new HashMap<String, String>();
		associationToColumnSales.put(A3, A3_COLUMN);
		associationToColumnSales.put(A4, A4_COLUMN);
		Map<String, Map<String, String>> datasetToAssociations = new HashMap<String, Map<String, String>>();
		datasetToAssociations.put(STORE, associationToColumnStore);
		datasetToAssociations.put(PRODUCT, associationToColumnProduct);
		datasetToAssociations.put(SALES, associationToColumnSales);

		List<SimpleFilter> selections = new ArrayList<>(1);
		IDataSet dataSet = dataSetDAO.loadDataSetByLabel(PRODUCT);
		selections.add(new InFilter(new Projection(dataSet, "brand_name"), "Queen"));

		// TODO: Manage real time here!
		Set<String> realtimeDatasets = new HashSet<String>();
		Map<String, Map<String, String>> datasetParameters = new HashMap<String, Map<String, String>>();
		Set<String> documents = new HashSet<String>();

		Map<EdgeGroup, Set<String>> edgeGroupToValues = null;

		Config config = AssociativeLogicUtils.buildConfig(AssociativeStrategyFactory.INNER_STRATEGY, graph, datasetToAssociations, selections, realtimeDatasets,
				datasetParameters, documents);

		try {
			InnerAssociativityManager manager = new InnerAssociativityManager(config, UserProfileManager.getProfile());
			edgeGroupToValues = manager.process().getEdgeGroupValues();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.toString());
		}

		assertNotNull(edgeGroupToValues);
		assertEquals(2, edgeGroupToValues.size());

		Set<LabeledEdge<String>> labeledEdges = new HashSet<LabeledEdge<String>>();
		labeledEdges.add(labeledEdgeSalesProduct);
		EdgeGroup edgeGroup = new EdgeGroup(labeledEdges);
		assertTrue(edgeGroupToValues.containsKey(edgeGroup));

		Set<String> values = edgeGroupToValues.get(edgeGroup);
		assertEquals(2, values.size());
		assertTrue(values.contains("('41')"));
		assertTrue(values.contains("('42')"));

		labeledEdges.clear();
		labeledEdges.add(labeledEdgeStoreSales);
		edgeGroup = new EdgeGroup(labeledEdges);
		assertTrue(edgeGroupToValues.containsKey(edgeGroup));

		values = edgeGroupToValues.get(edgeGroup);
		assertEquals(22, values.size());
		assertTrue(!values.contains("('0')"));
		assertTrue(!values.contains("('2')"));
		assertTrue(!values.contains("('22')"));
	}

	@Test
	public void testProcessTwoDatasetsOneComplexAssociation() {
		Pseudograph<String, LabeledEdge<String>> graph = new Pseudograph<String, LabeledEdge<String>>(
				new ClassBasedEdgeFactory<String, LabeledEdge<String>>((Class<LabeledEdge<String>>) (Object) LabeledEdge.class));
		graph.addVertex(STORE);
		graph.addVertex(CUSTOMER);
		LabeledEdge<String> labeledEdgeStoreCustomer1 = new LabeledEdge<String>(STORE, CUSTOMER, A7);
		graph.addEdge(STORE, CUSTOMER, labeledEdgeStoreCustomer1);
		LabeledEdge<String> labeledEdgeStoreCustomer2 = new LabeledEdge<String>(STORE, CUSTOMER, A8);
		graph.addEdge(STORE, CUSTOMER, labeledEdgeStoreCustomer2);

		Map<String, String> associationToColumnStore = new HashMap<String, String>();
		associationToColumnStore.put(A7, STORE_A7_COLUMN);
		associationToColumnStore.put(A8, STORE_A8_COLUMN);
		Map<String, String> associationToColumnCustomer = new HashMap<String, String>();
		associationToColumnCustomer.put(A7, CUSTOMER_A7_COLUMN);
		associationToColumnCustomer.put(A8, CUSTOMER_A8_COLUMN);
		Map<String, Map<String, String>> datasetToAssociations = new HashMap<String, Map<String, String>>();
		datasetToAssociations.put(STORE, associationToColumnStore);
		datasetToAssociations.put(CUSTOMER, associationToColumnCustomer);

		List<SimpleFilter> selections = new ArrayList<>(1);
		IDataSet dataSet = dataSetDAO.loadDataSetByLabel(CUSTOMER);
		selections.add(new InFilter(new Projection(dataSet, "gender"), "F"));

		// TODO: Manage real time here!
		Set<String> realtimeDatasets = new HashSet<String>();
		Map<String, Map<String, String>> datasetParameters = new HashMap<String, Map<String, String>>();
		Set<String> documents = new HashSet<String>();

		Map<EdgeGroup, Set<String>> edgeGroupToValues = null;

		Config config = AssociativeLogicUtils.buildConfig(AssociativeStrategyFactory.INNER_STRATEGY, graph, datasetToAssociations, selections, realtimeDatasets,
				datasetParameters, documents);

		try {
			InnerAssociativityManager manager = new InnerAssociativityManager(config, UserProfileManager.getProfile());
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
		assertEquals(23, values.size());
	}

	@Test
	public void testProcessFiveDatasetsTwoComplexAssociationsTwoSimpleAssociations() {
		Pseudograph<String, LabeledEdge<String>> graph = new Pseudograph<String, LabeledEdge<String>>(
				new ClassBasedEdgeFactory<String, LabeledEdge<String>>((Class<LabeledEdge<String>>) (Object) LabeledEdge.class));

		graph.addVertex(W);
		graph.addVertex(Y);
		graph.addVertex(X);
		graph.addVertex(Z);
		graph.addVertex(K);

		LabeledEdge<String> labeledEdgeWY = new LabeledEdge<String>(W, Y, R4);
		graph.addEdge(W, Y, labeledEdgeWY);

		LabeledEdge<String> labeledEdgeYX1 = new LabeledEdge<String>(Y, X, R1);
		graph.addEdge(Y, X, labeledEdgeYX1);
		LabeledEdge<String> labeledEdgeYX5 = new LabeledEdge<String>(Y, X, R5);
		graph.addEdge(Y, X, labeledEdgeYX5);

		LabeledEdge<String> labeledEdgeYZ1 = new LabeledEdge<String>(Y, Z, R1);
		graph.addEdge(Y, Z, labeledEdgeYZ1);
		LabeledEdge<String> labeledEdgeYZ5 = new LabeledEdge<String>(Y, Z, R5);
		graph.addEdge(Y, Z, labeledEdgeYZ5);

		LabeledEdge<String> labeledEdgeXZ1 = new LabeledEdge<String>(X, Z, R1);
		graph.addEdge(X, Z, labeledEdgeXZ1);
		LabeledEdge<String> labeledEdgeXZ5 = new LabeledEdge<String>(X, Z, R5);
		graph.addEdge(X, Z, labeledEdgeXZ5);
		LabeledEdge<String> labeledEdgeXZ6 = new LabeledEdge<String>(X, Z, R6);
		graph.addEdge(X, Z, labeledEdgeXZ6);

		LabeledEdge<String> labeledEdgeZK = new LabeledEdge<String>(Z, K, R3);
		graph.addEdge(Z, K, labeledEdgeZK);

		Map<String, String> associationToColumnW = new HashMap<String, String>();
		associationToColumnW.put(R4, R4_COLUMN);

		Map<String, String> associationToColumnY = new HashMap<String, String>();
		associationToColumnY.put(R4, R4_COLUMN);
		associationToColumnY.put(R1, R1_COLUMN);
		associationToColumnY.put(R5, R5_COLUMN);

		Map<String, String> associationToColumnX = new HashMap<String, String>();
		associationToColumnX.put(R1, R1_COLUMN);
		associationToColumnX.put(R5, R5_COLUMN);
		associationToColumnX.put(R6, R6_COLUMN);

		Map<String, String> associationToColumnZ = new HashMap<String, String>();
		associationToColumnZ.put(R1, R1_COLUMN);
		associationToColumnZ.put(R5, R5_COLUMN);
		associationToColumnZ.put(R6, R6_COLUMN);
		associationToColumnZ.put(R3, R3_COLUMN);

		Map<String, String> associationToColumnK = new HashMap<String, String>();
		associationToColumnK.put(R3, R3_COLUMN);

		Map<String, Map<String, String>> datasetToAssociations = new HashMap<String, Map<String, String>>();
		datasetToAssociations.put(W, associationToColumnW);
		datasetToAssociations.put(Y, associationToColumnY);
		datasetToAssociations.put(X, associationToColumnX);
		datasetToAssociations.put(Z, associationToColumnZ);
		datasetToAssociations.put(K, associationToColumnK);

		List<SimpleFilter> selections = new ArrayList<>(1);
		IDataSet dataSet = dataSetDAO.loadDataSetByLabel(Z);
		selections.add(new InFilter(new Projection(dataSet, "country"), "USA"));

		// TODO: Manage real time here!
		Set<String> realtimeDatasets = new HashSet<String>();
		Map<String, Map<String, String>> datasetParameters = new HashMap<String, Map<String, String>>();
		Set<String> documents = new HashSet<String>();

		Map<EdgeGroup, Set<String>> edgeGroupToValues = null;

		Config config = AssociativeLogicUtils.buildConfig(AssociativeStrategyFactory.INNER_STRATEGY, graph, datasetToAssociations, selections, realtimeDatasets,
				datasetParameters, documents);

		try {
			InnerAssociativityManager manager = new InnerAssociativityManager(config, UserProfileManager.getProfile());
			edgeGroupToValues = manager.process().getEdgeGroupValues();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.toString());
		}

		assertNotNull(edgeGroupToValues);
		assertEquals(4, edgeGroupToValues.size());

		Set<LabeledEdge<String>> labeledEdges = new HashSet<LabeledEdge<String>>();
		labeledEdges.add(labeledEdgeZK);
		EdgeGroup edgeGroup = new EdgeGroup(labeledEdges);
		assertTrue(edgeGroupToValues.containsKey(edgeGroup));
	}

	@AfterClass
	public static void tearDown() {
		deleteDataSetFromCache(STORE);
		deleteDataSetFromCache(SALES);
		deleteDataSetFromCache(PRODUCT);
		deleteDataSetFromCache(CUSTOMER);
		// deleteDataSetFromCache(K);
		// deleteDataSetFromCache(W);
		// deleteDataSetFromCache(X);
		// deleteDataSetFromCache(Y);
		// deleteDataSetFromCache(Z);
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
