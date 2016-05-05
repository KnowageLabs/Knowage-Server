package it.eng.spagobi.analiticalmodel.document.handlers;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.navigation.LightNavigationManager;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spago.validation.EMFValidationError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.execution.bo.defaultvalues.DefaultValue;
import it.eng.spagobi.analiticalmodel.execution.bo.defaultvalues.DefaultValuesList;
import it.eng.spagobi.analiticalmodel.execution.bo.defaultvalues.DefaultValuesRetriever;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ObjParuse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ParameterUse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IObjParuseDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterUseDAO;
import it.eng.spagobi.behaviouralmodel.check.bo.Check;
import it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail;
import it.eng.spagobi.behaviouralmodel.lov.bo.LovDetailFactory;
import it.eng.spagobi.behaviouralmodel.lov.bo.LovResultHandler;
import it.eng.spagobi.behaviouralmodel.lov.bo.ModalitiesValue;
import it.eng.spagobi.behaviouralmodel.lov.bo.QueryDetail;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.ObjectsTreeConstants;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.messages.IMessageBuilder;
import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;
import it.eng.spagobi.commons.validation.SpagoBIValidationImpl;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.engines.drivers.IEngineDriver;
import it.eng.spagobi.engines.drivers.kpi.KpiDriver;
import it.eng.spagobi.monitoring.dao.AuditManager;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

public class DocumentUrlManager {

	static private Logger logger = Logger.getLogger(DocumentUrlManager.class);
	private static final String TREE_INNER_LOV_TYPE = "treeinner";

	private IEngUserProfile userProfile = null;
	private Locale locale = null;

	public DocumentUrlManager(IEngUserProfile userProfile, Locale locale) {
		this.userProfile = userProfile;
		this.locale = locale;
	}

	// Auditing
	private Integer createAuditId(BIObject obj, String executionModality, String role) {
		logger.debug("IN");
		try {
			AuditManager auditManager = AuditManager.getInstance();
			Integer executionAuditId = auditManager.insertAudit(obj, null, userProfile, role, executionModality);
			return executionAuditId;
		} finally {
			logger.debug("OUT");
		}
	}

	private void addSystemParametersForExternalEngines(Map mapPars, Locale locale, BIObject obj, String executionModality, String role) {
		mapPars.put(SpagoBIConstants.SBI_CONTEXT, GeneralUtilities.getSpagoBiContext());
		mapPars.put(SpagoBIConstants.SBI_HOST, GeneralUtilities.getSpagoBiHost());
		mapPars.put(SpagoBIConstants.SBI_SPAGO_CONTROLLER, GeneralUtilities.getSpagoAdapterHttpUrl());
		// mapPars.put("SBI_EXECUTION_ID", this.executionId);
		mapPars.put(SpagoBIConstants.EXECUTION_ROLE, role);
		Integer auditId = createAuditId(obj, executionModality, role);
		if (auditId != null) {
			mapPars.put(AuditManager.AUDIT_ID, auditId);
		}
		if (locale != null) {
			if (locale.getLanguage() != null) {
				mapPars.put(SpagoBIConstants.SBI_LANGUAGE, locale.getLanguage());
			}
			if (locale.getCountry() != null) {
				mapPars.put(SpagoBIConstants.SBI_COUNTRY, locale.getCountry());
			}
		}
	}

