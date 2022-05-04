package it.eng.spagobi.analiticalmodel.document.handlers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import it.eng.spago.base.SourceBean;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.DocumentExecutionUtils;
import it.eng.spagobi.analiticalmodel.execution.bo.LovValue;
import it.eng.spagobi.analiticalmodel.execution.bo.defaultvalues.DefaultValuesList;
import it.eng.spagobi.analiticalmodel.execution.bo.defaultvalues.DefaultValuesRetriever;
import it.eng.spagobi.analiticalmodel.execution.bo.minmaxvalue.MinMaxValuesRetriever;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.AbstractDriver;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.AbstractParuse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.AbstractParview;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIMetaModelParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ParameterUse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IBIMetaModelParameterDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IBIObjectParameterDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IMetaModelParuseDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IMetaModelParviewDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IObjParuseDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IObjParviewDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterUseDAO;
import it.eng.spagobi.behaviouralmodel.lov.bo.DependenciesPostProcessingLov;
import it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail;
import it.eng.spagobi.behaviouralmodel.lov.bo.LovDetailFactory;
import it.eng.spagobi.behaviouralmodel.lov.bo.LovResultHandler;
import it.eng.spagobi.behaviouralmodel.lov.bo.ModalitiesValue;
import it.eng.spagobi.behaviouralmodel.lov.exceptions.MissingLOVDependencyException;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.catalogue.metadata.IDrivableBIResource;
import it.eng.spagobi.user.UserProfileManager;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

public abstract class AbstractDriverRuntime<T extends AbstractDriver> {
	private static Logger logger = Logger.getLogger(AbstractDriverRuntime.class);
	public static final String SERVICE_NAME = "GET_PARAMETERS_FOR_EXECUTION_SERVICE";

	// DAOs
	private IParameterUseDAO ANALYTICAL_DRIVER_USE_MODALITY_DAO;
	private IObjParuseDAO DATA_DEPENDENCIES_DOC_DAO;
	private IObjParviewDAO VISUAL_DEPENDENCIES_DOC_DAO;
	private IBIObjectParameterDAO DRIVER_DOC_DAO;

	private IMetaModelParuseDAO DATA_DEPENDENCIES_BM_DAO;
	private IMetaModelParviewDAO VISUAL_DEPENDENCIES_BM_DAO;
	private IBIMetaModelParameterDAO DRIVER_BM_DAO;

	private IParameterDAO ANALYTICAL_DRIVER_DAO;

	// attribute loaded from spagobi's metadata
	T driver;
	Parameter analyticalDriver;
	ParameterUse analyticalDriverExecModality;
	List<AbstractParuse> dataDependencies;
	List<AbstractParview> visualDependencies;
	List lovDependencies;

	// attribute used by the serializer
	Integer biObjectId;
	Integer biMetaModelId;
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

	boolean isFromCross;

	String lovValueColumnName;
	String lovDescriptionColumnName;
	List<String> lovVisibleColumnsNames;
	List<String> lovInvisibleColumnsNames;
	private BiMap<String, String> colPlaceholder2ColName = HashBiMap.create();

	int valuesCount;
	// used to comunicate to the client the unique
	// valid value in case valuesCount = 1
	String value;

	String description;

	// in case of massive export these are the parameter ids referred by current parameter
	List<Integer> objParameterIds;

	List<Integer> metaModelParameterIds;

	DefaultValuesList defaultValues;
	LovValue maxValue;
	ArrayList<HashMap<String, Object>> admissibleValues;

	// dependencies (dataDep & visualDep & lovDep) for document and business model
	Map<String, List<DriverDependencyRuntime>> dependencies;

	String executionRole;
	Locale locale;
	IDrivableBIResource biResource;

	public abstract class DriverDependencyRuntime {
		public String urlName;
	};

	public class DataDependencyRuntime extends DriverDependencyRuntime {
	}

	public class VisualDependencyRuntime extends DriverDependencyRuntime {
		public AbstractParview condition;
	}

	public class LovDependencyRuntime extends DriverDependencyRuntime {
	}

	public AbstractDriverRuntime() {
	}

