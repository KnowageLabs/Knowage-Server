package integration;

import java.io.File;

public class CalculatedMemberTestCase extends AbstractWhatIfCalculatedMemberTestCase {

	@Override
	public String getTemplate() {
		File userDir = new File("").getAbsoluteFile();
		File f = new File(userDir, "\\test\\integration\\calculatedmember\\resources\\tpl.xml");
		return f.getAbsolutePath();
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
	}

	/*
	 * public void testInjectCalculatedIntoMdxQuery() throws Exception { WhatIfEngineInstance ei = getWhatifengineiEngineInstance(getCatalogue());
	 * CalculatedMemberManager cm = new CalculatedMemberManager(ei); Member parentMember = CubeUtilities.getMember(ei.getPivotModel().getCube(),
	 * "[Product].[Food]"); String cc = "[Product].[Food].[Dairy]"; cm.injectCalculatedIntoMdxQuery("name", cc, parentMember, Axis.ROWS); String s =
	 * ei.getPivotModel().getCurrentMdx(); System.out.println(s); }
	 * 
	 * @Override public String getCatalogue() { File userDir = new File("").getAbsoluteFile(); File f = new File(userDir,
	 * "\\test\\integration\\calculatedmember\\resources\\FoodMartMySQL.xml"); return f.getAbsolutePath(); }
	 * 
	 * public void testInjectCalculatedMeasureIntoMdxQuery() throws Exception { WhatIfEngineInstance ei = getWhatifengineiEngineInstance(getCatalogue());
	 * CalculatedMemberManager cm = new CalculatedMemberManager(ei); // assert test true if mdx query is what I'm aspecting StringBuilder builder = new
	 * StringBuilder(); builder.append("WITH MEMBER [Measures].[measure name] AS ");
	 * builder.append("'SUM([Measures].[Sales Count], [Measures].[Sales Count])' ");
	 * builder.append("SELECT {{[Measures].[Store Sales], ([Measures].[measure name])}} ON COLUMNS, "); builder.append("{{[Product].[Food]}} ON ROWS ");
	 * builder.append("FROM [Sales_V] "); builder.append("WHERE "); builder.append("CrossJoin([Version].[1], [Region].[Mexico Central])"); String expectedMdx =
	 * builder.toString(); String formula = "SUM([Measures].[Sales Count], [Measures].[Sales Count])"; Member parentMember =
	 * CubeUtilities.getMember(ei.getPivotModel().getCube(), "[Measures].[Store Sales]"); cm.injectCalculatedIntoMdxQuery("measure name", formula, parentMember,
	 * Axis.COLUMNS); String actualMdx = ei.getPivotModel().getCurrentMdx(); System.out.println(new String("expected value ") + expectedMdx);
	 * System.out.println(new String("actual value ") + actualMdx); assertEquals(expectedMdx, actualMdx); }
	 */

	@Override
	public String getCatalogue() {
		// TODO Auto-generated method stub
		return null;
	}
}
