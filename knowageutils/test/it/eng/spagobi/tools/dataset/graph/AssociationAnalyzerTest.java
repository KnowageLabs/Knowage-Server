package it.eng.spagobi.tools.dataset.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import it.eng.spagobi.tools.dataset.common.association.Association;
import it.eng.spagobi.tools.dataset.common.association.Association.Field;
import it.eng.spagobi.tools.dataset.common.association.AssociationGroup;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.Pseudograph;
import org.junit.Before;
import org.junit.Test;

public class AssociationAnalyzerTest {
	private static final String K = "k";
	private static final String W = "w";
	private static final String X = "x";
	private static final String Y = "y";
	private static final String Z = "z";

	private static final String A1 = "A1";
	private static final String A3 = "A3";
	private static final String A4 = "A4";
	private static final String A5 = "A5";
	private static final String A6 = "A6";

	private Map<String, Association> oneAssociationMap;
	private Map<String, Association> fiveAssociationsMap;

	@Before
	public void setUp() {
		oneAssociationMap = new HashMap<String, Association>();
		oneAssociationMap.put(A1, buildAssociation(A1, new String[] { X, Y }));

		fiveAssociationsMap = new HashMap<String, Association>();
		fiveAssociationsMap.put(A1, buildAssociation(A1, new String[] { X, Y, Z }));
		fiveAssociationsMap.put(A3, buildAssociation(A3, new String[] { K, Z }));
		fiveAssociationsMap.put(A4, buildAssociation(A4, new String[] { W, Y }));
		fiveAssociationsMap.put(A5, buildAssociation(A5, new String[] { X, Y, Z }));
		fiveAssociationsMap.put(A6, buildAssociation(A6, new String[] { X, Z }));
	}

	@Test
	public void testProcessOneAssociation() {
		AssociationAnalyzer analyzer = new AssociationAnalyzer(oneAssociationMap.values());
		analyzer.process();
		Map<String, Map<String, String>> datasetToAssociationToColumnsMap = analyzer.getDatasetToAssociationToColumnMap();
		UndirectedGraph<String, LabeledEdge<String>> graph = analyzer.getGraph();

		assertTrue(datasetToAssociationToColumnsMap.containsKey(X));
		assertTrue(datasetToAssociationToColumnsMap.containsKey(Y));

		Map<String, String> associationToColumnsMap = null;

		associationToColumnsMap = datasetToAssociationToColumnsMap.get(X);
		assertTrue(associationToColumnsMap.containsKey(A1));
		assertEquals(buildColumnLabel(X, A1), associationToColumnsMap.get(A1));

		associationToColumnsMap = datasetToAssociationToColumnsMap.get(Y);
		assertTrue(associationToColumnsMap.containsKey(A1));
		assertEquals(buildColumnLabel(Y, A1), associationToColumnsMap.get(A1));

		assertEquals(2, graph.vertexSet().size());
		assertTrue(graph.containsVertex(X));
		assertTrue(graph.containsVertex(Y));

		assertEquals(1, graph.edgeSet().size());
		assertTrue(graph.containsEdge(new LabeledEdge<String>(Y, X, A1)));
	}

