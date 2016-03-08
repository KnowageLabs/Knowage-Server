package integration.calculatedfield;

import integration.agorithms.AbstractWhatIfInMemoryTestCase;

public class CalculatedFieldsTestCase extends AbstractWhatIfInMemoryTestCase {

	@Override
	public void setUp() throws Exception {
		super.setUp();
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
	}

	public void testInjectCalculatedIntoMdxQuery() throws Exception {

		// WhatIfEngineInstance ei =
		// getWhatifengineiEngineInstance(getCatalogue());
		//
		// CalculatedMemberManager cm = new CalculatedMemberManager(ei);
		//
		// Member parentMember =
		// CubeUtilities.getMember(ei.getPivotModel().getCube(),
		// "[Product].[Food]");
		//
		// String cc = "[Product].[Food].[Dairy]";
		// cm.injectCalculatedIntoMdxQuery("name", cc, parentMember, Axis.ROWS);
		//
		// String s = ei.getPivotModel().getCurrentMdx();
		// System.out.println(s);
	}

}