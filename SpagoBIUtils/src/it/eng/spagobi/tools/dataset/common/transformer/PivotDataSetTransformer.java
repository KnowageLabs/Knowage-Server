/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.dataset.common.transformer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import it.eng.spagobi.tools.dataset.common.datastore.DataStore;
import it.eng.spagobi.tools.dataset.common.datastore.Field;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.datastore.Record;
import it.eng.spagobi.tools.dataset.common.metadata.FieldMetadata;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 * 
 */
public class PivotDataSetTransformer extends AbstractDataStoreTransformer {

	String pivotFieldName; // pivotColumn
	String valueFieldName; // pivotValue
	String groupFieldName; // pivotRow
	boolean numRows; 	   // defines whether the output columns must be numbered ie: x1, y1, z1, x2,y2,z2,x3,y,3,z3,... or not ie: x,y,z
	
	List pivotedFieldNames;

	private static transient Logger logger = Logger
			.getLogger(PivotDataSetTransformer.class);

	/*
	 * Prerequisites: the pivotDataSetTransform needs in input a datastore with the next structure:
	 * - X that identifies the row on which the values are aggregated(ie. the month)
	 * - SER that identifies the column that will pivotted (ie. SER1, SER2, SER3, SER4,..)
	 * - VAL that identifies the column with the real values of the series
	 * - xxx other fields that are not used in the operations of pivot
	 * 
	 * ie:
	 *  X                      SER           VAL         IDX   ....
	Gennaio            SER1           33            1
	Gennaio            SER2           12            1
	Gennaio            SER3           64            1
	Gennaio            SER4           21            1
	Febbraio           SER1           56            2
	Febbraio           SER2           35            2
	Febbraio           SER3           13            2
	Febbraio           SER4           75            2
	 */
	public PivotDataSetTransformer(String pivotFieldName,
			String valueFieldName, String groupFieldName, boolean numRows) {
		this.setPivotFieldName(pivotFieldName);
		this.setValueFieldName(valueFieldName);
		this.setGroupFieldName(groupFieldName);
		this.setNumRows(numRows);
		pivotedFieldNames = new ArrayList();
	}

	
	void transformDataSetMetaData(IDataStore dataStore) {
		IMetaData dataStoreMeta;
		int pivotFieldIndex;
		int valueFieldIndex;
		
		dataStoreMeta = dataStore.getMetaData();
		
		pivotFieldIndex = dataStoreMeta.getFieldIndex(getPivotFieldName());
		valueFieldIndex = dataStoreMeta.getFieldIndex(getValueFieldName());
		
		Class pivotedFieldType = dataStoreMeta.getFieldMeta( valueFieldIndex ).getType();
		
		dataStoreMeta = dataStore.getMetaData();
		dataStoreMeta.deleteFieldMetaDataAt( pivotFieldIndex );
		dataStoreMeta.deleteFieldMetaDataAt( valueFieldIndex-1 );
		
		for(int i = 0; i < pivotedFieldNames.size(); i++) {
			IFieldMetaData fieldMeta;			
			fieldMeta = new FieldMetadata((String)pivotedFieldNames.get(i), pivotedFieldType);			
			dataStoreMeta.addFiedMeta( fieldMeta );
		}
	}

	void transformDataSetRecords(IDataStore dataStore) {
		IMetaData dataStoreMeta;
		int pivotFieldIndex;
		int valueFieldIndex;
		int groupFieldIndex;

		List newRecords = new ArrayList();
		IRecord newRecord = null;
		Object selectedGroupValue = null;
		
		pivotedFieldNames = new ArrayList();
		
		dataStoreMeta = dataStore.getMetaData();
		pivotFieldIndex = dataStoreMeta.getFieldIndex(getPivotFieldName());
		valueFieldIndex = dataStoreMeta.getFieldIndex(getValueFieldName());
		groupFieldIndex = dataStoreMeta.getFieldIndex(getGroupFieldName());

		/************************************************************************************
		* Renaming output column for dynamic records (alias record with different number of columns )
		* if requested
		/************************************************************************************/
		if (numRows){
			String precGroupField = null;
			String precPivotField = null;
			int cont = 0;
			Iterator iterator = dataStore.iterator();
			while (iterator.hasNext()) {
				IRecord record = (IRecord) iterator.next();
	
				IField pivotField = record.getFieldAt(pivotFieldIndex);
				IField valueField = record.getFieldAt(valueFieldIndex);
				IField groupField = record.getFieldAt(groupFieldIndex);
				 
				if (precGroupField == null)
					precGroupField = groupField.getValue().toString();
				
				if (precPivotField == null)
					precPivotField = pivotField.getValue().toString();
				
				if (precGroupField.equalsIgnoreCase(groupField.getValue().toString())){
					if (precPivotField.equalsIgnoreCase(pivotField.getValue().toString())){
						pivotField.setValue(pivotField.getValue().toString()+String.valueOf(cont));
						cont++;
					}
					else{
						cont = 0;
						precPivotField = pivotField.getValue().toString();
						pivotField.setValue(pivotField.getValue().toString()+String.valueOf(cont));	
						cont++;
					}
				}
				else{
					cont = 0;
					precGroupField = groupField.getValue().toString();
					precPivotField = pivotField.getValue().toString();
					pivotField.setValue(pivotField.getValue().toString()+String.valueOf(cont));
					cont++;
				}
			}
		}

		
		Iterator pivotedFieldNamesIterator = dataStore.getFieldDistinctValues(pivotFieldIndex).iterator();		
		while ( pivotedFieldNamesIterator.hasNext() ) {
			pivotedFieldNames.add( pivotedFieldNamesIterator.next() );
		}
		
		Iterator it = dataStore.iterator();
		while (it.hasNext()) {
			IRecord record = (IRecord) it.next();

			IField pivotField = record.getFieldAt(pivotFieldIndex);
			IField valueField = record.getFieldAt(valueFieldIndex);
			IField groupField = record.getFieldAt(groupFieldIndex);

			if(selectedGroupValue == null || !selectedGroupValue.toString().equals( groupField.getValue().toString() )) {
				selectedGroupValue = groupField.getValue();
				if(newRecord != null) {
					newRecord.getFields().remove(pivotFieldIndex);
					newRecord.getFields().remove(valueFieldIndex-1);
					newRecords.add( newRecord );
				}
				newRecord = record;
								
				for (int i = 0; i < pivotedFieldNames.size(); i++) {					
					//newRecord.appendField(new Field(null));
					newRecord.appendField(new Field(0));
				}
				
				
			}
			
			int pivotedFieldIndex = dataStoreMeta.getFieldCount() + pivotedFieldNames.indexOf( pivotField.getValue() );
			newRecord.getFieldAt( pivotedFieldIndex ).setValue( valueField.getValue() );
		}
		if (newRecord != null){
			newRecord.getFields().remove(pivotFieldIndex);
			newRecord.getFields().remove(valueFieldIndex-1);
			newRecords.add( newRecord );
		
			((DataStore) dataStore).setRecords(newRecords);
		}
	}

