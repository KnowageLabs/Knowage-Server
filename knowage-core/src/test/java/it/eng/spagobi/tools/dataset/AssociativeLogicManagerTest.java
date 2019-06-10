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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.Pseudograph;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.emory.mathcs.backport.java.util.Arrays;
import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spago.configuration.FileCreatorConfiguration;
import it.eng.spagobi.UtilitiesForTest;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOConfig;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.UtilitiesDAOForTest;
import it.eng.spagobi.tenant.Tenant;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.tools.dataset.associativity.strategy.AssociativeStrategyFactory;
import it.eng.spagobi.tools.dataset.associativity.strategy.OuterAssociativityManager;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.tools.dataset.graph.EdgeGroup;
import it.eng.spagobi.tools.dataset.graph.LabeledEdge;
import it.eng.spagobi.tools.dataset.graph.Tuple;
import it.eng.spagobi.tools.dataset.graph.associativity.Config;
import it.eng.spagobi.tools.dataset.graph.associativity.utils.AssociativeLogicUtils;
import it.eng.spagobi.tools.dataset.metasql.query.item.InFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.Projection;
import it.eng.spagobi.tools.dataset.metasql.query.item.SimpleFilter;
import it.eng.spagobi.user.UserProfileManager;

public class AssociativeLogicManagerTest {

	private static final String DS_STORE = "store";
	private static final String DS_PRODUCT = "product";
	private static final String DS_SALES_FACT_1997 = "sales_fact_1997";
	private static final String DS_SALES_FACT_1998 = "sales_fact_1998";
	private static final String DS_CUSTOMER = "customer";

	private static final String COL_STORE_ID = "store_id";
	private static final String COL_PRODUCT_ID = "product_id";
	private static final String COL_STORE_CITY = "store_city";
	private static final String COL_CITY = "city";
	private static final String COL_STORE_COUNTRY = "store_country";
	private static final String COL_COUNTRY = "country";

	private static IDataSetDAO dataSetDAO;

	@BeforeClass
	public static void setUp() {
		try {
			UtilitiesDAOForTest.setUpDatabaseTestJNDI();

			System.setProperty("AF_CONFIG_FILE", "master.xml");
			String absolutePath = new File(UtilitiesForTest.class.getResource(".").getFile()).getAbsolutePath()  + "/../../../";
			ConfigSingleton.setConfigurationCreation(new FileCreatorConfiguration(absolutePath));
			ConfigSingleton.getRootPath();
			DAOConfig.setHibernateConfigurationFileFile(new File(absolutePath + "hibernate.cfg.xml"));

			TenantManager.setTenant(new Tenant("DEFAULT_TENANT"));
			UserProfileManager.setProfile(new UserProfile("biadmin", "DEFAULT_TENANT"));

			dataSetDAO = DAOFactory.getDataSetDAO();
		} catch (Exception e) {
			fail(e.toString());
		}
	}

	@Test
	public void testProcessTwoDatasetsOneSimpleAssociation() {
		Pseudograph<String, LabeledEdge<String>> graph = new Pseudograph<>(
				new ClassBasedEdgeFactory<String, LabeledEdge<String>>((Class<LabeledEdge<String>>) (Object) LabeledEdge.class));
		graph.addVertex(DS_STORE);
		graph.addVertex(DS_SALES_FACT_1998);
		LabeledEdge<String> labeledEdge = new LabeledEdge<>(DS_STORE, DS_SALES_FACT_1998, COL_STORE_ID);
		graph.addEdge(DS_STORE, DS_SALES_FACT_1998, labeledEdge);

		Map<String, Map<String, String>> datasetToAssociations = new HashMap<>();
		Map<String, String> associationToColumns = new HashMap<>();
		associationToColumns.put(COL_STORE_ID, COL_STORE_ID);
		datasetToAssociations.put(DS_STORE, associationToColumns);
		datasetToAssociations.put(DS_SALES_FACT_1998, associationToColumns);

		List<SimpleFilter> selections = new ArrayList<>(1);
		IDataSet dataSet = dataSetDAO.loadDataSetByLabel(DS_STORE);
		selections.add(new InFilter(new Projection(dataSet, "store_type"), Arrays.asList(new String[]{"Small Grocery"})));

		// realtimes means that they are not cached
		Set<String> realtimeDatasets = new HashSet<>();
		realtimeDatasets.add(DS_STORE);
		realtimeDatasets.add(DS_SALES_FACT_1998);

		Map<String, Map<String, String>> datasetParameters = new HashMap<>();
		HashMap<String, String> storeParameters = new HashMap<>();
		storeParameters.put("store_id", "100");
		datasetParameters.put(DS_STORE, storeParameters);

		Set<String> documents = new HashSet<>();

		Map<EdgeGroup, Set<Tuple>> edgeGroupToValues = null;

		Config config = AssociativeLogicUtils.buildConfig(AssociativeStrategyFactory.OUTER_STRATEGY, graph, datasetToAssociations, selections, realtimeDatasets,
				datasetParameters, documents);

		try {
			OuterAssociativityManager manager = new OuterAssociativityManager(config, UserProfileManager.getProfile());
			manager.process();
			edgeGroupToValues = manager.getResult().getEdgeGroupValues();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.toString());
		}

