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
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.measurecatalogue.MeasureCatalogue;
import it.eng.spagobi.tools.dataset.measurecatalogue.MeasureCatalogueSingleton;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public class TimeDimensionDatasetValidator  extends AbstractDatasetValidator {
	
	
	public static transient Logger logger = Logger.getLogger(TimeDimensionDatasetValidator.class);
	public final String TIME_HIERARCHY_NAME = "time"; //this validator check only hierarchies with this name
	
	public TimeDimensionDatasetValidator(IDatasetValidator child){
		childValidator = child;
	}

    /* (non-Javadoc)
     * @see it.eng.spagobi.tools.dataset.validation.IDatasetValidator#validateDataset(it.eng.spagobi.tools.dataset.common.datastore.IDataStore)
     */
	@Override
	public ValidationErrors doValidateDataset(IDataStore dataStore,Map<String, HierarchyLevel> hierarchiesColumnsToCheck ) {
		ValidationErrors validationErrors = new ValidationErrors();
		//MeasureCatalogue measureCatalogue = new MeasureCatalogue();
		MeasureCatalogue measureCatalogue = MeasureCatalogueSingleton.getMeasureCatologue();
		if (measureCatalogue.isValid()){
			MetaModelWrapper metamodelWrapper = measureCatalogue.getMetamodelWrapper();
			
			for (Map.Entry<String, HierarchyLevel> entry : hierarchiesColumnsToCheck.entrySet())
			{
				MessageBuilder msgBuild = new MessageBuilder();
			    logger.debug("Column Name= "+entry.getKey() + " / HierarchyLevel" + entry.getValue());
			    String columnName = entry.getKey();
			    HierarchyLevel hierarchyLevel = entry.getValue();
			    if (hierarchyLevel.isValidEntry()){
			    	String hierarchyName = hierarchyLevel.getHierarchy_name();
			    	String hierarchyLevelName = hierarchyLevel.getLevel_name();
			    	if (hierarchyName.equalsIgnoreCase(TIME_HIERARCHY_NAME)){
			    		HierarchyWrapper hierarchy = metamodelWrapper.getHierarchy(TIME_HIERARCHY_NAME);
			    		if (hierarchy != null){
			    			if (hierarchy.getName().equalsIgnoreCase(hierarchyName)){
				    			List<Level> levels = hierarchy.getLevels();
				    			Level level = hierarchy.getLevel(hierarchyLevelName);
				    			if (level != null){
				    				String levelName = level.getName();
					    			IDataStore dataStoreLevel = hierarchy.getMembers(levelName); //return a dataStore with one column only

					    			Set<String> admissibleValues = dataStoreLevel.getFieldDistinctValuesAsString(0);
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
					    	    			if (!admissibleValues.contains(fieldValue))
					    	    			{
					    	    				String errorDescription = msgBuild.getMessage("dataset.wizard.validation.err.wrongvalue", getLocale());
				    	    					errorDescription = errorDescription
				    	    						.replaceAll("%0", ((String)fieldValue).replaceAll("'", "\'"))
				    	    						.replaceAll("%1", TIME_HIERARCHY_NAME)
				    	    						.replaceAll("%2", levelName)
				    	    						.replaceAll("%3", hint);
					    	    				//String errorDescription = "Error in validation: "+fieldValue+" is not valid for hierarchy "+TIME_HIERARCHY_NAME+" on level "+levelName+". "+hint+"...";
					    	    				validationErrors.addError(rowNumber, columnIndex, field, errorDescription);
					    	    			}
					    	    		} else {
					    	    			String errorDescription = msgBuild.getMessage("dataset.wizard.validation.err.nullvalue", getLocale());
			    	    					errorDescription = errorDescription
			    	    						.replaceAll("%0", TIME_HIERARCHY_NAME)
			    	    						.replaceAll("%1", levelName)
			    	    						.replaceAll("%2", hint);
				    	    				//String errorDescription = "Error in validation: null is not valid for hierarchy "+TIME_HIERARCHY_NAME+" on level "+levelName+". "+hint+"...";
				    	    				validationErrors.addError(rowNumber, columnIndex, field, errorDescription);
					    	    		}
					    	    		rowNumber++;
					    	    	}
				    			} else {
					    			logger.warn("Attention: the hierarchy "+TIME_HIERARCHY_NAME+" doesn't contain a level "+hierarchyLevelName);
				    			}
				    			

			    			}		    			
			    		} else {
			    			logger.warn("Attention: the validation model doesn't contain a hierarchy with name "+TIME_HIERARCHY_NAME+". Validation will not be performed.");
			    		}

			    	}
			    }

			}			
		}
		


		return validationErrors;
	}
	
	//Generate a String with some possible admissible values as an hint
	public String generateHintValues(Set<String> admissibleValues){
		MessageBuilder msgBuild = new MessageBuilder();
		String hint = msgBuild.getMessage("dataset.wizard.validation.hints", getLocale());
		
		Iterator<String> it = admissibleValues.iterator();
		int counter = 0;
        while (it.hasNext()) {
        	if (counter < 3){
            	hint = hint+it.next()+", ";
                counter++;
        	} else {
        		break;
        	}

        } 
        return hint;
		
	}	

}