	public AbstractDriverRuntime(T driver2, String exeRole, Locale loc, IDrivableBIResource doc, AbstractBIResourceRuntime dum,
			List<? extends AbstractDriver> objParameters) {
		driver = driver2;
		executionRole = exeRole;
		biResource = doc;
		locale = loc;

		initDAO();
		initAttributes(driver);
		initDependencies(driver, objParameters);

		loadAdmissibleValues(driver, dum);

		loadDefaultValues(driver);
		loadMaxValue(driver);
		objParameterIds = new ArrayList<Integer>();
	}

	public AbstractDriverRuntime(T driver2, String exeRole, Locale loc, IDrivableBIResource doc, boolean _isFromCross, boolean loadAdmissible,
			AbstractBIResourceRuntime dum, List<? extends AbstractDriver> objParameters) {
		driver = driver2;
		executionRole = exeRole;
		biResource = doc;
		locale = loc;
		isFromCross = _isFromCross;
		initDAO();
		initAttributes(driver);
		initDependencies(driver, objParameters);
		if (loadAdmissible) {
			loadAdmissibleValues(driver, dum);
		}
		loadDefaultValues(driver);
		loadMaxValue(driver);
		objParameterIds = new ArrayList<Integer>();
	}

	public void initDAO() {
		ANALYTICAL_DRIVER_USE_MODALITY_DAO = DAOFactory.getParameterUseDAO();

		ANALYTICAL_DRIVER_DAO = DAOFactory.getParameterDAO();

	}

	void initAttributes(AbstractDriver driver) {
		id = driver.getParameterUrlName();
		biObjectId = driver.getId();
		// label = localize( driver.getLabel() );
		label = driver.getLabel();
		Integer parameterId = driver.getParameter().getId();
		analyticalDriver = driver.getParameter();
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
		mandatory = driver.getRequired() == 1;
		multivalue = driver.isMultivalue();
		visible = driver.getVisible() == 1;
		colspan = driver.getColSpan() != null ? driver.getColSpan() : 1;
		thickPerc = driver.getThickPerc() != null ? driver.getThickPerc() : 0;

		try {
			analyticalDriverExecModality = ANALYTICAL_DRIVER_USE_MODALITY_DAO.loadByParameterIdandRole(driver.getParID(), executionRole);
		} catch (Exception e) {
			throw new SpagoBIServiceException(SERVICE_NAME,
					"Impossible to find any valid execution modality for parameter [" + id + "] and role [" + executionRole + "]", e);
		}
		Assert.assertNotNull(analyticalDriverExecModality,
				"Impossible to find any valid execution modality for parameter [" + id + "] and role [" + executionRole + "]");
		parameterUseId = analyticalDriverExecModality.getUseID();
		enableMaximizer = analyticalDriverExecModality.isMaximizerEnabled();
	}

	private void initDependencies(T driver, List<? extends AbstractDriver> objParameters) {
		initDataDependencies(driver);
		initVisualDependencies(driver);
		initLovDependencies(driver, objParameters);
	}

	public abstract void initVisualDependencies(T driver);

	protected void initLovDependencies(AbstractDriver driver, List<? extends AbstractDriver> objParameters) {
		if (dependencies == null) {
			dependencies = new HashMap<String, List<DriverDependencyRuntime>>();
		}
		AbstractBIResourceRuntime dum = null;
		if (driver instanceof BIObjectParameter) {
			dum = new DocumentRuntime(UserProfileManager.getProfile(), locale);
		} else if (driver instanceof BIMetaModelParameter) {
			dum = new BusinessModelRuntime(UserProfileManager.getProfile(), locale);
		}
		ILovDetail lovDetail = dum.getLovDetail(driver);
		Set<String> lovParameters = null;
		try {
			if (lovDetail != null) {
				lovParameters = lovDetail.getParameterNames();
				if (lovParameters != null && !lovParameters.isEmpty()) {
					logger.debug("Found one or more parameters inside the LOV");
					LovDependencyRuntime lovDependency = new LovDependencyRuntime();
					lovDependencies = new ArrayList<>();
					for (AbstractDriver objParameter : objParameters) {
						Parameter objAnalyticalDriver = ANALYTICAL_DRIVER_DAO.loadForDetailByParameterID(objParameter.getParameter().getId());
						if (objAnalyticalDriver != null && lovParameters.contains(objAnalyticalDriver.getLabel())) {
							logger.debug("Found the analytical driver [" + objAnalyticalDriver.getLabel() + "] associated to the placeholder in the LOV");
							lovDependency.urlName = objParameter.getParameterUrlName();
							lovDependencies.add(lovDependency.urlName);
						}
					}
					if (lovDependency.urlName == null || lovDependency.urlName.isEmpty()) {
						throw new SpagoBIRuntimeException(
								"Impossible to found a parameter to satisfy the dependecy associated with the placeholder in the LOV [" + id + "]");
					}

					if (!dependencies.containsKey(lovDependency.urlName)) {
						dependencies.put(lovDependency.urlName, new ArrayList<DriverDependencyRuntime>());
					}
					List<DriverDependencyRuntime> depList = dependencies.get(lovDependency.urlName);
					depList.add(lovDependency);
				}
			}
		} catch (Exception e) {
			throw new SpagoBIServiceException("An error occurred while loading parameter lov dependecies for parameter [" + id + "]", e);
		}
	}

