package it.eng.spagobi.analiticalmodel.document.handlers;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spago.validation.EMFValidationError;
import it.eng.spagobi.analiticalmodel.execution.bo.LovValue;
import it.eng.spagobi.analiticalmodel.execution.bo.defaultvalues.DefaultValuesList;
import it.eng.spagobi.analiticalmodel.execution.bo.defaultvalues.DefaultValuesRetriever;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.AbstractDriver;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIMetaModelParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ObjParuse;
import it.eng.spagobi.behaviouralmodel.check.bo.Check;
import it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail;
import it.eng.spagobi.behaviouralmodel.lov.bo.LovDetailFactory;
import it.eng.spagobi.behaviouralmodel.lov.bo.LovResultHandler;
import it.eng.spagobi.behaviouralmodel.lov.bo.ModalitiesValue;
import it.eng.spagobi.behaviouralmodel.lov.bo.QueryDetail;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.validation.SpagoBIValidationImpl;
import it.eng.spagobi.tools.catalogue.metadata.IDrivableBIResource;
import it.eng.spagobi.utilities.objects.Couple;

public class DriversValidationAPI {

	static private Logger logger = Logger.getLogger(DriversValidationAPI.class);
	private static final String TREE_INNER_LOV_TYPE = "treeinner";

	private IEngUserProfile userProfile = null;
	private Locale locale = null;

	/**
	 * @param profile
	 * @param locale
	 */
	public DriversValidationAPI(UserProfile profile, Locale locale) {
		this.userProfile = profile;
		this.locale = locale;
	}