	@Test
	public void testProcessFiveAssociations() {
		AssociationAnalyzer analyzer = new AssociationAnalyzer(fiveAssociationsMap.values());
		analyzer.process();
		Map<String, Map<String, String>> datasetToAssociationToColumnMap = analyzer.getDatasetToAssociationToColumnMap();
		Pseudograph<String, LabeledEdge<String>> graph = analyzer.getGraph();

		assertTrue(datasetToAssociationToColumnMap.containsKey(K));
		assertTrue(datasetToAssociationToColumnMap.containsKey(W));
		assertTrue(datasetToAssociationToColumnMap.containsKey(X));
		assertTrue(datasetToAssociationToColumnMap.containsKey(Y));
		assertTrue(datasetToAssociationToColumnMap.containsKey(Z));

		Map<String, String> associationToColumnMap = null;

		associationToColumnMap = datasetToAssociationToColumnMap.get(K);
		assertTrue(associationToColumnMap.containsKey(A3));
		assertEquals(buildColumnLabel(K, A3), associationToColumnMap.get(A3));

		associationToColumnMap = datasetToAssociationToColumnMap.get(W);
		assertTrue(associationToColumnMap.containsKey(A4));
		assertEquals(buildColumnLabel(W, A4), associationToColumnMap.get(A4));

		associationToColumnMap = datasetToAssociationToColumnMap.get(X);
		assertTrue(associationToColumnMap.containsKey(A1));
		assertEquals(buildColumnLabel(X, A1), associationToColumnMap.get(A1));
		assertTrue(associationToColumnMap.containsKey(A5));
		assertEquals(buildColumnLabel(X, A5), associationToColumnMap.get(A5));
		assertTrue(associationToColumnMap.containsKey(A6));
		assertEquals(buildColumnLabel(X, A6), associationToColumnMap.get(A6));

		associationToColumnMap = datasetToAssociationToColumnMap.get(Y);
		assertTrue(associationToColumnMap.containsKey(A1));
		assertEquals(buildColumnLabel(Y, A1), associationToColumnMap.get(A1));
		assertTrue(associationToColumnMap.containsKey(A4));
		assertEquals(buildColumnLabel(Y, A4), associationToColumnMap.get(A4));
		assertTrue(associationToColumnMap.containsKey(A5));
		assertEquals(buildColumnLabel(Y, A5), associationToColumnMap.get(A5));

		associationToColumnMap = datasetToAssociationToColumnMap.get(Z);
		assertTrue(associationToColumnMap.containsKey(A1));
		assertEquals(buildColumnLabel(Z, A1), associationToColumnMap.get(A1));
		assertTrue(associationToColumnMap.containsKey(A5));
		assertEquals(buildColumnLabel(Z, A5), associationToColumnMap.get(A5));
		assertTrue(associationToColumnMap.containsKey(A6));
		assertEquals(buildColumnLabel(Z, A6), associationToColumnMap.get(A6));

		assertEquals(5, graph.vertexSet().size());
		assertTrue(graph.containsVertex(K));
		assertTrue(graph.containsVertex(W));
		assertTrue(graph.containsVertex(X));
		assertTrue(graph.containsVertex(Y));
		assertTrue(graph.containsVertex(Z));

		assertEquals(9, graph.edgeSet().size());
		assertTrue(graph.containsEdge(new LabeledEdge<String>(X, Y, A1)));
		assertTrue(graph.containsEdge(new LabeledEdge<String>(Y, Z, A1)));
		assertTrue(graph.containsEdge(new LabeledEdge<String>(Z, X, A1)));
		assertTrue(graph.containsEdge(new LabeledEdge<String>(K, Z, A3)));
		assertTrue(graph.containsEdge(new LabeledEdge<String>(W, Y, A4)));
		assertTrue(graph.containsEdge(new LabeledEdge<String>(X, Y, A5)));
		assertTrue(graph.containsEdge(new LabeledEdge<String>(Y, Z, A5)));
		assertTrue(graph.containsEdge(new LabeledEdge<String>(X, Z, A5)));
		assertTrue(graph.containsEdge(new LabeledEdge<String>(Z, X, A6)));
	}

