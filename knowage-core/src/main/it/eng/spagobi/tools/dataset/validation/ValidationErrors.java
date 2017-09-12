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
package it.eng.spagobi.tools.dataset.validation;

import it.eng.spagobi.tools.dataset.common.datastore.IField;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;



/**
 * This class contains the fields that haven't passed the validation
 * 
 * 
 * @author Marco Cortella (marco.cortella@eng.it)
 */
public class ValidationErrors {

	

	private static transient Logger logger = Logger.getLogger(ValidationErrors.class);

	//The key is the rowNumber, for each rowNumber there is a List of ErrorField (columnNumber,Ifield)
	Map<Integer,List<ErrorField>> errorsMap;
	
	public ValidationErrors(){
		errorsMap = new HashMap<Integer,List<ErrorField>>();
	}
	
	/*
	 * add error on a specific rowNumber and columnNumber
	 */
	public void addError(Integer rowNumber ,Integer columnNumber, IField field, String errorDescription){
		List <ErrorField> errorsOnRecord = errorsMap.get(rowNumber);
		if (errorsOnRecord == null){
			errorsOnRecord = new ArrayList<ErrorField>();
			ErrorField errorField = new ErrorField(columnNumber, field, errorDescription);
			errorsOnRecord.add(errorField);
			errorsMap.put(rowNumber, errorsOnRecord);
		} else {
			ErrorField errorField = new ErrorField(columnNumber, field, errorDescription);
			errorsOnRecord.add(errorField);
			errorsMap.put(rowNumber, errorsOnRecord);
		}
		
	}
	
	/*
	 * If isEmpty = true, validation is OK
	 */
	public boolean isEmpty(){
		return errorsMap.isEmpty();
	}
	
	/*
	 * get errors on row
	 */
	public List<ErrorField> getErrorsOnRow(Integer rowNumber){
		if (rowNumber != null){
			List<ErrorField> errorsOnRow = errorsMap.get(rowNumber);
			return errorsOnRow;
		}
		return null;
	}
	
	/*
	 * get errors on Column
	 */
	public List<ErrorField> getErrorsOnColumn(Integer columnNumber){
		List<ErrorField> errorsOnColumn = new ArrayList<ErrorField>();
		for (Map.Entry<Integer, List<ErrorField>> entry : errorsMap.entrySet())
		{
		    logger.debug("Error Field : columnIndex = "+entry.getKey() + " / errorField" + entry.getValue());
		    
		   List<ErrorField> rowErrors = entry.getValue();
		   for (ErrorField  errorField : rowErrors){
			   if (errorField.getColumnIndex() == columnNumber.intValue()){
				   errorsOnColumn.add(errorField);
			   }
		   }
		}
		return errorsOnColumn;
	}
	
	/*
	 * get all Errors
	 */
	public Map<Integer,List<ErrorField>> getAllErrors(){
		return errorsMap;
	}

	/*
	 * Copies the content of a ValidationErrors object inside the errorsMap of the current object
	 */
	public void addAll(ValidationErrors validationErrors){
		Map<Integer, List<ErrorField>> otherErrorsMap = validationErrors.getAllErrors();
		if (errorsMap.isEmpty()){
			errorsMap.putAll(otherErrorsMap);
		}{
			//merge the two maps, adding values to the List<ErrorField> for the same Key
			for (Map.Entry<Integer, List<ErrorField>> entry : otherErrorsMap.entrySet()) {
				List<ErrorField> errorFields = errorsMap.get(entry.getKey());
				if (errorFields != null){
					errorFields.addAll(entry.getValue());
					errorsMap.put(entry.getKey(), errorFields);
				} else {
					errorFields = otherErrorsMap.get(entry.getKey());
					errorsMap.put(entry.getKey(), errorFields);
				}
			}
		}
	}
	

	
	
	
}
