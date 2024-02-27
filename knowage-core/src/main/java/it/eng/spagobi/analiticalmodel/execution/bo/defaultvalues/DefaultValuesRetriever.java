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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.handlers.AbstractBIResourceRuntime;
import it.eng.spagobi.analiticalmodel.document.handlers.BusinessModelRuntime;
import it.eng.spagobi.analiticalmodel.document.handlers.DocumentRuntime;
import it.eng.spagobi.analiticalmodel.document.handlers.ExecutionInstance;
import it.eng.spagobi.analiticalmodel.document.handlers.LovResultCacheManager;
import it.eng.spagobi.analiticalmodel.execution.bo.LovValue;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.AbstractDriver;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail;
import it.eng.spagobi.behaviouralmodel.lov.bo.LovResultHandler;
import it.eng.spagobi.tools.catalogue.bo.MetaModel;
import it.eng.spagobi.tools.catalogue.metadata.IDrivableBIResource;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class DefaultValuesRetriever {

	private static final Logger LOGGER = Logger.getLogger(DefaultValuesRetriever.class);

	public DefaultValuesList getDefaultValues(BIObjectParameter analyticalDocumentParameter,
			ExecutionInstance executionInstance, IEngUserProfile profile) {
		LOGGER.debug("IN");
		DefaultValuesList defaultValues = null;
		try {
			ILovDetail lovForDefault = executionInstance.getLovDetailForDefault(analyticalDocumentParameter);
			if (lovForDefault != null) {
				LOGGER.debug("A LOV for default values is defined : " + lovForDefault);
				defaultValues = getDefaultValuesFromDefaultLov(executionInstance, profile, lovForDefault);
			} else {
				LOGGER.debug("No LOV for default values defined");
				String formulaForDefault = analyticalDocumentParameter.getParameter().getDefaultFormula();
				if (formulaForDefault != null) {
					IDefaultFormula defaultFormula = DefaultFormulas.get(formulaForDefault);
					defaultValues = defaultFormula.getDefaultValues(analyticalDocumentParameter, executionInstance,
							profile);
				}
			}
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Impossible to get parameter's default values", e);
		}
		if (defaultValues == null) {
			defaultValues = new DefaultValuesList();
		}
		LOGGER.debug("OUT");
		return defaultValues;
	}

	protected DefaultValuesList getDefaultValuesFromDefaultLov(ExecutionInstance executionInstance,
			IEngUserProfile profile, ILovDetail lovForDefault) throws Exception {
		LOGGER.debug("IN");
		DefaultValuesList defaultValues = new DefaultValuesList();

		// get from cache, if available
		LovResultCacheManager executionCacheManager = new LovResultCacheManager();
		String lovResult = executionCacheManager.getLovResult(profile, lovForDefault, new ArrayList<>(),
				executionInstance, true);
		LovResultHandler lovResultHandler = new LovResultHandler(lovResult);
		List rows = lovResultHandler.getRows();
		LOGGER.debug("LOV result retrieved without errors");
		LOGGER.debug("LOV contains " + rows.size() + " values");
		Iterator it = rows.iterator();
		String valueColumn = lovForDefault.getValueColumnName();
		String descriptionColumn = lovForDefault.getDescriptionColumnName();
		while (it.hasNext()) {
			SourceBean row = (SourceBean) it.next();
			LovValue defaultValue = new LovValue();
			defaultValue.setValue(row.getAttribute(valueColumn));
			defaultValue.setDescription(row.getAttribute(descriptionColumn));
			defaultValues.add(defaultValue);
		}
		LOGGER.debug("OUT");
		return defaultValues;
	}

	public DefaultValuesList getDefaultQueryValues(BIObjectParameter biparam, ExecutionInstance executionInstance,
			IEngUserProfile userProfile) {

		LovResultCacheManager executionCacheManager = new LovResultCacheManager();
		ILovDetail lovProvDet = executionInstance.getLovDetail(biparam);
		String columnName = null;
		String lovResult = null;
		try {
			lovResult = executionCacheManager.getLovResult(userProfile, lovProvDet,
					executionInstance.getDependencies(biparam), executionInstance, true);

			columnName = lovProvDet.getValueColumnName();
		} catch (Exception e) {
			logger.error("getLovResult",e);
		}

		// get all the rows of the result
		LovResultHandler lovResultHandler = null;
		try {
			lovResultHandler = new LovResultHandler(lovResult);
		} catch (SourceBeanException e) {
			e.printStackTrace();
		}

		DefaultValuesList defaultValuesList = new DefaultValuesList();
		List<SourceBean> rows = lovResultHandler.getRows();

		for (SourceBean row : rows) {
			String rowValue = (String) row.getAttribute(columnName);

			LovValue defaultValue = new LovValue();
			defaultValue.setValue(rowValue);

			defaultValuesList.add(defaultValue);
		}

		return defaultValuesList;
	}

	/*
	 * GET DEFAULT VALUE FROM DOCUMENT_URL_MANAGER
	 */

	public DefaultValuesList getDefaultValuesDum(AbstractDriver driver, IDrivableBIResource object,
			IEngUserProfile profile, Locale locale, String role) {
		LOGGER.debug("IN");
		AbstractBIResourceRuntime dum = null;
		if (object instanceof BIObject) {
			dum = new DocumentRuntime(profile, locale);
		} else if (object instanceof MetaModel) {
			dum = new BusinessModelRuntime(profile, locale);
		}
		DefaultValuesList defaultValues = null;
		try {
			ILovDetail lovForDefault = dum.getLovDetailForDefault(driver);
			if (lovForDefault != null) {
				LOGGER.debug("A LOV for default values is defined : " + lovForDefault);
				defaultValues = getDefaultValuesFromDefaultLovDum(object, profile, lovForDefault, locale);
			} else {
				LOGGER.debug("No LOV for default values defined");
				String formulaForDefault = driver.getParameter().getDefaultFormula();
				if (formulaForDefault != null) {
					IDefaultFormulaDum defaultFormulaDum = DefaultFormulasDum.get(formulaForDefault);
					defaultValues = defaultFormulaDum.getDefaultValues(driver, dum, profile, object, locale, role);
				}
			}
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Impossible to get parameter's default values", e);
		}
		if (defaultValues == null) {
			defaultValues = new DefaultValuesList();
		}
		LOGGER.debug("OUT");
		return defaultValues;
	}

	protected DefaultValuesList getDefaultValuesFromDefaultLovDum(IDrivableBIResource object, IEngUserProfile profile,
			ILovDetail lovForDefault, Locale locale) throws Exception {
		LOGGER.debug("IN");
		DefaultValuesList defaultValues = new DefaultValuesList();

		// get from cache, if available
		LovResultCacheManager executionCacheManager = new LovResultCacheManager();
		String lovResult = executionCacheManager.getLovResultDum(profile, lovForDefault, new ArrayList<>(), object,
				true, locale);
		LovResultHandler lovResultHandler = new LovResultHandler(lovResult);
		List rows = lovResultHandler.getRows();
		LOGGER.debug("LOV result retrieved without errors");
		LOGGER.debug("LOV contains " + rows.size() + " values");
		Iterator it = rows.iterator();
		String valueColumn = lovForDefault.getValueColumnName();
		String descriptionColumn = lovForDefault.getDescriptionColumnName();
		while (it.hasNext()) {
			SourceBean row = (SourceBean) it.next();
			LovValue defaultValue = new LovValue();
			defaultValue.setValue(row.getAttribute(valueColumn));
			defaultValue.setDescription(row.getAttribute(descriptionColumn));
			defaultValues.add(defaultValue);
		}
		LOGGER.debug("OUT");
		return defaultValues;
	}

	public DefaultValuesList getDefaultQueryValuesDum(AbstractDriver biparam, AbstractBIResourceRuntime dum,
			IEngUserProfile userProfile, IDrivableBIResource object, Locale locale, String role) {

		LovResultCacheManager executionCacheManager = new LovResultCacheManager();
		ILovDetail lovProvDet = dum.getLovDetail(biparam);
		String columnName = null;
		String lovResult = null;
		try {
			lovResult = executionCacheManager.getLovResultDum(userProfile, lovProvDet,
					dum.getDependencies(biparam, role), object, true, locale);

			columnName = lovProvDet.getValueColumnName();
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}

		// get all the rows of the result
		LovResultHandler lovResultHandler = null;
		try {
			lovResultHandler = new LovResultHandler(lovResult);
		} catch (SourceBeanException e) {
			LOGGER.error(e.getMessage());
		}

		DefaultValuesList defaultValuesList = new DefaultValuesList();
		if (lovResultHandler != null) {
			List<SourceBean> rows = lovResultHandler.getRows();
			for (SourceBean row : rows) {
				String rowValue = (String) row.getAttribute(columnName);

				LovValue defaultValue = new LovValue();
				defaultValue.setValue(rowValue);

				defaultValuesList.add(defaultValue);
			}
		}
		return defaultValuesList;
	}

}
