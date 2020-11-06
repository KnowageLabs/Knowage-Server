package it.eng.spagobi.analiticalmodel.document.handlers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIMetaModelParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IBIMetaModelParameterDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.catalogue.bo.MetaModel;
import it.eng.spagobi.tools.catalogue.dao.IMetaModelsDAO;
import it.eng.spagobi.tools.dataset.bo.BIObjDataSet;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.VersionedDataSet;
import it.eng.spagobi.tools.dataset.dao.IBIObjDataSetDAO;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;

public class DriversRuntimeLoader {

	static private Logger logger = Logger.getLogger(DriversRuntimeLoader.class);

	public BIObject loadBIObjectForExecutionByLabelAndRole(String label, String role) throws EMFUserError {
		IBIObjectDAO dao = DAOFactory.getBIObjectDAO();
		BIObject biObject = dao.loadBIObjectByLabel(label);
		List<BIObjectParameter> documentDrivers = dao.loadDocumentDrivers(biObject, role);
		List<BIMetaModelParameter> datasetDrivers = getMetamodelDatasetDrivers(biObject, role);
		List<BIObjectParameter> dsDrivers = getDatasetDrivers(biObject, role);
		// i can either have document drivers or drivers inherited from meta model, but NOT both
		if (documentDrivers != null && !documentDrivers.isEmpty()) {
			biObject.setDrivers(documentDrivers);
		} else if (datasetDrivers != null && !datasetDrivers.isEmpty()) {
			biObject.setMetamodelDrivers(datasetDrivers);
			biObject.setDrivers(dsDrivers);
		}
		return biObject;
	}

	private List<BIMetaModelParameter> getMetamodelDatasetDrivers(BIObject biObject, String role) {
		List<BIMetaModelParameter> toReturn = new ArrayList<BIMetaModelParameter>();
		List<BIMetaModelParameter> qbeDatasetDrivers = new ArrayList<BIMetaModelParameter>();

		IBIMetaModelParameterDAO driversDao = DAOFactory.getBIMetaModelParameterDAO();
		IMetaModelsDAO businessModelsDAO = DAOFactory.getMetaModelsDAO();
		IBIObjDataSetDAO biObjDataSetDAO = DAOFactory.getBIObjDataSetDAO();
		IDataSetDAO datasetDao = DAOFactory.getDataSetDAO();

		try {
			ArrayList<BIObjDataSet> biObjDataSetList = biObjDataSetDAO.getBiObjDataSets(biObject.getId());
			Iterator itDs = biObjDataSetList.iterator();
			while (itDs.hasNext()) {
				BIObjDataSet biObjDataSet = (BIObjDataSet) itDs.next();
				Integer dsId = biObjDataSet.getDataSetId();
				IDataSet dataset = datasetDao.loadDataSetById(dsId);
				dataset = dataset instanceof VersionedDataSet ? ((VersionedDataSet) dataset).getWrappedDataset() : dataset;
				if (dataset != null && dataset.getDsType() == "SbiQbeDataSet") {
					JSONObject jsonConfig = new JSONObject(dataset.getConfiguration());
					String businessModelName = jsonConfig.getString("qbeDatamarts");
					MetaModel businessModel = businessModelsDAO.loadMetaModelByName(businessModelName);
					qbeDatasetDrivers = driversDao.loadBIMetaModelParameterByMetaModelId(businessModel.getId());
					if (qbeDatasetDrivers != null && !qbeDatasetDrivers.isEmpty()) {
						toReturn = toMetamodelDrivers(qbeDatasetDrivers, role);
						// in a document i can use only one qbe dataset WITH DRIVERS
						break;
					}
				}
			}
		} catch (Exception e) {
			logger.error("Couldn't retrieve drivers from meta model", e);
		}
		return toReturn;
	}

	private List<BIObjectParameter> getDatasetDrivers(BIObject biObject, String role) {
		List<BIObjectParameter> toReturn = new ArrayList<BIObjectParameter>();
		List<BIMetaModelParameter> qbeDatasetDrivers = new ArrayList<BIMetaModelParameter>();

		IBIMetaModelParameterDAO driversDao = DAOFactory.getBIMetaModelParameterDAO();
		IMetaModelsDAO businessModelsDAO = DAOFactory.getMetaModelsDAO();
		IBIObjDataSetDAO biObjDataSetDAO = DAOFactory.getBIObjDataSetDAO();
		IDataSetDAO datasetDao = DAOFactory.getDataSetDAO();

		try {
			ArrayList<BIObjDataSet> biObjDataSetList = biObjDataSetDAO.getBiObjDataSets(biObject.getId());
			Iterator itDs = biObjDataSetList.iterator();
			while (itDs.hasNext()) {
				BIObjDataSet biObjDataSet = (BIObjDataSet) itDs.next();
				Integer dsId = biObjDataSet.getDataSetId();
				IDataSet dataset = datasetDao.loadDataSetById(dsId);
				dataset = dataset instanceof VersionedDataSet ? ((VersionedDataSet) dataset).getWrappedDataset() : dataset;
				if (dataset != null && dataset.getDsType() == "SbiQbeDataSet") {
					JSONObject jsonConfig = new JSONObject(dataset.getConfiguration());
					String businessModelName = jsonConfig.getString("qbeDatamarts");
					MetaModel businessModel = businessModelsDAO.loadMetaModelByName(businessModelName);
					qbeDatasetDrivers = driversDao.loadBIMetaModelParameterByMetaModelId(businessModel.getId());
					if (qbeDatasetDrivers != null && !qbeDatasetDrivers.isEmpty()) {
						toReturn = toDocumentDrivers(qbeDatasetDrivers, biObject, role);
						// in a document i can use only one qbe dataset WITH DRIVERS
						break;
					}
				}
			}
		} catch (Exception e) {
			logger.error("Couldn't retrieve drivers from meta model", e);
		}
		return toReturn;
	}

