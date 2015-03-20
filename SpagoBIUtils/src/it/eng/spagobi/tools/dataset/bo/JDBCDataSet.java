/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */


package it.eng.spagobi.tools.dataset.bo;

import it.eng.spagobi.services.dataset.bo.SpagoBiDataSet;
import it.eng.spagobi.tools.dataset.common.datareader.JDBCStandardDataReader;

import org.apache.log4j.Logger;

/**
 * @authors
 * Angelo Bernabei
 *         angelo.bernabei@eng.it
 * Giulio Gavardi
 *     giulio.gavardi@eng.it
 *  Andrea Gioia
 *         andrea.gioia@eng.it
 *  Alberto Ghedin
 *         alberto.ghedin@eng.it
 *  Monica Franceschini
 *  	   monica.franceschini@eng.it *  
 */
public class JDBCDataSet extends AbstractJDBCDataset {
	
	public static String DS_TYPE = "SbiQueryDataSet";
	
	private static transient Logger logger = Logger.getLogger(JDBCDataSet.class);
    
	
	/**
     * Instantiates a new empty JDBC data set.
     */
    public JDBCDataSet() {
		super();
		setDataReader( new JDBCStandardDataReader() );
	}
    
    public JDBCDataSet(JDBCDataSet jdbcDataset) {
    	super(jdbcDataset);
    }
    
    public JDBCDataSet(SpagoBiDataSet dataSetConfig) {
		super(dataSetConfig);
	}
    
}
