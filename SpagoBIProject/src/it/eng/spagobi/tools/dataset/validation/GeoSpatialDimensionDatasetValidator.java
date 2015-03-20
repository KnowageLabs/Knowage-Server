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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import it.eng.spagobi.commons.utilities.messages.MessageBuilder;
import it.eng.spagobi.meta.model.olap.Level;
import it.eng.spagobi.metamodel.HierarchyWrapper;
import it.eng.spagobi.metamodel.MetaModelWrapper;
import it.eng.spagobi.metamodel.SiblingsFileWrapper;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.measurecatalogue.MeasureCatalogue;
import it.eng.spagobi.tools.dataset.measurecatalogue.MeasureCatalogueSingleton;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public class GeoSpatialDimensionDatasetValidator  extends AbstractDatasetValidator {

	public static transient Logger logger = Logger.getLogger(GeoSpatialDimensionDatasetValidator.class);
	public final String GEO_HIERARCHY_NAME = "geo"; //this validator check only hierarchies with this name
	public final String GEO_DIMENSION_NAME = "geo";

	public GeoSpatialDimensionDatasetValidator(IDatasetValidator child){
		childValidator = child;
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.tools.dataset.validation.AbstractDatasetValidator#doValidateDataset(it.eng.spagobi.tools.dataset.common.datastore.IDataStore)
	 */
	@Override
	public ValidationErrors doValidateDataset(IDataStore dataStore,Map<String, HierarchyLevel> hierarchiesColumnsToCheck ) {
		MessageBuilder msgBuild = new MessageBuilder();
		ValidationErrors validationErrors = new ValidationErrors();
		MeasureCatalogue measureCatalogue = MeasureCatalogueSingleton.getMeasureCatologue();
		if (measureCatalogue.isValid()){
			MetaModelWrapper metamodelWrapper = measureCatalogue.getMetamodelWrapper();

			
			for (Map.Entry<String, HierarchyLevel> entry : hierarchiesColumnsToCheck.entrySet())
			{
			    logger.debug("Column Name= "+entry.getKey() + " / HierarchyLevel" + entry.getValue());
			    String columnName = entry.getKey();
			    HierarchyLevel hierarchyLevel = entry.getValue();
			    if (hierarchyLevel.isValidEntry()){
			    	String hierarchyName = hierarchyLevel.getHierarchy_name();
			    	String hierarchyLevelName = hierarchyLevel.getLevel_name();
			    	if (hierarchyName.equalsIgnoreCase(GEO_HIERARCHY_NAME)){
			    		HierarchyWrapper hierarchy = metamodelWrapper.getHierarchy(GEO_HIERARCHY_NAME);
			    		if (hierarchy != null){
			    			if (hierarchy.getName().equalsIgnoreCase(hierarchyName)){
				    			//List<Level> levels = hierarchy.getLevels();
				    			Level level = hierarchy.getLevel(hierarchyLevelName);
				    			if (level != null){
				    				String levelName = level.getName();
					    			//IDataStore dataStoreLevel = hierarchy.getMembers(levelName); //return a dataStore with one column only

					    			/* Test for what values use in the validation */
					    			Set<String> admissibleValues = testValidationCriteria(metamodelWrapper, hierarchy, dataStore, levelName,
					    					columnName);
					    			
					    			//Default Criteria commented
					    			//Set<String> admissibleValues = dataStoreLevel.getFieldDistinctValuesAsString(0);
					    			String hint = generateHintValues(admissibleValues);

					    			//Iterate the datastore (of the dataset) and check if values are ammissible
					    			Iterator it = dataStore.iterator();
					    			int columnIndex = dataStore.getMetaData().getFieldIndex(columnName); 
					    			int rowNumber = 0;
					    			while( it.hasNext() ) {
					    	    		IRecord record = (IRecord)it.next();
					    	    		IField field = record.getFieldAt(columnIndex);
					    	    		Object fieldValue = field.getValue(); 
					    	    		if(fieldValue != null)  {
					    	    			if (fieldValue instanceof String){
					    	    				String valueString = (String)fieldValue;
					    	    				//Case Empty String
					    	    				if (valueString.isEmpty()){
					    	    					String errorDescription = msgBuild.getMessage("dataset.wizard.validation.err.emptyvalue", getLocale());
					    	    					errorDescription = errorDescription
					    	    						.replaceAll("%0", GEO_HIERARCHY_NAME)
					    	    						.replaceAll("%1", levelName)
					    	    						.replaceAll("%2", hint);
					    	    					//errorDescription = "Error in validation: empty value is not valid for hierarchy "+GEO_HIERARCHY_NAME+" on level "+levelName+". "+hint+"...";
						    	    				validationErrors.addError(rowNumber, columnIndex, field, errorDescription);
					    	    				} else {
							    	    			if (!admissibleValues.contains(fieldValue))
							    	    			{
							    	    				String errorDescription = msgBuild.getMessage("dataset.wizard.validation.err.wrongvalue", getLocale());
							    	    				String f = (fieldValue.toString()).replace("'", " ");
							    	    				errorDescription = errorDescription
						    	    						.replaceAll("%0", f)
						    	    						.replaceAll("%1", GEO_HIERARCHY_NAME)
						    	    						.replaceAll("%2", levelName)
						    	    						.replaceAll("%3", hint);
							    	    				//errorDescription = "Error in validation: "+fieldValue+" is not valid for hierarchy "+GEO_HIERARCHY_NAME+" on level "+levelName+". "+hint+"...";
							    	    				validationErrors.addError(rowNumber, columnIndex, field, errorDescription);
							    	    			}
					    	    				}
					    	    			} else {
						    	    			if (!admissibleValues.contains(fieldValue))
						    	    			{
						    	    				String errorDescription = msgBuild.getMessage("dataset.wizard.validation.err.wrongvalue", getLocale());
					    	    					errorDescription = errorDescription
					    	    						.replaceAll("%0", (fieldValue.toString()).replace("'", " "))
					    	    						.replaceAll("%1", GEO_HIERARCHY_NAME)
					    	    						.replaceAll("%2", levelName)
					    	    						.replaceAll("%3", hint);
						    	    				//String errorDescription = "Error in validation: "+fieldValue+" is not valid for hierarchy "+GEO_HIERARCHY_NAME+" on level "+levelName+". "+hint+"...";
						    	    				validationErrors.addError(rowNumber, columnIndex, field, errorDescription);
						    	    			}
					    	    			}

					    	    		} else {
					    	    			String errorDescription = msgBuild.getMessage("dataset.wizard.validation.err.nullvalue", getLocale());
			    	    					errorDescription = errorDescription
			    	    						.replaceAll("%0", GEO_HIERARCHY_NAME)
			    	    						.replaceAll("%1", levelName)
			    	    						.replaceAll("%2", hint);
				    	    				//errorDescription = "Error in validation: null is not valid for hierarchy "+GEO_HIERARCHY_NAME+" on level "+levelName+". "+hint+"...";
				    	    				validationErrors.addError(rowNumber, columnIndex, field, errorDescription);
					    	    		}
					    	    		rowNumber++;
					    	    	}
				    			} else {
					    			logger.warn("Attention: the hierarchy "+GEO_HIERARCHY_NAME+" doesn't contain a level "+hierarchyLevelName);
				    			}
				    			

			    			}		    			
			    		} else {
			    			logger.warn("Attention: the validation model doesn't contain a hierarchy with name "+GEO_HIERARCHY_NAME+". Validation will not be performed.");
			    		}

			    	}
			    }

			}			
		}
	
		return validationErrors;
	}
	
	/**
	 *
	 * @return the admissible values to use for the validation
	 */
	public Set<String> testValidationCriteria(MetaModelWrapper metaModelWrapper, HierarchyWrapper hierarchy, IDataStore datastoreToValidate, String levelName, String columnNameOnDataset ){
		Object fieldValue = null;
		
		//Get the first not null or not empty value of the datastore to validate
		Iterator it = datastoreToValidate.iterator();
		int columnIndex = datastoreToValidate.getMetaData().getFieldIndex(columnNameOnDataset); 
		while( it.hasNext() ) {
	   		IRecord record = (IRecord)it.next();
	    	IField field = record.getFieldAt(columnIndex);
	    	fieldValue = field.getValue();   	
	    	if (fieldValue != null){
	    		if (fieldValue instanceof String){
	    			String stringValue = (String) fieldValue;
	    			if (!stringValue.isEmpty()){
	    				break;
	    			}
	    		} else {
	    			break;
	    		}
	    	}
		}
	

		
		//then check if the value is ammissible for the Level members (default values used as identifiers values)
		IDataStore dataStoreLevel = hierarchy.getMembers(levelName); //return a dataStore with one column only
		Set<String> admissibleValues = dataStoreLevel.getFieldDistinctValuesAsString(0);
		if(fieldValue != null)  {
			if (admissibleValues.contains(fieldValue)){
				return admissibleValues; // use default criteria
			} else {
				//otherwise check the values on the siblings columns (if any)
				SiblingsFileWrapper siblingsFile = metaModelWrapper.getSiblingsFileWrapper();
				if (siblingsFile != null){
					List<String> siblingsColumnsNames = siblingsFile.getLevelSiblings(GEO_DIMENSION_NAME, GEO_HIERARCHY_NAME, levelName);
					
					if (!siblingsColumnsNames.isEmpty()){
						for (String siblingColumnName : siblingsColumnsNames){
							IDataStore dataStoreSibling = hierarchy.getSiblingValues(siblingColumnName);
							Set<String> admissibleValuesSibling  = dataStoreSibling.getFieldDistinctValuesAsString(0);
							if (admissibleValuesSibling.contains(fieldValue)){
								//found a valid criteria, validate dataset with this values
								return admissibleValuesSibling;
							}
						}
					}
				}
				
				
				
			}
		} 
		//in all other cases use default validation
		return admissibleValues;
		
		
		
		
	}
	
	//Generate a String with some possible admissible values as an hint
	public String generateHintValues(Set<String> admissibleValues){
		MessageBuilder msgBuild = new MessageBuilder();
		String hint = msgBuild.getMessage("dataset.wizard.validation.hints", getLocale());
		hint += ": ";
		
		Iterator<String> it = admissibleValues.iterator();
		int counter = 0;
        while (it.hasNext()) {
        	if (counter < 3){
            	hint = hint + it.next()+", ";
                counter++;
        	} else {
        		break;
        	}

        } 
        return hint;
		
	}
	
	public boolean checkValue(Set admissibleValues, Object fieldValue){
		boolean findString = false;
		boolean findNumber = false;
		
		if (fieldValue instanceof String){
			findString = true;
		} else if (fieldValue instanceof Number){
			findNumber = true;
		}
		
		for (Object admissibleValue : admissibleValues) {
			if (admissibleValue instanceof String){
				if (findString) {
					if (admissibleValue.equals(fieldValue)){
						return true;
					}
				}
			} else if (admissibleValue instanceof Number){
				if (findNumber){
					double admissibleValueDouble = ((Number)admissibleValue).doubleValue();
					double fieldValueDouble = ((Number)fieldValue).doubleValue();
					if (admissibleValueDouble == fieldValueDouble){
						return true;
					}

				}
			}
		}
		
		return false;
	}
	


}