	public String getExecutionUrl(BIObject obj, String executionModality, String role) {
		logger.debug("IN");
		String url = null;
		Engine engine = obj.getEngine();
		Domain engineType;
		try {
			engineType = DAOFactory.getDomainDAO().loadDomainById(engine.getEngineTypeId());
		} catch (EMFUserError e) {
			throw new SpagoBIServiceException("Impossible to load engine type domain", e);
		}
		// IF THE ENGINE IS EXTERNAL
		if ("EXT".equalsIgnoreCase(engineType.getValueCd())) {
			// instance the driver class
			String driverClassName = engine.getDriverName();
			IEngineDriver aEngineDriver = null;
			try {
				aEngineDriver = (IEngineDriver) Class.forName(driverClassName).newInstance();
			} catch (Exception e) {
				throw new SpagoBIServiceException("Cannot istantiate engine driver class: " + driverClassName, e);
			}
			// get the map of the parameters
			Map mapPars = aEngineDriver.getParameterMap(obj, userProfile, role);
			// adding "system" parameters
			addSystemParametersForExternalEngines(mapPars, locale, obj, executionModality, role);
			url = GeneralUtilities.getUrl(engine.getUrl(), mapPars);

		}
		// IF THE ENGINE IS INTERNAL
		else {
			StringBuffer buffer = new StringBuffer();
			buffer.append(GeneralUtilities.getSpagoBIProfileBaseUrl(((UserProfile) userProfile).getUserId().toString()));
			buffer.append("&PAGE=ExecuteBIObjectPage");
			buffer.append("&" + SpagoBIConstants.TITLE_VISIBLE + "=FALSE");
			buffer.append("&" + SpagoBIConstants.TOOLBAR_VISIBLE + "=FALSE");
			buffer.append("&" + ObjectsTreeConstants.OBJECT_LABEL + "=" + obj.getLabel());
			buffer.append("&" + SpagoBIConstants.ROLE + "=" + role);
			buffer.append("&" + SpagoBIConstants.RUN_ANYWAY + "=TRUE");
			buffer.append("&" + SpagoBIConstants.IGNORE_SUBOBJECTS_VIEWPOINTS_SNAPSHOTS + "=TRUE");
			// buffer.append("&SBI_EXECUTION_ID=" + this.executionId); // adds constants if it works!!

			String kpiClassName = KpiDriver.class.getCanonicalName();
			if (engine.getClassName().equals(kpiClassName)) {
				Integer auditId = createAuditId(obj, executionModality, role);
				if (auditId != null) {
					buffer.append("&" + AuditManager.AUDIT_ID + "=" + auditId); // adds constants if it works!!
				}
			}

			// identity string for context
			UUIDGenerator uuidGen = UUIDGenerator.getInstance();
			UUID uuid = uuidGen.generateRandomBasedUUID();
			buffer.append("&" + LightNavigationManager.LIGHT_NAVIGATOR_ID + "=" + uuid.toString());
			List parameters = obj.getBiObjectParameters();
			if (parameters != null && parameters.size() > 0) {
				Iterator it = parameters.iterator();
				while (it.hasNext()) {
					BIObjectParameter aParameter = (BIObjectParameter) it.next();

					List list = aParameter.getParameterValues();
					if (list != null && !list.isEmpty()) {
						Iterator r = list.iterator();
						while (r.hasNext()) {
							String value = (String) r.next();
							if (value != null && !value.equals("")) {
								// encoding value
								try {
									value = URLEncoder.encode(value, "UTF-8");
								} catch (UnsupportedEncodingException e) {
									logger.warn("UTF-8 encoding is not supported!!!", e);
									logger.warn("Using system encoding...");
									value = URLEncoder.encode(value);
								}
								buffer.append("&" + aParameter.getParameterUrlName() + "=" + value);
							}
						}
					}
					/*
					 * ParameterValuesEncoder encoder = new ParameterValuesEncoder(); String encodedValue = encoder.encode(aParameter); if(encodedValue!=null &&
					 * !encodedValue.equals("")){ buffer.append("&" + aParameter.getParameterUrlName() + "=" + encodedValue); }
					 */
				}
			}
			url = buffer.toString();
		}
		logger.debug("OUT: returning url = [" + url + "]");
		return url;
	}

