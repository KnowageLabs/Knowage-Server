/**
 * 
 */
package test.writeback.tabledescriptor;

import it.eng.spagobi.engines.whatif.WhatIfEngineInstance;
import it.eng.spagobi.tools.dataset.persist.IDataSetTableDescriptor;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.database.temporarytable.TemporaryTableManager;

import java.util.Set;

import junit.framework.Assert;

import test.AbstractWhatIfTestCase;

/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

/**
 * @author ghedin
 *
 */
public abstract class AbstractTableDescriptoTastCase extends AbstractWhatIfTestCase{
	
	public static final String tableName = "sales_fact_1998_virtual";
	public static final int columnsNumber = 8;
	
	public void testGetTemporaryTableManager( ) throws Exception{
		WhatIfEngineInstance ei = getWhatifengineiEngineInstance(getCatalogue());
		
		IDataSource dataSource = ei.getDataSource();
		
		IDataSetTableDescriptor tabledescriptor = TemporaryTableManager.getTableDescriptor(null, tableName, dataSource);
		Set<String> columns = tabledescriptor.getColumnNames();
		Assert.assertEquals(columns.size(), columnsNumber);
		Assert.assertTrue(columns.contains("store_cost") || (columns.contains(("store_cost").toUpperCase())));
		Assert.assertTrue(columns.contains("wbversion")|| (columns.contains(("wbversion").toUpperCase())));
		Assert.assertTrue(columns.contains("product_id")|| (columns.contains(("product_id").toUpperCase())));
		Assert.assertTrue(columns.contains("store_sales")|| (columns.contains(("store_sales").toUpperCase())));
		Assert.assertTrue(columns.contains("store_id")|| (columns.contains(("store_id").toUpperCase())));
		Assert.assertTrue(columns.contains("time_id")|| (columns.contains(("time_id").toUpperCase())));
		Assert.assertTrue(columns.contains("customer_id")|| (columns.contains(("customer_id").toUpperCase())));
		Assert.assertTrue(columns.contains("promotion_id")|| (columns.contains(("promotion_id").toUpperCase())));
	}
	
	public String getTableName(){
		return tableName;
	}
	
	public abstract String getCatalogue();
}