	public abstract void initDataDependencies(T driver);

	/**
	 * Load admissible values from LOV done if not lookup and not manual input done if is from cross and not manual input
	 */

	protected void loadAdmissibleValues(AbstractDriver driver, AbstractBIResourceRuntime dum) {
		logger.debug("IN");
		try {

			// DocumentRuntime dum = new DocumentRuntime(UserProfileManager.getProfile(), locale);

			// get LOV info
			Integer paruseId = analyticalDriverExecModality.getUseID();
			ParameterUse parameterUse = DAOFactory.getParameterUseDAO().loadByUseID(paruseId);
			// get admissible values metadata (i.e. LOV metadata, in case AD has LOV)
			if ("lov".equalsIgnoreCase(parameterUse.getValueSelection())) {
				// get LOV metadata
				ILovDetail lovProvDet = dum.getLovDetail(driver);

				lovVisibleColumnsNames = lovProvDet.getVisibleColumnNames();
				lovInvisibleColumnsNames = lovProvDet.getInvisibleColumnNames();
				lovDescriptionColumnName = lovProvDet.getDescriptionColumnName();
				lovValueColumnName = lovProvDet.getValueColumnName();

//				colName2colPlaceholder.put("_col0", lovValueColumnName);
//				colName2colPlaceholder.put("_col1", lovDescriptionColumnName);

				AtomicInteger colCount = new AtomicInteger(0);

				lovVisibleColumnsNames.forEach(e -> {
					colPlaceholder2ColName.put("_col" + colCount.getAndIncrement(), e);
				});

				lovInvisibleColumnsNames.forEach(e -> {
					colPlaceholder2ColName.put("_col" + colCount.getAndIncrement(), e);
				});
			}

			boolean retrieveAdmissibleValue = false;
			retrieveAdmissibleValue = areAdmissibleValuesToBePreloaded(driver);

			// Load values by executing LOV
			if (retrieveAdmissibleValue) {
				List rows;
				try {
					rows = executeLOV(dum);
				} catch (MissingLOVDependencyException e) {
					logger.debug("Could not get LOV values because of a missing LOV dependency", e);
					setValuesCount(-1); // it means that we don't know the lov size
					return;
				}
				rows = applyPostProcessingDependencies(rows, dum);
				setValuesCount(rows == null ? 0 : rows.size());
				logger.debug("Loaded " + valuesCount + "values");

				// field
				admissibleValues = new ArrayList<HashMap<String, Object>>();

				// if the parameter is mandatory and there is only one admissible value set it to BiObjectParameter
				if (getValuesCount() == 1 && this.isMandatory()) {
					SourceBean lovSB = (SourceBean) rows.get(0);
					value = getValueFromLov(lovSB);
					description = getDescriptionFromLov(lovSB);
					driver.setParameterValues(new ArrayList<>(Arrays.asList(value)));
					driver.setParameterValuesDescription(new ArrayList<>(Arrays.asList(description)));
				}

				JSONObject valuesJSON = DocumentExecutionUtils.buildJSONForLOV(dum.getLovDetail(driver), rows, DocumentExecutionUtils.MODE_SIMPLE);
				JSONArray valuesJSONArray = valuesJSON.getJSONArray("root");

				for (int i = 0; i < valuesJSONArray.length(); i++) {
					// System.out.println(i);
					JSONObject item = valuesJSONArray.getJSONObject(i);

					if (item.length() > 0) {

						HashMap<String, Object> itemAsMap = fromJSONtoMap(item);

						ArrayList<HashMap<String, Object>> defaultErrorValues = new ArrayList<HashMap<String, Object>>();
						boolean defaultParameterAlreadyExist = false;
						// if it is a LOOKUP
						if (driver.getParameter() != null && driver.getParameter().getModalityValue() != null
								&& driver.getParameter().getModalityValue().getSelectionType() != null
								&& !driver.getParameter().getModalityValue().getSelectionType().equals("LOOKUP")) {

							for (HashMap<String, Object> defVal : admissibleValues) {
								if (item.has("value") && item.has("description")) {
									if (defVal.get("value") != null && defVal.get("value").equals(item.get("value")) && !item.isNull("label")) {
										if (defVal.get("label").equals(item.get("label")) && defVal.get("description") != null
												&& item.opt("description") != null && defVal.get("description").equals(item.get("description"))) {
											defaultParameterAlreadyExist = true;
											break;
										} else {
											HashMap<String, Object> itemErrorMap = new HashMap<String, Object>();
											itemErrorMap.put("error", true);
											itemErrorMap.put("value", defVal.get("value"));
											itemErrorMap.put("labelAlreadyExist", defVal.get("label"));
											itemErrorMap.put("labelSameValue", item.get("label"));
											defaultErrorValues.add(itemErrorMap);
											// return defaultErrorValues;
											admissibleValues = defaultErrorValues;
										}
									}
								} else {
									defaultParameterAlreadyExist = true;
									continue;
								}
							}
						}

						if (!defaultParameterAlreadyExist) {
							admissibleValues.add(itemAsMap);
						}
					}
				}

				// retrieve description for cross case
				if (isFromCross) {
					logger.debug("Parameter value for parameter " + driver.getParameterUrlName()
							+ " is retrieved from cross so it is necessary to retrieve description from admissible values");
					if (driver.getParameterValues() == null) {
						driver.setParameterValues(new ArrayList());
					}
					// add values description search for description
					List<String> descriptions = new ArrayList<String>();
					for (Iterator iterator = driver.getParameterValues().iterator(); iterator.hasNext();) {
						Object parameterValue = iterator.next();
						String value = parameterValue != null && parameterValue instanceof String ? parameterValue.toString() : null;
						if (value != null) {
							boolean found = false;
							for (Iterator iterator2 = admissibleValues.iterator(); iterator2.hasNext() && !found;) {
								Map map = (Map) iterator2.next();
								String valueD = map.get("value") != null && map.get("value") instanceof String ? map.get("value").toString() : null;
								if (valueD != null && valueD.equals(value)) {
									String description = map.get("description") != null && map.get("description") instanceof String
											? map.get("description").toString()
											: null;
									if (description != null) {
										logger.debug("Description found for cross navigation parameter: " + description);
										descriptions.add(description);
									} else {
										logger.debug("No description found for cross navigation parameter use value as default: " + value);
										descriptions.add(value);
									}
									found = true;
								}

							}
						}
					}
					driver.setParameterValuesDescription(descriptions);

					// if parameter is of type lookup empty admissible values
					if (isFromCross && "LOOKUP".equalsIgnoreCase(selectionType)) {
						admissibleValues = null;
					}

				} // end retrieve description from cross case

			} else {
				setValuesCount(-1); // it means that we don't know the lov size
			}

		} catch (Exception e) {
			logger.error("Errpr in retrieving admissible values");
			throw new SpagoBIRuntimeException(e);
		}
		logger.debug("OUT");

	}

