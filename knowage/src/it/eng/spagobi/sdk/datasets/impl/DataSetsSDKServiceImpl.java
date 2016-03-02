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
package it.eng.spagobi.sdk.datasets.impl;

import it.eng.qbe.dataset.QbeDataSet;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.sdk.AbstractSDKService;
import it.eng.spagobi.sdk.datasets.DataSetsSDKService;
import it.eng.spagobi.sdk.datasets.bo.SDKDataSet;
import it.eng.spagobi.sdk.datasets.bo.SDKDataSetParameter;
import it.eng.spagobi.sdk.datasets.bo.SDKDataStoreMetadata;
import it.eng.spagobi.sdk.exceptions.InvalidParameterValue;
import it.eng.spagobi.sdk.exceptions.MissingParameterValue;
import it.eng.spagobi.sdk.exceptions.NotAllowedOperationException;
import it.eng.spagobi.sdk.utilities.SDKObjectsConverter;
import it.eng.spagobi.tools.dataset.bo.DataSetParameterItem;
import it.eng.spagobi.tools.dataset.bo.DataSetParametersList;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.VersionedDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datawriter.JSONDataWriter;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.MetaData;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.tools.dataset.utils.DatasetMetadataParser;
import it.eng.spagobi.tools.dataset.utils.datamart.SpagoBICoreDatamartRetriever;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.validator.GenericValidator;
import org.apache.log4j.Logger;

public class DataSetsSDKServiceImpl extends AbstractSDKService implements DataSetsSDKService {

	static private Logger logger = Logger.getLogger(DataSetsSDKServiceImpl.class);

	public SDKDataSet getDataSet(Integer dataSetId) throws NotAllowedOperationException {
		SDKDataSet toReturn = null;
		logger.debug("IN: dataSetId in input = " + dataSetId);
		
		this.setTenant();
		
		try {
			super.checkUserPermissionForFunctionality(SpagoBIConstants.DATASET_MANAGEMENT, "User cannot see datasets congifuration.");
			if (dataSetId == null) {
				logger.warn("DataSet identifier in input is null!");
				return null;
			}
			
			IDataSetDAO dao = DAOFactory.getDataSetDAO();
			dao.setUserProfile(getUserProfile());
			IDataSet dataSet = dao.loadDataSetById(dataSetId);
			
			if (dataSet == null) {
				logger.warn("DataSet with identifier [" + dataSetId + "] not existing.");
				return null;
			}
			toReturn = new SDKObjectsConverter().fromSpagoBiDataSetToSDKDataSet(dataSet.toSpagoBiDataSet());
		} catch(NotAllowedOperationException e) {
			throw e;
		} catch(Exception e) {
			logger.error("Error while retrieving SDKEngine list", e);
			logger.debug("Returning null");
			return null;
		} finally {
			this.unsetTenant();
			logger.debug("OUT");
		}
		return toReturn;
	}

	public SDKDataSet[] getDataSets() throws NotAllowedOperationException {
		SDKDataSet[] toReturn = null;
		logger.debug("IN");
		
		this.setTenant();
		
		try {
			super.checkUserPermissionForFunctionality(SpagoBIConstants.DATASET_MANAGEMENT, "User cannot see datasets congifuration.");
			
			IDataSetDAO dao = DAOFactory.getDataSetDAO();
			dao.setUserProfile(getUserProfile());
			List dataSetList = dao.loadDataSets();
			
			toReturn = new SDKDataSet[dataSetList.size()];
			for (int i = 0; i < dataSetList.size(); i++) {
				IDataSet dataSet = (IDataSet) dataSetList.get(i);
				SDKDataSet sdkDataSet = new SDKObjectsConverter().fromSpagoBiDataSetToSDKDataSet(dataSet.toSpagoBiDataSet());
				toReturn[i] = sdkDataSet;
			}
		} catch(NotAllowedOperationException e) {
			throw e;
		} catch(Exception e) {
			logger.error("Error while retrieving SDKEngine list", e);
			logger.debug("Returning null");
			return null;
		} finally {
			this.unsetTenant();
			logger.debug("OUT");
		}
		return toReturn;
	}

