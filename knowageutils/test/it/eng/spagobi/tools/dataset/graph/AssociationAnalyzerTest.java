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
		Map<String, Map<String, String>> datasetAssociationColumnMap = analyzer.getDatasetAssociationColumnMap();
		UndirectedGraph<String, LabeledEdge<String>> graph = analyzer.getGraph();

		assertTrue(datasetAssociationColumnMap.containsKey(X));
		assertTrue(datasetAssociationColumnMap.containsKey(Y));

		Map<String, String> map = null;

		map = datasetAssociationColumnMap.get(X);
		assertTrue(map.containsKey(A1));
		assertEquals(buildColumnLabel(X, A1), map.get(A1));

		map = datasetAssociationColumnMap.get(Y);
		assertTrue(map.containsKey(A1));
		assertEquals(buildColumnLabel(Y, A1), map.get(A1));

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
		Map<String, Map<String, String>> datasetAssociationColumnMap = analyzer.getDatasetAssociationColumnMap();
		UndirectedGraph<String, LabeledEdge<String>> graph = analyzer.getGraph();

		assertTrue(datasetAssociationColumnMap.containsKey(K));
		assertTrue(datasetAssociationColumnMap.containsKey(W));
		assertTrue(datasetAssociationColumnMap.containsKey(X));
		assertTrue(datasetAssociationColumnMap.containsKey(Y));
		assertTrue(datasetAssociationColumnMap.containsKey(Z));

		Map<String, String> map = null;

		map = datasetAssociationColumnMap.get(K);
		assertTrue(map.containsKey(A3));
		assertEquals(buildColumnLabel(K, A3), map.get(A3));

		map = datasetAssociationColumnMap.get(W);
		assertTrue(map.containsKey(A4));
		assertEquals(buildColumnLabel(W, A4), map.get(A4));

		map = datasetAssociationColumnMap.get(X);
		assertTrue(map.containsKey(A1));
		assertEquals(buildColumnLabel(X, A1), map.get(A1));
		assertTrue(map.containsKey(A5));
		assertEquals(buildColumnLabel(X, A5), map.get(A5));
		assertTrue(map.containsKey(A6));
		assertEquals(buildColumnLabel(X, A6), map.get(A6));

		map = datasetAssociationColumnMap.get(Y);
		assertTrue(map.containsKey(A1));
		assertEquals(buildColumnLabel(Y, A1), map.get(A1));
		assertTrue(map.containsKey(A4));
		assertEquals(buildColumnLabel(Y, A4), map.get(A4));
		assertTrue(map.containsKey(A5));
		assertEquals(buildColumnLabel(Y, A5), map.get(A5));

		map = datasetAssociationColumnMap.get(Z);
		assertTrue(map.containsKey(A1));
		assertEquals(buildColumnLabel(Z, A1), map.get(A1));
		assertTrue(map.containsKey(A5));
		assertEquals(buildColumnLabel(Z, A5), map.get(A5));
		assertTrue(map.containsKey(A6));
		assertEquals(buildColumnLabel(Z, A6), map.get(A6));

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
	public void testGetOneSelection() {
		Set<LabeledEdge<String>> edgeSet = new HashSet<LabeledEdge<String>>();
		edgeSet.add(new LabeledEdge<String>(X, Y, A1));
		edgeSet.add(new LabeledEdge<String>(X, Y, A5));
		EdgeGroup resultEdgeGroup = new EdgeGroup(edgeSet);
		Set<String> resultValues = new HashSet<String>();
		resultValues.add("('744','2275')");
		resultValues.add("('744','10226')");
		Map<EdgeGroup, Set<String>> egdeGroupValuesMap = new HashMap<EdgeGroup, Set<String>>();
		egdeGroupValuesMap.put(resultEdgeGroup, resultValues);

		AssociationGroup associationGroup = new AssociationGroup();
		associationGroup.addAssociations(fiveAssociationsMap.values());
		Map<String, Map<String, Set<String>>> selections = AssociationAnalyzer.getSelections(associationGroup, egdeGroupValuesMap);

		Map<String, Set<String>> map = null;
		String columns = null;

		assertTrue(selections.containsKey(X));
		map = selections.get(X);
		columns = buildColumnLabel(X, A1) + "," + buildColumnLabel(X, A5);
		assertTrue(map.containsKey(columns));
		assertTrue(map.get(columns).equals(resultValues));

		assertTrue(selections.containsKey(Y));
		map = selections.get(Y);
		columns = buildColumnLabel(Y, A1) + "," + buildColumnLabel(Y, A5);
		assertTrue(map.containsKey(columns));
		assertTrue(map.get(columns).equals(resultValues));
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
