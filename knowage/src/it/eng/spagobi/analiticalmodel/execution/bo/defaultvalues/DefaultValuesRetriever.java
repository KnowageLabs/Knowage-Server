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

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.handlers.ExecutionInstance;
import it.eng.spagobi.analiticalmodel.document.handlers.LovResultCacheManager;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ObjParuse;
import it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail;
import it.eng.spagobi.behaviouralmodel.lov.bo.LovResultHandler;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

public class DefaultValuesRetriever {
	
	private static Logger logger = Logger.getLogger(DefaultValuesRetriever.class);

	public DefaultValuesList getDefaultValues(
			BIObjectParameter analyticalDocumentParameter,
			ExecutionInstance executionInstance, IEngUserProfile profile) {
		logger.debug("IN");
		DefaultValuesList defaultValues = null;
		try {
			ILovDetail lovForDefault = executionInstance.getLovDetailForDefault(analyticalDocumentParameter);
			if (lovForDefault != null) {
				logger.debug("A LOV for default values is defined : " + lovForDefault);
				defaultValues = getDefaultValuesFromDefaultLov(
						executionInstance, profile, lovForDefault);
			} else {
				logger.debug("No LOV for default values defined");
				String formulaForDefault = analyticalDocumentParameter.getParameter().getDefaultFormula();
				if (formulaForDefault != null) {
					IDefaultFormula defaultFormula = DefaultFormulas.get(formulaForDefault);
					defaultValues = defaultFormula.getDefaultValues(analyticalDocumentParameter, executionInstance, profile);
				}
			}
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Impossible to get parameter's default values", e);
		}
		if (defaultValues == null) {
			defaultValues = new DefaultValuesList();
		}
		logger.debug("OUT");
		return defaultValues;
	}

	protected DefaultValuesList getDefaultValuesFromDefaultLov(
			ExecutionInstance executionInstance, IEngUserProfile profile,
			ILovDetail lovForDefault) throws Exception, SourceBeanException {
		logger.debug("IN");
		DefaultValuesList defaultValues = new DefaultValuesList();
		
		// get from cache, if available
		LovResultCacheManager executionCacheManager = new LovResultCacheManager();
		String lovResult = executionCacheManager.getLovResult(profile, lovForDefault, new ArrayList<ObjParuse>(), executionInstance, true);
		LovResultHandler lovResultHandler = new LovResultHandler(lovResult);		
		List rows = lovResultHandler.getRows();
		logger.debug("LOV result retrieved without errors");
		logger.debug("LOV contains " + rows.size() + " values");
		Iterator it = rows.iterator();
		String valueColumn = lovForDefault.getValueColumnName();
		String descriptionColumn = lovForDefault.getDescriptionColumnName();
		while (it.hasNext()) {
			SourceBean row = (SourceBean) it.next();
			DefaultValue defaultValue = new DefaultValue();
			defaultValue.setValue(row.getAttribute(valueColumn));
			defaultValue.setDescription(row.getAttribute(descriptionColumn));
			defaultValues.add(defaultValue);
		}
		logger.debug("OUT");
		return defaultValues;
	}

	public DefaultValuesList getDefaultQueryValues(BIObjectParameter biparam,
			ExecutionInstance executionInstance, IEngUserProfile userProfile) {
		
		LovResultCacheManager executionCacheManager = new LovResultCacheManager();
		ILovDetail lovProvDet = executionInstance.getLovDetail(biparam);
		String columnName = null;
		String lovResult = null;
		try {
			lovResult = executionCacheManager.getLovResult(userProfile,
					lovProvDet,
					executionInstance.getDependencies(biparam),
					executionInstance, true);
			
			columnName = lovProvDet.getValueColumnName();
		} catch (Exception e) {
			e.printStackTrace();
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
		
		for(SourceBean row : rows) {
			String rowValue = (String) row.getAttribute(columnName);
			
			DefaultValue defaultValue = new DefaultValue();
			defaultValue.setValue(rowValue);
			
			defaultValuesList.add(defaultValue);
		}
				
		
		return defaultValuesList;
	}
}