	/*
	 * ERRORS HANDLER
	 */
	public List getParametersErrors(BIObject object, String role) throws Exception {
		logger.debug("IN");
		List toReturn = new ArrayList();
		List biparams = object.getBiObjectParameters();
		if (biparams.size() == 0)
			return toReturn;
		Iterator iterParams = biparams.iterator();
		while (iterParams.hasNext()) {
			BIObjectParameter biparam = (BIObjectParameter) iterParams.next();
			// internalization of the label for display
			String viewLabel = biparam.getLabel();
			String oldViewLabel = viewLabel;
			IMessageBuilder msgBuilder = MessageBuilderFactory.getMessageBuilder();
			viewLabel = msgBuilder.getI18nMessage(locale, viewLabel);
			logger.debug(oldViewLabel + " is internazionalized in " + viewLabel);
			biparam.setLabel(viewLabel);
			logger.debug("Evaluating errors for biparameter " + biparam.getLabel() + " ...");
			List errorsOnChecks = getValidationErrorsOnChecks(biparam);
			List values = biparam.getParameterValues();
			if (biparam.isRequired() && (values == null || values.isEmpty() || normalizeList(values).size() == 0)) {
				EMFValidationError error = SpagoBIValidationImpl.validateField(biparam.getParameterUrlName(), biparam.getLabel(), null, "MANDATORY", null, null,
						null);
				errorsOnChecks.add(error);
			}
			if (errorsOnChecks != null && errorsOnChecks.size() > 0) {
				logger.warn("Found " + errorsOnChecks.size() + " errors on checks for biparameter " + biparam.getLabel());
			}
			toReturn.addAll(errorsOnChecks);
			if (values != null && values.size() >= 1 && !(values.size() == 1 && (values.get(0) == null || values.get(0).toString().trim().equals("")))) {
				List errorsOnValues = getValidationErrorsOnValues(biparam, object, role);
				if (errorsOnValues != null && errorsOnValues.size() > 0) {
					logger.warn("Found " + errorsOnValues.size() + " errors on values for biparameter " + biparam.getLabel());
				}
				toReturn.addAll(errorsOnValues);
			}
			boolean hasValidValues = false;
			// if parameter has values and there are no errors, the parameter has valid values
			if (values != null && values.size() > 0 && toReturn.isEmpty()) {
				hasValidValues = true;
			}
			biparam.setHasValidValues(hasValidValues);
		}
		logger.debug("OUT");
		return toReturn;
	}

	private List getValidationErrorsOnChecks(BIObjectParameter biparameter) throws Exception {
		logger.debug("IN");
		List toReturn = new ArrayList();
		List checks = biparameter.getParameter().getChecks();
		String label = biparameter.getLabel();
		if (checks == null || checks.size() == 0) {
			logger.debug("OUT. No checks associated for biparameter [" + label + "].");
			return toReturn;
		} else {
			Iterator it = checks.iterator();
			Check check = null;
			while (it.hasNext()) {
				check = (Check) it.next();
				if (check.getValueTypeCd().equalsIgnoreCase("MANDATORY"))
					continue;
				logger.debug("Applying check [" + check.getLabel() + "] to biparameter [" + label + "] ...");
				List errors = getValidationErrorOnCheck(biparameter, check);
				if (errors != null && errors.size() > 0) {
					Iterator errorsIt = errors.iterator();
					while (errorsIt.hasNext()) {
						EMFValidationError error = (EMFValidationError) errorsIt.next();
						logger.warn("Found an error applying check [" + check.getLabel() + "] for biparameter [" + label + "]: " + error.getDescription());
					}
					toReturn.addAll(errors);
				} else {
					logger.debug("No errors found applying check [" + check.getLabel() + "] to biparameter [" + label + "].");
				}
			}
			logger.debug("OUT");
			return toReturn;
		}
	}