	public SDKDataStoreMetadata getDataStoreMetadata(SDKDataSet sdkDataSet) throws NotAllowedOperationException, MissingParameterValue, InvalidParameterValue {
		SDKDataStoreMetadata toReturn = null;
		IMetaData dsMeta=null;
		Integer dataSetId = null;
		logger.debug("IN");
		
		this.setTenant();
		
		try {
			super.checkUserPermissionForFunctionality(SpagoBIConstants.DATASET_MANAGEMENT, "User cannot see datasets congifuration.");
			if (sdkDataSet == null) {
				logger.warn("SDKDataSet in input is null!");
				return null;
			}
			dataSetId = sdkDataSet.getId();
			logger.debug("Looking for dataset with id = " + dataSetId);
			IDataSetDAO dao = DAOFactory.getDataSetDAO();
			dao.setUserProfile(getUserProfile());
			IDataSet dataSet = dao.loadDataSetById(dataSetId);

			if (dataSet == null) {
				logger.warn("DataSet with identifier [" + dataSetId + "] not found.");
				return null;
			}

			// First I try recovering dsMetadataField, only if not present I try executing the dataset, 

			String dsMetadata=dataSet.getDsMetadata();
			if(dsMetadata!=null){
				try{
					dsMeta = new DatasetMetadataParser().xmlToMetadata(dsMetadata);

				}
				catch (Exception e) {
					logger.warn("error in parsing, recover metadata executing again the dataset! ", e);
				}
			}

			if(dsMeta==null)	
			{
				logger.warn("error in parsing, recover metadata executing again the dataset! ");
				Map parameters = new HashMap();
				List parametersToFill = null;
				String parametersXML=dataSet.toSpagoBiDataSet().getParameters();
				if(parametersXML!=null && !((parametersXML.trim()).equals(""))){
					DataSetParametersList dsParam=new DataSetParametersList(parametersXML);
					parametersToFill=dsParam.getItems();				
				}
				if (parametersToFill != null && parametersToFill.size() > 0) {
					Iterator it = parametersToFill.iterator();
					while (it.hasNext()) {
						DataSetParameterItem aDataSetParameterItem = (DataSetParameterItem) it.next();
						SDKDataSetParameter sdkParameter = findRelevantSDKDataSetParameter(aDataSetParameterItem, sdkDataSet);
						if (sdkParameter == null) {
							logger.error("SDKDataSetParameter for DataSetParameterItem with name [" + aDataSetParameterItem.getName() + "] not found!!");
							throw new MissingParameterValue(aDataSetParameterItem.getName());
						}
						String[] values = sdkParameter.getValues();
						logger.debug("Values set for parameter [" + aDataSetParameterItem.getName() + "] are: " + values);
						if (values == null || values.length == 0) {
							logger.error("SDKDataSetParameter contains no values for DataSetParameterItem with name [" + aDataSetParameterItem.getName() + "]!!");
							throw new MissingParameterValue(aDataSetParameterItem.getName());
						}
						checkParameterValues(values, aDataSetParameterItem);
						String parameterValues = getParameterValues(values, aDataSetParameterItem);
						logger.debug("Setting values [" + parameterValues + "] for parameter with name = [" + aDataSetParameterItem.getName() + "]");
						parameters.put(aDataSetParameterItem.getName(), parameterValues);
					}
				}
				dataSet.setParamsMap(parameters);
				dataSet.loadData();
				IDataStore dataStore = dataSet.getDataStore();
				dsMeta = (MetaData) dataStore.getMetaData();

			}

			toReturn = new SDKObjectsConverter().fromDataStoreMetadataToSDKDataStoreMetadata((MetaData)dsMeta);

		} catch(NotAllowedOperationException e) {
			throw e;
		} catch(MissingParameterValue e) {
			throw e;
		} catch(InvalidParameterValue e) {
			throw e;
		} catch(Exception e) {
			logger.error("Error while retrieving SDKDataStoreMetadata for dataset with id = " + dataSetId, e);
			logger.debug("Returning null");
			return null;
		} finally {
			this.unsetTenant();
			logger.debug("OUT");
		}
		return toReturn;
	}
	
