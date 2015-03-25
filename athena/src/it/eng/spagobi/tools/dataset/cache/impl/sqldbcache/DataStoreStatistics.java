/*
 * SpagoBI, the Open Source Business Intelligence suite
 * © 2005-2015 Engineering Group
 *
 * This file is part of SpagoBI. SpagoBI is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 2.1 of the License, or any later version. 
 * SpagoBI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details. You should have received
 * a copy of the GNU Lesser General Public License along with SpagoBI. If not, see: http://www.gnu.org/licenses/.
 * The complete text of SpagoBI license is included in the COPYING.LESSER file. 
 */
package it.eng.spagobi.tools.dataset.cache.impl.sqldbcache;

import java.math.BigDecimal;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData.FieldType;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class DataStoreStatistics {
	
	IDataStore store;
	List<Properties> objectsTypeDimension;
	
	boolean extimateVarCharMemorySize;
	boolean extimateVarCharMemorySizeUsingSample;
	int sampleDimension;
	

	static private Logger logger = Logger.getLogger(DataStoreStatistics.class);
	
	public DataStoreStatistics(IDataStore store) {
		this.store = store;
		extimateVarCharMemorySize = true;
		extimateVarCharMemorySizeUsingSample = false;
		sampleDimension = 100;
	}
	
	/**
	 * @return the number of bytes used by the resultSet (approximate)
	 */
	public BigDecimal extimateStoreMemorySize() {
		BigDecimal recordMemorySize = extimateRecordMemorySize();
		BigDecimal storeMemorySize = recordMemorySize.multiply(new BigDecimal(store.getRecordsCount())) ;
		logger.debug("Dimension estimated for the new resultset [rowWeight*rows]: " + storeMemorySize + " ["+recordMemorySize+" * "+store.getRecordsCount()+"]");
		return storeMemorySize;
	}
	
	private BigDecimal extimateRecordMemorySize() {
		BigDecimal recordMemorySize = new BigDecimal(0);
		BigDecimal[] fieldsMemorySize = extimateFieldsMemorySize();
		for(int i = 0; i < fieldsMemorySize.length; i++) {
			if(fieldsMemorySize[i] != null) {
				recordMemorySize = recordMemorySize.add( fieldsMemorySize[i] );
			} else {
				logger.warn("Impossible to estimate size of field [" + i + "]");
			}
			
		}
		return recordMemorySize;
	}
	
	private BigDecimal[] extimateFieldsMemorySize() {
		
		IMetaData md = store.getMetaData();
		int fieldCount = md.getFieldCount();
		BigDecimal[] fieldsMemorySize = new BigDecimal[fieldCount];
	
		long recCount = 1;
		if(extimateVarCharMemorySize == false) {
			recCount = 1;
		} else {
			if(extimateVarCharMemorySizeUsingSample == true) {
				recCount = sampleDimension;
			} else {
				recCount = store.getRecordsCount();
			}
		}
		
		for(int i = 0; i < recCount; i++) {
			fieldsMemorySize = extimateFieldsMemorySize(store.getRecordAt(i), fieldsMemorySize);
		}
		
		return fieldsMemorySize;
	}
	
	private BigDecimal[] extimateFieldsMemorySize(IRecord record, BigDecimal[] fieldsMaxMemorySize) {
		
		BigDecimal[] fieldsMemorySize = null;
		
		try {
			IMetaData md = store.getMetaData();
			int fieldCount = md.getFieldCount();
			fieldsMemorySize = new BigDecimal[fieldCount];
			
			for (int i=0; i < fieldCount; i++){	
				IFieldMetaData fmd = md.getFieldMeta(i);
				try {
					normalizeFieldType(fmd);
					String fieldTypeName = fmd.getType().toString();
					
					BigDecimal fieldMemorySize = null;
					if(fieldTypeName.contains("String") && extimateVarCharMemorySize) {
						String value = (String)record.getFieldAt(i).getValue();
						int valueLength = value!=null? value.length(): 0;
						fieldMemorySize = new BigDecimal(valueLength);
					} else {
						fieldMemorySize = getBytesForType(fieldTypeName);
						if(fieldMemorySize == null) {
							logger.warn("Impossible to get memory size of field [" + fmd.getName() + "] of type [" + fieldTypeName + "]");
						}
					}
	
					if(fieldsMaxMemorySize[i] == null) fieldsMaxMemorySize[i] = fieldMemorySize;
					
					if(fieldsMaxMemorySize[i].compareTo(fieldMemorySize) < 0) {
						fieldsMemorySize[i] = fieldMemorySize;	
					} else {
						fieldsMemorySize[i] = fieldsMaxMemorySize[i];
					}	
				} catch(Throwable t) {
					throw new RuntimeException("An unexpected error occured while extimating field [" + fmd.getName() + "] memory size whose type is equal to [" + fmd.getType().toString() + "]", t);
				}
			}
		} catch(Throwable t) {
			throw new RuntimeException("An unexpected error occured while extimating fields memory size", t);
		}
		
		
		return fieldsMemorySize;
	}
	
	/**
	 * TODO this method modify the content of store metadata. This is dangerous. It should be rmoved.
	 *
	 * @param fmd the field to normalize. Normalization change from string to double the type of all 
	 * field whose role is MEASURE
	 */
	private void normalizeFieldType(IFieldMetaData fmd) {
		FieldType fieldRole = fmd.getFieldType(); // FieldType.MEASURE or FieldType.ATTRIBUTE
		String fieldTypeName = fmd.getType().toString();
		
		// in case of a measure with String type, convert it into a Double
		if (fieldRole.equals(FieldType.MEASURE) && fieldTypeName.contains("String")) {
			fmd.setType(java.lang.Double.class);
			logger.debug("Column type is string but the field is measure: converting it into a double");	
		} else if(fmd.getType().toString().contains("[B")) {  //BLOB		
			//TODO something else?
		} else if(fmd.getType().toString().contains("[C")) {	 //CLOB				
			//TODO something else?
		}
	}
	
	private BigDecimal getBytesForType(String type){
		BigDecimal toReturn = new BigDecimal(8); //for default sets a generic Object size
		
		try {
			List<Properties> objectsTypeDimension = getObjectsTypeDimension();
			for (int i=0, l= objectsTypeDimension.size(); i<l; i++){
				String typeName = ((Properties)objectsTypeDimension.get(i)).getProperty("name");
				if (type.contains(typeName)){
					toReturn = new BigDecimal(((Properties)objectsTypeDimension.get(i)).getProperty("bytes"));
					//logger.debug("Used configurated type: " + type + " - weight: " + toReturn.toString());
					break;
				}
			}
		} catch(Throwable t) {
			throw new RuntimeException("An unexpected error occured while calculating byte used by type [" + type + "]", t);
		}
		
		return toReturn;		
	}
	
	
	// ============================================================================================================
	// ACCESSOR METHODS
	// ============================================================================================================
	
	public List<Properties> getObjectsTypeDimension() {
		return objectsTypeDimension;
	}

	public void setObjectsTypeDimension(List<Properties> objectsTypeDimension) {
		this.objectsTypeDimension = objectsTypeDimension;
	}
	
	public IDataStore getStore() {
		return store;
	}

	public void setStore(IDataStore store) {
		this.store = store;
	}

	/**
	 * @return true if the varchar dimension is calculated using the max dimension calculated 
	 * over a sample set of store's records. False otherwise if the default size for string column 
	 * is returned for each column in each store.
	 */
	public boolean isExtimateVarCharMemorySize() {
		return extimateVarCharMemorySize;
	}

	public void setExtimateVarCharMemorySize(boolean extimateVarCharMemorySize) {
		this.extimateVarCharMemorySize = extimateVarCharMemorySize;
	}

	/**
	 * 
	 * @return false if the sample used to extimate varchar dimensions contains all the records of the 
	 * store, true otherwise
	 */
	public boolean isExtimateVarCharMemorySizeUsingSample() {
		return extimateVarCharMemorySizeUsingSample;
	}

	public void setExtimateVarCharMemorySizeUsingSample(boolean extimateVarCharMemorySizeUsingSample) {
		this.extimateVarCharMemorySizeUsingSample = extimateVarCharMemorySizeUsingSample;
	}

	/**
	 * 
	 * @return the number of record contained in the sample used to extimate varchar dimensions
	 */
	public int getSampleDimension() {
		return sampleDimension;
	}

	public void setSampleDimension(int sampleDimension) {
		this.sampleDimension = sampleDimension;
	}
	
	
	static public BigDecimal extimateMemorySize(IDataStore store, List<Properties> objectsTypeDimension) {
		DataStoreStatistics storeStatistics = new DataStoreStatistics(store);
		storeStatistics.setObjectsTypeDimension(objectsTypeDimension);
		return storeStatistics.extimateStoreMemorySize();
	}
}