	private List getValidationErrorOnCheck(BIObjectParameter biparameter, Check check) throws Exception {
		logger.debug("IN: Examining check with name " + check.getName() + " ...");
		List toReturn = new ArrayList();
		String urlName = biparameter.getParameterUrlName();
		String label = biparameter.getLabel();
		List values = biparameter.getParameterValues();
		if (check.getValueTypeCd().equalsIgnoreCase("MANDATORY")) {
			if (values == null || values.isEmpty()) {
				EMFValidationError error = SpagoBIValidationImpl.validateField(urlName, label, null, "MANDATORY", null, null, null);
				toReturn.add(error);
			} else {
				Iterator valuesIt = values.iterator();
				boolean hasAtLeastOneValue = false;
				while (valuesIt.hasNext()) {
					String aValue = (String) valuesIt.next();
					if (aValue != null && !aValue.trim().equals("")) {
						hasAtLeastOneValue = true;
						break;
					}
				}
				if (!hasAtLeastOneValue) {
					EMFValidationError error = SpagoBIValidationImpl.validateField(urlName, label, null, "MANDATORY", null, null, null);
					toReturn.add(error);
				}
			}
		} else {
			if (values != null && !values.isEmpty()) {
				Iterator valuesIt = values.iterator();
				while (valuesIt.hasNext()) {
					String aValue = (String) valuesIt.next();
					EMFValidationError error = null;
					if (check.getValueTypeCd().equalsIgnoreCase("LETTERSTRING")) {
						error = SpagoBIValidationImpl.validateField(urlName, label, aValue, "LETTERSTRING", null, null, null);
					} else if (check.getValueTypeCd().equalsIgnoreCase("ALFANUMERIC")) {
						error = SpagoBIValidationImpl.validateField(urlName, label, aValue, "ALFANUMERIC", null, null, null);
					} else if (check.getValueTypeCd().equalsIgnoreCase("NUMERIC")) {
						error = SpagoBIValidationImpl.validateField(urlName, label, aValue, "NUMERIC", null, null, null);
					} else if (check.getValueTypeCd().equalsIgnoreCase("EMAIL")) {
						error = SpagoBIValidationImpl.validateField(urlName, label, aValue, "EMAIL", null, null, null);
					} else if (check.getValueTypeCd().equalsIgnoreCase("FISCALCODE")) {
						error = SpagoBIValidationImpl.validateField(urlName, label, aValue, "FISCALCODE", null, null, null);
					} else if (check.getValueTypeCd().equalsIgnoreCase("INTERNET ADDRESS")) {
						error = SpagoBIValidationImpl.validateField(urlName, label, aValue, "URL", null, null, null);
					} else if (check.getValueTypeCd().equalsIgnoreCase("DECIMALS")) {
						error = SpagoBIValidationImpl.validateField(urlName, label, aValue, "DECIMALS", check.getFirstValue(), check.getSecondValue(), null);
					} else if (check.getValueTypeCd().equalsIgnoreCase("RANGE")) {
						if (biparameter.getParameter().getType().equalsIgnoreCase("DATE")) {
							// In a Parameter where parameterType == DATE the mask represent the date format
							error = SpagoBIValidationImpl.validateField(urlName, label, aValue, "DATERANGE", check.getFirstValue(), check.getSecondValue(),
									biparameter.getParameter().getMask());
						} else if (biparameter.getParameter().getType().equalsIgnoreCase("NUM")) {
							// In a Parameter where parameterType == NUM the mask represent the decimal format
							error = SpagoBIValidationImpl.validateField(urlName, label, aValue, "NUMERICRANGE", check.getFirstValue(), check.getSecondValue(),
									biparameter.getParameter().getMask());
						} else if (biparameter.getParameter().getType().equalsIgnoreCase("STRING")) {
							error = SpagoBIValidationImpl.validateField(urlName, label, aValue, "STRINGRANGE", check.getFirstValue(), check.getSecondValue(),
									null);
						}
					} else if (check.getValueTypeCd().equalsIgnoreCase("MAXLENGTH")) {
						error = SpagoBIValidationImpl.validateField(urlName, label, aValue, "MAXLENGTH", check.getFirstValue(), null, null);
					} else if (check.getValueTypeCd().equalsIgnoreCase("MINLENGTH")) {
						error = SpagoBIValidationImpl.validateField(urlName, label, aValue, "MINLENGTH", check.getFirstValue(), null, null);
					} else if (check.getValueTypeCd().equalsIgnoreCase("REGEXP")) {
						error = SpagoBIValidationImpl.validateField(urlName, label, aValue, "REGEXP", check.getFirstValue(), null, null);
					} else if (check.getValueTypeCd().equalsIgnoreCase("DATE")) {
						error = SpagoBIValidationImpl.validateField(urlName, label, aValue, "DATE", check.getFirstValue(), null, null);
					}
					if (error != null)
						toReturn.add(error);
				}
			}
		}
		logger.debug("OUT");
		return toReturn;
	}

