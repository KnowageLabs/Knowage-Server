/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 * 
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