	private List<BIObjectParameter> toDocumentDrivers(List<BIMetaModelParameter> qbeDatasetDrivers, BIObject biObject, String role) {
		List<BIObjectParameter> toReturn = new ArrayList<BIObjectParameter>();
		for (BIMetaModelParameter d : qbeDatasetDrivers) {
			toReturn.add(transformDSDrivertoBIObjectParameter(d, biObject, role));
		}
		return toReturn;
	}

	private List<BIMetaModelParameter> toMetamodelDrivers(List<BIMetaModelParameter> qbeDatasetDrivers, String role) {
		List<BIMetaModelParameter> toReturn = new ArrayList<BIMetaModelParameter>();
		for (BIMetaModelParameter d : qbeDatasetDrivers) {
			toReturn.add(transformDSDrivertoBIMetaModelParameter(d, role));
		}
		return toReturn;
	}

	public BIMetaModelParameter transformDSDrivertoBIMetaModelParameter(BIMetaModelParameter datasetDriver, String role) {
		BIMetaModelParameter docDriver = new BIMetaModelParameter();
		IParameterDAO aParameterDAO = DAOFactory.getParameterDAO();
		docDriver.setId(datasetDriver.getId());
		docDriver.setLabel(datasetDriver.getLabel());
		docDriver.setModifiable(datasetDriver.getModifiable());
		docDriver.setMultivalue(datasetDriver.getMultivalue());
		docDriver.setBiMetaModelID(datasetDriver.getBiMetaModelID());
		docDriver.setParameterUrlName(datasetDriver.getParameterUrlName());
		docDriver.setParID(datasetDriver.getParID());
		docDriver.setRequired(datasetDriver.getRequired());
		docDriver.setVisible(datasetDriver.getVisible());
		docDriver.setPriority(datasetDriver.getPriority());
		docDriver.setProg(datasetDriver.getProg());
		docDriver.setColSpan(datasetDriver.getColSpan());
		docDriver.setThickPerc(datasetDriver.getThickPerc());

		Parameter parameter = new Parameter();
		try {
			parameter = aParameterDAO.loadForExecutionByParameterIDandRoleName(docDriver.getParID(), role, false);
		} catch (EMFUserError e) {
			e.printStackTrace();
		}
		parameter.setId(datasetDriver.getParameter().getId());
		parameter.setType(datasetDriver.getParameter().getType());
		docDriver.setParameter(parameter);
		return docDriver;
	}

	public BIObjectParameter transformDSDrivertoBIObjectParameter(BIMetaModelParameter datasetDriver, BIObject biObject, String role) {
		BIObjectParameter docDriver = new BIObjectParameter();
		IParameterDAO aParameterDAO = DAOFactory.getParameterDAO();
		docDriver.setId(biObject.getId());
		docDriver.setLabel(datasetDriver.getLabel());
		docDriver.setModifiable(datasetDriver.getModifiable());
		docDriver.setMultivalue(datasetDriver.getMultivalue());
		docDriver.setBiObjectID(datasetDriver.getBiMetaModelID());
		docDriver.setParameterUrlName(datasetDriver.getParameterUrlName());
		docDriver.setParID(datasetDriver.getParID());
		docDriver.setRequired(datasetDriver.getRequired());
		docDriver.setVisible(datasetDriver.getVisible());
		docDriver.setPriority(datasetDriver.getPriority());
		docDriver.setProg(datasetDriver.getProg());
		docDriver.setColSpan(datasetDriver.getColSpan());
		docDriver.setThickPerc(datasetDriver.getThickPerc());

		Parameter parameter = new Parameter();
		try {
			parameter = aParameterDAO.loadForExecutionByParameterIDandRoleName(docDriver.getParID(), role, false);
		} catch (EMFUserError e) {
			e.printStackTrace();
		}
		parameter.setId(datasetDriver.getParameter().getId());
		parameter.setType(datasetDriver.getParameter().getType());
		docDriver.setParameter(parameter);
		return docDriver;
	}
}
