package it.eng.spagobi.analiticalmodel.document.handlers;

import java.util.ArrayList;
import java.util.Collections;
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
//		MetaModel meta = DAOFactory.getMetaModelsDAO().loadMetaModelById(id)
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

		IBIObjDataSetDAO biObjDataSetDAO = DAOFactory.getBIObjDataSetDAO();
		List<BIObjectParameter> toAdd = null;
		IDataSetDAO datasetDao = DAOFactory.getDataSetDAO();
		IMetaModelsDAO businessModelsDAO = DAOFactory.getMetaModelsDAO();
		IBIMetaModelParameterDAO driversDao = DAOFactory.getBIMetaModelParameterDAO();

		try {
			ArrayList<BIObjDataSet> biObjDataSetList = biObjDataSetDAO.getBiObjDataSets(biObject.getId());
			Iterator itDs = biObjDataSetList.iterator();
			while (itDs.hasNext()) {
				BIObjDataSet biObjDataSet = (BIObjDataSet) itDs.next();
				Integer dsId = biObjDataSet.getDataSetId();

				toAdd = Collections.emptyList();

				IDataSet dataset = datasetDao.loadDataSetById(dsId);
				dataset = dataset instanceof VersionedDataSet ? ((VersionedDataSet) dataset).getWrappedDataset() : dataset;
				if (dataset != null && dataset.getDsType() == "SbiQbeDataSet") {
					JSONObject jsonConfig = new JSONObject(dataset.getConfiguration());
					String businessModelName = jsonConfig.getString("qbeDatamarts");
					MetaModel businessModel = businessModelsDAO.loadMetaModelByName(businessModelName);
					qbeDatasetDrivers = driversDao.loadBIMetaModelParameterByMetaModelId(businessModel.getId());
					if (qbeDatasetDrivers != null && !qbeDatasetDrivers.isEmpty()) {
						toAdd = toDocumentDrivers(qbeDatasetDrivers, biObject, role);
					}
				}

				toReturn.addAll(toAdd);

				// in a document i can use only one qbe dataset WITH DRIVERS
				if (!toReturn.isEmpty()) {
					break;
				}
			}
		} catch (Exception e) {
			logger.error("Couldn't retrieve drivers from meta model", e);
		}
		return toReturn;
	}

	/**
	 * @deprecated Where possible, prefer {@link #getDatasetMetaModel(Integer, String)} or {@link #getBusinessModelDrivers(String, String)}
	 */
	@Deprecated
	public List<BIMetaModelParameter> getDatasetDrivers(Integer dsId, String role) {
		final List<BIMetaModelParameter> ret = new ArrayList<>();
		List<BIMetaModelParameter> toAdd = new ArrayList<>();

		try {
			IDataSetDAO datasetDao = DAOFactory.getDataSetDAO();
			IDataSet dataset = datasetDao.loadDataSetById(dsId);
			dataset = dataset instanceof VersionedDataSet ? ((VersionedDataSet) dataset).getWrappedDataset() : dataset;
			if (dataset != null && dataset.getDsType() == "SbiQbeDataSet") {
				JSONObject jsonConfig = new JSONObject(dataset.getConfiguration());
				String businessModelName = jsonConfig.getString("qbeDatamarts");
				toAdd.addAll(getBusinessModelDrivers(businessModelName, role));
			}

			ret.addAll(toAdd);
		} catch (Exception e) {
			logger.error("Couldn't retrieve drivers from meta model", e);
		}

		return ret;
	}

	public List<BIMetaModelParameter> getBusinessModelDrivers(String businessModelName, String role) {
		List<BIMetaModelParameter> toAdd2 = new ArrayList<>();
		IMetaModelsDAO businessModelsDAO = DAOFactory.getMetaModelsDAO();
		MetaModel businessModel = businessModelsDAO.loadMetaModelByName(businessModelName);
		IBIMetaModelParameterDAO driversDao = DAOFactory.getBIMetaModelParameterDAO();
		List<BIMetaModelParameter> qbeDatasetDrivers = driversDao.loadBIMetaModelParameterByMetaModelId(businessModel.getId());
		if (qbeDatasetDrivers != null && !qbeDatasetDrivers.isEmpty()) {
			toAdd2.addAll(toMetamodelDrivers(qbeDatasetDrivers, role));
		}
		return toAdd2;
	}

	public MetaModel getDatasetMetaModel(Integer dsId, String role) {
		IDataSetDAO datasetDao = DAOFactory.getDataSetDAO();
		MetaModel businessModel = null;

		try {
			IDataSet dataset = datasetDao.loadDataSetById(dsId);
			dataset = dataset instanceof VersionedDataSet ? ((VersionedDataSet) dataset).getWrappedDataset() : dataset;
			if (dataset != null && dataset.getDsType() == "SbiQbeDataSet") {
				IMetaModelsDAO businessModelsDAO = DAOFactory.getMetaModelsDAO();
				IBIMetaModelParameterDAO driversDao = DAOFactory.getBIMetaModelParameterDAO();
				List<BIMetaModelParameter> qbeDatasetDrivers = new ArrayList<BIMetaModelParameter>();
				List<BIMetaModelParameter> toAdd = null;
				List<BIMetaModelParameter> ret = new ArrayList<>();

				JSONObject jsonConfig = new JSONObject(dataset.getConfiguration());
				String businessModelName = jsonConfig.getString("qbeDatamarts");
				businessModel = businessModelsDAO.loadMetaModelByName(businessModelName);
				qbeDatasetDrivers = driversDao.loadBIMetaModelParameterByMetaModelId(businessModel.getId());
				if (qbeDatasetDrivers != null && !qbeDatasetDrivers.isEmpty()) {
					toAdd = toMetamodelDrivers(qbeDatasetDrivers, role);
				}

				businessModel.getDrivers().clear();
				businessModel.getDrivers().addAll(toAdd);
			}

		} catch (Exception e) {
			logger.error("Couldn't retrieve drivers from meta model", e);
		}

		return businessModel;
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
		docDriver.setParameterValues(datasetDriver.getParameterValues());
		docDriver.setParameterValuesDescription(datasetDriver.getParameterValuesDescription());

		Parameter parameter = new Parameter();
		try {
			parameter = aParameterDAO.loadForExecutionByParameterIDandRoleName(docDriver.getParID(), role, false);
		} catch (EMFUserError e) {
			logger.info("Non fatal error during values loading for driver " + datasetDriver + " for the role " + role, e);
		}
		parameter.setId(datasetDriver.getParameter().getId());
		parameter.setType(datasetDriver.getParameter().getType());
		docDriver.setParameter(parameter);
		return docDriver;
	}

	private BIObjectParameter transformDSDrivertoBIObjectParameter(BIMetaModelParameter datasetDriver, BIObject biObject, String role) {
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

		docDriver.setParameterValues(datasetDriver.getParameterValues());
		docDriver.setParameterValuesDescription(datasetDriver.getParameterValuesDescription());

		Parameter parameter = new Parameter();
		try {
			parameter = aParameterDAO.loadForExecutionByParameterIDandRoleName(docDriver.getParID(), role, false);
		} catch (EMFUserError e) {
			logger.info("Non fatal error during values loading for driver " + datasetDriver + " for the role " + role + " with object " + biObject, e);
		}
		parameter.setId(datasetDriver.getParameter().getId());
		parameter.setType(datasetDriver.getParameter().getType());
		docDriver.setParameter(parameter);
		return docDriver;
	}
}