	public boolean areAdmissibleValuesToBePreloaded(AbstractDriver driver) {
		boolean preloadAdmissibleValues = true;
		if ("LOOKUP".equalsIgnoreCase(selectionType) || "TREE".equalsIgnoreCase(selectionType) || selectionType.isEmpty()) {
			preloadAdmissibleValues = false;
		}
		return preloadAdmissibleValues;
	}

	private HashMap<String, Object> fromJSONtoMap(JSONObject item) throws JSONException {
		HashMap<String, Object> itemAsMap = new HashMap<String, Object>();

		for (int j = 0; j < lovVisibleColumnsNames.size(); j++) {
			String colKey = lovVisibleColumnsNames.get(j);
			String colKeyUp = colKey.toUpperCase();

			if (item.has(colKeyUp)) {
				Object value = item.get(colKeyUp);
				itemAsMap.put(colKeyUp, value);

				itemAsMap.put(colPlaceholder2ColName.inverse().get(colKey), value);
			}
		}

		for (int j = 0; j < lovInvisibleColumnsNames.size(); j++) {
			String colKey = lovInvisibleColumnsNames.get(j);
			String colKeyUp = colKey.toUpperCase();

			if (item.has(colKeyUp)) {
				Object value = item.get(colKeyUp);
				itemAsMap.put(colKeyUp, value);

				itemAsMap.put(colPlaceholder2ColName.inverse().get(colKey), value);
			}
		}

		itemAsMap.put("value", item.opt("value"));
		itemAsMap.put("label", item.has("label") ? item.get("label") : item.opt("value"));
		if (item.has("id")) {
			itemAsMap.put("id", item.get("id"));
		}
		if (item.has("leaf")) {
			itemAsMap.put("leaf", item.get("leaf"));
		}
		// System.out.println(item.get("value") + " - " + item.get("description"));
		if (item.opt("description") == null) {
			itemAsMap.put("description", item.opt("value"));
		} else {
			itemAsMap.put("description", item.get("description"));
		}
		itemAsMap.put("isEnabled", true);

		return itemAsMap;
	}