	// Thanks to Emanuele Granieri of osmosit.com
	private List normalizeList(List l) {
		Iterator i = l.iterator();
		while (i.hasNext()) {
			Object el = i.next();
			if (el instanceof String) {
				String elString = ((String) el);
				if (elString == null || elString.length() == 0) {
					i.remove();
				}
			}
		}
		return l;
	}

	private List getValidationErrorsOnValues(BIObjectParameter biparam, BIObject object, String role) throws Exception {
		logger.debug("IN");
		String biparamLabel = biparam.getLabel();
		// outputType parameter is not validated
		String urlName = biparam.getParameterUrlName();
		if ("outputType".equals(urlName)) {
			logger.debug("Parameter is outputType parameter, it is not validated");
			return new ArrayList();
		}
		// manual inputs are not validated
		ModalitiesValue lov = biparam.getParameter().getModalityValue();
		if (lov.getITypeCd().equals("MAN_IN")) {
			logger.debug("Modality in use for biparameter [" + biparamLabel + "] is manual input");
			return new ArrayList();
		}
		// patch for default date value
		if (biparam.getParameter().getType().equalsIgnoreCase("DATE")) {
			logger.debug("Parameter [" + biparamLabel + "] has lov defined just for default value: any other chose allowed");
			return new ArrayList();
		}
		// we need to process default values and non-default values separately: default values do not require validation,
		// non-default values instead require validation
		DefaultValuesRetriever retriever = new DefaultValuesRetriever();
		DefaultValuesList allDefaultValues = retriever.getDefaultValuesDum(biparam, object, this.userProfile, this.locale, role);
		// from the complete list of values, get the values that are default values
		DefaultValuesList selectedDefaultValue = this.getSelectedDefaultValues(biparam, allDefaultValues);
		// validation must proceed only with non-default values
		// from the complete list of values, get the values that are not default values
		List nonDefaultValues = null;
		if (lov.getITypeCd().equalsIgnoreCase("QUERY")) {
			DefaultValuesList allDefaultQueryValues = retriever.getDefaultQueryValuesDum(biparam, this, this.userProfile, object, this.locale, role);
			nonDefaultValues = this.getNonDefaultQueryValues(biparam, allDefaultQueryValues);
		} else {
			nonDefaultValues = this.getNonDefaultValues(biparam, allDefaultValues);
		}
		if (nonDefaultValues.isEmpty()) {
			logger.debug("All selected values are default values; no need to validate them");
			return new ArrayList();
		}
		BIObjectParameter clone = biparam.clone();
		clone.setParameterValues(nonDefaultValues);
		// get the lov provider detail
		String lovProv = lov.getLovProvider();
		ILovDetail lovProvDet = LovDetailFactory.getLovFromXML(lovProv);
		// get lov result
		String lovResult = null;
		List toReturn = null;
		if (lovProvDet instanceof QueryDetail) {
			toReturn = getValidationErrorsOnValuesForQueries((QueryDetail) lovProvDet, clone, object, role);
		} else {
			LovResultCacheManager executionCacheManager = new LovResultCacheManager();
			lovResult = executionCacheManager.getLovResultDum(this.userProfile, lovProvDet, this.getDependencies(clone, role), object, true, this.locale);
			toReturn = getValidationErrorsOnValuesByLovResult(lovResult, clone, lovProvDet, role);
		}
		mergeDescriptions(biparam, selectedDefaultValue, clone);
		logger.debug("OUT");
		return toReturn;
	}

