package it.eng.spagobi.analiticalmodel.document.handlers;

import it.eng.spago.base.SourceBean;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
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
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.user.UserProfileManager;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

public class DocumentParameters {
	private static Logger logger = Logger.getLogger(DocumentParameters.class);
	public static final String SERVICE_NAME = "GET_PARAMETERS_FOR_EXECUTION_SERVICE";

	// DAOs
	private IParameterUseDAO ANALYTICAL_DRIVER_USE_MODALITY_DAO;
	private IObjParuseDAO DATA_DEPENDENCIES_DAO;
	private IObjParviewDAO VISUAL_DEPENDENCIES_DAO;
	private IBIObjectParameterDAO ANALYTICAL_DOCUMENT_PARAMETER_DAO;
	private IParameterDAO ANALYTICAL_DRIVER_DAO;

	// attribute loaded from spagobi's metadata
	BIObjectParameter analyticalDocumentParameter;
	Parameter analyticalDriver;
	ParameterUse analyticalDriverExecModality;
	List dataDependencies;
	List visualDependencies;
	List lovDependencies;

	// attribute used by the serializer
	Integer biObjectId;
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

	String executionRole;
	Locale locale;
	BIObject object;

	public abstract class ParameterDependency {
		public String urlName;
	};

	public class DataDependency extends ParameterDependency {
	}

	public class VisualDependency extends ParameterDependency {
		public ObjParview condition;
	}

	public class LovDependency extends ParameterDependency {
	}

	public DocumentParameters(BIObjectParameter biParam, String exeRole, Locale loc, BIObject obj) {
		analyticalDocumentParameter = biParam;
		executionRole = exeRole;
		object = obj;
		locale = loc;
		initDAO();
		initAttributes();
		initDependencies();
		loadValues();
		loadDefaultValues();
		objParameterIds = new ArrayList<Integer>();
	}

	private void initDAO() {
		try {
			ANALYTICAL_DRIVER_USE_MODALITY_DAO = DAOFactory.getParameterUseDAO();
		} catch (EMFUserError e) {
			throw new SpagoBIServiceException("An error occurred while retrieving DAO [" + ANALYTICAL_DRIVER_USE_MODALITY_DAO.getClass().getName() + "]", e);
		}

		try {
			DATA_DEPENDENCIES_DAO = DAOFactory.getObjParuseDAO();
		} catch (EMFUserError e) {
			throw new SpagoBIServiceException("An error occurred while retrieving DAO [" + DATA_DEPENDENCIES_DAO.getClass().getName() + "]", e);
		}

		try {
			VISUAL_DEPENDENCIES_DAO = DAOFactory.getObjParviewDAO();
		} catch (EMFUserError e) {
			throw new SpagoBIServiceException("An error occurred while retrieving DAO [" + VISUAL_DEPENDENCIES_DAO.getClass().getName() + "]", e);

		}
		try {
			ANALYTICAL_DOCUMENT_PARAMETER_DAO = DAOFactory.getBIObjectParameterDAO();
		} catch (EMFUserError e) {
			throw new SpagoBIServiceException("An error occurred while retrieving DAO [" + ANALYTICAL_DOCUMENT_PARAMETER_DAO.getClass().getName() + "]", e);
		}

		try {
			ANALYTICAL_DRIVER_DAO = DAOFactory.getParameterDAO();
		} catch (EMFUserError e) {
			throw new SpagoBIServiceException("An error occurred while retrieving DAO [" + ANALYTICAL_DRIVER_DAO.getClass().getName() + "]", e);
		}

	}