	private List applyPostProcessingDependencies(List rows, AbstractBIResourceRuntime dum) {
		Map selectedParameterValues = getSelectedParameterValuesAsMap();
		// IEngUserProfile profile = UserProfileManager.getProfile();
		// DocumentRuntime dum = new DocumentRuntime(profile, this.locale);
		List<AbstractParuse> biParameterExecDependencies = dum.getDependencies(driver, this.executionRole);
		ILovDetail lovProvDet = dum.getLovDetail(driver);
		if (lovProvDet instanceof DependenciesPostProcessingLov && selectedParameterValues != null && biParameterExecDependencies != null
				&& biParameterExecDependencies.size() > 0) {
			rows = ((DependenciesPostProcessingLov) lovProvDet).processDependencies(rows, selectedParameterValues, biParameterExecDependencies);
		}
		return rows;
	}

	private Map getSelectedParameterValuesAsMap() {
		Map toReturn = new HashMap();
		List<AbstractDriver> parameters = this.biResource.getDrivers();
		for (AbstractDriver parameter : parameters) {
			toReturn.put(parameter.getParameterUrlName(), parameter.getParameterValues());
		}
		return toReturn;
	}

	public void loadDefaultValues(AbstractDriver driver) {
		logger.debug("IN");
		try {
			DefaultValuesRetriever retriever = new DefaultValuesRetriever();
			IEngUserProfile profile = UserProfileManager.getProfile();
			defaultValues = retriever.getDefaultValuesDum(driver, this.biResource, profile, this.locale, this.executionRole);

			if (defaultValues.size() > 0 && (driver.getParameterValues() == null || driver.getParameterValues().isEmpty())) {
				// if parameter has no values set, but it has default values, those values are considered as values
				defaultValues = buildDefaultValueList();
				if (defaultValues != null) {
					driver.setParameterValues(defaultValues.getValuesAsList());
					driver.setParameterValuesDescription(defaultValues.getDescriptionsAsList());
				}
			}

		} catch (Exception e) {
			throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to get parameter's default values", e);
		}
		logger.debug("OUT");
	}

	public void loadMaxValue(AbstractDriver driver) {
		logger.debug("IN");
		try {
			MinMaxValuesRetriever retriever = new MinMaxValuesRetriever();
			IEngUserProfile profile = UserProfileManager.getProfile();
			maxValue = retriever.getMaxValueDum(driver, this.biResource, profile, this.locale, this.executionRole);

			if (driver.getMaxValue() == null) {
				maxValue = buildMaxValue();
				if (maxValue != null && maxValue.getValue() != null) {
					driver.setMaxValue(maxValue.getValue().toString());
				}
			}

		} catch (Exception e) {
			throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to get parameter's default values", e);
		}
		logger.debug("OUT");
	}