	public Integer saveDataset(SDKDataSet sdkDataSet) throws NotAllowedOperationException {
		logger.debug("IN");
		Integer dataSetId = null;
		Integer toReturn = null;
		
		this.setTenant();
		
		try {
			IEngUserProfile profile = getUserProfile();
			super.checkUserPermissionForFunctionality(SpagoBIConstants.DATASET_MANAGEMENT, "User cannot see datasets congifuration.");
			if (sdkDataSet == null) {
				logger.warn("SDKDataSet in input is null!");
				return null;
			}
			//defines the new dataset from the sdk object
			IDataSet sbiDataset = new SDKObjectsConverter().fromSDKDatasetToBIDataset(sdkDataSet);	
			
			try {
				Map parameters = sbiDataset.getParamsMap();
				if (parameters == null) {
					parameters = new HashMap();
					sbiDataset.setParamsMap(parameters);
				}
				SpagoBICoreDatamartRetriever retriever = new SpagoBICoreDatamartRetriever();
				parameters.put(SpagoBIConstants.DATAMART_RETRIEVER, retriever);
				
				sbiDataset.loadData();
				IMetaData metadata = sbiDataset.getDataStore().getMetaData();
				sbiDataset.setMetadata(metadata);
				DatasetMetadataParser parser = new DatasetMetadataParser();
				String metadataStr = parser.metadataToXML(metadata);
				sbiDataset.setDsMetadata(metadataStr);
			} catch (Throwable t) {
				logger.error("Cannot retrieve dataset's metadata", t);
			}
			
			dataSetId = sdkDataSet.getId();
			logger.debug("Looking for dataset with id = " + dataSetId);
			if (dataSetId == null){
				logger.warn("DataSet with identifier [" + dataSetId + "] not found. Create it!");		
				IDataSetDAO dataSetDao = DAOFactory.getDataSetDAO();
				dataSetDao.setUserProfile(profile);
				toReturn = dataSetDao.insertDataSet(sbiDataset);
				if (toReturn != null) {
					logger.info("DataSet saved with id = " + toReturn);
				} else {
					logger.error("DataSet not modified!!");
				}
			}else{
				logger.warn("DataSet with identifier [" + dataSetId + "] found. Modified it!");			
				IDataSetDAO datasetDAO = DAOFactory.getDataSetDAO();
				datasetDAO.setUserProfile(profile);

				// if going in update must not change visibility settings
				IDataSet datasetPrevious = datasetDAO.loadDataSetById(dataSetId);

				logger.debug("keep previous settings as organization "+datasetPrevious.getOrganization()+" anbd visibility public: "+datasetPrevious.isPublic());
				sbiDataset.setOrganization(datasetPrevious.getOrganization());
				sbiDataset.setPublic(datasetPrevious.isPublic());
				datasetDAO.modifyDataSet(sbiDataset);
			}		
			
		} catch(Exception e) {
			logger.error("Error while saving dataset", e);
			return null;
			// TODO: throw an exception when error rises
		} finally {
			this.unsetTenant();
			logger.debug("OUT");
		}
		
		return toReturn;
	}
	/**
	 * 
	 */
	public String executeDataSet(String label, SDKDataSetParameter[] params) throws NotAllowedOperationException{
		String toReturn = null;
		logger.debug("IN: label in input = " + label);
		
		this.setTenant();
		
		try {
			super.checkUserPermissionForFunctionality(SpagoBIConstants.DATASET_MANAGEMENT, "User cannot see datasets congifuration.");
			if (label == null) {
				logger.warn("DataSet identifier in input is null!");
				return null;
			}
			IDataSetDAO dao = DAOFactory.getDataSetDAO();
			dao.setUserProfile(getUserProfile());
			IDataSet dataSet = dao.loadDataSetByLabel(label);
			if (dataSet == null) {
				logger.warn("DataSet with label [" + label + "] not existing.");
				return null;
			}
			if (params!=null && params.length>0){
				HashMap parametersFilled= new HashMap();
				for(int i=0; i< params.length; i++){
					SDKDataSetParameter par = params[i];
					parametersFilled.put(par.getName(),par.getValues()[0]);
					logger.debug("Add parameter: "+par.getName()+"/"+par.getValues()[0]);
				}				
				dataSet.setParamsMap(parametersFilled);
			}
			
			
			//add the jar retriver in case of a Qbe DataSet
			if (dataSet instanceof QbeDataSet || (dataSet instanceof VersionedDataSet && ((VersionedDataSet)dataSet).getWrappedDataset() instanceof QbeDataSet )) {
				SpagoBICoreDatamartRetriever retriever = new SpagoBICoreDatamartRetriever();
				Map parameters = dataSet.getParamsMap();
				if (parameters == null) {
					parameters = new HashMap();
					dataSet.setParamsMap(parameters);
				}
				dataSet.getParamsMap().put(SpagoBIConstants.DATAMART_RETRIEVER, retriever);
			}
			
			dataSet.loadData();
			//toReturn = dataSet.getDataStore().toXml();
			
			JSONDataWriter writer= new JSONDataWriter();
			toReturn = (writer.write(dataSet.getDataStore())).toString(); 
			
		} catch(NotAllowedOperationException e) {
			throw e;
		} catch(Exception e) {
			logger.error("Error while retrieving SDKEngine list", e);
			logger.debug("Returning null");
			return null;
		} finally {
			this.unsetTenant();
			logger.debug("OUT");
		}
		return toReturn;
	}
	private String getParameterValues(String[] values,
			DataSetParameterItem dataSetParameterItem) {
		logger.debug("IN");
		StringBuffer toReturn = new StringBuffer();
		try {
			String parameterType = dataSetParameterItem.getType();
			if (parameterType.equalsIgnoreCase("String")) {
				for (int i = 0; i < values.length; i++) {
					String value = values[i];
					if (!value.startsWith("'")) {
						value = "'" + value;
					}
					if (!value.endsWith("'")) {
						value = value + "'";
					}
					toReturn.append(value);
					if (i < values.length - 1) {
						toReturn.append(",");
					}
				}
			} else {
				for (int i = 0; i < values.length; i++) {
					String value = values[i];
					toReturn.append(value);
					if (i < values.length - 1) {
						toReturn.append(",");
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error while retrieving revelant SDKDataSetParameter for a DataSetParameterItem", e);
			logger.debug("Returning null");
			return null;
		} finally {
			logger.debug("OUT");
		}
		return toReturn.toString();
	}

	private void checkParameterValues(String[] values,
			DataSetParameterItem dataSetParameterItem) throws InvalidParameterValue {
		logger.debug("IN");
		try {
			String parameterType = dataSetParameterItem.getType();
			if (parameterType.equalsIgnoreCase("Number")) {
				for (int i = 0; i < values.length; i++) {
					String value = values[i];
					if (GenericValidator.isBlankOrNull(value) || 
							(!(GenericValidator.isInt(value) 
									|| GenericValidator.isFloat(value) 
									|| GenericValidator.isDouble(value)
									|| GenericValidator.isShort(value)
									|| GenericValidator.isLong(value)))) {
						InvalidParameterValue error = new InvalidParameterValue();
						error.setParameterName(dataSetParameterItem.getName());
						error.setParameterType(parameterType);
						error.setWrongParameterValue(value);
						error.setParameterFormat("");
						throw error;
					}
				}
			}
		} finally {
			logger.debug("OUT");
		}
	}

	private SDKDataSetParameter findRelevantSDKDataSetParameter(
			DataSetParameterItem dataSetParameterItem, SDKDataSet sdkDataSet) {
		logger.debug("IN");
		SDKDataSetParameter toReturn = null;
		try {
			String parameterName = dataSetParameterItem.getName();
			SDKDataSetParameter[] parameters = sdkDataSet.getParameters();
			if (parameters != null && parameters.length > 0) {
				for (int i = 0; i < parameters.length; i++) {
					SDKDataSetParameter aSDKDataSetParameter = parameters[i];
					if (aSDKDataSetParameter.getName().equals(parameterName)) {
						toReturn = aSDKDataSetParameter;
						break;
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error while retrieving revelant SDKDataSetParameter for a DataSetParameterItem", e);
			logger.debug("Returning null");
			return null;
		} finally {
			logger.debug("OUT");
		}
		return toReturn;
	}

}
