/**
 * 
 */
package test.cache;

import it.eng.spagobi.engines.whatif.WhatIfEngineInstance;
import it.eng.spagobi.engines.whatif.model.SpagoBIPivotModel;
import it.eng.spagobi.writeback4j.mondrian.CacheManager;

import java.io.File;
import java.util.Random;

import org.olap4j.CellSet;
import org.olap4j.OlapDataSource;
import org.olap4j.Position;

import test.AbstractWhatIfTestCase;
import test.DbConfigContainer;

/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

/**
 * @author ghedin
 *
 */
public class CleanCacheTestCase extends AbstractWhatIfTestCase {
	
	
	private static final String mdx = ( "SELECT {[Measures].[Store Sales]} ON COLUMNS, {[Product].[Food]} ON ROWS FROM [Sales_V] WHERE [Version].[1]");
	
	public void setUp() throws Exception {
		super.setUp();
	}
	
	public void tearDown() throws Exception {
		super.tearDown();
	}

	
	public String getCatalogue(){
		
        File userDir = new File("").getAbsoluteFile();
        File f  = new File(userDir,  "\\test\\test\\writeback\\resources\\FoodMartMySQL.xml");
		return f.getAbsolutePath();
	}
	
	public void testRecreatequrery(){
		WhatIfEngineInstance ei = getWhatifengineiEngineInstance( getCatalogue());
		OlapDataSource connection = ei.getOlapDataSource();
		SpagoBIPivotModel pivotModel = new SpagoBIPivotModel(connection);
		pivotModel.setMdx( mdx);
		pivotModel.initialize();
		
		boolean equal = true;
		
		CellSet cs = pivotModel.getCellSet();
		executeQuery("update sales_fact_1998_virtual cubealias set `store_sales` = "+(new Random(System.currentTimeMillis()).nextDouble()*100)+" where cubealias.version=1 and cubealias.product_id in (select t1.product_id from  product t1,  product_class t2 where  ( t1.product_class_id = t2.product_class_id )  and  ( t2.product_family = 'Food' )  and  ( t2.product_department = 'Meat' ) )");
		
		CacheManager.flushCache(connection);
		String mdx = pivotModel.getMdx();
		pivotModel.setMdx( mdx);
		pivotModel.initialize();
		
		CellSet cs1 = pivotModel.getCellSet();
		
		try {
			for (Position axis_0_Position : cs.getAxes().get(0).getPositions()) {

				for (Position axis_1_Position : cs.getAxes().get(1).getPositions()) {

					Object d = cs.getCell(axis_0_Position, axis_1_Position).getValue();
					Object d2 = cs1.getCell(axis_0_Position, axis_1_Position).getValue();
					if(!d.equals(d2)){
						equal = false;
						break;
					}
				}
			}	
		} catch (Exception e) {
			equal = false;
			e.printStackTrace();
		}

		
		assertEquals(equal, false);
		
	}
	

	public String getTemplate(){
		return DbConfigContainer.getMySqlTemplate();
	}
}