	void initAttributes() {
		id = analyticalDocumentParameter.getParameterUrlName();
		biObjectId = analyticalDocumentParameter.getId();
		// label = localize( analyticalDocumentParameter.getLabel() );
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
		thickPerc = analyticalDocumentParameter.getThickPerc() != null ? analyticalDocumentParameter.getThickPerc() : 0;

		try {
			analyticalDriverExecModality = ANALYTICAL_DRIVER_USE_MODALITY_DAO.loadByParameterIdandRole(analyticalDocumentParameter.getParID(), executionRole);
		} catch (Exception e) {
			throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to find any valid execution modality for parameter [" + id + "] and role ["
					+ executionRole + "]", e);
		}
		Assert.assertNotNull(analyticalDriverExecModality, "Impossible to find any valid execution modality for parameter [" + id + "] and role ["
				+ executionRole + "]");
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
			dependencies = new HashMap<String, List<ParameterDependency>>();
		}
		try {
			visualDependencies = VISUAL_DEPENDENCIES_DAO.loadObjParviews(analyticalDocumentParameter.getId());
		} catch (EMFUserError e) {
			throw new SpagoBIServiceException("An error occurred while loading parameter visual dependecies for parameter [" + id + "]", e);
		}
		Iterator it = visualDependencies.iterator();
		while (it.hasNext()) {
			ObjParview dependency = (ObjParview) it.next();
			Integer objParFatherId = dependency.getObjParFatherId();
			try {
				BIObjectParameter objParFather = ANALYTICAL_DOCUMENT_PARAMETER_DAO.loadForDetailByObjParId(objParFatherId);
				VisualDependency visualDependency = new VisualDependency();
				visualDependency.urlName = objParFather.getParameterUrlName();
				visualDependency.condition = dependency;
				if (!dependencies.containsKey(visualDependency.urlName)) {
					dependencies.put(visualDependency.urlName, new ArrayList<ParameterDependency>());
				}
				List<ParameterDependency> depList = dependencies.get(visualDependency.urlName);
				depList.add(visualDependency);
			} catch (EMFUserError e) {
				throw new SpagoBIServiceException("An error occurred while loading parameter [" + objParFatherId + "]", e);
			}
		}
	}

	private void initLovDependencies() {

		if (dependencies == null) {
			dependencies = new HashMap<String, List<ParameterDependency>>();
		}
		// the execution instance could be a map if in massive export case
		// ExecutionInstance executionInstance = null;
		// Assert.assertNotNull(getContext().isExecutionInstanceAMap(ExecutionInstance.class.getName()), "Execution instance cannot be null");
		// boolean isAMap = getContext().isExecutionInstanceAMap(ExecutionInstance.class.getName());
		// if (!isAMap) {
		// executionInstance = getContext().getExecutionInstance(ExecutionInstance.class.getName());
		// } else {
		// Map<Integer, ExecutionInstance> instances = getContext().getExecutionInstancesAsMap(ExecutionInstance.class.getName());
		// Integer objId = analyticalDocumentParameter.getBiObjectID();
		// executionInstance = instances.get(objId);
		// }
		// if (executionInstance == null) {
		// throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to find in context execution instance for execution of document with id " + " ["
		// + analyticalDocumentParameter.getBiObjectID() + "]: was it searched as a map? " + isAMap);
		// }

		DocumentUrlManager dum = new DocumentUrlManager(UserProfileManager.getProfile(), locale);
		ILovDetail lovDetail = dum.getLovDetail(analyticalDocumentParameter);
		Set<String> lovParameters = null;
		try {
			if (lovDetail != null) {
				lovParameters = lovDetail.getParameterNames();
				if (lovParameters != null && !lovParameters.isEmpty()) {
					logger.debug("Found one or more parameters inside the LOV");
					List<BIObjectParameter> objParameters = ANALYTICAL_DOCUMENT_PARAMETER_DAO.loadBIObjectParametersById(analyticalDocumentParameter
							.getBiObjectID());
					LovDependency lovDependency = new LovDependency();
					for (BIObjectParameter objParameter : objParameters) {
						Parameter objAnalyticalDriver = ANALYTICAL_DRIVER_DAO.loadForDetailByParameterID(objParameter.getParameter().getId());
						if (objAnalyticalDriver != null && lovParameters.contains(objAnalyticalDriver.getLabel())) {
							logger.debug("Found the analytical driver [" + objAnalyticalDriver.getLabel() + "] associated to the placeholder in the LOV");
							lovDependency.urlName = objParameter.getParameterUrlName();
							break;
						}
					}
					if (lovDependency.urlName == null || lovDependency.urlName.isEmpty()) {
						throw new SpagoBIRuntimeException(
								"Impossible to found a parameter to satisfy the dependecy associated with the placeholder in the LOV [" + id + "]");
					}

					if (!dependencies.containsKey(lovDependency.urlName)) {
						dependencies.put(lovDependency.urlName, new ArrayList<ParameterDependency>());
						lovDependencies = new ArrayList<>();
						lovDependencies.add(lovDependency.urlName);
					}
					List<ParameterDependency> depList = dependencies.get(lovDependency.urlName);
					depList.add(lovDependency);
				}
			}
		} catch (Exception e) {
			throw new SpagoBIServiceException("An error occurred while loading parameter lov dependecies for parameter [" + id + "]", e);
		}
	}

	private void initDataDependencies() {
		if (dependencies == null) {
			dependencies = new HashMap<String, List<ParameterDependency>>();
		}
		try {
			dataDependencies = DATA_DEPENDENCIES_DAO.loadObjParuse(analyticalDocumentParameter.getId(), analyticalDriverExecModality.getUseID());
		} catch (EMFUserError e) {
			throw new SpagoBIServiceException("An error occurred while loading parameter data dependecies for parameter [" + id + "]", e);
		}
		Iterator it = dataDependencies.iterator();
		while (it.hasNext()) {
			ObjParuse dependency = (ObjParuse) it.next();
			Integer objParFatherId = dependency.getObjParFatherId();
			try {
				BIObjectParameter objParFather = ANALYTICAL_DOCUMENT_PARAMETER_DAO.loadForDetailByObjParId(objParFatherId);
				DataDependency dataDependency = new DataDependency();
				dataDependency.urlName = objParFather.getParameterUrlName();
				if (!dependencies.containsKey(dataDependency.urlName)) {
					dependencies.put(dataDependency.urlName, new ArrayList<ParameterDependency>());
				}
				List<ParameterDependency> depList = dependencies.get(dataDependency.urlName);
				depList.add(dataDependency);
			} catch (EMFUserError e) {
				throw new SpagoBIServiceException("An error occurred while loading parameter [" + objParFatherId + "]", e);
			}
		}
	}

	public void loadValues() {
		// if ("COMBOBOX".equalsIgnoreCase(selectionType) || "LIST".equalsIgnoreCase(selectionType) || "SLIDER".equalsIgnoreCase(selectionType)
		// || "TREE".equalsIgnoreCase(selectionType)) { // load values only if it is not a lookup

		if (!hasParameterInsideLOV()
				&& ("COMBOBOX".equalsIgnoreCase(selectionType) || "LIST".equalsIgnoreCase(selectionType) || "SLIDER".equalsIgnoreCase(selectionType) || "TREE"
						.equalsIgnoreCase(selectionType))) { // load values only if it is not a lookup

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
		logger.debug("IN");
		try {
			DefaultValuesRetriever retriever = new DefaultValuesRetriever();
			// IEngUserProfile profile = getUserProfile();
			IEngUserProfile profile = UserProfileManager.getProfile();
			defaultValues = retriever.getDefaultValuesDum(analyticalDocumentParameter, this.object, profile, this.locale, this.executionRole);
		} catch (Exception e) {
			throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to get parameter's default values", e);
		}
		logger.debug("OUT");
	}

	private List getLOV() {
		List rows = null;
		String lovResult = null;
		try {
			// get the result of the lov
			// IEngUserProfile profile = getUserProfile();
			IEngUserProfile profile = UserProfileManager.getProfile();
			LovResultCacheManager executionCacheManager = new LovResultCacheManager();
			// lovResult = executionCacheManager.getLovResult(profile, executionInstance.getLovDetail(analyticalDocumentParameter),
			// executionInstance.getDependencies(analyticalDocumentParameter), executionInstance, true);
			DocumentUrlManager dum = new DocumentUrlManager(UserProfileManager.getProfile(), locale);
			lovResult = executionCacheManager.getLovResultDum(profile, dum.getLovDetail(analyticalDocumentParameter),
					dum.getDependencies(analyticalDocumentParameter, this.executionRole), this.object, true, this.locale);
			// get all the rows of the result
			LovResultHandler lovResultHandler = new LovResultHandler(lovResult);
			rows = lovResultHandler.getRows();
		} catch (Exception e) {
			throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to get parameter's values", e);
		}
		logger.debug("OUT");
		return rows;
	}

	private boolean hasParameterInsideLOV() {
		DocumentUrlManager dum = new DocumentUrlManager(UserProfileManager.getProfile(), locale);
		ILovDetail lovDetail = dum.getLovDetail(analyticalDocumentParameter);
		if (lovDetail != null) {
			Set<String> parameterNames = null;
			try {
				parameterNames = lovDetail.getParameterNames();
			} catch (Exception e) {
				throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to find in context execution lov parameters for execution of document with id "
						+ " [" + analyticalDocumentParameter.getBiObjectID() + "]", e);
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

	public Integer getBiObjectId() {
		return biObjectId;
	}

	public void setBiObjectId(Integer biObjectId) {
		this.biObjectId = biObjectId;
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

	public List getVisualDependencies() {
		return visualDependencies;
	}

	public void setVisualDependencies(List visualDependencies) {
		this.visualDependencies = visualDependencies;
	}

	public List getDataDependencies() {
		return dataDependencies;
	}

	public void setDataDependencies(List dataDependencies) {
		this.dataDependencies = dataDependencies;
	}

	public List getLovDependencies() {
		return lovDependencies;
	}

	public void setLovDependencies(List lovDependencies) {
		this.lovDependencies = lovDependencies;
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

	public BIObjectParameter getAnalyticalDocumentParameter() {
		return analyticalDocumentParameter;
	}

	public void setAnalyticalDocumentParameter(BIObjectParameter analyticalDocumentParameter) {
		this.analyticalDocumentParameter = analyticalDocumentParameter;
	}

}