	/**
	 *
	 */
	public DriversValidationAPI() {

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

	/*
	 * ERRORS HANDLER
	 */
	public List getParametersErrors(IDrivableBIResource object, String role, AbstractBIResourceRuntime dum) throws Exception {
		logger.debug("IN");
		List toReturn = new ArrayList();
		List drivers = object.getDrivers();
		if (drivers.isEmpty()) {
			return toReturn;
		}
		Iterator iterParams = drivers.iterator();
		while (toReturn.isEmpty() && iterParams.hasNext()) {
			AbstractDriver driver = (AbstractDriver) iterParams.next();
			// internalization of the label for display
			// String viewLabel = biparam.getLabel();
			// String oldViewLabel = viewLabel;
			// IMessageBuilder msgBuilder = MessageBuilderFactory.getMessageBuilder();
			// viewLabel = msgBuilder.getI18nMessage(locale, viewLabel);
			// logger.debug(oldViewLabel + " is internazionalized in " + viewLabel);
			// biparam.setLabel(viewLabel);
			logger.debug("Evaluating errors for driver " + driver.getLabel() + " ...");
			List errorsOnChecks = getValidationErrorsOnChecks(driver);
			List values = driver.getParameterValues();
			if (driver.isRequired() && (values == null || values.isEmpty() || normalizeList(values).size() == 0)) {
				EMFValidationError error = SpagoBIValidationImpl.validateField(driver.getParameterUrlName(), driver.getLabel(), null, "MANDATORY", null, null,
						null);
				errorsOnChecks.add(error);
			}
			if (errorsOnChecks != null && !errorsOnChecks.isEmpty()) {
				logger.warn("Found " + errorsOnChecks.size() + " errors on checks for driver " + driver.getLabel());
			}
			toReturn.addAll(errorsOnChecks);
			if (values != null && values.size() >= 1 && !(values.size() == 1 && (values.get(0) == null || values.get(0).toString().trim().equals("")))) {
				List errorsOnValues = getValidationErrorsOnValues(driver, object, role, dum);
				if (errorsOnValues != null && !errorsOnValues.isEmpty()) {
					logger.warn("Found " + errorsOnValues.size() + " errors on values for driver " + driver.getLabel());
				}
				toReturn.addAll(errorsOnValues);
			}
			boolean hasValidValues = false;
			// if parameter has values and there are no errors, the parameter has valid values
			if (values != null && values.size() > 0 && toReturn.isEmpty()) {
				hasValidValues = true;
			}
			driver.setHasValidValues(hasValidValues);
		}
		logger.debug("OUT");
		return toReturn;
	}

	private List getValidationErrorsOnChecks(AbstractDriver driver) throws Exception {
		logger.debug("IN");
		List toReturn = new ArrayList();
		List checks = driver.getParameter().getChecks();
		String label = driver.getLabel();
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
				List errors = getValidationErrorOnCheck(driver, check);
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

	private List getValidationErrorOnCheck(AbstractDriver driver, Check check) throws Exception {
		logger.debug("IN: Examining check with name " + check.getName() + " ...");
		List toReturn = new ArrayList();
		String urlName = driver.getParameterUrlName();
		String label = driver.getLabel();
		List values = driver.getParameterValues();
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
						if (driver.getParameter().getType().equalsIgnoreCase("DATE")) {
							// In a Parameter where parameterType == DATE the mask represent the date format
							error = SpagoBIValidationImpl.validateField(urlName, label, aValue, "DATERANGE", check.getFirstValue(), check.getSecondValue(),
									driver.getParameter().getMask());
						} else if (driver.getParameter().getType().equalsIgnoreCase("NUM")) {
							// In a Parameter where parameterType == NUM the mask represent the decimal format
							error = SpagoBIValidationImpl.validateField(urlName, label, aValue, "NUMERICRANGE", check.getFirstValue(), check.getSecondValue(),
									driver.getParameter().getMask());
						} else if (driver.getParameter().getType().equalsIgnoreCase("STRING")) {
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

	private List getValidationErrorsOnValues(AbstractDriver driver, IDrivableBIResource object, String role, AbstractBIResourceRuntime dum) throws Exception {
		logger.debug("IN");
		String biparamLabel = driver.getLabel();
		// outputType parameter is not validated
		String urlName = driver.getParameterUrlName();
		if ("outputType".equals(urlName)) {
			logger.debug("Parameter is outputType parameter, it is not validated");
			return new ArrayList();
		}
		// manual inputs are not validated
		ModalitiesValue lov = driver.getParameter().getModalityValue();
		if (lov.getITypeCd().equals("MAN_IN")) {
			logger.debug("Modality in use for biparameter [" + biparamLabel + "] is manual input");
			return new ArrayList();
		}
		// patch for default date value
		if (driver.getParameter().getType().equalsIgnoreCase("DATE")) {
			logger.debug("Parameter [" + biparamLabel + "] has lov defined just for default value: any other chose allowed");
			return new ArrayList();
		}
		// we need to process default values and non-default values separately: default values do not require validation,
		// non-default values instead require validation
		DefaultValuesRetriever retriever = new DefaultValuesRetriever();
		DefaultValuesList allDefaultValues = retriever.getDefaultValuesDum(driver, object, this.userProfile, this.locale, role);
		// from the complete list of values, get the values that are default values
		DefaultValuesList selectedDefaultValue = this.getSelectedDefaultValues(driver, allDefaultValues);
		// validation must proceed only with non-default values
		// from the complete list of values, get the values that are not default values
		List nonDefaultValues = null;
		if (lov.getITypeCd().equalsIgnoreCase("QUERY")) {
			DefaultValuesList allDefaultQueryValues = retriever.getDefaultQueryValuesDum(driver, dum, this.userProfile, object, this.locale, role);
			nonDefaultValues = this.getNonDefaultQueryValues(driver, allDefaultQueryValues);
		} else {
			nonDefaultValues = this.getNonDefaultValues(driver, allDefaultValues);
		}
		if (nonDefaultValues.isEmpty()) {
			logger.debug("All selected values are default values; no need to validate them");
			return new ArrayList();
		}
		AbstractDriver clone;
		if (driver instanceof BIObjectParameter) {
			BIObjectParameter biParam = (BIObjectParameter) driver;
			biParam.clone();
			clone = biParam;
		} else {
			BIMetaModelParameter biMetaParam = (BIMetaModelParameter) driver;
			biMetaParam.clone();
			clone = biMetaParam;
		}
		// BIObjectParameter clone = driver.clone();
		clone.setParameterValues(nonDefaultValues);
		// get the lov provider detail
		String lovProv = lov.getLovProvider();
		ILovDetail lovProvDet = LovDetailFactory.getLovFromXML(lovProv);
		// get lov result
		String lovResult = null;
		List toReturn = null;
		if (lovProvDet instanceof QueryDetail) {
			toReturn = getValidationErrorsOnValuesForQueries((QueryDetail) lovProvDet, clone, object, role, dum);
		} else {
			LovResultCacheManager executionCacheManager = new LovResultCacheManager();
			lovResult = executionCacheManager.getLovResultDum(this.userProfile, lovProvDet, dum.getDependencies(clone, role), object, true, this.locale);
			toReturn = getValidationErrorsOnValuesByLovResult(lovResult, clone, lovProvDet, role);
		}
		mergeDescriptions(driver, selectedDefaultValue, clone);
		logger.debug("OUT");
		return toReturn;
	}

	private List getValidationErrorsOnValuesForQueries(QueryDetail queryDetail, AbstractDriver driver, IDrivableBIResource object, String role,
			AbstractBIResourceRuntime dum) throws Exception {
		List toReturn = null;
		LovResultCacheManager executionCacheManager = new LovResultCacheManager();
		// if query is not in cache, do not execute it as it is!!!
		String lovResult = executionCacheManager.getLovResultDum(this.userProfile, dum.getLovDetail(driver), dum.getDependencies(driver, role), object, false,
				this.locale);
		if (lovResult == null) {
			// lov is not in cache: we must validate values
			toReturn = queryDetail.validateValues(this.userProfile, driver);
		} else {
			toReturn = getValidationErrorsOnValuesByLovResult(lovResult, driver, queryDetail, role);
			if (toReturn.isEmpty()) {
				// values are ok, this should be most often the case
			} else {
				// if there are dependencies, we should not consider them since they are not mandatory
				List<ObjParuse> dependencies = dum.getDependencies(driver, role);
				if (!dependencies.isEmpty()) {
					toReturn = queryDetail.validateValues(this.userProfile, driver);
				}
			}
		}
		return toReturn;
	}

	private List getValidationErrorsOnValuesByLovResult(String lovResult, AbstractDriver driver, ILovDetail lovProvDet, String role) throws Exception {
		logger.debug("IN");
		List toReturn = new ArrayList();
		boolean valueFound = false;
		List parameterValuesDescription = new ArrayList();
		// get lov result handler
		LovResultHandler lovResultHandler = new LovResultHandler(lovResult);
		List values = driver.getParameterValues();
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
					// TODO: REVIEW for modification to hierarchy tree !!!!
				} else if (lovProvDet.getLovType().equals(TREE_INNER_LOV_TYPE)) {
					// List<String> treeColumns = lovProvDet.getTreeLevelsColumns();
					List<Couple<String, String>> treeColumns = lovProvDet.getTreeLevelsColumns();
					if (treeColumns != null) {
						for (int j = 0; j < treeColumns.size(); j++) {
							valueFound = lovResultHandler.containsValueForTree(value, treeColumns.get(j).getFirst());
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
					logger.error("Parameter '" + driver.getLabel() + "' cannot assume value '" + value + "'" + " for user '"
							+ ((UserProfile) this.userProfile).getUserId().toString() + "' with role '" + role + "'.");
					List l = new ArrayList();
					l.add(driver.getLabel());
					l.add(value);
					EMFUserError userError = new EMFUserError(EMFErrorSeverity.ERROR, 1077, l);
					toReturn.add(userError);
					description = "NOT ADMISSIBLE";
				}
				parameterValuesDescription.add(description);
			}
		}
		driver.setParameterValuesDescription(parameterValuesDescription);
		logger.debug("OUT");
		return toReturn;
	}

	private DefaultValuesList getSelectedDefaultValues(AbstractDriver driver, DefaultValuesList defaultValues) {
		logger.debug("IN");
		DefaultValuesList toReturn = new DefaultValuesList();
		if (defaultValues == null || defaultValues.isEmpty()) {
			logger.debug("No default values in input");
			return toReturn;
		}
		List values = driver.getParameterValues();
		if (values != null && values.size() > 0) {
			for (int i = 0; i < values.size(); i++) {
				String value = values.get(i).toString();
				LovValue defaultValue = defaultValues.getDefaultValue(value);
				if (defaultValue != null) {
					logger.debug("Value [" + defaultValue + "] is a selected value.");
					toReturn.add(defaultValue);
				}
			}
		}
		logger.debug("OUT");
		return toReturn;
	}

	private List getNonDefaultValues(AbstractDriver driver, DefaultValuesList defaultValues) {
		logger.debug("IN");
		List toReturn = new ArrayList<String>();
		List values = driver.getParameterValues();
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

	private List<String> getNonDefaultQueryValues(AbstractDriver driver, DefaultValuesList defaultValues) {
		logger.debug("IN");
		List<String> toReturn = new ArrayList<String>();
		List<String> values = driver.getParameterValues();
		if (values != null && values.size() > 0) {
			for (int i = 0; i < values.size(); i++) {
				// Removes the single quotes from each single parameter value
				String value = values.get(i).toString().replaceAll("^'(.*)'$", "$1");
				if (!defaultValues.contains(value)) {
					// if is multivalue the values come as a single string value
					if (driver.isMultivalue()) {
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

	private void mergeDescriptions(AbstractDriver driver, DefaultValuesList selectedDefaultValue, AbstractDriver cloned) {
		int valuePosition;
		List nonDefaultValues = cloned.getParameterValues();
		List nonDefaultDescriptions = cloned.getParameterValuesDescription();
		List parameterValues = driver.getParameterValues();
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
					LovValue defaultValue = selectedDefaultValue.getDefaultValue(aValue);
					parameterDescriptions.add((defaultValue != null) ? defaultValue.getDescription() : "");
				}
			}
		}
		driver.setParameterValuesDescription(parameterDescriptions);
	}

}