	private DefaultValuesList buildDefaultValueList() {
		SimpleDateFormat serverDateFormat = new SimpleDateFormat(SingletonConfig.getInstance().getConfigValue("SPAGOBI.DATE-FORMAT-SERVER.format"));

		if (parType != null && (parType.equals("DATE") || parType.equals("DATE_RANGE"))) {
			String valueDate = this.getDefaultValues().get(0).getValue().toString();
			String[] date = valueDate.split("#");
			if (date.length < 2) {
				throw new SpagoBIServiceException(SERVICE_NAME, "Illegal format for Value List Date Type [" + valueDate + "], unable to find symbol [#]");
			}
			SimpleDateFormat format = new SimpleDateFormat(date[1]);
			DefaultValuesList valueList = new DefaultValuesList();
			LovValue valueDef = new LovValue();

			if (parType.equals("DATE")) {
				try {
					Date d = format.parse(date[0]);
					String dateServerFormat = serverDateFormat.format(d);
					valueDef.setValue(dateServerFormat);
					valueDef.setDescription(this.getDefaultValues().get(0).getDescription());
					valueList.add(valueDef);
					return valueList;
				} catch (ParseException e) {
					logger.error("Error while building default Value List Date Type", e);
					return null;
				}
			} else {
				try {
					String dateRange = date[0];
					String[] dateRangeArr = dateRange.split("_");
					String range = dateRangeArr[dateRangeArr.length - 1];
					dateRange = dateRange.replace("_" + range, "");
					Date d = format.parse(dateRange);
					String dateServerFormat = serverDateFormat.format(d);
					valueDef.setValue(dateServerFormat + "_" + range);
					valueDef.setDescription(this.getDefaultValues().get(0).getDescription());
					valueList.add(valueDef);
					return valueList;
				} catch (ParseException e) {
					logger.error("Error while building default Value List Date Type", e);
					return null;
				}
			}
		} else {
			return this.getDefaultValues();
		}
	}

	/**
	 * Parse LOV value.
	 *
	 * @return
	 * @author Marco Libanori
	 */
	private LovValue buildMaxValue() {
		SimpleDateFormat serverDateFormat = new SimpleDateFormat(SingletonConfig.getInstance().getConfigValue("SPAGOBI.DATE-FORMAT-SERVER.format"));

		if (maxValue.getValue() != null) {
			if (parType != null && (parType.equals("DATE") || parType.equals("DATE_RANGE"))) {
				String valueDate = maxValue.getValue().toString();
				String[] date = valueDate.split("#");
				if (date.length < 2) {
					throw new SpagoBIServiceException(SERVICE_NAME, "Illegal format for Value List Date Type [" + valueDate + "], unable to find symbol [#]");
				}
				SimpleDateFormat format = new SimpleDateFormat(date[1]);
				LovValue ret = new LovValue();

				if (parType.equals("DATE")) {
					try {
						Date d = format.parse(date[0]);
						String dateServerFormat = serverDateFormat.format(d);
						ret.setValue(dateServerFormat);
						ret.setDescription(this.getMaxValue().getDescription());
						return ret;
					} catch (ParseException e) {
						logger.error("Error while building default Value List Date Type", e);
						return null;
					}
				} else {
					try {
						String dateRange = date[0];
						String[] dateRangeArr = dateRange.split("_");
						String range = dateRangeArr[dateRangeArr.length - 1];
						dateRange = dateRange.replace("_" + range, "");
						Date d = format.parse(dateRange);
						String dateServerFormat = serverDateFormat.format(d);
						ret.setValue(dateServerFormat + "_" + range);
						ret.setDescription(this.getMaxValue().getDescription());
						return ret;
					} catch (ParseException e) {
						logger.error("Error while building default Value List Date Type", e);
						return null;
					}
				}
			} else {
				return maxValue;
			}
		} else {
			return maxValue;
		}
	}

