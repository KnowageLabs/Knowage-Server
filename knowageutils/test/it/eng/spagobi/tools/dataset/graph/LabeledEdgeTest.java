package it.eng.spagobi.tools.dataset.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class LabeledEdgeTest {
	@Test
	public void testEquals() {
		assertEquals(new LabeledEdge<String>("x", "y", "label"), new LabeledEdge<String>("x", "y", "label"));
		assertEquals(new LabeledEdge<String>("x", "y", "label"), new LabeledEdge<String>("y", "x", "label"));
		
		assertNotEquals(new LabeledEdge<String>("x", "y", "label"), new LabeledEdge<String>("z", "y", "label"));
		assertNotEquals(new LabeledEdge<String>("x", "y", "label"), new LabeledEdge<String>("x", "z", "label"));
		assertNotEquals(new LabeledEdge<String>("x", "y", "label"), new LabeledEdge<String>("x", "z", "tag"));
	}
}
