package it.eng.spagobi.tools.dataset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.UtilitiesForTest;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.UtilitiesDAOForTest;
import it.eng.spagobi.tenant.Tenant;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.cache.ICache;
import it.eng.spagobi.tools.dataset.cache.SpagoBICacheManager;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.tools.dataset.graph.EdgeGroup;
import it.eng.spagobi.tools.dataset.graph.LabeledEdge;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.Pseudograph;
import org.junit.Before;
import org.junit.Test;

public class AssociativeLogicManagerTest {

	private static final String K = "store";
	private static final String W = "product";
	private static final String X = "sales";
	private static final String Y = "sales";
	private static final String Z = "sales";

	private static final String A1 = "A1";
	private static final String A3 = "A3";
	private static final String A4 = "A4";
	private static final String A5 = "A5";
	private static final String A6 = "A6";

	private static final String A1_COLUMN = "time_id";
	private static final String A3_COLUMN = "store_id";
	private static final String A4_COLUMN = "product_id";
	private static final String A5_COLUMN = "customer_id";
	private static final String A6_COLUMN = "promotion_id";

	@Before
	public void setUp() throws Exception {
		UtilitiesForTest.setUpMasterConfiguration();
		UtilitiesDAOForTest.setUpDatabaseTestJNDI();
		TenantManager.setTenant(new Tenant("SPAGOBI"));

		ICache cache = SpagoBICacheManager.getCache();
		try {
			IDataSetDAO dataSetDAO = DAOFactory.getDataSetDAO();
			checkDataSetInCache(cache, dataSetDAO, K);
			checkDataSetInCache(cache, dataSetDAO, W);
			checkDataSetInCache(cache, dataSetDAO, Y);
		} catch (EMFUserError e) {
			fail(e.toString());
		}
	}

	private void checkDataSetInCache(ICache cache, IDataSetDAO dataSetDAO, String dataSetLabel) {
		IDataSet dataSet;
		dataSet = dataSetDAO.loadDataSetByLabel(dataSetLabel);
		if (!cache.contains(dataSet)) {
			dataSet.loadData();
			cache.put(dataSet, dataSet.getDataStore());
		}
	}

	@Test
	public void testProcessOneAssociation() {
		Pseudograph<String, LabeledEdge<String>> graph = new Pseudograph<String, LabeledEdge<String>>(new ClassBasedEdgeFactory<String, LabeledEdge<String>>(
				(Class<LabeledEdge<String>>) (Object) LabeledEdge.class));
		graph.addVertex(K);
		graph.addVertex(X);
		LabeledEdge<String> labeledEdge = new LabeledEdge<String>(K, X, A3);
		graph.addEdge(K, X, labeledEdge);

		Map<String, Map<String, String>> datasetToAssociations = new HashMap<String, Map<String, String>>();
		Map<String, String> associationToColumns = new HashMap<String, String>();
		associationToColumns.put(A3, A3_COLUMN);
		datasetToAssociations.put(K, associationToColumns);
		datasetToAssociations.put(X, associationToColumns);

		AssociativeLogicManager manager = new AssociativeLogicManager(graph, datasetToAssociations, K, "store_type = 'Small Grocery'");
		Map<EdgeGroup, Set<String>> edgeGroupToValues = null;
		try {
			edgeGroupToValues = manager.process();
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
	public void testProcessTwoAssociations() {
		Pseudograph<String, LabeledEdge<String>> graph = new Pseudograph<String, LabeledEdge<String>>(new ClassBasedEdgeFactory<String, LabeledEdge<String>>(
				(Class<LabeledEdge<String>>) (Object) LabeledEdge.class));
		graph.addVertex(K);
		graph.addVertex(X);
		graph.addVertex(W);
		LabeledEdge<String> labeledEdgeKX = new LabeledEdge<String>(K, X, A3);
		graph.addEdge(K, X, labeledEdgeKX);
		LabeledEdge<String> labeledEdgeXW = new LabeledEdge<String>(X, W, A4);
		graph.addEdge(X, W, labeledEdgeXW);

		Map<String, String> associationToColumnK = new HashMap<String, String>();
		associationToColumnK.put(A3, A3_COLUMN);
		Map<String, String> associationToColumnW = new HashMap<String, String>();
		associationToColumnW.put(A4, A4_COLUMN);
		Map<String, String> associationToColumnX = new HashMap<String, String>();
		associationToColumnX.put(A3, A3_COLUMN);
		associationToColumnX.put(A4, A4_COLUMN);
		Map<String, Map<String, String>> datasetToAssociations = new HashMap<String, Map<String, String>>();
		datasetToAssociations.put(K, associationToColumnK);
		datasetToAssociations.put(W, associationToColumnW);
		datasetToAssociations.put(X, associationToColumnX);

		AssociativeLogicManager manager = new AssociativeLogicManager(graph, datasetToAssociations, W, "brand_name= 'Queen'");
		Map<EdgeGroup, Set<String>> edgeGroupToValues = null;
		try {
			edgeGroupToValues = manager.process();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.toString());
		}

		assertNotNull(edgeGroupToValues);

		Set<LabeledEdge<String>> labeledEdges = new HashSet<LabeledEdge<String>>();
		labeledEdges.add(labeledEdgeKX);
		EdgeGroup edgeGroup = new EdgeGroup(labeledEdges);
		assertTrue(edgeGroupToValues.containsKey(edgeGroup));

		Set<String> values = edgeGroupToValues.get(edgeGroup);
		assertEquals(4, values.size());
		assertTrue(values.contains("('2')"));
		assertTrue(values.contains("('5')"));
		assertTrue(values.contains("('14')"));
		assertTrue(values.contains("('22')"));
	}
}