	private void mergeDescriptions(BIObjectParameter biparam, DefaultValuesList selectedDefaultValue, BIObjectParameter cloned) {
		int valuePosition;
		List nonDefaultValues = cloned.getParameterValues();
		List nonDefaultDescriptions = cloned.getParameterValuesDescription();
		List parameterValues = biparam.getParameterValues();
		List parameterDescriptions = new ArrayList<String>();
		if (parameterValues != null) {
			for (int i = 0; i < parameterValues.size(); i++) {
				Object aValue = parameterValues.get(i);
				valuePosition = nonDefaultValues.indexOf(aValue);
				if (valuePosition >= 0) {
					// this means that the value IS NOT a default value
					parameterDescriptions.add(nonDefaultDescriptions.get(valuePosition));
				} else {
					// this means that the value IS a default value
					DefaultValue defaultValue = selectedDefaultValue.getDefaultValue(aValue);
					parameterDescriptions.add((defaultValue != null) ? defaultValue.getDescription() : "");
				}
			}
		}
		biparam.setParameterValuesDescription(parameterDescriptions);
	}

	private List getNonDefaultValues(BIObjectParameter analyticalDocumentParameter, DefaultValuesList defaultValues) {
		logger.debug("IN");
		List toReturn = new ArrayList<String>();
		List values = analyticalDocumentParameter.getParameterValues();
		if (values != null && values.size() > 0) {
			for (int i = 0; i < values.size(); i++) {
				String value = values.get(i).toString();
				if (!defaultValues.contains(value)) {
					logger.debug("Value [" + value + "] is not a default value.");
					toReturn.add(value);
				}
			}
		}
		logger.debug("OUT");
		return toReturn;
	}

	private List<String> getNonDefaultQueryValues(BIObjectParameter analyticalDocumentParameter, DefaultValuesList defaultValues) {
		logger.debug("IN");
		List<String> toReturn = new ArrayList<String>();
		List<String> values = analyticalDocumentParameter.getParameterValues();
		if (values != null && values.size() > 0) {
			for (int i = 0; i < values.size(); i++) {
				// Removes the single quotes from each single parameter value
				String value = values.get(i).toString().replaceAll("^'(.*)'$", "$1");
				if (!defaultValues.contains(value)) {
					// if is multivalue the values come as a single string value
					if (analyticalDocumentParameter.isMultivalue()) {
						String[] singleLineValues = value.split("','");

						for (String singleValue : singleLineValues) {
							if (!defaultValues.contains(singleValue)) {
								logger.debug("Value [" + value + "] is not a default value.");
								toReturn.add(value);
								break;
							}
						}
					} else {
						logger.debug("Value [" + value + "] is not a default value.");
						toReturn.add(value);
					}
				}
			}
		}
		logger.debug("OUT");
		return toReturn;
	}

	private DefaultValuesList getSelectedDefaultValues(BIObjectParameter analyticalDocumentParameter, DefaultValuesList defaultValues) {
		logger.debug("IN");
		DefaultValuesList toReturn = new DefaultValuesList();
		if (defaultValues == null || defaultValues.isEmpty()) {
			logger.debug("No default values in input");
			return toReturn;
		}
		List values = analyticalDocumentParameter.getParameterValues();
		if (values != null && values.size() > 0) {
			for (int i = 0; i < values.size(); i++) {
				String value = values.get(i).toString();
				DefaultValue defaultValue = defaultValues.getDefaultValue(value);
				if (defaultValue != null) {
					logger.debug("Value [" + defaultValue + "] is a selected value.");
					toReturn.add(defaultValue);
				}
			}
		}
		logger.debug("OUT");
		return toReturn;
	}

