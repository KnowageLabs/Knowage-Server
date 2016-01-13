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

	private static final String Y = "sales";
	private static final String K = "store";
	private static final String PRODUCT = "product";

	private static final String A1 = "A1";
	private static final String A3 = "A3";
	private static final String A4 = "A4";
	private static final String A5 = "A5";
	private static final String A6 = "A6";

	private static final String A1_COLUMN = "store_id";

	@Before
	public void setUp() throws Exception {
		UtilitiesForTest.setUpMasterConfiguration();
		UtilitiesDAOForTest.setUpDatabaseTestJNDI();
		TenantManager.setTenant(new Tenant("SPAGOBI"));

		ICache cache = SpagoBICacheManager.getCache();
		try {
			IDataSetDAO dataSetDAO = DAOFactory.getDataSetDAO();
			IDataSet dataSet;
			dataSet = dataSetDAO.loadDataSetByLabel(K);
			if (!cache.contains(dataSet)) {
				dataSet.loadData();
				cache.put(dataSet, dataSet.getDataStore());
			}
			dataSet = dataSetDAO.loadDataSetByLabel(Y);
			if (!cache.contains(dataSet)) {
				dataSet.loadData();
				cache.put(dataSet, dataSet.getDataStore());
			}
		} catch (EMFUserError e) {
			fail("Unable to get DataSet DAO");
		}
	}

	@Test
	public void testProcessOneAssociation() {
		Pseudograph<String, LabeledEdge<String>> graph = new Pseudograph<String, LabeledEdge<String>>(new ClassBasedEdgeFactory<String, LabeledEdge<String>>(
				(Class<LabeledEdge<String>>) (Object) LabeledEdge.class));
		graph.addVertex(Y);
		graph.addVertex(K);
		LabeledEdge<String> labeledEdge = new LabeledEdge<String>(Y, K, A1);
		graph.addEdge(Y, K, labeledEdge);

		Map<String, Map<String, String>> datasetToAssociations = new HashMap<String, Map<String, String>>();
		Map<String, String> associationToColumns = new HashMap<String, String>();
		associationToColumns.put(A1, A1_COLUMN);
		datasetToAssociations.put(Y, associationToColumns);
		datasetToAssociations.put(K, associationToColumns);

		AssociativeLogicManager manager = new AssociativeLogicManager(graph, datasetToAssociations, K, "store_type = 'Small Grocery'");
		Map<EdgeGroup, Set<String>> edgeGroupToValues = null;
		try {
			edgeGroupToValues = manager.process();
		} catch (Exception e) {
			e.printStackTrace();
			fail();
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
}
