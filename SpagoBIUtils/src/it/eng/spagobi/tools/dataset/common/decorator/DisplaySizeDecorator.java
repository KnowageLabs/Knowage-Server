/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.dataset.common.decorator;

import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class DisplaySizeDecorator extends AbstractDataStoreDecorator {

	public static final String PROPERTY_NAME = "width";
	
	public DisplaySizeDecorator(){}

	public DisplaySizeDecorator(IDataStoreDecorator nextDecorator) {
		this.setNextDecoratr(nextDecorator);
	}
	
	void doUpdateDecoration(IDataStore dataStore, IRecord record) {
		IMetaData dataStoreMeta = dataStore.getMetaData();
		int filedNo = dataStoreMeta.getFieldCount();
		
		for(int i = 0; i < filedNo; i++) {
			IFieldMetaData fieldMeta = dataStoreMeta.getFieldMeta(i);
			IField field = (IField)record.getFieldAt(i);
			
			Integer w = (Integer)fieldMeta.getProperty( PROPERTY_NAME );
			if(w == null) {
				//fieldMeta.setProperty(PROPERTY_NAME, new Integer(0));
				w = new Integer(0);
			}
			Object value = field.getValue();
			String valueStr = "" + value;
			int displaySize = (field.getValue() == null? 0: valueStr.length());
			if(w.intValue() < displaySize) w = new Integer(displaySize);
			fieldMeta.setProperty(PROPERTY_NAME, w);
		}
	}

}