	private List getValidationErrorsOnValuesForQueries(QueryDetail queryDetail, BIObjectParameter biparam, BIObject object, String role) throws Exception {
		List toReturn = null;
		LovResultCacheManager executionCacheManager = new LovResultCacheManager();
		// if query is not in cache, do not execute it as it is!!!
		String lovResult = executionCacheManager.getLovResultDum(this.userProfile, this.getLovDetail(biparam), this.getDependencies(biparam, role), object,
				false, this.locale);
		if (lovResult == null) {
			// lov is not in cache: we must validate values
			toReturn = queryDetail.validateValues(this.userProfile, biparam);
		} else {
			toReturn = getValidationErrorsOnValuesByLovResult(lovResult, biparam, queryDetail, role);
			if (toReturn.isEmpty()) {
				// values are ok, this should be most often the case
			} else {
				// if there are dependencies, we should not consider them since they are not mandatory
				List<ObjParuse> dependencies = this.getDependencies(biparam, role);
				if (!dependencies.isEmpty()) {
					toReturn = queryDetail.validateValues(this.userProfile, biparam);
				}
			}
		}
		return toReturn;
	}

	private List getValidationErrorsOnValuesByLovResult(String lovResult, BIObjectParameter biparam, ILovDetail lovProvDet, String role) throws Exception {
		logger.debug("IN");
		List toReturn = new ArrayList();
		boolean valueFound = false;
		List parameterValuesDescription = new ArrayList();
		// get lov result handler
		LovResultHandler lovResultHandler = new LovResultHandler(lovResult);
		List values = biparam.getParameterValues();
		if (values != null && values.size() > 0) {
			for (int i = 0; i < values.size(); i++) {
				// String value = values.get(i).toString();
				String value = null;
				String val = values.get(i).toString();
				if (val.equalsIgnoreCase("%")) {
					value = "%";
				} else {
					value = URLDecoder.decode(val, "UTF-8");
				}
				String description = null;
				if (value.equals("")) {
					valueFound = true;
				} else if (lovProvDet.getLovType().equals(TREE_INNER_LOV_TYPE)) {
					List<String> treeColumns = lovProvDet.getTreeLevelsColumns();
					if (treeColumns != null) {
						for (int j = 0; j < treeColumns.size(); j++) {
							valueFound = lovResultHandler.containsValueForTree(value, treeColumns.get(j));
							if (valueFound) {
								break;
							}
						}
					}
				} else if (lovResultHandler.containsValue(value, lovProvDet.getValueColumnName())) {
					valueFound = true;
				}
				if (valueFound) {
					description = lovResultHandler.getValueDescription(value, lovProvDet.getValueColumnName(), lovProvDet.getDescriptionColumnName());
				} else {
					logger.error("Parameter '" + biparam.getLabel() + "' cannot assume value '" + value + "'" + " for user '"
							+ ((UserProfile) this.userProfile).getUserId().toString() + "' with role '" + role + "'.");
					List l = new ArrayList();
					l.add(biparam.getLabel());
					l.add(value);
					EMFUserError userError = new EMFUserError(EMFErrorSeverity.ERROR, 1077, l);
					toReturn.add(userError);
					description = "NOT ADMISSIBLE";
				}
				parameterValuesDescription.add(description);
			}
		}
		biparam.setParameterValuesDescription(parameterValuesDescription);
		logger.debug("OUT");
		return toReturn;
	}

