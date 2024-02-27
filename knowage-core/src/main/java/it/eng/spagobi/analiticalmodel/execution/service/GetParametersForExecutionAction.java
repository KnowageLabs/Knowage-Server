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
package it.eng.spagobi.analiticalmodel.execution.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.json.JSONArray;

import it.eng.spago.base.SourceBean;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.handlers.ExecutionInstance;
import it.eng.spagobi.analiticalmodel.document.handlers.LovResultCacheManager;
import it.eng.spagobi.analiticalmodel.execution.bo.defaultvalues.DefaultValuesList;
import it.eng.spagobi.analiticalmodel.execution.bo.defaultvalues.DefaultValuesRetriever;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ObjParuse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ObjParview;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ParameterUse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IBIObjectParameterDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IObjParuseDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IObjParviewDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterUseDAO;
import it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail;
import it.eng.spagobi.behaviouralmodel.lov.bo.LovDetailFactory;
import it.eng.spagobi.behaviouralmodel.lov.bo.LovResultHandler;
import it.eng.spagobi.behaviouralmodel.lov.bo.ModalitiesValue;
import it.eng.spagobi.commons.constants.ObjectsTreeConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.serializer.SerializationException;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.commons.services.AbstractSpagoBIAction;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.service.JSONSuccess;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class GetParametersForExecutionAction extends AbstractSpagoBIAction {

	private static final Logger LOGGER = Logger.getLogger(GetParametersForExecutionAction.class);

	public static final String SERVICE_NAME = "GET_PARAMETERS_FOR_EXECUTION_SERVICE";

	// request parameters
	public static final String DOCUMENT_ID = ObjectsTreeConstants.OBJECT_ID;
	public static final String DOCUMENT_LABEL = ObjectsTreeConstants.OBJECT_LABEL;
	public static final String CALLBACK = "callback";
	// LOGGER component
	private IParameterDAO ANALYTICAL_DRIVER_DAO;

	@Override
	public void doService() {

		List<ParameterForExecution> parametersForExecution = getParameters();

		JSONArray parametersJSON = null;
		try {
			parametersJSON = (JSONArray) SerializerFactory.getSerializer("application/json")
					.serialize(parametersForExecution, getLocale());
		} catch (SerializationException e) {
			e.printStackTrace();
		}

		String callback = getAttributeAsString(CALLBACK);
		LOGGER.debug("Parameter [" + CALLBACK + "] is equals to [" + callback + "]");

		try {
			writeBackToClient(new JSONSuccess(parametersJSON, callback));
		} catch (IOException e) {
			throw new SpagoBIServiceException("Impossible to write back the responce to the client", e);
		}
	}

	public List<ParameterForExecution> getParameters() {

		List<ParameterForExecution> parametersForExecution = new ArrayList<>();

		Assert.assertNotNull(getContext(), "Execution context cannot be null");
		Assert.assertNotNull(getContext().getExecutionInstance(ExecutionInstance.class.getName()),
				"Execution instance cannot be null");

		ExecutionInstance executionInstance = getContext().getExecutionInstance(ExecutionInstance.class.getName());

		BIObject document = executionInstance.getBIObject();

		List<BIObjectParameter> parameters = document.getDrivers();
		if (parameters != null && !parameters.isEmpty()) {
			Iterator<BIObjectParameter> it = parameters.iterator();
			while (it.hasNext()) {
				BIObjectParameter parameter = it.next();
				parametersForExecution.add(new ParameterForExecution(parameter));
			}
		}

		return parametersForExecution;
	}

	public class ParameterForExecution {

		// DAOs
		private IParameterUseDAO ANALYTICAL_DRIVER_USE_MODALITY_DAO;
		private IObjParuseDAO DATA_DEPENDENCIES_DAO;
		private IObjParviewDAO VISUAL_DEPENDENCIES_DAO;
		private IBIObjectParameterDAO ANALYTICAL_DOCUMENT_PARAMETER_DAO;

		// attribute loaded from spagobi's metadata
		BIObjectParameter analyticalDocumentParameter;
		Parameter analyticalDriver;
		ParameterUse analyticalDriverExecModality;
		List<ObjParuse> dataDependencies;
		List<ObjParview> visualDependencies;

		// attribute used by the serializer
		String id;
		Integer parameterUseId;
		String label;
		String parType; // DATE, STRING, ...
		String selectionType; // COMBOBOX, LIST, ...
		String valueSelection; // "lov", "man_in", "map_in"
		String selectedLayer;
		String selectedLayerProp;
		boolean enableMaximizer;
		String typeCode; // SpagoBIConstants.INPUT_TYPE_X
		boolean mandatory;
		boolean multivalue;
		boolean visible;
		Integer colspan;
		Integer thickPerc;

		int valuesCount;
		// used to comunicate to the client the unique
		// valid value in case valuesCount = 1
		String value;

		// in case of massive export these are the parameter ids referred by current parameter
		List<Integer> objParameterIds;

		DefaultValuesList defaultValues;

		// dependencies (dataDep & visualDep &lovDep)
		Map<String, List<ParameterDependency>> dependencies;

		public abstract class ParameterDependency {
			public String urlName;
		}

		public class DataDependency extends ParameterDependency {
		}

		public class VisualDependency extends ParameterDependency {
			public ObjParview condition;
		}

		public class LovDependency extends ParameterDependency {
		}

		public ParameterForExecution(BIObjectParameter biParam) {

			analyticalDocumentParameter = biParam;

			initDAO();
			initAttributes();
			initDependencies();
			loadValues();
			loadDefaultValues();

			objParameterIds = new ArrayList<>();
		}

		private void initDAO() {
			ANALYTICAL_DRIVER_USE_MODALITY_DAO = DAOFactory.getParameterUseDAO();

			DATA_DEPENDENCIES_DAO = DAOFactory.getObjParuseDAO();

			VISUAL_DEPENDENCIES_DAO = DAOFactory.getObjParviewDAO();
			try {
				ANALYTICAL_DOCUMENT_PARAMETER_DAO = DAOFactory.getBIObjectParameterDAO();
			} catch (HibernateException e) {
				throw new SpagoBIServiceException("An error occurred while retrieving DAO ["
						+ ANALYTICAL_DOCUMENT_PARAMETER_DAO.getClass().getName() + "]", e);
			}

			ANALYTICAL_DRIVER_DAO = DAOFactory.getParameterDAO();
		}

		ExecutionInstance getExecutionInstance() {
			ExecutionInstance executionInstance = null;

			Assert.assertNotNull(getContext().isExecutionInstanceAMap(ExecutionInstance.class.getName()),
					"Execution instance cannot be null");
			boolean isAMap = getContext().isExecutionInstanceAMap(ExecutionInstance.class.getName());

			if (!isAMap) {
				executionInstance = getContext().getExecutionInstance(ExecutionInstance.class.getName());
			} else {
				Map<Integer, ExecutionInstance> instances = getContext()
						.getExecutionInstancesAsMap(ExecutionInstance.class.getName());
				Integer objId = analyticalDocumentParameter.getBiObjectID();
				executionInstance = instances.get(objId);
			}
			return executionInstance;
		}

		void initAttributes() {
			ExecutionInstance executionInstance = this.getExecutionInstance();
			if (executionInstance == null) {
				throw new SpagoBIServiceException(SERVICE_NAME,
						"Impossible to find in context execution instance for execution of document with id " + " ["
								+ analyticalDocumentParameter.getBiObjectID() + "]");
			}

			id = analyticalDocumentParameter.getParameterUrlName();
			label = analyticalDocumentParameter.getLabel();

			Integer parameterId = analyticalDocumentParameter.getParameter().getId();
			analyticalDriver = analyticalDocumentParameter.getParameter();
			parType = analyticalDriver.getType();

			selectionType = analyticalDriver.getModalityValue().getSelectionType();

			valueSelection = analyticalDriver.getValueSelection();
			selectedLayer = analyticalDriver.getSelectedLayer();
			selectedLayerProp = analyticalDriver.getSelectedLayerProp();

			typeCode = analyticalDriver.getModalityValue().getITypeCd();

			/*
			 * mandatory = false; Iterator it = analyticalDriver.getChecks().iterator(); while (it.hasNext()){ Check check = (Check)it.next(); if
			 * (check.getValueTypeCd().equalsIgnoreCase("MANDATORY")){ mandatory = true; break; } }
			 */
			mandatory = analyticalDocumentParameter.getRequired() == 1;

			multivalue = analyticalDocumentParameter.isMultivalue();

			visible = analyticalDocumentParameter.getVisible() == 1;

			colspan = analyticalDocumentParameter.getColSpan() != null ? analyticalDocumentParameter.getColSpan() : 1;
			thickPerc = analyticalDocumentParameter.getThickPerc() != null ? analyticalDocumentParameter.getThickPerc()
					: 0;

			try {
				analyticalDriverExecModality = ANALYTICAL_DRIVER_USE_MODALITY_DAO.loadByParameterIdandRole(
						analyticalDocumentParameter.getParID(), executionInstance.getExecutionRole());
			} catch (Exception e) {
				throw new SpagoBIServiceException(SERVICE_NAME,
						"Impossible to find any valid execution modality for parameter [" + id + "] and role ["
								+ executionInstance.getExecutionRole() + "]",
						e);
			}

			Assert.assertNotNull(analyticalDriverExecModality,
					"Impossible to find any valid execution modality for parameter [" + id + "] and role ["
							+ executionInstance.getExecutionRole() + "]");

			parameterUseId = analyticalDriverExecModality.getUseID();

			enableMaximizer = analyticalDriverExecModality.isMaximizerEnabled();
		}

		private void initDependencies() {
			initDataDependencies();
			initVisualDependencies();
			initLovDependencies();
		}

		private void initVisualDependencies() {
			if (dependencies == null) {
				dependencies = new HashMap<>();
			}

			try {
				visualDependencies = VISUAL_DEPENDENCIES_DAO.loadObjParviews(analyticalDocumentParameter.getId());
			} catch (HibernateException e) {
				throw new SpagoBIServiceException(
						"An error occurred while loading parameter visual dependecies for parameter [" + id + "]", e);
			}

			Iterator<ObjParview> it = visualDependencies.iterator();
			while (it.hasNext()) {
				ObjParview dependency = it.next();
				Integer objParFatherId = dependency.getParFatherId();
				try {
					BIObjectParameter objParFather = ANALYTICAL_DOCUMENT_PARAMETER_DAO
							.loadForDetailByObjParId(objParFatherId);
					VisualDependency visualDependency = new VisualDependency();
					visualDependency.urlName = objParFather.getParameterUrlName();
					visualDependency.condition = dependency;
					if (!dependencies.containsKey(visualDependency.urlName)) {
						dependencies.put(visualDependency.urlName, new ArrayList<>());
					}
					List<ParameterDependency> depList = dependencies.get(visualDependency.urlName);
					depList.add(visualDependency);
				} catch (EMFUserError e) {
					throw new SpagoBIServiceException(
							"An error occurred while loading parameter [" + objParFatherId + "]", e);
				}
			}
		}

		private void initDataDependencies() {

			if (dependencies == null) {
				dependencies = new HashMap<>();
			}

			try {
				dataDependencies = DATA_DEPENDENCIES_DAO.loadObjParuse(analyticalDocumentParameter.getId(),
						analyticalDriverExecModality.getUseID());
			} catch (EMFUserError e) {
				throw new SpagoBIServiceException(
						"An error occurred while loading parameter data dependecies for parameter [" + id + "]", e);
			}

			Iterator<ObjParuse> it = dataDependencies.iterator();
			while (it.hasNext()) {
				ObjParuse dependency = it.next();
				Integer objParFatherId = dependency.getParFatherId();
				try {
					BIObjectParameter objParFather = ANALYTICAL_DOCUMENT_PARAMETER_DAO
							.loadForDetailByObjParId(objParFatherId);
					DataDependency dataDependency = new DataDependency();
					dataDependency.urlName = objParFather.getParameterUrlName();
					if (!dependencies.containsKey(dataDependency.urlName)) {
						dependencies.put(dataDependency.urlName, new ArrayList<>());
					}
					List<ParameterDependency> depList = dependencies.get(dataDependency.urlName);
					depList.add(dataDependency);
				} catch (EMFUserError e) {
					throw new SpagoBIServiceException(
							"An error occurred while loading parameter [" + objParFatherId + "]", e);
				}
			}
		}

		private void initLovDependencies() {

			if (dependencies == null) {
				dependencies = new HashMap<>();
			}
			// the execution instance could be a map if in massive export case
			ExecutionInstance executionInstance = null;
			Assert.assertNotNull(getContext().isExecutionInstanceAMap(ExecutionInstance.class.getName()),
					"Execution instance cannot be null");
			boolean isAMap = getContext().isExecutionInstanceAMap(ExecutionInstance.class.getName());
			if (!isAMap) {
				executionInstance = getContext().getExecutionInstance(ExecutionInstance.class.getName());
			} else {
				Map<Integer, ExecutionInstance> instances = getContext()
						.getExecutionInstancesAsMap(ExecutionInstance.class.getName());
				Integer objId = analyticalDocumentParameter.getBiObjectID();
				executionInstance = instances.get(objId);
			}
			if (executionInstance == null) {
				throw new SpagoBIServiceException(SERVICE_NAME,
						"Impossible to find in context execution instance for execution of document with id " + " ["
								+ analyticalDocumentParameter.getBiObjectID() + "]: was it searched as a map? "
								+ isAMap);
			}

			ILovDetail lovDetail = executionInstance.getLovDetail(analyticalDocumentParameter);
			Set<String> lovParameters = null;
			try {
				if (lovDetail != null) {
					lovParameters = lovDetail.getParameterNames();
					if (lovParameters != null && !lovParameters.isEmpty()) {
						LOGGER.debug("Found one or more parameters inside the LOV");
						List<BIObjectParameter> objParameters = ANALYTICAL_DOCUMENT_PARAMETER_DAO
								.loadBIObjectParametersById(analyticalDocumentParameter.getBiObjectID());
						LovDependency lovDependency = new LovDependency();
						for (BIObjectParameter objParameter : objParameters) {
							Parameter objAnalyticalDriver = ANALYTICAL_DRIVER_DAO
									.loadForDetailByParameterID(objParameter.getParameter().getId());
							if (objAnalyticalDriver != null && lovParameters.contains(objAnalyticalDriver.getLabel())) {
								LOGGER.debug("Found the analytical driver [" + objAnalyticalDriver.getLabel()
										+ "] associated to the placeholder in the LOV");
								lovDependency.urlName = objParameter.getParameterUrlName();
								break;
							}
						}
						if (lovDependency.urlName == null || lovDependency.urlName.isEmpty()) {
							throw new SpagoBIRuntimeException(
									"Impossible to found a parameter to satisfy the dependecy associated with the placeholder in the LOV ["
											+ id + "]");
						}

						if (!dependencies.containsKey(lovDependency.urlName)) {
							dependencies.put(lovDependency.urlName, new ArrayList<>());
						}
						List<ParameterDependency> depList = dependencies.get(lovDependency.urlName);
						depList.add(lovDependency);
					}
				}
			} catch (Exception e) {
				throw new SpagoBIServiceException(
						"An error occurred while loading parameter lov dependecies for parameter [" + id + "]", e);
			}
		}

		public void loadValues() {

			if (!hasParameterInsideLOV()
					&& ("COMBOBOX".equalsIgnoreCase(selectionType) || "LIST".equalsIgnoreCase(selectionType)
							|| "SLIDER".equalsIgnoreCase(selectionType) || "TREE".equalsIgnoreCase(selectionType))) { // load values only if it is not a lookup

				List lovs = getLOV();
				setValuesCount(lovs == null ? 0 : lovs.size());
				if (getValuesCount() == 1) {
					SourceBean lovSB = (SourceBean) lovs.get(0);
					value = getValueFromLov(lovSB);
				}
			} else {
				setValuesCount(-1); // it means that we don't know the lov size
			}
		}

		public void loadDefaultValues() {
			LOGGER.debug("IN");
			try {
				ExecutionInstance executionInstance = this.getExecutionInstance();
				DefaultValuesRetriever retriever = new DefaultValuesRetriever();
				IEngUserProfile profile = getUserProfile();
				defaultValues = retriever.getDefaultValues(analyticalDocumentParameter, executionInstance, profile);
			} catch (Exception e) {
				throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to get parameter's default values", e);
			}
			LOGGER.debug("OUT");
		}

		private List getLOV() {
			LOGGER.debug("IN");
			// the execution instance could be a map if in massive export case
			ExecutionInstance executionInstance = null;
			Assert.assertNotNull(getContext().isExecutionInstanceAMap(ExecutionInstance.class.getName()),
					"Execution instance cannot be null");
			boolean isAMap = getContext().isExecutionInstanceAMap(ExecutionInstance.class.getName());
			if (!isAMap) {
				executionInstance = getContext().getExecutionInstance(ExecutionInstance.class.getName());
			} else {
				Map<Integer, ExecutionInstance> instances = getContext()
						.getExecutionInstancesAsMap(ExecutionInstance.class.getName());
				Integer objId = analyticalDocumentParameter.getBiObjectID();
				executionInstance = instances.get(objId);
			}
			if (executionInstance == null) {
				throw new SpagoBIServiceException(SERVICE_NAME,
						"Impossible to find in context execution instance for execution of document with id " + " ["
								+ analyticalDocumentParameter.getBiObjectID() + "]: was it searched as a map? "
								+ isAMap);
			}

			List rows = null;
			String lovResult = null;
			try {
				// get the result of the lov
				IEngUserProfile profile = getUserProfile();

				LovResultCacheManager executionCacheManager = new LovResultCacheManager();
				lovResult = executionCacheManager.getLovResult(profile,
						executionInstance.getLovDetail(analyticalDocumentParameter),
						executionInstance.getDependencies(analyticalDocumentParameter), executionInstance, true);

				// get all the rows of the result
				LovResultHandler lovResultHandler = new LovResultHandler(lovResult);
				rows = lovResultHandler.getRows();
			} catch (Exception e) {
				throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to get parameter's values", e);
			}
			LOGGER.debug("OUT");
			return rows;
		}

		private boolean hasParameterInsideLOV(ExecutionInstance executionInstance) {
			ILovDetail lovDetail = executionInstance.getLovDetail(analyticalDocumentParameter);
			if (lovDetail != null) {
				Set<String> parameterNames = null;
				try {
					parameterNames = lovDetail.getParameterNames();
				} catch (Exception e) {
					throw new SpagoBIServiceException(SERVICE_NAME,
							"Impossible to find in context execution lov parameters for execution of document with id "
									+ " [" + analyticalDocumentParameter.getBiObjectID() + "]",
							e);
				}
				if (parameterNames != null && !parameterNames.isEmpty()) {
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		}

		private boolean hasParameterInsideLOV() {
			// ExecutionInstance executionInstance = getContext().getExecutionInstance( ExecutionInstance.class.getName() );
			// the execution instance could be a map if in massive export case
			ExecutionInstance executionInstance = null;
			Assert.assertNotNull(getContext().isExecutionInstanceAMap(ExecutionInstance.class.getName()),
					"Execution instance cannot be null");
			boolean isAMap = getContext().isExecutionInstanceAMap(ExecutionInstance.class.getName());
			if (!isAMap) {
				executionInstance = getContext().getExecutionInstance(ExecutionInstance.class.getName());
			} else {
				Map<Integer, ExecutionInstance> instances = getContext()
						.getExecutionInstancesAsMap(ExecutionInstance.class.getName());
				Integer objId = analyticalDocumentParameter.getBiObjectID();
				executionInstance = instances.get(objId);
			}
			if (executionInstance == null) {
				throw new SpagoBIServiceException(SERVICE_NAME,
						"Impossible to find in context execution instance for execution of document with id " + " ["
								+ analyticalDocumentParameter.getBiObjectID() + "]: was it searched as a map? "
								+ isAMap);
			}
			return hasParameterInsideLOV(executionInstance);
		}

		private String getValueFromLov(SourceBean lovSB) {
			String value = null;
			ILovDetail lovProvDet = null;
			try {
				Parameter par = analyticalDocumentParameter.getParameter();
				ModalitiesValue lov = par.getModalityValue();
				// build the ILovDetail object associated to the lov
				String lovProv = lov.getLovProvider();
				lovProvDet = LovDetailFactory.getLovFromXML(lovProv);

				value = (String) lovSB.getAttribute(lovProvDet.getValueColumnName());
			} catch (Exception e) {
				throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to get parameter's value", e);
			}

			return value;
		}

		// ========================================================================================
		// ACCESSOR METHODS
		// ========================================================================================
		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getTypeCode() {
			return typeCode;
		}

		public void setTypeCode(String typeCode) {
			this.typeCode = typeCode;
		}

		public Parameter getPar() {
			return analyticalDriver;
		}

		public void setPar(Parameter par) {
			this.analyticalDriver = par;
		}

		public String getParType() {
			return parType;
		}

		public void setParType(String parType) {
			this.parType = parType;
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		public boolean isMandatory() {
			return mandatory;
		}

		public void setMandatory(boolean mandatory) {
			this.mandatory = mandatory;
		}

		public boolean isMultivalue() {
			return multivalue;
		}

		public void setMultivalue(boolean multivalue) {
			this.multivalue = multivalue;
		}

		public boolean isVisible() {
			return visible;
		}

		public void setVisible(boolean visible) {
			this.visible = visible;
		}

		public String getSelectionType() {
			return selectionType;
		}

		public void setSelectionType(String selectionType) {
			this.selectionType = selectionType;
		}

		public String getValueSelection() {
			return valueSelection;
		}

		public void setValueSelection(String valueSelection) {
			this.valueSelection = valueSelection;
		}

		public String getSelectedLayer() {
			return selectedLayer;
		}

		public void setSelectedLayer(String selectedLayer) {
			this.selectedLayer = selectedLayer;
		}

		public String getSelectedLayerProp() {
			return selectedLayerProp;
		}

		public void setSelectedLayerProp(String selectedLayerProp) {
			this.selectedLayerProp = selectedLayerProp;
		}

		public boolean isEnableMaximizer() {
			return enableMaximizer;
		}

		public void setEnableMaximizer(boolean enableMaximizer) {
			this.enableMaximizer = enableMaximizer;
		}

		public int getValuesCount() {
			return valuesCount;
		}

		public void setValuesCount(int valuesCount) {
			this.valuesCount = valuesCount;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public Map<String, List<ParameterDependency>> getDependencies() {
			return dependencies;
		}

		public void setDependencies(Map<String, List<ParameterDependency>> dependencies) {
			this.dependencies = dependencies;
		}

		public Integer getParameterUseId() {
			return parameterUseId;
		}

		public void setParameterUseId(Integer parameterUseId) {
			this.parameterUseId = parameterUseId;
		}

		public List<Integer> getObjParameterIds() {
			return objParameterIds;
		}

		public void setObjParameterIds(List<Integer> objParameterIds) {
			this.objParameterIds = objParameterIds;
		}

		public List<ObjParview> getVisualDependencies() {
			return visualDependencies;
		}

		public void setVisualDependencies(List<ObjParview> visualDependencies) {
			this.visualDependencies = visualDependencies;
		}

		public List<ObjParuse> getDataDependencies() {
			return dataDependencies;
		}

		public void setDataDependencies(List<ObjParuse> dataDependencies) {
			this.dataDependencies = dataDependencies;
		}

		public DefaultValuesList getDefaultValues() {
			return defaultValues;
		}

		public void setDefaultValues(DefaultValuesList defaultValues) {
			this.defaultValues = defaultValues;
		}

		public int getColspan() {
			return colspan;
		}

		public void setColspan(int colspan) {
			this.colspan = colspan;
		}

		public Integer getThickPerc() {
			return thickPerc;
		}

		public void setThickPerc(Integer thickPerc) {
			this.thickPerc = thickPerc;
		}

	}

}
