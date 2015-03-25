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
package it.eng.spagobi.tools.dataset.validation;

import it.eng.spagobi.commons.utilities.messages.MessageBuilder;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;

import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * 	This validator checks that the numeric columns contains numeric values
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public class NumericColumnValidator extends AbstractDatasetValidator {
	
	public static transient Logger logger = Logger.getLogger(NumericColumnValidator.class);
	


	/* (non-Javadoc)
	 * @see it.eng.spagobi.tools.dataset.validation.AbstractDatasetValidator#doValidateDataset(it.eng.spagobi.tools.dataset.common.datastore.IDataStore, java.util.Map)
	 */
	@Override
	public ValidationErrors doValidateDataset(IDataStore dataStore,
			Map<String, HierarchyLevel> hierarchiesColumnsToCheck) {
		MessageBuilder msgBuild = new MessageBuilder();
		ValidationErrors validationErrors = new ValidationErrors();
		for (Map.Entry<String, HierarchyLevel> entry : hierarchiesColumnsToCheck.entrySet())
		{
			logger.debug("Column Name= "+entry.getKey());
		    String columnName = entry.getKey();
		    HierarchyLevel hierarchyLevel = entry.getValue();
		    String columnType = hierarchyLevel.getColumn_type();
		    
		    //Check only columns marked with numeric types
		    if ((columnType != null) && (columnType.equalsIgnoreCase("numeric"))){
			    //Iterate the datastore (of the dataset) and check if values are ammissible
				Iterator it = dataStore.iterator();
				int columnIndex = dataStore.getMetaData().getFieldIndex(columnName); 
				int rowNumber = 0;
				while( it.hasNext() ) {
					IRecord record = (IRecord)it.next();
		    		IField field = record.getFieldAt(columnIndex);
		    		Object fieldValue = field.getValue(); 
		    		if(fieldValue != null)  {
		    			if (!fieldValue.toString().isEmpty()){
				    		if (!isNumeric(fieldValue)){
				    			String errorDescription = msgBuild.getMessage("dataset.wizard.validation.err.notanumber", getLocale());
    	    					errorDescription = errorDescription.replaceAll("%0", ((String)fieldValue).replaceAll("'", "\'"));
    	 
			    				//String errorDescription = "Error in validation: "+fieldValue+" is not a numeric value ";
			    				validationErrors.addError(rowNumber, columnIndex, field, errorDescription);
				    		}
		    			}
		    		}else {
		    			String errorDescription = msgBuild.getMessage("	dataset.wizard.validation.err.nullnotanumber", getLocale());
	    				validationErrors.addError(rowNumber, columnIndex, field, errorDescription);
		    		}
		    		rowNumber++;

				}
		    }
		    

		}
		
		
		return validationErrors;
	}
	
	private boolean isNumeric(Object fieldValue) {
		boolean isNumeric = true;
		try {
			Double.parseDouble(fieldValue.toString());
		} catch(Throwable t) {
			isNumeric = false;
		}

		return isNumeric;
	}

}