	public ILovDetail getLovDetailForDefault(BIObjectParameter parameter) {
		Parameter par = parameter.getParameter();
		ModalitiesValue lov = par.getModalityValueForDefault();
		if (lov == null) {
			logger.debug("No LOV for default values defined");
			return null;
		}
		logger.debug("A LOV for default values is defined : " + lov);
		// build the ILovDetail object associated to the lov
		String lovProv = lov.getLovProvider();
		ILovDetail lovProvDet = null;
		try {
			lovProvDet = LovDetailFactory.getLovFromXML(lovProv);
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Impossible to get LOV detail associated to the analytical driver for default values", e);
		}
		return lovProvDet;
	}

	public List<ObjParuse> getDependencies(BIObjectParameter parameter, String role) {

		List<ObjParuse> biParameterExecDependencies = new ArrayList<ObjParuse>();
		try {
			IParameterUseDAO parusedao = DAOFactory.getParameterUseDAO();
			ParameterUse biParameterExecModality = parusedao.loadByParameterIdandRole(parameter.getParID(), role);
			IObjParuseDAO objParuseDAO = DAOFactory.getObjParuseDAO();
			biParameterExecDependencies.addAll(objParuseDAO.loadObjParuse(parameter.getId(), biParameterExecModality.getUseID()));
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Impossible to get dependencies", e);
		}
		return biParameterExecDependencies;
	}

	public ILovDetail getLovDetail(BIObjectParameter parameter) {
		Parameter par = parameter.getParameter();
		ModalitiesValue lov = par.getModalityValue();
		// build the ILovDetail object associated to the lov
		String lovProv = lov.getLovProvider();
		ILovDetail lovProvDet = null;
		try {
			lovProvDet = LovDetailFactory.getLovFromXML(lovProv);
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Impossible to get lov detail associated to input BIObjectParameter", e);
		}
		return lovProvDet;
	}

	public void refreshParametersValues(JSONObject jsonObject, boolean transientMode, BIObject object) {
		logger.debug("IN");
		Assert.assertNotNull(jsonObject, "JSONObject in input is null!!");
		List biparams = object.getBiObjectParameters();
		Iterator iterParams = biparams.iterator();
		while (iterParams.hasNext()) {
			BIObjectParameter biparam = (BIObjectParameter) iterParams.next();
			refreshParameter(biparam, jsonObject, transientMode);
		}
		logger.debug("OUT");
	}

	public void refreshParameterForFilters(BIObjectParameter biparam, JSONObject parameter) {
		refreshParameter(biparam, parameter, false);
	}

	private void refreshParameter(BIObjectParameter biparam, JSONObject jsonObject, boolean transientMode) {
		logger.debug("IN");
		Assert.assertNotNull(biparam, "Parameter in input is null!!");
		Assert.assertNotNull(jsonObject, "JSONObject in input is null!!");
		String nameUrl = biparam.getParameterUrlName();
		List values = new ArrayList();
		try {
			Object o = jsonObject.opt(nameUrl);
			if (o != null) {
				if (o instanceof JSONArray) {
					JSONArray jsonArray = (JSONArray) o;
					for (int c = 0; c < jsonArray.length(); c++) {
						Object anObject = jsonArray.get(c);
						if (anObject != null) {
							values.add(anObject.toString());
						}
					}
				} else {
					// trim value at beginning and end of the string
					String valToInsert = o.toString();
					valToInsert = valToInsert.trim();
					if (!valToInsert.isEmpty()) {
						values.add(valToInsert);
					}
				}
			}
		} catch (JSONException e) {
			logger.error("Cannot get " + nameUrl + " values from JSON object", e);
			throw new SpagoBIServiceException("Cannot retrieve values for biparameter " + biparam.getLabel(), e);
		}

		if (values.size() > 0) {
			logger.debug("Updating values of biparameter " + biparam.getLabel() + " to " + values.toString());
			biparam.setParameterValues(values);
		} else {
			logger.debug("Erasing values of biparameter " + biparam.getLabel());
			biparam.setParameterValues(null);
		}

		biparam.setTransientParmeters(transientMode);
		logger.debug("OUT");
	}

}
