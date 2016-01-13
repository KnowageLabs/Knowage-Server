package it.eng.spagobi.tools.dataset.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class EdgeGroupTest {

	private EdgeGroup eg1;
	private EdgeGroup eg2;
	private EdgeGroup eg3;
	private EdgeGroup eg4;

	@Before
	public void setUp() {
		LabeledEdge<String> le1 = new LabeledEdge<String>("x", "y", "A1");
		LabeledEdge<String> le2 = new LabeledEdge<String>("x", "y", "A5");
		LabeledEdge<String> le3 = new LabeledEdge<String>("x", "z", "A5");
		LabeledEdge<String> le4 = new LabeledEdge<String>("x", "y", "A6");

		Set<LabeledEdge<String>> set1 = new HashSet<LabeledEdge<String>>();
		set1.add(le1);
		set1.add(le2);
		eg1 = new EdgeGroup(set1);

		Set<LabeledEdge<String>> set2 = new HashSet<LabeledEdge<String>>();
		set2.add(le1);
		set2.add(le3);
		eg2 = new EdgeGroup(set2);

		Set<LabeledEdge<String>> set3 = new HashSet<LabeledEdge<String>>();
		set3.add(le1);
		set3.add(le4);
		eg3 = new EdgeGroup(set3);

		Set<LabeledEdge<String>> set4 = new HashSet<LabeledEdge<String>>();
		set4.add(le2);
		set4.add(le1);
		eg4 = new EdgeGroup(set4);
	}

	@Test
	public void testEquals() {
		assertEquals(eg1, eg1);
		assertEquals(eg1, eg2);

		assertNotEquals(eg1, eg3);
	}

	@Test
	public void testGetColumnNames() {
		assertEquals(eg1.getOrderedEdgeNames(), eg1.getOrderedEdgeNames());
		assertEquals(eg1.getOrderedEdgeNames(), eg4.getOrderedEdgeNames());
	}
}