	@Test
	public void testGetSelections() {
		Set<String> valuesA1A5 = new HashSet<String>();
		valuesA1A5.add("('744','2275')");

		Set<String> valuesA1A5A6 = new HashSet<String>();
		valuesA1A5A6.add("('744','2275','0')");

		Set<String> valuesA3 = new HashSet<String>();
		valuesA3.add("('1')");

		Set<String> valuesA4 = new HashSet<String>();
		valuesA4.add("('11')");

		Set<LabeledEdge<String>> edgesA1A5 = new HashSet<LabeledEdge<String>>();
		edgesA1A5.add(new LabeledEdge<String>(X, Y, A1));
		edgesA1A5.add(new LabeledEdge<String>(Y, X, A5));

		Set<LabeledEdge<String>> edgesA1A5A6 = new HashSet<LabeledEdge<String>>();
		edgesA1A5A6.add(new LabeledEdge<String>(X, Z, A1));
		edgesA1A5A6.add(new LabeledEdge<String>(Z, X, A5));
		edgesA1A5A6.add(new LabeledEdge<String>(X, Z, A6));

		Set<LabeledEdge<String>> edgesA3 = new HashSet<LabeledEdge<String>>();
		edgesA3.add(new LabeledEdge<String>(Z, K, A3));

		Set<LabeledEdge<String>> edgesA4 = new HashSet<LabeledEdge<String>>();
		edgesA4.add(new LabeledEdge<String>(Y, W, A4));

		EdgeGroup edgeGroupA1A5 = new EdgeGroup(edgesA1A5);
		EdgeGroup edgeGroupA1A5A6 = new EdgeGroup(edgesA1A5A6);
		EdgeGroup edgeGroupA3 = new EdgeGroup(edgesA3);
		EdgeGroup edgeGroupA4 = new EdgeGroup(edgesA4);

		Map<EdgeGroup, Set<String>> egdegroupToValuesMap = new HashMap<EdgeGroup, Set<String>>();
		egdegroupToValuesMap.put(edgeGroupA1A5, valuesA1A5);
		egdegroupToValuesMap.put(edgeGroupA1A5A6, valuesA1A5A6);
		egdegroupToValuesMap.put(edgeGroupA3, valuesA3);
		egdegroupToValuesMap.put(edgeGroupA4, valuesA4);

		AssociationAnalyzer analyzer = new AssociationAnalyzer(fiveAssociationsMap.values());
		analyzer.process();
		Pseudograph<String, LabeledEdge<String>> graph = analyzer.getGraph();

		AssociationGroup associationGroup = new AssociationGroup();
		associationGroup.addAssociations(fiveAssociationsMap.values());
		Map<String, Map<String, Set<String>>> selections = AssociationAnalyzer.getSelections(associationGroup, graph, egdegroupToValuesMap);

		Map<String, Set<String>> columnsToValuesMap = null;
		String columns = null;

		assertTrue(selections.containsKey(K));
		columnsToValuesMap = selections.get(K);
		columns = buildColumnLabel(K, A3);
		assertTrue(columnsToValuesMap.containsKey(columns));
		assertTrue(columnsToValuesMap.get(columns).equals(valuesA3));

		assertTrue(selections.containsKey(W));
		columnsToValuesMap = selections.get(W);
		columns = buildColumnLabel(W, A4);
		assertTrue(columnsToValuesMap.containsKey(columns));
		assertTrue(columnsToValuesMap.get(columns).equals(valuesA4));

		assertTrue(selections.containsKey(X));
		columnsToValuesMap = selections.get(X);
		columns = buildColumnLabel(X, A1) + "," + buildColumnLabel(X, A5) + "," + buildColumnLabel(X, A6);
		assertTrue(columnsToValuesMap.containsKey(columns));
		assertTrue(columnsToValuesMap.get(columns).equals(valuesA1A5A6));

		assertTrue(selections.containsKey(Y));
		columnsToValuesMap = selections.get(Y);
		columns = buildColumnLabel(Y, A1) + "," + buildColumnLabel(Y, A5);
		assertTrue(columnsToValuesMap.containsKey(columns));
		assertTrue(columnsToValuesMap.get(columns).equals(valuesA1A5));
		columns = buildColumnLabel(Y, A4);
		assertTrue(columnsToValuesMap.containsKey(columns));
		assertTrue(columnsToValuesMap.get(columns).equals(valuesA4));

		assertTrue(selections.containsKey(Z));
		columnsToValuesMap = selections.get(Z);
		columns = buildColumnLabel(Z, A1) + "," + buildColumnLabel(Z, A5) + "," + buildColumnLabel(Z, A6);
		assertTrue(columnsToValuesMap.containsKey(columns));
		assertTrue(columnsToValuesMap.get(columns).equals(valuesA1A5A6));
		columns = buildColumnLabel(Z, A3);
		assertTrue(columnsToValuesMap.containsKey(columns));
		assertTrue(columnsToValuesMap.get(columns).equals(valuesA3));
	}

	private Association buildAssociation(String associationId, String[] datasets) {
		Association association = new Association(associationId, "");
		for (String dataset : datasets) {
			association.addField(new Field(dataset, buildColumnLabel(dataset, associationId)));
		}
		return association;
	}

	private String buildColumnLabel(String dataset, String associationId) {
		return dataset + "_" + associationId;
	}
}
