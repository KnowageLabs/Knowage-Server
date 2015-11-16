/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.dataset.common.transformer;

import it.eng.spago.base.SourceBeanAttribute;
import it.eng.spagobi.tools.dataset.common.datastore.Field;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.datastore.Record;
import it.eng.spagobi.tools.dataset.common.metadata.FieldMetadata;
import it.eng.spagobi.tools.dataset.common.metadata.MetaData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * @author Angelo Bernabei
 *         angelo.bernabei@eng.it
 */
public class PivotingTransformer implements IDataTransformer {
	private static transient Logger logger = Logger.getLogger(PivotingTransformer.class);
    
	/**
	 * Gets the result of dataset and execute a pivot operation for split data on row, 
	 * column and value specified
	 * into dataset interface.
     * The dataset has a structure like: ROW | SER | VAL | other info
	 */
    public List transformData(List records, String pivotColumn, String pivotRow, String pivotValue){
    	logger.info("IN");
	    
    	List newRecords = new ArrayList();
	    IRecord newRecord = null;
	    String newFName = null;
	    String newFValue = null;
	    
	    Iterator it = records.iterator();
	    boolean sameRow = true;
	    String rowValue = "";
	    newRecord = new Record();

    	while (it.hasNext()){
			IRecord record =(IRecord) it.next();	
			MetaData dataStoreMeta = (MetaData)record.getDataStore().getMetaData();
			List fields = record.getFields();
			for(int j = 0; j < fields.size(); j++) {		
				IField field = (IField)fields.get(j);
				String fieldName = dataStoreMeta.getFieldAlias(j);
				String fieldValue = "" + field.getValue();
				
				//checks if the field is a row, a column or a value specified into configuration and manages them
				if (fieldName.equalsIgnoreCase(pivotRow)){
					if (rowValue.equals("")) rowValue = fieldValue;
					if (!(rowValue.trim()).equalsIgnoreCase(fieldValue.trim())){
						rowValue = fieldValue;
						newRecords.add(newRecord);
						newRecord = new Record();
					}
					if (newRecord.getFieldAt(dataStoreMeta.getFieldIndex(fieldName) ) == null)
						newRecord.appendField(field);
				}
				else if (fieldName.equalsIgnoreCase(pivotColumn)){
					newFName = fieldValue;
					IField fv = record.getFieldAt(dataStoreMeta.getFieldIndex(pivotValue));
					if (fv == null){
						logger.error("Pivot value column '"+ pivotValue +"' not found into dataset. Pivot not applicated!");
						return null;
					}
					SourceBeanAttribute newFObject =(SourceBeanAttribute) fv.getValue();
					newFValue = newFObject.getValue().toString();
					
					FieldMetadata fieldMeta = new FieldMetadata();
					fieldMeta.setName(newFName);
					fieldMeta.setType(newFValue.getClass());
					dataStoreMeta.addFiedMeta(fieldMeta);
					IField newf = new Field(newFValue);
					newRecord.appendField(newf);
				}
				else if (fieldName.equalsIgnoreCase(pivotValue)){
					//skip field
				}
				else {
					//if the field isn't into record comes added
					if (newRecord.getFieldAt(dataStoreMeta.getFieldIndex(fieldName)) == null)
						newRecord.appendField(field);
				}
			}	    	
		}
    	//Adds the last record
		if (newRecord != null) newRecords.add(newRecord);
	    
	    logger.info("OUT");
    	return newRecords;
    }
    
    public List transformData(List records){
    	logger.info("IN");
    	logger.debug("Method not implemented");
    	logger.info("OUT");
    	return null;
    }
}
