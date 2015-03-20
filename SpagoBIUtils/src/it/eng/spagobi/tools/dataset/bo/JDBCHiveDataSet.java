/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.dataset.bo;

import it.eng.spagobi.services.dataset.bo.SpagoBiDataSet;
import it.eng.spagobi.tools.dataset.common.datareader.JDBCHiveDataReader;


public class JDBCHiveDataSet extends AbstractJDBCDataset {
	
    public JDBCHiveDataSet() {
		super();
		setDataReader( new JDBCHiveDataReader() );
	}
	    
    public JDBCHiveDataSet(SpagoBiDataSet dataSetConfig) {
   		super(dataSetConfig);
   		setDataReader( new JDBCHiveDataReader() );
   	}
}
