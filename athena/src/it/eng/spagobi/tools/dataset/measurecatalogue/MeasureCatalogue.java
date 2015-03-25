 /* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.tools.dataset.measurecatalogue;

import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.bo.Config;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IConfigDAO;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.meta.model.Model;
import it.eng.spagobi.metamodel.MetaModelLoader;
import it.eng.spagobi.metamodel.MetaModelWrapper;
import it.eng.spagobi.metamodel.SiblingsFileLoader;
import it.eng.spagobi.metamodel.SiblingsFileWrapper;
import it.eng.spagobi.services.common.EnginConf;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.tools.dataset.event.DataSetEvent;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MeasureCatalogue implements Observer {

	
	public static transient Logger logger = Logger.getLogger(MeasureCatalogue.class);
	private MetaModelWrapper metamodelWrapper;
	private Set<MeasureCatalogueMeasure> measures = new HashSet<MeasureCatalogueMeasure>();
	private boolean valid;
	
	public MeasureCatalogue(){
		initModel();
		if(isValid()){
			initSiblings();
			initMeasures();
		}
	}
	
	public MeasureCatalogue(Model model){
		metamodelWrapper = new MetaModelWrapper(model);
		initMeasures();
	}
	
	/**
	 * Update the catalogue after a change in the dataset list
	 */
	public void update(Observable o, Object arg) {
		DataSetEvent event = (DataSetEvent) arg;
		logger.debug("Updating the measures catalogue after a change in the dataset list");
		if(isValid()){
			initMeasures();
		}
	 }
	
	
	/**
	 * Initialize the model. Get the name of the model form SbiConfig and load he model from the *.sbimodelfile
	 */
	private void initModel(){
		Config modelConfig=null;
		
		try {
			IConfigDAO configDao = DAOFactory.getSbiConfigDAO();
			modelConfig = configDao.loadConfigParametersByLabel(MeasureCatalogueCostants.modelConfigLabel);
		} catch (Exception e) {
			logger.error("Error loading the name of the model that contains the hierarchies",e);
			throw new SpagoBIRuntimeException("Error loading the name of the model that contains the hierarchies",e);
		}
		
		Assert.assertNotNull("The model with the definition of the cube must be difined in the configs (property "+MeasureCatalogueCostants.modelConfigLabel+")", modelConfig);
		
		String modelname = modelConfig.getValueCheck();
		logger.debug("Initilaizing model [" + modelname + "]");
		
		File modelFile = new File(getResourcePath()+File.separator+"qbe" + File.separator + "datamarts" + File.separator + modelname+File.separator+modelname+".sbimodel");
		Assert.assertNotNull("The model with the definition of the cube must be uploaded in the server. The name of the model in the configs is "+modelname, modelFile);
		
		if(modelFile==null){
			valid = false;
			logger.debug("No hierarchy model loaded");
		}else{
			valid = true;
			logger.debug("Model file name is equal to [" + modelname + "]");
			try {
				metamodelWrapper = new MetaModelWrapper(MetaModelLoader.load(modelFile));
				logger.debug("Model [" + modelname + "] succesfully initialized");
			} catch (Exception e) {
				logger.debug("Error loading the hierarchy. No model loaded");
				valid = false;
			}
			
			
			
		}

	}
	
	/**
	 * Get the name of the model from SbiConfig and load the information about siblings columns for levels from the *.siblings (if present, not mandatory)
	 */
	private void initSiblings(){
		Config modelConfig=null;
		
		try {
			IConfigDAO configDao = DAOFactory.getSbiConfigDAO();
			modelConfig = configDao.loadConfigParametersByLabel(MeasureCatalogueCostants.modelConfigLabel);
		} catch (Exception e) {
			logger.error("Error loading the name of the model that contains the hierarchies",e);
			throw new SpagoBIRuntimeException("Error loading the name of the model that contains the hierarchies",e);
		}
		
		Assert.assertNotNull("The model with the definition of the cube must be difined in the configs (property "+MeasureCatalogueCostants.modelConfigLabel+")", modelConfig);
		
		String modelname = modelConfig.getValueCheck();
		logger.debug("Loading Siblings File [" + modelname +".siblings ]");
		
		File siblingsFile = new File(getResourcePath()+File.separator+"qbe" + File.separator + "datamarts" + File.separator + modelname+File.separator+modelname+".siblings");
		
		if (siblingsFile.exists()){
			logger.debug("Siblings file name is equal to [" + modelname + "]");
			Document xmlDocument = SiblingsFileLoader.load(siblingsFile);
			SiblingsFileWrapper siblingsFileWrapper = new SiblingsFileWrapper(xmlDocument);
			if (metamodelWrapper != null){
				metamodelWrapper.setSiblingsFileWrapper(siblingsFileWrapper);
			}
						
			logger.debug("Siblings [" + modelname + "] succesfully initialized");

		} else {
			//do nothing
			logger.debug("Siblings File [" + modelname +".siblings ] not present");

		}
		

	}

	
	/**
	 * Init the set of measures 
	 */
	public void initMeasures(){
		
		measures = new HashSet<MeasureCatalogueMeasure>();
		List<IDataSet> datasets = new ArrayList<IDataSet>();
		
		try {
			IDataSetDAO dataSetDao = DAOFactory.getDataSetDAO();
			datasets = dataSetDao.loadDataSets();
		} catch (Exception e) {
			logger.error("Error loading the active dataset",e);
			throw new SpagoBIRuntimeException("Error loading the active dataset",e);
		}
		
		/**
		 * create a measure for each measure that appears in a dataset
		 */
		if(datasets!=null){
			for(int i=0; i<datasets.size();i++){
				
				IDataSet aDs = datasets.get(i);
				logger.debug("Creating the measures for the dataset "+aDs.getName());
				try {
					List<IFieldMetaData> aDsMeasures = null;
					try  {
						aDsMeasures = getMeasures(aDs);
					} catch(Throwable t) {
						logger.error("Impossible to extract measure from dataset [" + aDs.getName()+ "]", t);
						continue;
					}
					Set<MeasureCatalogueDimension> datasetDimension = null;
					for(int j=0; j<aDsMeasures.size(); j++){
						try {
							/**
							 * Search the measure in the list of measures
							 */
							logger.debug("Creating the measure for the field "+aDsMeasures.get(j).getAlias());
							MeasureCatalogueMeasure aDsMeasure = getMeasure(aDsMeasures.get(j),aDs);
							if(aDsMeasure==null){
								/**
								 * The measures has not been already saved, so we create it
								 */
								logger.debug("The measure is new.. Create a new MeasureCatalogueMeasureObject");
								aDsMeasure = new MeasureCatalogueMeasure(aDsMeasures.get(j), metamodelWrapper, aDs, datasetDimension);
								measures.add(aDsMeasure);
								datasetDimension = aDsMeasure.getDatasetDimension();
							}
						} catch(Throwable t) {
							logger.error("Impossible to handle [" + j + "] measure from dataset [" + aDs.getName()+ "]", t);
							continue;
						}
	
					}
				} catch(Throwable t) {
					logger.error("An unexpected error occured while extracting measures from dataset [" + aDs.getLabel() + "]", t);
					throw new SpagoBIRuntimeException("An unexpected error occured while extracting measures from dataset [" + aDs.getLabel() + "]", t);
				}
			}
		}
	}
	

    
    /**
     * Gets the list of the measures of a dataset
     * @param ds
     * @return
     */
    public static List<IFieldMetaData> getMeasures(IDataSet ds){
    	logger.debug("IN");
    	IMetaData md = ds.getMetadata();
    	List<IFieldMetaData> measures = new ArrayList<IFieldMetaData>();
    	if(md!=null){
    		for(int i=0; i<md.getFieldCount(); i++){
    			if(MeasureCatalogueMeasure.isMeasure(md.getFieldMeta(i))){
    				measures.add(md.getFieldMeta(i));
    			}
    		}
    	}
    	logger.debug("OUT");
    	return measures;
    }
    
    /**
     * Gets the list of the measures of a dataset
     * @param ds
     * @return
     */
    public List<MeasureCatalogueMeasure> getMeasureCatalogueMeasure(IDataSet ds){
    	List<MeasureCatalogueMeasure> measuresToReturn = new ArrayList<MeasureCatalogueMeasure>();
    	if(this.measures!=null){
    		Iterator<MeasureCatalogueMeasure> it = this.measures.iterator();
    		while (it.hasNext()) {
    			MeasureCatalogueMeasure type = (MeasureCatalogueMeasure) it.next();
    			if(type.getDataSet().getLabel().equals(ds.getLabel())){
    				measuresToReturn.add(type);
    			}
			}
    	}
    	return measuresToReturn;
    }
    
    /**
     * Get from the list of measures the measure equal to the field
     * @param field
     * @return
     */
    public MeasureCatalogueMeasure getMeasure(IFieldMetaData field, IDataSet ds){
    	for (Iterator<MeasureCatalogueMeasure> iterator = measures.iterator(); iterator.hasNext();) {
    		MeasureCatalogueMeasure measure =  iterator.next();
			if(measure.isEqual(field, ds)){
				return measure;
			}
		}
    	return null;
    }

    /**
     * Get measure by id
     * @param measureId
     * @return
     */
	public MeasureCatalogueMeasure getMeasureByLabel(String measureLabel){
    	for (Iterator<MeasureCatalogueMeasure> iterator = measures.iterator(); iterator.hasNext();) {
    		MeasureCatalogueMeasure measure =  iterator.next();
			if(measure.getLabel().equals(measureLabel)){
				return measure;
			}
		}
    	return null;
    }

	
	/**
	 * @param metamodelWrapper the metamodelWrapper to set
	 */
	public void setMetamodelWrapper(MetaModelWrapper metamodelWrapper) {
		this.metamodelWrapper = metamodelWrapper;
	}
	

	
	//*************************************************
	//RESOURCES TO IGNORE IN THE SERIALIZATION
	//*************************************************

	/**
	 * @return the metamodelWrapper
	 */
	@JsonIgnore
	public MetaModelWrapper getMetamodelWrapper() {
		return metamodelWrapper;
	}
	
	/**
	 * Gets the path to the model with the hierarchies
	 * @return
	 */
	@JsonIgnore
    public String getResourcePath() {
    	String resPath;
		try {
			String jndiName = SingletonConfig.getInstance().getConfigValue("SPAGOBI.RESOURCE_PATH_JNDI_NAME");
			resPath = SpagoBIUtilities.readJndiResource(jndiName);
		} catch (Throwable t) {
			logger.debug(t);
			resPath = EnginConf.getInstance().getResourcePath();
		}

		if (resPath == null) {
			throw new SpagoBIRuntimeException("Resource path not found!!!");
		}
		return resPath;
	}
	
	@JsonIgnore
	public boolean isValid() {
		return valid;
	}

	
	//*************************************************
	//RESOURCES TO INCLUDE IN THE SERIALIZATION
	//*************************************************


	public Set<MeasureCatalogueMeasure> getMeasures() {
		return measures;
	}
	
	
    
    public String toString(){
		ObjectMapper mapper = new ObjectMapper();    
		try {
//			SimpleModule simpleModule = new SimpleModule("SimpleModule", new Version(1,0,0,null));
//			simpleModule.addSerializer(IDataSet.class, new IDataSetJaksonSerializer());
//			mapper.registerModule(simpleModule);
			return  mapper.writeValueAsString(this);
		} catch (Exception e) {
			logger.error("Error serializing the measure catalogue",e);
			throw new SpagoBIRuntimeException("Error serializing the measure catalogue",e);
		}
    }
    
    public String toString(String owner, boolean isAdminUser){
		ObjectMapper mapper = new ObjectMapper();    
		try {
			Set<MeasureCatalogueMeasure> filteredMeasures = filterMeasuresByUserVisibility(owner, isAdminUser);
			return  "{measures:"+mapper.writeValueAsString(filteredMeasures)+"}";
		} catch (Exception e) {
			logger.error("Error serializing the measure catalogue",e);
			throw new SpagoBIRuntimeException("Error serializing the measure catalogue",e);
		}
    }
    
    private Set<MeasureCatalogueMeasure> filterMeasuresByUserVisibility(String owner, boolean isAdminUser){
    	Set<MeasureCatalogueMeasure> filteredMeasures = new HashSet<MeasureCatalogueMeasure>();
    	for (Iterator iterator = measures.iterator(); iterator.hasNext();) {
    		MeasureCatalogueMeasure measureCatalogueMeasure = (MeasureCatalogueMeasure) iterator.next();
    		if(isAdminUser || measureCatalogueMeasure.getDataSet().isPublic() || (!owner.equals(null) && owner.equals(measureCatalogueMeasure.getDataSet().getOwner()) ) ){
    			filteredMeasures.add(measureCatalogueMeasure);
    		}
    	}
    	return filteredMeasures;
    }
    
}