	private List executeLOV(AbstractBIResourceRuntime dum) {
		List rows = null;
		String lovResult = null;
		try {
			// get the result of the lov
			IEngUserProfile profile = UserProfileManager.getProfile();
			LovResultCacheManager executionCacheManager = new LovResultCacheManager();

			// DocumentRuntime dum = new DocumentRuntime(UserProfileManager.getProfile(), locale);

			lovResult = executionCacheManager.getLovResultDum(profile, dum.getLovDetail(driver), dum.getDependencies(driver, this.executionRole),
					this.biResource, true, this.locale);
			// get all the rows of the result
			LovResultHandler lovResultHandler = new LovResultHandler(lovResult);
			rows = lovResultHandler.getRows();
		} catch (MissingLOVDependencyException e) {
			throw e;
		} catch (Exception e) {
			throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to get parameter's values", e);
		}
		logger.debug("OUT");
		return rows;
	}

	private boolean hasParameterInsideLOV(AbstractBIResourceRuntime dum) {
		// DocumentRuntime dum = new DocumentRuntime(UserProfileManager.getProfile(), locale);
		ILovDetail lovDetail = dum.getLovDetail(driver);
		if (lovDetail != null) {
			Set<String> parameterNames = null;
			try {
				parameterNames = lovDetail.getParameterNames();
			} catch (Exception e) {
				throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to find in context execution lov parameters for execution of document", e);
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
		try {
			value = (String) lovSB.getAttribute(getLovProvider().getValueColumnName());
		} catch (Exception e) {
			throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to get parameter's value", e);
		}
		return value;
	}

	private String getDescriptionFromLov(SourceBean lovSB) {
		String description = null;
		try {
			description = (String) lovSB.getAttribute(getLovProvider().getDescriptionColumnName());
		} catch (Exception e) {
			throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to get parameter's description", e);
		}
		return description;
	}

	private ILovDetail getLovProvider() {
		ILovDetail lovProvDet = null;
		try {
			Parameter par = driver.getParameter();
			ModalitiesValue lov = par.getModalityValue();
			// build the ILovDetail object associated to the lov
			String lovProv = lov.getLovProvider();
			lovProvDet = LovDetailFactory.getLovFromXML(lovProv);
		} catch (Exception e) {
			throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to get lov provider", e);
		}
		return lovProvDet;
	}

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

	public ParameterUse getAnalyticalDriverExecModality() {
		return analyticalDriverExecModality;
	}

	public void setAnalyticalDriverExecModality(ParameterUse modality) {
		this.analyticalDriverExecModality = modality;
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Map<String, List<DriverDependencyRuntime>> getDependencies() {
		return dependencies;
	}

	public void setDependencies(Map<String, List<DriverDependencyRuntime>> dependencies) {
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

	/**
	 * @return
	 * @author Marco Libanori
	 */
	public LovValue getMaxValue() {
		return maxValue;
	}

	/**
	 * @param maxValue
	 * @author Marco Libanori
	 */
	public void setMaxValue(LovValue maxValue) {
		this.maxValue = maxValue;
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

	public AbstractDriver getDriver() {
		return driver;
	}

	public void setDriver(T driver) {
		this.driver = driver;
	}

	public String getLovValueColumnName() {
		return lovValueColumnName;
	}

	public void setLovValueColumnName(String lovValueColumnName) {
		this.lovValueColumnName = lovValueColumnName;
	}

	public String getLovDescriptionColumnName() {
		return lovDescriptionColumnName;
	}

	public void setLovDescriptionColumnName(String lovDescriptionColumnName) {
		this.lovDescriptionColumnName = lovDescriptionColumnName;
	}

	public List<String> getLovVisibleColumnsNames() {
		return lovVisibleColumnsNames;
	}

	public void setLovVisibleColumnsNames(List<String> lovColumnsNames) {
		this.lovVisibleColumnsNames = lovColumnsNames;
	}

	public ArrayList<HashMap<String, Object>> getAdmissibleValues() {
		return admissibleValues;
	}

	public void setAdmissibleValues(ArrayList<HashMap<String, Object>> admissibleValues) {
		this.admissibleValues = admissibleValues;
	}

	public List<String> getLovInvisibleColumnsNames() {
		return lovInvisibleColumnsNames;
	}

	public BiMap<String, String> getColPlaceholder2ColName() {
		return colPlaceholder2ColName;
	}

}