		assertNotNull(edgeGroupToValues);

		Set<LabeledEdge<String>> labeledEdges = new HashSet<>();
		labeledEdges.add(labeledEdge);
		// edge group is the collection if relations (associations) between 2 datasets
		EdgeGroup edgeGroup = new EdgeGroup(labeledEdges);
		assertTrue(edgeGroupToValues.containsKey(edgeGroup));

		Set<Tuple> values = edgeGroupToValues.get(edgeGroup);
		assertEquals(4, values.size());
		assertTrue(values.contains(new Tuple(Arrays.asList(new Integer[]{2}))));
		assertTrue(values.contains(new Tuple(Arrays.asList(new Integer[]{5}))));
		assertTrue(values.contains(new Tuple(Arrays.asList(new Integer[]{14}))));
		assertTrue(values.contains(new Tuple(Arrays.asList(new Integer[]{22}))));
	}

	@Test
	public void testProcessThreeDatasetsTwoSimpleAssociations() {
		Pseudograph<String, LabeledEdge<String>> graph = new Pseudograph<>(
				new ClassBasedEdgeFactory<String, LabeledEdge<String>>((Class<LabeledEdge<String>>) (Object) LabeledEdge.class));
		graph.addVertex(DS_STORE);
		graph.addVertex(DS_SALES_FACT_1997);
		graph.addVertex(DS_PRODUCT);
		LabeledEdge<String> labeledEdgeStoreSales = new LabeledEdge<>(DS_STORE, DS_SALES_FACT_1997, COL_STORE_ID);
		graph.addEdge(DS_STORE, DS_SALES_FACT_1997, labeledEdgeStoreSales);
		LabeledEdge<String> labeledEdgeSalesProduct = new LabeledEdge<>(DS_SALES_FACT_1997, DS_PRODUCT, COL_PRODUCT_ID);
		graph.addEdge(DS_SALES_FACT_1997, DS_PRODUCT, labeledEdgeSalesProduct);

		Map<String, String> associationToColumnStore = new HashMap<>();
		associationToColumnStore.put(COL_STORE_ID, COL_STORE_ID);
		Map<String, String> associationToColumnProduct = new HashMap<>();
		associationToColumnProduct.put(COL_PRODUCT_ID, COL_PRODUCT_ID);
		Map<String, String> associationToColumnSales = new HashMap<>();
		associationToColumnSales.put(COL_STORE_ID, COL_STORE_ID);
		associationToColumnSales.put(COL_PRODUCT_ID, COL_PRODUCT_ID);
		Map<String, Map<String, String>> datasetToAssociations = new HashMap<>();
		datasetToAssociations.put(DS_STORE, associationToColumnStore);
		datasetToAssociations.put(DS_PRODUCT, associationToColumnProduct);
		datasetToAssociations.put(DS_SALES_FACT_1997, associationToColumnSales);

		List<SimpleFilter> selections = new ArrayList<>(1);
		IDataSet dataSet = dataSetDAO.loadDataSetByLabel(DS_PRODUCT);
		selections.add(new InFilter(new Projection(dataSet, "brand_name"), "Queen"));

		Set<String> realtimeDatasets = new HashSet<>();
		realtimeDatasets.add(DS_STORE);
		realtimeDatasets.add(DS_SALES_FACT_1997);
		realtimeDatasets.add(DS_PRODUCT);

		Map<String, Map<String, String>> datasetParameters = new HashMap<>();
		HashMap<String, String> storeParameters = new HashMap<>();
		storeParameters.put("store_id", "100");
		datasetParameters.put(DS_STORE, storeParameters);

		Set<String> documents = new HashSet<>();

		Map<EdgeGroup, Set<Tuple>> edgeGroupToValues = null;

		Config config = AssociativeLogicUtils.buildConfig(AssociativeStrategyFactory.OUTER_STRATEGY, graph, datasetToAssociations, selections, realtimeDatasets,
				datasetParameters, documents);

		try {
			OuterAssociativityManager manager = new OuterAssociativityManager(config, UserProfileManager.getProfile());
			manager.process();
			edgeGroupToValues = manager.getResult().getEdgeGroupValues();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.toString());
		}

		assertNotNull(edgeGroupToValues);
		assertEquals(2, edgeGroupToValues.size());

		Set<LabeledEdge<String>> labeledEdges = new HashSet<>();
		labeledEdges.add(labeledEdgeSalesProduct);
		EdgeGroup edgeGroup = new EdgeGroup(labeledEdges);
		assertTrue(edgeGroupToValues.containsKey(edgeGroup));

		Set<Tuple> values = edgeGroupToValues.get(edgeGroup);
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
		Pseudograph<String, LabeledEdge<String>> graph = new Pseudograph<>(
				new ClassBasedEdgeFactory<String, LabeledEdge<String>>((Class<LabeledEdge<String>>) (Object) LabeledEdge.class));
		graph.addVertex(DS_STORE);
		graph.addVertex(DS_CUSTOMER);
		LabeledEdge<String> labeledEdgeStoreCustomer1 = new LabeledEdge<>(DS_STORE, DS_CUSTOMER, COL_CITY);
		graph.addEdge(DS_STORE, DS_CUSTOMER, labeledEdgeStoreCustomer1);
		LabeledEdge<String> labeledEdgeStoreCustomer2 = new LabeledEdge<>(DS_STORE, DS_CUSTOMER, COL_COUNTRY);
		graph.addEdge(DS_STORE, DS_CUSTOMER, labeledEdgeStoreCustomer2);

		Map<String, String> associationToColumnStore = new HashMap<>();
		associationToColumnStore.put(COL_CITY, COL_STORE_CITY);
		associationToColumnStore.put(COL_COUNTRY, COL_STORE_COUNTRY);
		Map<String, String> associationToColumnCustomer = new HashMap<>();
		associationToColumnCustomer.put(COL_CITY, COL_CITY);
		associationToColumnCustomer.put(COL_COUNTRY, COL_COUNTRY);
		Map<String, Map<String, String>> datasetToAssociations = new HashMap<>();
		datasetToAssociations.put(DS_STORE, associationToColumnStore);
		datasetToAssociations.put(DS_CUSTOMER, associationToColumnCustomer);

		List<SimpleFilter> selections = new ArrayList<>(1);
		IDataSet dataSet = dataSetDAO.loadDataSetByLabel(DS_CUSTOMER);
		selections.add(new InFilter(new Projection(dataSet, "gender"), "F"));

		Set<String> realtimeDatasets = new HashSet<>();
		Map<String, Map<String, String>> datasetParameters = new HashMap<>();
		Set<String> documents = new HashSet<>();

		Map<EdgeGroup, Set<Tuple>> edgeGroupToValues = null;

		Config config = AssociativeLogicUtils.buildConfig(AssociativeStrategyFactory.OUTER_STRATEGY, graph, datasetToAssociations, selections, realtimeDatasets,
				datasetParameters, documents);

		try {
			OuterAssociativityManager manager = new OuterAssociativityManager(config, UserProfileManager.getProfile());
			manager.process();
			edgeGroupToValues = manager.getResult().getEdgeGroupValues();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.toString());
		}

		assertNotNull(edgeGroupToValues);
		assertEquals(1, edgeGroupToValues.size());

		Set<LabeledEdge<String>> labeledEdges = new HashSet<>();
		labeledEdges.add(labeledEdgeStoreCustomer1);
		labeledEdges.add(labeledEdgeStoreCustomer2);
		EdgeGroup edgeGroup = new EdgeGroup(labeledEdges);
		assertTrue(edgeGroupToValues.containsKey(edgeGroup));

		Set<Tuple> values = edgeGroupToValues.get(edgeGroup);
		assertEquals(23, values.size());
	}
}
