/**

SpagoBI - The Business Intelligence Free Platform

Copyright (C) 2005-2010 Engineering Ingegneria Informatica S.p.A.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

**/
package test.parser;

import java.io.File;

import org.olap4j.OlapDataSource;

import test.AbstractWhatIfTestCase;
import test.DbConfigContainer;
import it.eng.spagobi.engines.whatif.WhatIfEngineInstance;
import it.eng.spagobi.engines.whatif.model.SpagoBICellSetWrapper;
import it.eng.spagobi.engines.whatif.model.SpagoBICellWrapper;
import it.eng.spagobi.engines.whatif.model.SpagoBIPivotModel;
import it.eng.spagobi.engines.whatif.parser.Lexer;
import it.eng.spagobi.engines.whatif.parser.parser;
import it.eng.spagobi.pivot4j.mdx.MdxQueryExecutor;
import junit.framework.TestCase;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public class MetalanguageParserTestCase extends AbstractWhatIfTestCase {

	parser parserIstance;
	protected void setUp() throws Exception {
		parserIstance = new parser();
		parserIstance.setVerbose(true);
		
		WhatIfEngineInstance ei = getWhatifengineiEngineInstance(getCatalogue());
		SpagoBIPivotModel pivotModel = (SpagoBIPivotModel)ei.getPivotModel();
		OlapDataSource olapDataSource = ei.getOlapDataSource();		

		//expand first level node
		String mdx = "SELECT {[Measures].[Store Sales]} ON COLUMNS, Hierarchize(Union({[Product].[Food]}, [Product].[Food].Children)) ON ROWS FROM [Sales_V] WHERE CrossJoin([Version].[0], [Region].[Mexico Central])";
		
		pivotModel.setMdx(mdx);
		
		SpagoBICellSetWrapper cellSetWrapper = (SpagoBICellSetWrapper)pivotModel.getCellSet();
		SpagoBICellWrapper cellWrapper = (SpagoBICellWrapper) cellSetWrapper.getCell(6);
		
		parserIstance.setWhatIfInfo(cellWrapper, pivotModel, olapDataSource, ei);
	}
	
	public void testSetSimpleValue(){
    	String expression ="30";
		Lexer lexerInstance = new Lexer(new java.io.StringReader(expression));
		lexerInstance.setVerbose(true);
		parserIstance.setScanner(lexerInstance);
		Object result = null;
		try {
			result = parserIstance.parse().value;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		assertEquals(result, 30.0);

	}
	
	public void testSetDecimalCommaValue(){
    	String expression ="30,0";
		Lexer lexerInstance = new Lexer(new java.io.StringReader(expression));
		lexerInstance.setVerbose(true);
		parserIstance.setScanner(lexerInstance);
		Object result = null;
		try {
			result = parserIstance.parse().value;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		assertEquals(result, 30.0);

	}	
	
	public void testSetDecimalDotValue(){
    	String expression ="30.0";
		Lexer lexerInstance = new Lexer(new java.io.StringReader(expression));
		lexerInstance.setVerbose(true);
		parserIstance.setScanner(lexerInstance);
		Object result = null;
		try {
			result = parserIstance.parse().value;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		assertEquals(result, 30.0);

	}	
	
	public void testSetMember(){
		boolean noException = true;
		
    	String expression ="Product.Eggs";
		Lexer lexerInstance = new Lexer(new java.io.StringReader(expression));
		lexerInstance.setVerbose(true);
		parserIstance.setScanner(lexerInstance);
		Object result = null;
		try {
			result = parserIstance.parse().value;
		} catch (Exception e) {
			noException = false;
		}
		assertEquals(noException, true);
	}
	
	public void testSetMultipleMember(){
		boolean noException = true;
		
    	String expression ="Product.Eggs;Measures.Unit Sales";
		Lexer lexerInstance = new Lexer(new java.io.StringReader(expression));
		lexerInstance.setVerbose(true);
		parserIstance.setScanner(lexerInstance);
		Object result = null;
		try {
			result = parserIstance.parse().value;
		} catch (Exception e) {
			noException = false;
		}
		assertEquals(noException, true);
	}
	
	public void testSetMultipleMemberWithAmbiguity(){
		boolean noException = true;
		
    	String expression ="Measures.Unit Sales;[Product].[Eggs]";
		Lexer lexerInstance = new Lexer(new java.io.StringReader(expression));
		lexerInstance.setVerbose(true);
		parserIstance.setScanner(lexerInstance);
		Object result = null;
		try {
			result = parserIstance.parse().value;
		} catch (Exception e) {
			noException = false;
		}
		assertEquals(noException, true);
	}
	
	public void testSetPercent(){
		
    	String expression ="50+10%";
		Lexer lexerInstance = new Lexer(new java.io.StringReader(expression));
		lexerInstance.setVerbose(true);
		parserIstance.setScanner(lexerInstance);
		Object result = null;
		try {
			result = parserIstance.parse().value;
		} catch (Exception e) {
		}
		assertEquals(result, 55.0);
	}
	
	public void testSetVariable(){
    	String expression ="var";
		Lexer lexerInstance = new Lexer(new java.io.StringReader(expression));
		lexerInstance.setVerbose(true);
		parserIstance.setScanner(lexerInstance);
		Object result = null;
		try {
			result = parserIstance.parse().value;
		} catch (Exception e) {
		}
		assertEquals(result, 5.0);
	}
	
	public void testSetVariablePercent(){
    	String expression ="var+10%";
		Lexer lexerInstance = new Lexer(new java.io.StringReader(expression));
		lexerInstance.setVerbose(true);
		parserIstance.setScanner(lexerInstance);
		Object result = null;
		try {
			result = parserIstance.parse().value;
		} catch (Exception e) {
		}
		assertEquals(result, 5.5);
	}
	
	public void testSetDecimalNumbersExpression(){
    	String expression ="5,7+1.3";
		Lexer lexerInstance = new Lexer(new java.io.StringReader(expression));
		lexerInstance.setVerbose(true);
		parserIstance.setScanner(lexerInstance);
		Object result = null;
		try {
			result = parserIstance.parse().value;
		} catch (Exception e) {
		}
		assertFalse(result.equals(7));
	}
	
	public void testSetEqualExpression(){
    	String expression ="=5,7+1.3";
		Lexer lexerInstance = new Lexer(new java.io.StringReader(expression));
		lexerInstance.setVerbose(true);
		parserIstance.setScanner(lexerInstance);
		Object result = null;
		try {
			result = parserIstance.parse().value;
		} catch (Exception e) {
		}
		assertFalse(result.equals(7));
	}
	
	public void testSetVariablesExpression(){
    	String expression ="var+5*(6-3)+1+var";
		Lexer lexerInstance = new Lexer(new java.io.StringReader(expression));
		lexerInstance.setVerbose(true);
		parserIstance.setScanner(lexerInstance);
		Object result = null;
		try {
			result = parserIstance.parse().value;
		} catch (Exception e) {
		}
		assertEquals(result,26.0);
	}
	
	public void testSetVariableMember(){
		boolean noException = true;
		
    	String expression ="Measures.Unit Sales*var";
		Lexer lexerInstance = new Lexer(new java.io.StringReader(expression));
		lexerInstance.setVerbose(true);
		parserIstance.setScanner(lexerInstance);
		Object result = null;
		try {
			result = parserIstance.parse().value;
		} catch (Exception e) {
			noException = false;
		}
		assertEquals(noException, true);
	}
	
	
	public void testSetMemberAdd(){
		boolean noException = true;
		
    	String expression ="Measures.Store Sales+100";
		Lexer lexerInstance = new Lexer(new java.io.StringReader(expression));
		lexerInstance.setVerbose(true);
		parserIstance.setScanner(lexerInstance);
		Object result = null;
		try {
			result = parserIstance.parse().value;
		} catch (Exception e) {
			noException = false;
		}
		assertEquals(noException, true);
	}
	
	public void testSetMemberPercent(){
		boolean noException = true;
		
    	String expression ="Measures.Store Sales+4%";
		Lexer lexerInstance = new Lexer(new java.io.StringReader(expression));
		lexerInstance.setVerbose(true);
		parserIstance.setScanner(lexerInstance);
		Object result = null;
		try {
			result = parserIstance.parse().value;
		} catch (Exception e) {
			noException = false;
		}
		assertEquals(noException, true);
	}
	
	public void testSetExpressionMemberVariable(){
		boolean noException = true;
		
    	String expression ="Measures.Store Sales-var+100";
		Lexer lexerInstance = new Lexer(new java.io.StringReader(expression));
		lexerInstance.setVerbose(true);
		parserIstance.setScanner(lexerInstance);
		Object result = null;
		try {
			result = parserIstance.parse().value;
		} catch (Exception e) {
			noException = false;
		}
		assertEquals(noException, true);
	}
	
	public void testSetExpressionParentheses(){
		boolean noException = true;
		
    	String expression ="((var*2)-10)+2";
		Lexer lexerInstance = new Lexer(new java.io.StringReader(expression));
		lexerInstance.setVerbose(true);
		parserIstance.setScanner(lexerInstance);
		Object result = null;
		try {
			result = parserIstance.parse().value;
		} catch (Exception e) {
			noException = false;
		}
		assertEquals(result,2.0);
	}
	
	public void testSetMultipleMembersAdd(){
		boolean noException = true;
		
    	String expression ="Measures.Store Sales;Product.Eggs+100";
		Lexer lexerInstance = new Lexer(new java.io.StringReader(expression));
		lexerInstance.setVerbose(true);
		parserIstance.setScanner(lexerInstance);
		Object result = null;
		try {
			result = parserIstance.parse().value;
		} catch (Exception e) {
			noException = false;
		}
		assertEquals(noException, true);
	}
	
	public void testSetMemberWithSpace(){
		boolean noException = true;
		
    	String expression ="Measures.Store Sales;Product.Canned Foods+100";
		Lexer lexerInstance = new Lexer(new java.io.StringReader(expression));
		lexerInstance.setVerbose(true);
		parserIstance.setScanner(lexerInstance);
		Object result = null;
		try {
			result = parserIstance.parse().value;
		} catch (Exception e) {
			noException = false;
		}
		assertEquals(noException, true);
	}
	
	public void testExpressionVariableString(){
		boolean noException = true;
		
    	String expression ="PD+10";
		Lexer lexerInstance = new Lexer(new java.io.StringReader(expression));
		lexerInstance.setVerbose(true);
		parserIstance.setScanner(lexerInstance);
		Object result = null;
		try {
			result = parserIstance.parse().value;
		} catch (Exception e) {
			noException = false;
		}
		assertEquals(noException, true);
	}
	
	public void testExpressionVariableInteger(){
		boolean noException = true;
		
    	String expression ="var+10";
		Lexer lexerInstance = new Lexer(new java.io.StringReader(expression));
		lexerInstance.setVerbose(true);
		parserIstance.setScanner(lexerInstance);
		Object result = null;
		try {
			result = parserIstance.parse().value;
		} catch (Exception e) {
			noException = false;
		}
		assertEquals(result, 15.0);
	}
	
	public void testExpressionVariableDecimal(){
		boolean noException = true;
		
    	String expression ="varD+10";
		Lexer lexerInstance = new Lexer(new java.io.StringReader(expression));
		lexerInstance.setVerbose(true);
		parserIstance.setScanner(lexerInstance);
		Object result = null;
		try {
			result = parserIstance.parse().value;
		} catch (Exception e) {
			noException = false;
		}
		assertEquals(result, 15.5);
	}
	
	public void testExpressionAliasDimension(){
		boolean noException = true;
		
    	String expression ="P.Eggs";
		Lexer lexerInstance = new Lexer(new java.io.StringReader(expression));
		lexerInstance.setVerbose(true);
		parserIstance.setScanner(lexerInstance);
		Object result = null;
		try {
			result = parserIstance.parse().value;
		} catch (Exception e) {
			noException = false;
		}
		assertEquals(noException, true);
	}
	
	public void testExpressionAliasGeneric(){
		boolean noException = true;
		
    	String expression ="Eggs";
		Lexer lexerInstance = new Lexer(new java.io.StringReader(expression));
		lexerInstance.setVerbose(true);
		parserIstance.setScanner(lexerInstance);
		Object result = null;
		try {
			result = parserIstance.parse().value;
		} catch (Exception e) {
			noException = false;
		}
		assertEquals(noException, true);
	}
	
	//--- Utility functions ---------------------------------------------------------------
	
	public String getCatalogue(){
		
        File userDir = new File("").getAbsoluteFile();
        File f  = new File(userDir,  "\\test\\test\\writeback\\resources\\FoodMartMySQL.xml");
		return f.getAbsolutePath();
	}

	//-------------------------------------------------------------------------------------
	

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	
	public String getTemplate(){
		return DbConfigContainer.getMySqlTemplate();
	}
}
