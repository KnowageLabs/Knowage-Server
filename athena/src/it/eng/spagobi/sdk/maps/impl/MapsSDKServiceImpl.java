/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.sdk.maps.impl;

import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.mapcatalogue.bo.GeoFeature;
import it.eng.spagobi.mapcatalogue.bo.GeoMap;
import it.eng.spagobi.sdk.AbstractSDKService;
import it.eng.spagobi.sdk.exceptions.NotAllowedOperationException;
import it.eng.spagobi.sdk.maps.bo.SDKFeature;
import it.eng.spagobi.sdk.maps.bo.SDKMap;
import it.eng.spagobi.sdk.maps.stub.MapsSDKService;
import it.eng.spagobi.sdk.utilities.SDKObjectsConverter;

import java.rmi.RemoteException;
import java.util.List;

import org.apache.log4j.Logger;

public class MapsSDKServiceImpl extends AbstractSDKService implements MapsSDKService{

	static private Logger logger = Logger.getLogger(MapsSDKServiceImpl.class);


	public SDKMap getMapById(Integer mapId) throws RemoteException,
			NotAllowedOperationException {
		SDKMap toReturn = null;
		logger.debug("IN: mapId in input = " + mapId);
		
		this.setTenant();
		
		try {
			super.checkUserPermissionForFunctionality(SpagoBIConstants.MAPCATALOGUE_MANAGEMENT, "User cannot see map catalogue congifuration.");
			if (mapId == null) {
				logger.warn("map identifier in input is null!");
				return null;
			}
			GeoMap geoMap = DAOFactory.getSbiGeoMapsDAO().loadMapByID(mapId);
			if (geoMap == null) {
				logger.warn("Geo Map with identifier [" + mapId + "] not existing.");
				return null;
			}
			toReturn = new SDKObjectsConverter().fromSbiGeoMapToSDKMap(geoMap.toSpagoBiGeoMaps());
		} catch(NotAllowedOperationException e) {
			throw e;
		} catch(Exception e) {
			logger.error("Error while retrieving SDKFeature list", e);
			logger.debug("Returning null");
			return null;
		} finally {
			this.unsetTenant();
			logger.debug("OUT");
		}
		return toReturn;
	}


	public SDKMap[] getMaps() throws RemoteException,
			NotAllowedOperationException {
		SDKMap[] toReturn = null;
		logger.debug("IN");
		
		this.setTenant();
		
		try {
			super.checkUserPermissionForFunctionality(SpagoBIConstants.MAPCATALOGUE_MANAGEMENT, "User cannot see map catalogues congifuration.");
			List mapList = DAOFactory.getSbiGeoMapsDAO().loadAllMaps();
			toReturn = new SDKMap[mapList.size()];
			for (int i = 0; i < mapList.size(); i++) {
				GeoMap geoMap = (GeoMap) mapList.get(i);
				SDKMap sdkMap = new SDKObjectsConverter().fromSbiGeoMapToSDKMap(geoMap.toSpagoBiGeoMaps());
				toReturn[i] = sdkMap;
			}
		} catch(NotAllowedOperationException e) {
			throw e;
		} catch(Exception e) {
			logger.error("Error while retrieving SDKMap list", e);
			logger.debug("Returning null");
			return null;
		} finally {
			this.unsetTenant();
			logger.debug("OUT");
		}
		return toReturn;	
	}


	public SDKFeature[] getFeatures() throws RemoteException, NotAllowedOperationException {
		SDKFeature[] toReturn = null;
		logger.debug("IN");
		
		this.setTenant();
		
		try {
			super.checkUserPermissionForFunctionality(SpagoBIConstants.MAPCATALOGUE_MANAGEMENT, "User cannot see map catalogues congifuration.");
			List featuresList = DAOFactory.getSbiGeoFeaturesDAO().loadAllFeatures();
			toReturn = new SDKFeature[featuresList.size()];
			for (int i = 0; i < featuresList.size(); i++) {
				GeoFeature geoFeature = (GeoFeature) featuresList.get(i);
				SDKFeature sdkFeature = new SDKObjectsConverter().fromSbiGeoFeatureToSDKFeature(geoFeature.toSpagoBiGeoFeatures());
				toReturn[i] = sdkFeature;
			}
		} catch(NotAllowedOperationException e) {
			throw e;
		} catch(Exception e) {
			logger.error("Error while retrieving SDKFeature list", e);
			logger.debug("Returning null");
			return null;
		} finally {
			this.unsetTenant();
			logger.debug("OUT");
		}
		return toReturn;	
	}


