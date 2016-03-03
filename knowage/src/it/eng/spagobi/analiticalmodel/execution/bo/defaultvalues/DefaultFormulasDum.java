package it.eng.spagobi.analiticalmodel.execution.bo.defaultvalues;

import it.eng.spago.base.SourceBean;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.handlers.DocumentUrlManager;
import it.eng.spagobi.analiticalmodel.document.handlers.LovResultCacheManager;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail;
import it.eng.spagobi.behaviouralmodel.lov.bo.LovResultHandler;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;

public class DefaultFormulasDum {

	private static Logger logger = Logger.getLogger(DefaultFormulas.class);

	private static Map<String, IDefaultFormulaDum> formulas;

	public static String NONE = "NONE";
	public static String FIRST = "FIRST";
	public static String LAST = "LAST";

	public static IDefaultFormulaDum NONE_FUNCTION = new IDefaultFormulaDum() {

		@Override
		public String getName() {
			return NONE;
		}

		@Override
		public DefaultValuesList getDefaultValues(BIObjectParameter analyticalDocumentParameter, DocumentUrlManager dum, IEngUserProfile profile,
				BIObject object, Locale locale, String role) {
			return new DefaultValuesList();
		}

	};

	public static IDefaultFormulaDum FIRST_FUNCTION = new IDefaultFormulaDum() {

		@Override
		public String getName() {
			return FIRST;
		}

		@Override
		public DefaultValuesList getDefaultValues(BIObjectParameter analyticalDocumentParameter, DocumentUrlManager dum, IEngUserProfile profile,
				BIObject object, Locale locale, String role) {
			logger.debug("Formula " + this.getName() + ": IN");
			DefaultValue defaultValue = null;
			try {
				ILovDetail lovDetails = dum.getLovDetail(analyticalDocumentParameter);
				logger.debug("LOV info retrieved");
				// get from cache, if available
				LovResultCacheManager executionCacheManager = new LovResultCacheManager();
				String lovResultStr = executionCacheManager.getLovResultDum(profile, lovDetails, dum.getDependencies(analyticalDocumentParameter, role),
						object, true, locale);
				logger.debug("LOV executed");
				// get all the rows of the result
				LovResultHandler lovResultHandler = new LovResultHandler(lovResultStr);
				List lovResult = lovResultHandler.getRows();
				logger.debug("LOV result parsed");
				if (lovResult == null || lovResult.isEmpty()) {
					throw new SpagoBIRuntimeException("LOV result is empty!!!!");
				}
				// getting first value
				SourceBean row = (SourceBean) lovResult.get(0);
				defaultValue = new DefaultValue();
				defaultValue.setValue(row.getAttribute(lovDetails.getValueColumnName()));
				defaultValue.setDescription(row.getAttribute(lovDetails.getDescriptionColumnName()));
				logger.debug("Default value found is " + defaultValue);
			} catch (Exception e) {
				throw new SpagoBIRuntimeException("Cannot get default value using formula " + this.getName(), e);
			}
			DefaultValuesList defaultValues = new DefaultValuesList();
			defaultValues.add(defaultValue);
			logger.debug("Formula " + this.getName() + ": OUT");
			return defaultValues;
		}
	};

	public static IDefaultFormulaDum LAST_FUNCTION = new IDefaultFormulaDum() {

		@Override
		public String getName() {
			return LAST;
		}

		@Override
		public DefaultValuesList getDefaultValues(BIObjectParameter analyticalDocumentParameter, DocumentUrlManager dum, IEngUserProfile profile,
				BIObject object, Locale locale, String role) {
			logger.debug("Formula " + this.getName() + ": IN");
			DefaultValue defaultValue = null;
			try {
				ILovDetail lovDetails = dum.getLovDetail(analyticalDocumentParameter);
				logger.debug("LOV info retrieved");
				// get from cache, if available
				LovResultCacheManager executionCacheManager = new LovResultCacheManager();
				String lovResultStr = executionCacheManager.getLovResultDum(profile, lovDetails, dum.getDependencies(analyticalDocumentParameter, role),
						object, true, locale);
				logger.debug("LOV executed");
				// get all the rows of the result
				LovResultHandler lovResultHandler = new LovResultHandler(lovResultStr);
				List lovResult = lovResultHandler.getRows();
				logger.debug("LOV result parsed");
				if (lovResult == null || lovResult.isEmpty()) {
					throw new SpagoBIRuntimeException("LOV result is empty!!!!");
				}
				// getting last value
				SourceBean row = (SourceBean) lovResult.get(lovResult.size() - 1);
				defaultValue = new DefaultValue();
				defaultValue.setValue(row.getAttribute(lovDetails.getValueColumnName()));
				defaultValue.setDescription(row.getAttribute(lovDetails.getDescriptionColumnName()));
				logger.debug("Default value found is " + defaultValue);
			} catch (Exception e) {
				throw new SpagoBIRuntimeException("Cannot get default value using formula " + this.getName(), e);
			}
			DefaultValuesList defaultValues = new DefaultValuesList();
			defaultValues.add(defaultValue);
			logger.debug("Formula " + this.getName() + ": OUT");
			return defaultValues;
		}
	};

	static {
		formulas = new HashMap<String, IDefaultFormulaDum>();
		formulas.put(NONE, NONE_FUNCTION);
		formulas.put(FIRST, FIRST_FUNCTION);
		formulas.put(LAST, LAST_FUNCTION);
	}

	public static IDefaultFormulaDum get(String functionName) {
		IDefaultFormulaDum toReturn = null;
		if (functionName != null && formulas.containsKey(functionName.toUpperCase())) {
			toReturn = formulas.get(functionName.toUpperCase());
			logger.debug("Recognized formula is [" + toReturn.getName() + "]");
		} else {
			logger.debug("Formula [" + functionName + "] not recognized");
			toReturn = NONE_FUNCTION;
		}
		return toReturn;
	}

}