	public String getPivotFieldName() {
		return pivotFieldName;
	}

	public void setPivotFieldName(String pivotFieldName) {
		this.pivotFieldName = pivotFieldName;
	}

	public String getValueFieldName() {
		return valueFieldName;
	}

	public void setValueFieldName(String valueFieldName) {
		this.valueFieldName = valueFieldName;
	}

	public String getGroupFieldName() {
		return groupFieldName;
	}

	public void setGroupFieldName(String groupFieldName) {
		this.groupFieldName = groupFieldName;
	}
	
	
	/**
	 X                      SER           VAL         IDX   ....
Gennaio            SER1           33            1
Gennaio            SER2           12            1
Gennaio            SER3           64            1
Gennaio            SER4           21            1
Febbraio           SER1           56            2
Febbraio           SER2           35            2
Febbraio           SER3           13            2
Febbraio           SER4           75            2
	 */
	public static void main(String[] args) {
		IDataStore dataStore;
		IRecord record;
		
		dataStore = new DataStore();
		
		dataStore.getMetaData().addFiedMeta(new FieldMetadata("X", String.class ));
		dataStore.getMetaData().addFiedMeta(new FieldMetadata("SER", String.class));
		dataStore.getMetaData().addFiedMeta(new FieldMetadata("VAL", Double.class));
		dataStore.getMetaData().addFiedMeta(new FieldMetadata("IDX", Integer.class));
		
		// ----------------------------------------------------
		
		record = new Record();
		record.appendField(new Field("Gennaio"));
		record.appendField(new Field("SER1"));
		record.appendField(new Field(new Double(33)));
		record.appendField(new Field(new Integer(1)));
		dataStore.appendRecord(record);
		
		record = new Record();
		record.appendField(new Field("Gennaio"));
		record.appendField(new Field("SER2"));
		record.appendField(new Field(new Field(new Double(12))));
		record.appendField(new Field(new Integer(1)));
		dataStore.appendRecord(record);
		
		record = new Record();
		record.appendField(new Field("Gennaio"));
		record.appendField(new Field("SER3"));
		record.appendField(new Field(new Field(new Double(64))));
		record.appendField(new Field(new Integer(1)));
		dataStore.appendRecord(record);
		
		record = new Record();
		record.appendField(new Field("Gennaio"));
		record.appendField(new Field("SER4"));
		record.appendField(new Field(new Field(new Double(21))));
		record.appendField(new Field(new Integer(1)));
		dataStore.appendRecord(record);
		
		// ----------------------------------------------------
		
		record = new Record();
		record.appendField(new Field("Febbraio"));
		record.appendField(new Field("SER1"));
		record.appendField(new Field("56"));
		record.appendField(new Field(new Integer(2)));
		dataStore.appendRecord(record);
		
		record = new Record();
		record.appendField(new Field("Febbraio"));
		record.appendField(new Field("SER2"));
		record.appendField(new Field(new Field(new Double(35))));
		record.appendField(new Field(new Integer(2)));
		dataStore.appendRecord(record);
		
		record = new Record();
		record.appendField(new Field("Febbraio"));
		record.appendField(new Field("SER3"));
		record.appendField(new Field(new Field(new Double(13))));
		record.appendField(new Field(new Integer(2)));
		dataStore.appendRecord(record);
		
		record = new Record();
		record.appendField(new Field("Febbraio"));
		record.appendField(new Field("SER4"));
		record.appendField(new Field(new Field(new Double(75))));
		record.appendField(new Field(new Integer(2)));
		dataStore.appendRecord(record);
		
		IDataStoreTransformer transformer = new PivotDataSetTransformer("SER", "VAL", "X", false);
		transformer.transform(dataStore);
		System.out.println(dataStore.toXml());
	}


	/**
	 * @return the numRows
	 */
	public boolean isNumRows() {
		return numRows;
	}


	/**
	 * @param numRows the numRows to set
	 */
	public void setNumRows(boolean numRows) {
		this.numRows = numRows;
	}

}