	public SDKFeature getFeatureById(Integer featureId) throws NotAllowedOperationException {
		SDKFeature toReturn = null;
		logger.debug("IN: dataSetId in input = " + featureId);
		
		this.setTenant();
		
		try {
			super.checkUserPermissionForFunctionality(SpagoBIConstants.MAPCATALOGUE_MANAGEMENT, "User cannot see map catalogue congifuration.");
			if (featureId == null) {
				logger.warn("Feature identifier in input is null!");
				return null;
			}
			GeoFeature geoFeature = DAOFactory.getSbiGeoFeaturesDAO().loadFeatureByID(featureId);
			if (geoFeature == null) {
				logger.warn("Geo Feature with identifier [" + featureId + "] not existing.");
				return null;
			}
			toReturn = new SDKObjectsConverter().fromSbiGeoFeatureToSDKFeature(geoFeature.toSpagoBiGeoFeatures());
		} catch(NotAllowedOperationException e) {
			throw e;
		} catch(Exception e) {
			logger.error("Error while retrieving SDKFeature list", e);
			logger.debug("Returning null");
			return null;
		} finally {
			this.unsetTenant();
			logger.debug("OUT");
		}
		return toReturn;
	}


	public SDKFeature[] getMapFeatures(Integer mapId) throws RemoteException, NotAllowedOperationException {
		SDKFeature[] toReturn = null;
		logger.debug("IN");
		
		this.setTenant();
		
		try {
			super.checkUserPermissionForFunctionality(SpagoBIConstants.MAPCATALOGUE_MANAGEMENT, "User cannot see map catalogues congifuration.");
			if (mapId == null) {
				logger.warn("Map identifier in input is null!");
				return null;
			}
			List featuresList = DAOFactory.getSbiGeoMapFeaturesDAO().loadFeaturesByMapId(mapId);
			toReturn = new SDKFeature[featuresList.size()];
			for (int i = 0; i < featuresList.size(); i++) {
				GeoFeature geoFeature = (GeoFeature) featuresList.get(i);
				SDKFeature sdkFeature = new SDKObjectsConverter().fromSbiGeoFeatureToSDKFeature(geoFeature.toSpagoBiGeoFeatures());
				toReturn[i] = sdkFeature;
			}
		} catch(NotAllowedOperationException e) {
			throw e;
		} catch(Exception e) {
			logger.error("Error while retrieving SDKFeature list", e);
			logger.debug("Returning null");
			return null;
		} finally {
			this.unsetTenant();
			logger.debug("OUT");
		}
		return toReturn;	
	}


//	public SDKDataSet getDataSet(Integer dataSetId) throws NotAllowedOperationException {
//		SDKDataSet toReturn = null;
//		logger.debug("IN: dataSetId in input = " + dataSetId);
//		
//		this.setTenant();
//		
//		try {
//			super.checkUserPermissionForFunctionality(SpagoBIConstants.DATASET_MANAGEMENT, "User cannot see datasets congifuration.");
//			if (dataSetId == null) {
//				logger.warn("DataSet identifier in input is null!");
//				return null;
//			}
//			IDataSet dataSet = DAOFactory.getDataSetDAO().loadActiveIDataSetByID(dataSetId);
//			if (dataSet == null) {
//				logger.warn("DataSet with identifier [" + dataSetId + "] not existing.");
//				return null;
//			}
//			toReturn = new SDKObjectsConverter().fromSpagoBiDataSetToSDKDataSet(dataSet.toSpagoBiDataSet());
//		} catch(NotAllowedOperationException e) {
//			throw e;
//		} catch(Exception e) {
//			logger.error("Error while retrieving SDKEngine list", e);
//			logger.debug("Returning null");
//			return null;
//		} finally {
//			this.unsetTenant();
//			logger.debug("OUT");
//		}
//		return toReturn;
//	}

//	public SDKDataSet[] getDataSets() throws NotAllowedOperationException {
//		SDKDataSet[] toReturn = null;
//		logger.debug("IN");
//		
//		this.setTenant();
//		
//		try {
//			super.checkUserPermissionForFunctionality(SpagoBIConstants.DATASET_MANAGEMENT, "User cannot see datasets congifuration.");
//			List dataSetList = DAOFactory.getDataSetDAO().loadAllActiveDataSets();
//			toReturn = new SDKDataSet[dataSetList.size()];
//			for (int i = 0; i < dataSetList.size(); i++) {
//				IDataSet dataSet = (IDataSet) dataSetList.get(i);
//				SDKDataSet sdkDataSet = new SDKObjectsConverter().fromSpagoBiDataSetToSDKDataSet(dataSet.toSpagoBiDataSet());
//				toReturn[i] = sdkDataSet;
//			}
//		} catch(NotAllowedOperationException e) {
//			throw e;
//		} catch(Exception e) {
//			logger.error("Error while retrieving SDKEngine list", e);
//			logger.debug("Returning null");
//			return null;
//		} finally {
//			this.unsetTenant();
//			logger.debug("OUT");
//		}
//		return toReturn;
//	}

//	public SDKDataStoreMetadata getDataStoreMetadata(SDKDataSet sdkDataSet) throws NotAllowedOperationException, MissingParameterValue, InvalidParameterValue {
//		SDKDataStoreMetadata toReturn = null;
//		Integer dataSetId = null;
//		logger.debug("IN");
//		
//		this.setTenant();
//		
//		try {
//			super.checkUserPermissionForFunctionality(SpagoBIConstants.DATASET_MANAGEMENT, "User cannot see datasets congifuration.");
//			if (sdkDataSet == null) {
//				logger.warn("SDKDataSet in input is null!");
//				return null;
//			}
//			dataSetId = sdkDataSet.getId();
//			logger.debug("Looking for dataset with id = " + dataSetId);
//			IDataSet dataSet = DAOFactory.getDataSetDAO().loadActiveIDataSetByID(dataSetId);
//			if (dataSet == null) {
//				logger.warn("DataSet with identifier [" + dataSetId + "] not found.");
//				return null;
//			}
//			Map parameters = new HashMap();
//			//List parametersToFill = DetailDataSetModule.getParametersToFill(dataSet.toSpagoBiDataSet());
//			//TODO da cambiare con il nuovo metodo
//			List parametersToFill = new ArrayList();
//			if (parametersToFill != null && parametersToFill.size() > 0) {
//				Iterator it = parametersToFill.iterator();
//				while (it.hasNext()) {
//					DataSetParameterItem aDataSetParameterItem = (DataSetParameterItem) it.next();
//					SDKDataSetParameter sdkParameter = findRelevantSDKDataSetParameter(aDataSetParameterItem, sdkDataSet);
//					if (sdkParameter == null) {
//						logger.error("SDKDataSetParameter for DataSetParameterItem with name [" + aDataSetParameterItem.getName() + "] not found!!");
//						throw new MissingParameterValue(aDataSetParameterItem.getName());
//					}
//					String[] values = sdkParameter.getValues();
//					logger.debug("Values set for parameter [" + aDataSetParameterItem.getName() + "] are: " + values);
//					if (values == null || values.length == 0) {
//						logger.error("SDKDataSetParameter contains no values for DataSetParameterItem with name [" + aDataSetParameterItem.getName() + "]!!");
//						throw new MissingParameterValue(aDataSetParameterItem.getName());
//					}
//					checkParameterValues(values, aDataSetParameterItem);
//					String parameterValues = getParameterValues(values, aDataSetParameterItem);
//					logger.debug("Setting values [" + parameterValues + "] for parameter with name = [" + aDataSetParameterItem.getName() + "]");
//					parameters.put(aDataSetParameterItem.getName(), parameterValues);
//				}
//			}
//			dataSet.setParamsMap(parameters);
//			dataSet.loadData();
//			IDataStore dataStore = dataSet.getDataStore();
//			MetaData dataStoreMetadata = (MetaData) dataStore.getMetaData();
//			toReturn = new SDKObjectsConverter().fromDataStoreMetadataToSDKDataStoreMetadata(dataStoreMetadata);
//		} catch(NotAllowedOperationException e) {
//			throw e;
//		} catch(MissingParameterValue e) {
//			throw e;
//		} catch(InvalidParameterValue e) {
//			throw e;
//		} catch(Exception e) {
//			logger.error("Error while retrieving SDKDataStoreMetadata for dataset with id = " + dataSetId, e);
//			logger.debug("Returning null");
//			return null;
//		} finally {
//			this.unsetTenant();
//			logger.debug("OUT");
//		}
//		return toReturn;
//	}

//	private String getParameterValues(String[] values,
//			DataSetParameterItem dataSetParameterItem) {
//		logger.debug("IN");
//		StringBuffer toReturn = new StringBuffer();
//		try {
//			String parameterType = dataSetParameterItem.getType();
//			if (parameterType.equalsIgnoreCase("String")) {
//				for (int i = 0; i < values.length; i++) {
//					String value = values[i];
//					if (!value.startsWith("'")) {
//						value = "'" + value;
//					}
//					if (!value.endsWith("'")) {
//						value = value + "'";
//					}
//					toReturn.append(value);
//					if (i < values.length - 1) {
//						toReturn.append(",");
//					}
//				}
//			} else {
//				for (int i = 0; i < values.length; i++) {
//					String value = values[i];
//					toReturn.append(value);
//					if (i < values.length - 1) {
//						toReturn.append(",");
//					}
//				}
//			}
//		} catch (Exception e) {
//			logger.error("Error while retrieving revelant SDKDataSetParameter for a DataSetParameterItem", e);
//			logger.debug("Returning null");
//			return null;
//		} finally {
//			logger.debug("OUT");
//		}
//		return toReturn.toString();
//	}

//	private void checkParameterValues(String[] values,
//			DataSetParameterItem dataSetParameterItem) throws InvalidParameterValue {
//		logger.debug("IN");
//		try {
//			String parameterType = dataSetParameterItem.getType();
//			if (parameterType.equalsIgnoreCase("Number")) {
//				for (int i = 0; i < values.length; i++) {
//					String value = values[i];
//					if (GenericValidator.isBlankOrNull(value) || 
//							(!(GenericValidator.isInt(value) 
//									|| GenericValidator.isFloat(value) 
//									|| GenericValidator.isDouble(value)
//									|| GenericValidator.isShort(value)
//									|| GenericValidator.isLong(value)))) {
//						InvalidParameterValue error = new InvalidParameterValue();
//						error.setParameterName(dataSetParameterItem.getName());
//						error.setParameterType(parameterType);
//						error.setWrongParameterValue(value);
//						error.setParameterFormat("");
//						throw error;
//					}
//				}
//			}
//		} finally {
//			logger.debug("OUT");
//		}
//	}

//	private SDKDataSetParameter findRelevantSDKDataSetParameter(
//			DataSetParameterItem dataSetParameterItem, SDKDataSet sdkDataSet) {
//		logger.debug("IN");
//		SDKDataSetParameter toReturn = null;
//		try {
//			String parameterName = dataSetParameterItem.getName();
//			SDKDataSetParameter[] parameters = sdkDataSet.getParameters();
//			if (parameters != null && parameters.length > 0) {
//				for (int i = 0; i < parameters.length; i++) {
//					SDKDataSetParameter aSDKDataSetParameter = parameters[i];
//					if (aSDKDataSetParameter.getName().equals(parameterName)) {
//						toReturn = aSDKDataSetParameter;
//						break;
//					}
//				}
//			}
//		} catch (Exception e) {
//			logger.error("Error while retrieving revelant SDKDataSetParameter for a DataSetParameterItem", e);
//			logger.debug("Returning null");
//			return null;
//		} finally {
//			logger.debug("OUT");
//		}
//		return toReturn;
//	}

}
