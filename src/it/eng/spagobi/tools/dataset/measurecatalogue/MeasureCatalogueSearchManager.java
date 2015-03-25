 /* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.tools.dataset.measurecatalogue;

import it.eng.spagobi.tools.dataset.bo.IDataSet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
/**
 * 
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 * 
 * This class search the measures in the catalogue.
 * There is more than one type os searc 
 *
 */
public class MeasureCatalogueSearchManager {

	/**
	 * Search the measures that has a property with a specific value
	 * @param measureCatalogue
	 * @param alias
	 * @return
	 */
	public static List<MeasureCatalogueMeasure> searchMeasureByPropery( MeasureCatalogue measureCatalogue, String propertyName, Object propertyValue){
		List<MeasureCatalogueMeasure> toreturn = new ArrayList<MeasureCatalogueMeasure>();
    	for (Iterator<MeasureCatalogueMeasure> iterator = measureCatalogue.getMeasures().iterator(); iterator.hasNext();) {
    		MeasureCatalogueMeasure measure =  iterator.next();
    		Object property =  measure.getProperty(propertyName);
    		if(property!=null && property.equals(propertyValue)){
    			toreturn.add(measure);
    		}
    	}
    	return toreturn;
	}

	/**
	 * Search the measures by alias
	 * @param measureCatalogue
	 * @param alias
	 * @return
	 */
	public static List<MeasureCatalogueMeasure> searchMeasureByAlias( MeasureCatalogue measureCatalogue, String alias){
		List<MeasureCatalogueMeasure> toreturn = new ArrayList<MeasureCatalogueMeasure>();
    	for (Iterator<MeasureCatalogueMeasure> iterator = measureCatalogue.getMeasures().iterator(); iterator.hasNext();) {
    		MeasureCatalogueMeasure measure =  iterator.next();
    		if(measure.getAlias().equals(alias)){
    			toreturn.add(measure);
    		}
    	}
    	return toreturn;
	}

	
	/**
	 * Search all the measures contained in the dataset..
	 * It does not considre inner objects (for example datasource)
	 * @param dataSetLabel
	 * @return
	 */
	public static List<MeasureCatalogueMeasure> searchMeasureByDataSetCriteria( MeasureCatalogue measureCatalogue, IDataSet dataSetLabel){
		List<MeasureCatalogueMeasure> toreturn = new ArrayList<MeasureCatalogueMeasure>();
    	for (Iterator<MeasureCatalogueMeasure> iterator = measureCatalogue.getMeasures().iterator(); iterator.hasNext();) {
    		MeasureCatalogueMeasure measure =  iterator.next();
    		IDataSet measureDataSet = measure.getDataSet();

    		if(dataSetLabel!=null){//if the passed dataset is null, returns all the measures
    			
        		if(dataSetLabel.getLabel()!=null && (measureDataSet.getLabel()==null || measureDataSet.getLabel().equals( dataSetLabel.getLabel()))){
        			continue;
        		}

        		if(dataSetLabel.isFlatDataset() != dataSetLabel.isFlatDataset()){
        			continue;
        		}
        		
        		if(dataSetLabel.isPersisted() != dataSetLabel.isPersisted()){
        			continue;
        		}
        		
        		if(dataSetLabel.isPublic() != dataSetLabel.isPublic()){
        			continue;
        		}
        		
    			
        		if(dataSetLabel.getCategoryCd()!=null && (measureDataSet.getCategoryCd()==null || measureDataSet.getCategoryCd().equals( dataSetLabel.getCategoryCd()))){
        			continue;
        		}
        		
        		if(dataSetLabel.getCategoryId()!=null && (measureDataSet.getCategoryId()==null || measureDataSet.getCategoryId().equals( dataSetLabel.getCategoryId()))){
        			continue;
        		}
        		
        		if(dataSetLabel.getConfiguration()!=null && (measureDataSet.getConfiguration()==null || measureDataSet.getConfiguration().equals( dataSetLabel.getConfiguration()))){
        			continue;
        		}

        		
        		if(dataSetLabel.getDateIn()!=null && (measureDataSet.getDateIn()==null || measureDataSet.getDateIn().equals( dataSetLabel.getDateIn()))){
        			continue;
        		}
        		
        		if(dataSetLabel.getDescription()!=null && (measureDataSet.getDescription()==null || measureDataSet.getDescription().equals( dataSetLabel.getDescription()))){
        			continue;
        		}
        		
        		if(dataSetLabel.getDsMetadata()!=null && (measureDataSet.getDsMetadata()==null || measureDataSet.getDsMetadata().equals( dataSetLabel.getDsMetadata()))){
        			continue;
        		}
        		
        		if(dataSetLabel.getName()!=null && (measureDataSet.getName()==null || measureDataSet.getName().equals( dataSetLabel.getName()))){
        			continue;
        		}
        		
        		if(dataSetLabel.getOwner()!=null && (measureDataSet.getOwner()==null || measureDataSet.getOwner().equals( dataSetLabel.getOwner()))){
        			continue;
        		}
        		
        		if(dataSetLabel.getParameters()!=null && (measureDataSet.getParameters()==null || measureDataSet.getParameters().equals( dataSetLabel.getParameters()))){
        			continue;
        		}
        		
        		if(dataSetLabel.getPivotColumnName()!=null && (measureDataSet.getPivotColumnName()==null || measureDataSet.getPivotColumnName().equals( dataSetLabel.getPivotColumnName()))){
        			continue;
        		}
        		
        		if(dataSetLabel.getPivotColumnValue()!=null && (measureDataSet.getPivotColumnValue()==null || measureDataSet.getPivotColumnValue().equals( dataSetLabel.getPivotColumnValue()))){
        			continue;
        		}
        		
        		if(dataSetLabel.getPivotRowName()!=null && (measureDataSet.getPivotRowName()==null || measureDataSet.getPivotRowName().equals( dataSetLabel.getPivotRowName()))){
        			continue;
        		}
        		
        		if(dataSetLabel.getSignature()!=null && (measureDataSet.getSignature()==null || measureDataSet.getSignature().equals( dataSetLabel.getSignature()))){
        			continue;
        		}
        		
        		if(dataSetLabel.getTransformerCd()!=null && (measureDataSet.getTransformerCd()==null || measureDataSet.getTransformerCd().equals( dataSetLabel.getTransformerCd()))){
        			continue;
        		}
        		
        		if(dataSetLabel.getTransformerId()!=null && (measureDataSet.getTransformerId()==null || measureDataSet.getTransformerId().equals( dataSetLabel.getTransformerId()))){
        			continue;
        		}
        		
        		
        		if(dataSetLabel.getUserIn()!=null && (measureDataSet.getUserIn()==null || measureDataSet.getUserIn().equals( dataSetLabel.getUserIn()))){
        			continue;
        		}
        		
    		}
    		toreturn.add(measure);
    		
		}
    	return toreturn;
	}
	

	
}
