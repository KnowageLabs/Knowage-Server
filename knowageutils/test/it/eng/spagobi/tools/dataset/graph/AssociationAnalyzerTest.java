package it.eng.spagobi.tools.dataset.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import it.eng.spagobi.tools.dataset.common.association.Association;
import it.eng.spagobi.tools.dataset.common.association.Association.Field;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.jgrapht.UndirectedGraph;
import org.junit.Test;

public class AssociationAnalyzerTest {
	public static final String K = "k";
	public static final String W = "w";
	public static final String X = "x";
	public static final String Y = "y";
	public static final String Z = "z";
	
	public static final String A1 = "A1";
	public static final String A3 = "A3";
	public static final String A4 = "A4";
	public static final String A5 = "A5";
	public static final String A6 = "A6";
	
	@Test
	public void testOneAssociation() {
		Collection<Association> associations = new HashSet<Association>();
		associations.add(buildAssociation(A1, new String[] {X, Y}));
		
		AssociationAnalyzer analyzer = new AssociationAnalyzer(associations);
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
	public void testFiveAssociations() {
		Collection<Association> associations = new HashSet<Association>();
		associations.add(buildAssociation(A1, new String[] {X, Y, Z}));
		associations.add(buildAssociation(A3, new String[] {K, Z}));
		associations.add(buildAssociation(A4, new String[] {W, Y}));
		associations.add(buildAssociation(A5, new String[] {X, Y, Z}));
		associations.add(buildAssociation(A6, new String[] {X, Z}));
		
		AssociationAnalyzer analyzer = new AssociationAnalyzer(associations);
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
	
	private Association buildAssociation(String associationId, String[] datasets) {
		Association association = new Association(associationId, "");
		for(String dataset : datasets) {
			association.addField(new Field(dataset, buildColumnLabel(dataset, associationId)));
		}
		return association;
	}
	
	private String buildColumnLabel(String dataset, String associationId){
		return dataset + "_" + associationId;
	}
}
