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
package it.eng.spagobi.analiticalmodel.execution.bo.defaultvalues;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import it.eng.spago.base.SourceBean;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.handlers.ExecutionInstance;
import it.eng.spagobi.analiticalmodel.document.handlers.LovResultCacheManager;
import it.eng.spagobi.analiticalmodel.execution.bo.LovValue;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail;
import it.eng.spagobi.behaviouralmodel.lov.bo.LovResultHandler;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class DefaultFormulas {

	private static final Logger LOGGER = Logger.getLogger(DefaultFormulas.class);

	private static Map<String, IDefaultFormula> formulas;

	public static final String NONE = "NONE";
	public static final String FIRST = "FIRST";
	public static final String LAST = "LAST";

	public static final IDefaultFormula NONE_FUNCTION = new IDefaultFormula() {

		@Override
		public String getName() {
			return NONE;
		}

		@Override
		public DefaultValuesList getDefaultValues(BIObjectParameter analyticalDocumentParameter,
				ExecutionInstance executionInstance, IEngUserProfile profile) {
			return new DefaultValuesList();
		}

	};

	public static final IDefaultFormula FIRST_FUNCTION = new IDefaultFormula() {

		@Override
		public String getName() {
			return FIRST;
		}

		@Override
		public DefaultValuesList getDefaultValues(BIObjectParameter analyticalDocumentParameter,
				ExecutionInstance executionInstance, IEngUserProfile profile) {
			LOGGER.debug("Formula " + this.getName() + ": IN");
			LovValue defaultValue = null;
			try {
				ILovDetail lovDetails = executionInstance.getLovDetail(analyticalDocumentParameter);
				LOGGER.debug("LOV info retrieved");
				// get from cache, if available
				LovResultCacheManager executionCacheManager = new LovResultCacheManager();
				String lovResultStr = executionCacheManager.getLovResult(profile, lovDetails,
						executionInstance.getDependencies(analyticalDocumentParameter), executionInstance, true);
				LOGGER.debug("LOV executed");
				// get all the rows of the result
				LovResultHandler lovResultHandler = new LovResultHandler(lovResultStr);
				List lovResult = lovResultHandler.getRows();
				LOGGER.debug("LOV result parsed");
				if (lovResult == null || lovResult.isEmpty()) {
					throw new SpagoBIRuntimeException("LOV result is empty!!!!");
				}
				// getting first value
				SourceBean row = (SourceBean) lovResult.get(0);
				defaultValue = new LovValue();
				defaultValue.setValue(row.getAttribute(lovDetails.getValueColumnName()));
				defaultValue.setDescription(row.getAttribute(lovDetails.getDescriptionColumnName()));
				LOGGER.debug("Default value found is " + defaultValue);
			} catch (Exception e) {
				throw new SpagoBIRuntimeException("Cannot get default value using formula " + this.getName(), e);
			}
			DefaultValuesList defaultValues = new DefaultValuesList();
			defaultValues.add(defaultValue);
			LOGGER.debug("Formula " + this.getName() + ": OUT");
			return defaultValues;
		}
	};

	public static final IDefaultFormula LAST_FUNCTION = new IDefaultFormula() {

		@Override
		public String getName() {
			return LAST;
		}

		@Override
		public DefaultValuesList getDefaultValues(BIObjectParameter analyticalDocumentParameter,
				ExecutionInstance executionInstance, IEngUserProfile profile) {
			LOGGER.debug("Formula " + this.getName() + ": IN");
			LovValue defaultValue = null;
			try {
				ILovDetail lovDetails = executionInstance.getLovDetail(analyticalDocumentParameter);
				LOGGER.debug("LOV info retrieved");
				// get from cache, if available
				LovResultCacheManager executionCacheManager = new LovResultCacheManager();
				String lovResultStr = executionCacheManager.getLovResult(profile, lovDetails,
						executionInstance.getDependencies(analyticalDocumentParameter), executionInstance, true);
				LOGGER.debug("LOV executed");
				// get all the rows of the result
				LovResultHandler lovResultHandler = new LovResultHandler(lovResultStr);
				List lovResult = lovResultHandler.getRows();
				LOGGER.debug("LOV result parsed");
				if (lovResult == null || lovResult.isEmpty()) {
					throw new SpagoBIRuntimeException("LOV result is empty!!!!");
				}
				// getting last value
				SourceBean row = (SourceBean) lovResult.get(lovResult.size() - 1);
				defaultValue = new LovValue();
				defaultValue.setValue(row.getAttribute(lovDetails.getValueColumnName()));
				defaultValue.setDescription(row.getAttribute(lovDetails.getDescriptionColumnName()));
				LOGGER.debug("Default value found is " + defaultValue);
			} catch (Exception e) {
				throw new SpagoBIRuntimeException("Cannot get default value using formula " + this.getName(), e);
			}
			DefaultValuesList defaultValues = new DefaultValuesList();
			defaultValues.add(defaultValue);
			LOGGER.debug("Formula " + this.getName() + ": OUT");
			return defaultValues;
		}
	};

	static {
		formulas = new HashMap<>();
		formulas.put(NONE, NONE_FUNCTION);
		formulas.put(FIRST, FIRST_FUNCTION);
		formulas.put(LAST, LAST_FUNCTION);
	}

	public static IDefaultFormula get(String functionName) {
		IDefaultFormula toReturn = null;
		if (functionName != null && formulas.containsKey(functionName.toUpperCase())) {
			toReturn = formulas.get(functionName.toUpperCase());
			LOGGER.debug("Recognized formula is [" + toReturn.getName() + "]");
		} else {
			LOGGER.debug("Formula [" + functionName + "] not recognized");
			toReturn = NONE_FUNCTION;
		}
		return toReturn;
	}

	private DefaultFormulas() {

	}

}
