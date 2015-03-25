/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.validation;

import it.eng.spago.base.Constants;
import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.ResponseContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spago.dispatching.service.RequestContextIFace;
import it.eng.spago.error.EMFErrorHandler;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.message.MessageBundle;
import it.eng.spago.tracing.TracerSingleton;
import it.eng.spago.util.ContextScooping;
import it.eng.spago.validation.EMFValidationError;
import it.eng.spago.validation.ValidationEngineIFace;
import it.eng.spago.validation.impl.ValidatorLocator;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.validator.GenericValidator;
import org.apache.commons.validator.UrlValidator;
import org.apache.log4j.Logger;

/**
 * @author zoppello
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SpagoBIValidationImpl implements ValidationEngineIFace {
	
	static private Logger logger = Logger.getLogger(SpagoBIValidationImpl.class);

	private String _serviceName = null;
	private String _serviceType = null;
	private SourceBean _serviceValidations;
	private SourceBean _validationStructure;

	public static final String NAME_ATTRIBUTE = "NAME";
	public static final String TOUPPERCASE_ATTRIBUTE = "TOUPPERCASE";

	public static final String ERROR_GENERIC = "9000";
	public static final String ERROR_MANDATORY="9001";
	public static final String ERROR_LETTERSTRING="9002";
	public static final String ERROR_ALFANUMERIC="9003";
	public static final String ERROR_NUMERIC="9004";
	public static final String ERROR_EMAIL="9005";
	public static final String ERROR_FISCALCODE="9006";
	public static final String ERROR_DECIMALS="9007";
	public static final String ERROR_RANGE="9008";
	public static final String ERROR_MAXLENGTH="9009";
	public static final String ERROR_MINLENGTH="9010";
	public static final String ERROR_REGEXP="9011";
	public static final String ERROR_DATE="9012";
	public static final String ERROR_URL="9013";
	public static final String ERROR_BOOLEAN="9014";
	public static final String ERROR_XSS="9015";
	public static final String ERROR_EXTENDED_ALFANUMERIC="9016";
	
	// Costanti per le espressioni regolari 
	private static final String LETTER_STRING_REGEXP= "^([a-zA-ZéèëïâêôûîçàùìòÀÉÎÕÜ])*$";
	private static final String FISCAL_CODE_REGEXP="^([A-Z]{6}[A-Z\\d]{2}[A-Z][A-Z\\d]{2}[A-Z][A-Z\\d]{3}[A-Z])*$";
	private static final String ALPHANUMERIC_EXTENDED_STRING_REGEXP="^([\\w\\s\\-\\_\\(\\)\\[\\]\\;\\:\\!\\?\\{\\,\\}\\.\\'\\\"\\x2F\\x5F])*$";
	private static final String ALPHANUMERIC_EXTENDED_STRING_REGEXP_NOSPACE="^([\\w\\-\\_\\(\\)\\[\\]\\;\\:\\!\\?\\{\\,\\}\\.\\x2F\\x5F])*$";
	private static final String ALPHANUMERIC_STRING_REGEXP="^([a-zA-Z0-9\\s\\-\\_])*$";
	private static final String ALPHANUMERIC_STRING_REGEXP_NOSPACE="^([a-zA-Z0-9\\-\\_])*$";
	
	/**
	 * Thise methodis called everyTime a service with label <code>serviceName</code>
	 * is called, if the service validator is configured correctly into the <code>validator.xml</code>
	 * configuration file. It starts the validation procedure.
	 */
	public SpagoBIValidationImpl() {
		
	}
	
	/**
	 * From the request and response container this method controls if validation conditions
	 * declarated into the <code>validator.xml>/code> file are verifyed, and, if yes, acknowledge
	 * to start validation control.
	 * 
	 * @param serviceType the service type
	 * @param serviceName the service name
	 * @param context the context
	 * 
	 * @return a boolean value which says if conditions are verified.
	 * 
	 * @throws Exception if an exception occurs.
	 */
	public boolean validate(String serviceType, String serviceName, RequestContextIFace context)  {
		RequestContainer requestContainer = context.getRequestContainer();
		ResponseContainer responseContainer = context.getResponseContainer();
		_serviceName = serviceName;
		_serviceType = serviceType;
		ConfigSingleton config = ConfigSingleton.getInstance();
		
		Object objServices = config.getFilteredSourceBeanAttribute(
				ValidatorLocator.VALIDATION_CONFIG_HEADER,
				ValidatorLocator.VALIDATION_SERVICE_NAME, _serviceName);
		
		if (objServices instanceof SourceBean)
			_serviceValidations = (SourceBean) objServices;
		
		
		else if (objServices instanceof List) {
			for (int i = 0; i < ((List) objServices).size(); i++) {
				SourceBean tmp = (SourceBean) (((List) objServices).get(i));
				if (((String) tmp.getAttribute("TYPE"))
						.equalsIgnoreCase(_serviceType))
					_serviceValidations = tmp;
				break;
			}
		}
		
		if (_serviceValidations == null){
			TracerSingleton.log(Constants.NOME_MODULO,
				TracerSingleton.INFORMATION,
				"Validation::validate: validazione non richiesta, nessuna busta <SERVICE> associata a "
									+ _serviceName + " di tipo " + _serviceType + " trovata nel file di Configurazione");
			return true;
		}
		
//		if (_serviceValidations == null
//				|| !((String) _serviceValidations.getAttribute("TYPE"))
//						.equalsIgnoreCase(_serviceType)) {
//			TracerSingleton
//					.log(
//							Constants.NOME_MODULO,
//							TracerSingleton.INFORMATION,
//							"Validation::validate:validazione non richiesta, nessuna busta <SERVICE> associata a "
//									+ _serviceName + " di tipo " + _serviceType + " trovata nel file di Configurazione");
//			String nomeBustaInSession = "VALIDATE_"+_serviceType.toUpperCase()+"_" + _serviceName;
//			
//			TracerSingleton
//			.log(
//					Constants.NOME_MODULO,
//					TracerSingleton.INFORMATION,
//					"Validation::validate:provo a cercare la busta nel Session Container con id " + nomeBustaInSession+ " associata a "
//							+ _serviceName + " di tipo " + _serviceType);
//			
//			try {
//				SourceBean request = requestContainer.getServiceRequest();
//				SessionContainer session = requestContainer.getSessionContainer();
//				SessionContainerControl sessionAccessControl = new SessionContainerControl(request, session);
//				_serviceValidations = (SourceBean) sessionAccessControl.get(nomeBustaInSession);
//			} catch (Exception e) {
//				TracerSingleton
//				.log(
//						Constants.NOME_MODULO,
//						TracerSingleton.MAJOR,
//						"Validation::validate: Busta di validazione con id  " + nomeBustaInSession + " non trovata "
//								+ _serviceName + " di tipo " + _serviceType + ". Provo a cercare la busta nel SessionContainer.");
//				_serviceValidations = (SourceBean)requestContainer.getSessionContainer().getAttribute(nomeBustaInSession);
//			}
//		
//			if (_serviceValidations == null){
//				TracerSingleton
//				.log(
//						Constants.NOME_MODULO,
//						TracerSingleton.INFORMATION,
//						"Validation::validate: Busta di validazione con id  " + nomeBustaInSession + " non trovata "
//								+ _serviceName + " di tipo " + _serviceType);
//				return true;
//			}
//			
//		}
		
		// Nello stesso elemento service ci possono essere piï¿½ buste validation
		// dipendenti dalle condizioni
		// Viene eseguita una sola
		List validations = _serviceValidations
				.getAttributeAsList(ValidatorLocator.VALIDATION);
		if (validations.size() == 0) {
			TracerSingleton
					.log(
							Constants.NOME_MODULO,
							TracerSingleton.INFORMATION,
							"Validation::validate:validazione non richiesta, nessuna busta <VALIDATION> associata a "
									+ _serviceName);
			return true;
		}
		
		SourceBean validation = null;
		List conditions = null;
		for (int i = 0; i < validations.size(); i++) {
			validation = (SourceBean) validations.get(i);
			conditions = validation.getAttributeAsList("CONDITIONS.PARAMETER");
			if (conditions.size() == 0) {
				TracerSingleton.log(Constants.NOME_MODULO,
						TracerSingleton.DEBUG,
						"Validation::validate: nessuna condizione impostata");
				_validationStructure = validation;
				break;
			}
			boolean conditionVerified = true;
			for (int j = 0; j < conditions.size(); j++) {
				SourceBean condition = (SourceBean) conditions.get(j);
				String parameterName = (String) condition.getAttribute("NAME");
				String parameterScope = (String) condition
						.getAttribute("SCOPE");
				String parameterValue = (String) condition
						.getAttribute("VALUE");
				String inParameterValue = null;
				Object parameterValueObject = ContextScooping
						.getScopedParameter(requestContainer,
								responseContainer, parameterName,
								parameterScope);
				if (parameterValueObject != null)
					inParameterValue = parameterValueObject.toString();
				if (parameterValue.equalsIgnoreCase("AF_DEFINED")) {
					if (inParameterValue == null) {
						conditionVerified = false;
						break;
					} // if (inParameterValue == null)
					continue;
				} // if (parameterValue.equalsIgnoreCase("AF_DEFINED"))
				if (parameterValue.equalsIgnoreCase("AF_NOT_DEFINED")) {
					if (inParameterValue != null) {
						conditionVerified = false;
						break;
					} // if (inParameterValue != null)
					continue;
				} // if (parameterValue.equalsIgnoreCase("AF_NOT_DEFINED"))
				if (!(parameterValue.equalsIgnoreCase(inParameterValue))) {
					conditionVerified = false;
					break;
				} // if (!(parameterValue.equalsIgnoreCase(inParameterValue)))
			} // for (int j = 0; conditions.size(); j++)
			if (conditionVerified) {
				_validationStructure = validation;
				break;
			}
		}// for (int i =0; i < validations.size(); i++)
		if (_validationStructure == null) {
			TracerSingleton
					.log(Constants.NOME_MODULO, TracerSingleton.INFORMATION,
							"Validation::validate:validazione non eseguita, nessuna condizione soddisfatta");
			return true;
		}
		return validateService(requestContainer, responseContainer);
	}

	
	
	/**
	 * A method which says if a validator has blocking properties. If a validator is blocking
	 * and his validation is not passed, all execution will be interrupted.
	 * 
	 * @return A boolean value saying if validator is blocking or not.
	 */
	public boolean isBlocking() {
		String isBlocking = (String) _validationStructure
				.getAttribute("blocking");
		if (isBlocking == null || !isBlocking.equalsIgnoreCase("FALSE"))
			return true;
		return false;
	}

	/**
	 * When the validation is started, if a control has generated errors, this method interrupts 
	 * validation without going on with other controls.
	 * 
	 * @param requestContainer The input request container
	 * @param responseContainer The input response container
	 * @return A boolean value saying if we have errors or not
	 * @throws Exception If any exception occurred
	 */
	private boolean validateService(RequestContainer requestContainer,
		ResponseContainer responseContainer)  {

		SessionContainer sessionContainer = requestContainer
				.getSessionContainer();	
		SourceBean serviceRequest = requestContainer.getServiceRequest();
		ValidatorLocator locator = ValidatorLocator.getInstance();
		EMFErrorHandler errorHandler = responseContainer.getErrorHandler();
		TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.INFORMATION,
				"Validation::validateService:automaticValidation");
		// Controlli formali automatici reperiti dal file validation.xml.xml
		automaticValidation(serviceRequest, errorHandler);
		if (!errorHandler.getErrors().isEmpty()) {
			// Se i controlli formali hanno prodotto errori, non proseguo con
			// gli altri controlli
			return false;
		}
		return true;
	}

		/**
		 * For each input field type (Numeric, URL, extc:), this method applies validation.
		 * Every time a validation fails, an error is added to the <code>errorHandler</code>
		 * errors stack.
		 * 
		 * @param serviceRequest The request Source Bean
		 * @param errorHandler The errors Stack 
		 * @throws Exception If any exception occurs.
		 */
	private void automaticValidation(SourceBean serviceRequest,
			EMFErrorHandler errorHandler)  {
			//if we are validation bi object page, we don't go on with validation
			// Reperisco l'elenco di tutti gli attributi che mi aspetto di trovare
		// nella richiesta
		List fields = _validationStructure.getAttributeAsList("FIELDS.FIELD");

		for (Iterator iter = fields.iterator(); iter.hasNext();) {
			
			Object valueObj = null;
			String value = null;
			
			List validators = null;
			SourceBean currentValidator = null;
			String validatorName = null;
			Iterator itValidators = null;
			try {
				SourceBean field = (SourceBean) iter.next();

				String fieldName = (String) field.getAttribute("name");
				
				String fieldLabel = (String) field.getAttribute("label");
				
				String multivaluesStr = (String) field.getAttribute("multivalues");
				boolean multivalues = (multivaluesStr != null && multivaluesStr.equalsIgnoreCase("true"))? true: false;
				String separator = null;
				if(multivalues) separator = (String) field.getAttribute("separator");
				
				// ********************************************
				if (fieldLabel != null && fieldLabel.startsWith("#")) {
					String key = fieldLabel.substring(1);
					String bundle = (String) field.getAttribute("bundle");
					String fieldDescription = "";
					if((bundle!=null) && !bundle.trim().equals("")){
						fieldDescription = MessageBundle.getMessage(key, bundle);
					} else {
						fieldDescription = MessageBundle.getMessage(key);
					}
					if (fieldDescription != null && !fieldDescription.trim().equals("")) fieldLabel = fieldDescription;
				}
				//********************************************
				
				if((fieldLabel==null) || (fieldLabel.trim().equals(""))) 
						fieldLabel = fieldName;
				
				valueObj = serviceRequest.getAttribute(fieldName);
				if (valueObj != null) value = valueObj.toString();
				String[] values = null;
				if(multivalues) values = value.split(separator);
				else values = new String[]{value};
				
				validators = field.getAttributeAsList("VALIDATOR");
				
				for(int i = 0; i < values.length; i++ ) {
					
					value = values[i];
					
					itValidators = validators.iterator();
					
					while (itValidators.hasNext()){
						currentValidator = (SourceBean)itValidators.next();
						validatorName = (String) currentValidator.getAttribute("validatorName");
						String arg0 = (String) currentValidator.getAttribute("arg0");
						String arg1 = (String) currentValidator.getAttribute("arg1");
						String arg2 = (String) currentValidator.getAttribute("arg2");
						EMFValidationError error = validateField(fieldName, fieldLabel, value, validatorName, arg0, arg1, arg2);
						errorHandler.addError(error);
					}//while (itValidators.hasNext())
				} // while 
				
			} catch (Exception ex) {
				TracerSingleton.log(Constants.NOME_MODULO,
						TracerSingleton.INFORMATION,
						"ValidationModule::automaticValidation", ex);

			}

		}
	}

	public static EMFValidationError validateField(String fieldName, String fieldLabel, String value, 
			String validatorName, String arg0, String arg1, String arg2) throws Exception {
		
		List params = null;
		
		if (validatorName.equalsIgnoreCase("MANDATORY")){
			logger.debug( "Apply the MANDATORY VALIDATOR to field ["+fieldName+"] with value ["+value+"]");
			if (GenericValidator.isBlankOrNull(value)){
				params = new ArrayList();
				params.add(fieldLabel);
				return new EMFValidationError(EMFErrorSeverity.ERROR, fieldName, ERROR_MANDATORY, params);	
				
			}

		} else if (validatorName.equalsIgnoreCase("URL")){
			logger.debug("Apply the URL VALIDATOR to field ["+fieldName+"] with value ["+value+"]");
			UrlValidator urlValidator = new SpagoURLValidator();
			if (!GenericValidator.isBlankOrNull(value) && !urlValidator.isValid(value)){
				params = new ArrayList();
				params.add(fieldLabel);
				return new EMFValidationError(EMFErrorSeverity.ERROR, fieldName, ERROR_URL,params);
				
			}
		} else if (validatorName.equalsIgnoreCase("LETTERSTRING")){
			logger.debug("Apply the LETTERSTRING VALIDATOR to field ["+fieldName+"] with value ["+value+"]");
			if (!GenericValidator.isBlankOrNull(value) && !GenericValidator.matchRegexp(value, LETTER_STRING_REGEXP)){
				params = new ArrayList();
				params.add(fieldLabel);
				return new EMFValidationError(EMFErrorSeverity.ERROR, fieldName, ERROR_LETTERSTRING,params);
				
			}
		} else if (validatorName.equalsIgnoreCase("ALFANUMERIC")){
			logger.debug( "Apply the ALFANUMERIC VALIDATOR to field ["+fieldName+"] with value ["+value+"]");
			if (!GenericValidator.isBlankOrNull(value) && !GenericValidator.matchRegexp(value, ALPHANUMERIC_STRING_REGEXP)){
			
				params = new ArrayList();
				params.add(fieldLabel);
				return new EMFValidationError(EMFErrorSeverity.ERROR, fieldName, ERROR_ALFANUMERIC,params);
				
			}
		} else if (validatorName.equalsIgnoreCase("EXTENDED_ALFANUMERIC")){
			logger.debug( "Apply the ALFANUMERIC VALIDATOR to field ["+fieldName+"] with value ["+value+"]");
			if (!GenericValidator.isBlankOrNull(value) && !GenericValidator.matchRegexp(value, ALPHANUMERIC_EXTENDED_STRING_REGEXP)){
			
				params = new ArrayList();
				params.add(fieldLabel);
				return new EMFValidationError(EMFErrorSeverity.ERROR, fieldName, ERROR_EXTENDED_ALFANUMERIC,params);
				
			}
		} else if (validatorName.equalsIgnoreCase("NUMERIC")){
			logger.debug( "Apply the NUMERIC VALIDATOR to field ["+fieldName+"] with value ["+value+"]");
			if (!GenericValidator.isBlankOrNull(value) && 
	            (!(GenericValidator.isInt(value) 
				|| GenericValidator.isFloat(value) 
				|| GenericValidator.isDouble(value)
				|| GenericValidator.isShort(value)
				|| GenericValidator.isLong(value)))){
				
				// The string is not a integer, not a float, not double, not short, not long
				// so is not a number
				
				params = new ArrayList();
				params.add(fieldLabel);
				return new EMFValidationError(EMFErrorSeverity.ERROR, fieldName, ERROR_NUMERIC,params);
				
			}
			
		} else if (validatorName.equalsIgnoreCase("EMAIL")){
			logger.debug( "Apply the EMAIL VALIDATOR to field ["+fieldName+"] with value ["+value+"]");
			if (!GenericValidator.isBlankOrNull(value) && !GenericValidator.isEmail(value)){
				
				// Generate errors
				params = new ArrayList();
				params.add(fieldLabel);
				return new EMFValidationError(EMFErrorSeverity.ERROR, fieldName, ERROR_EMAIL,params);
				
			}
		} else if (validatorName.equalsIgnoreCase("BOOLEAN")){
			logger.debug( "Apply the MANDATORY VALIDATOR to field ["+fieldName+"] with value ["+value+"]");
			if (!GenericValidator.isBlankOrNull(value) && !value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")){
				params = new ArrayList();
				params.add(fieldLabel);
				return new EMFValidationError(EMFErrorSeverity.ERROR, fieldName, ERROR_BOOLEAN, params);	
				
			}

		} 
		else if (validatorName.equalsIgnoreCase("FISCALCODE")){
			logger.debug( "Apply the FISCALCODE VALIDATOR to field ["+fieldName+"] with value ["+value+"]");
			if (!GenericValidator.isBlankOrNull(value) && !GenericValidator.matchRegexp(value, FISCAL_CODE_REGEXP)){
				
//				 Generate errors
				params = new ArrayList();
				params.add(fieldLabel);
				return new EMFValidationError(EMFErrorSeverity.ERROR, fieldName, ERROR_FISCALCODE,params);
				
			}
		} else if (validatorName.equalsIgnoreCase("DECIMALS")){
			if (!GenericValidator.isBlankOrNull(value)) {
				logger.debug("Apply the DECIMALS VALIDATOR to field ["
								+ fieldName + "] with value [" + value
								+ "]");
				int maxNumberOfDecimalDigit = Integer.valueOf(arg0).intValue();
				logger.debug("Max Numbers of decimals is ["
								+ maxNumberOfDecimalDigit + "]");
				String decimalSeparator = arg1;

				if (GenericValidator
						.isBlankOrNull(decimalSeparator)) {
					decimalSeparator = ".";
				}

				int pos = value.indexOf(decimalSeparator);
				String decimalCharacters = "";
				if (pos != -1)
					decimalCharacters = value.substring(pos + 1);

				if (decimalCharacters.length() > maxNumberOfDecimalDigit) {
					// Generate errors
					params = new ArrayList();
					params.add(fieldLabel);
					params.add(String
							.valueOf(maxNumberOfDecimalDigit));
					return new EMFValidationError(
							EMFErrorSeverity.ERROR, fieldName, ERROR_DECIMALS, params);
				}
			}
		} else if (validatorName.equalsIgnoreCase("NUMERICRANGE")){
			if (!GenericValidator.isBlankOrNull(value)) {
				logger.debug("Apply the NUMERICRANGE VALIDATOR to field ["+fieldName+"] with value ["+value+"]");
				String firstValueStr = arg0;
				String secondValueStr = arg1;
				logger.debug( "Range is ["+firstValueStr+"< x <"+secondValueStr+"]");
				boolean syntaxCorrect = true;
				if (!GenericValidator.isDouble(value)){
					logger.debug(" CANNOT APPLY THE NUMERICRANGE VALIDATOR  value ["+value+"] is not a Number");
					syntaxCorrect = false;
				}
				if (!GenericValidator.isDouble(firstValueStr)){
					logger.debug(" CANNOT APPLY THE NUMERICRANGE VALIDATOR  first value of range ["+firstValueStr+"] is not a Number");
					syntaxCorrect = false;
				}
				if (!GenericValidator.isDouble(secondValueStr)){
					logger.debug( " CANNOT APPLY THE NUMERICRANGE VALIDATOR  second value of range ["+secondValueStr+"] is not a Number");
					syntaxCorrect = false;
				}
				if (syntaxCorrect){
					double firstValue = Double.valueOf(firstValueStr).doubleValue();
					double secondValue = Double.valueOf(secondValueStr).doubleValue();
					double valueToCheckDouble = Double.valueOf(value).doubleValue();
					if (!(GenericValidator.isInRange(valueToCheckDouble, firstValue, secondValue))){
				
						params = new ArrayList();
						params.add(fieldLabel);
						params.add(firstValueStr);
						params.add(secondValueStr);
						return new EMFValidationError(EMFErrorSeverity.ERROR, fieldName, ERROR_RANGE,params);
				
					}
				}else{
					return new EMFValidationError(EMFErrorSeverity.ERROR, fieldName, ERROR_GENERIC);
				}
			}
		} else if (validatorName.equalsIgnoreCase("DATERANGE")){
			
			if (!GenericValidator.isBlankOrNull(value)) {
			logger.debug("Apply the DATERANGE VALIDATOR to field ["+fieldName+"] with value ["+value+"]");
			String firstValueStr = arg0;
			String secondValueStr = arg1;
			String dateFormat = arg2;
			logger.debug( "Range is ["+firstValueStr+"< x <"+secondValueStr+"]");
			logger.debug( "Date Format is  ["+dateFormat+"]");
//			//boolean syntaxCorrect = false;
			boolean syntaxCorrect = true;
			
			//if (!GenericValidator.isDate(value,dateFormat,true)){
			if (!GenericValidator.isDate(value,dateFormat,true)){
				logger.debug( " CANNOT APPLY THE DATERANGE VALIDATOR  value ["+value+"] is not a is not valid Date according to ["+dateFormat+"]");
				syntaxCorrect = false;
			}
			//if (!GenericValidator.isDate(firstValueStr,dateFormat,true)){
			if (!GenericValidator.isDate(firstValueStr,dateFormat,true)){
				logger.debug( " CANNOT APPLY THE DATERANGE VALIDATOR  first value of range ["+firstValueStr+"] is not valid Date according to ["+dateFormat+"]");
				syntaxCorrect = false;
			}
			//if (!GenericValidator.isDate(secondValueStr,dateFormat, true)){
			if (!GenericValidator.isDate(secondValueStr,dateFormat,true)){
				logger.debug( " CANNOT APPLY THE DATERANGE VALIDATOR  second value of range ["+secondValueStr+"] is not a valid Date according to ["+dateFormat+"]");
				syntaxCorrect = false;
			}
			
			
			if (syntaxCorrect){
				DateFormat df = new SimpleDateFormat(dateFormat);
			
				Date firstValueDate = df.parse(firstValueStr);
				Date secondValueDate = df.parse(secondValueStr);
				Date theValueDate = df.parse(value);
			
				if ((theValueDate.getTime() < firstValueDate.getTime()) || (theValueDate.getTime() > secondValueDate.getTime())){
					params = new ArrayList();
					params.add(fieldLabel);
					params.add(firstValueStr);
					params.add(secondValueStr);
					return new EMFValidationError(EMFErrorSeverity.ERROR, fieldName, ERROR_RANGE,params);
				}
			}else {
				return new EMFValidationError(EMFErrorSeverity.ERROR, fieldName, ERROR_GENERIC);
			}
			}
		} else if (validatorName.equalsIgnoreCase("STRINGRANGE")){
			if (!GenericValidator.isBlankOrNull(value)) {
			logger.debug("Apply the STRINGRANGE VALIDATOR to field ["+fieldName+"] with value ["+value+"]");
			
			String firstValueStr = arg0;
			String secondValueStr = arg1;
			logger.debug( "Range is ["+firstValueStr+"< x <"+secondValueStr+"]");
			//if (firstValueStr.compareTo(secondValueStr) > 0){
			if ((value.compareTo(firstValueStr) < 0) || (value.compareTo(secondValueStr) > 0)){
				params = new ArrayList();
				params.add(fieldLabel);
				params.add(firstValueStr);
				params.add(secondValueStr);
				return new EMFValidationError(EMFErrorSeverity.ERROR, fieldName, ERROR_RANGE,params);
			}
			}
		} else if (validatorName.equalsIgnoreCase("MAXLENGTH")){
			if (!GenericValidator.isBlankOrNull(value)) {
			logger.debug( "Apply the MAXLENGTH VALIDATOR to field ["+fieldName+"] with value ["+value+"]");
			int maxLength = Integer.valueOf(arg0).intValue();
			logger.debug( "maxLength is ["+maxLength+"]");
			if (!GenericValidator.maxLength(value, maxLength)){
				
				params = new ArrayList();
				params.add(fieldLabel);
				params.add(String.valueOf(maxLength));
				
				return new EMFValidationError(EMFErrorSeverity.ERROR, fieldName, ERROR_MAXLENGTH,params);
			
			}
			}
		} else if (validatorName.equalsIgnoreCase("MINLENGTH")){
			if (!GenericValidator.isBlankOrNull(value)) {
			logger.debug( "Apply the MINLENGTH VALIDATOR to field ["+fieldName+"] with value ["+value+"]");
			int minLength = Integer.valueOf(arg0).intValue();
			logger.debug( "minLength is ["+minLength+"]");
			if (!GenericValidator.minLength(value, minLength)){
				
				// Generate Errors
				params = new ArrayList();
				params.add(fieldLabel);
				params.add(String.valueOf(minLength));
				
				return new EMFValidationError(EMFErrorSeverity.ERROR, fieldName, ERROR_MINLENGTH,params);
			
			}
			}
		} else if (validatorName.equalsIgnoreCase("REGEXP")){
			if (!GenericValidator.isBlankOrNull(value)) {
			logger.debug("Apply the REGEXP VALIDATOR to field ["+fieldName+"] with value ["+value+"]");
			String regexp  = arg0;
			logger.debug( "regexp is ["+regexp+"]");
			if (!(GenericValidator.matchRegexp(value, regexp))){
				
				// Generate Errors
				params = new ArrayList();
				params.add(fieldLabel);
				params.add(regexp);
				
				return new EMFValidationError(EMFErrorSeverity.ERROR, fieldName, ERROR_REGEXP,params);
			
			}
			}
		}  else if (validatorName.equalsIgnoreCase("XSS")){
			if (!GenericValidator.isBlankOrNull(value)) {
				logger.debug("Apply the XSS VALIDATOR to field ["+fieldName+"] with value ["+value+"]");
				String toVerify = value.toUpperCase();
				if( toVerify.contains("<A") ||  toVerify.contains("<LINK") ||  toVerify.contains("<IMG") ||  toVerify.contains("<SCRIPT") ||
						toVerify.contains("&LT;A") ||  toVerify.contains("&LT;LINK") ||  toVerify.contains("&LT;IMG") ||  toVerify.contains("&LT;SCRIPT")	){

					// Generate Errors
					params = new ArrayList();
					params.add(fieldLabel);
					
					return new EMFValidationError(EMFErrorSeverity.ERROR, fieldName, ERROR_XSS,params);
				
				}
				}
		}else if (validatorName.equalsIgnoreCase("DATE")){
			
			if (!GenericValidator.isBlankOrNull(value)) {
			logger.debug( "Apply the DATE VALIDATOR to field ["+fieldName+"] with value ["+value+"]");
			String dateFormat = arg0;
			logger.debug("dateFormat is ["+dateFormat+"]");
			//if (!GenericValidator.isDate(value, dateFormat, true)){
			if (!GenericValidator.isDate(value, dateFormat, true)){
	
				//Generate Errors
				params = new ArrayList();
				params.add(fieldLabel);
				params.add(dateFormat);
				
				return new EMFValidationError(EMFErrorSeverity.ERROR, fieldName, ERROR_DATE,params);
			}
			}
			
		}
		
		// all checks had positive result (no errors)
		return null;
	}
	
}



	
