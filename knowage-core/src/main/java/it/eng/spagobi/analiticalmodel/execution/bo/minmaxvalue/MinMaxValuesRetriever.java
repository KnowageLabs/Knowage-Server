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

package it.eng.spagobi.analiticalmodel.execution.bo.minmaxvalue;

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
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ObjParuse;
import it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail;
import it.eng.spagobi.behaviouralmodel.lov.bo.LovResultHandler;
import it.eng.spagobi.tools.catalogue.bo.MetaModel;
import it.eng.spagobi.tools.catalogue.metadata.IDrivableBIResource;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;


public class MinMaxValuesRetriever {

	private static Logger logger = Logger.getLogger(MinMaxValuesRetriever.class);

	public LovValue getMaxValue(BIObjectParameter analyticalDocumentParameter, ExecutionInstance executionInstance, IEngUserProfile profile) {
		logger.debug("IN");
		LovValue retValue = null;
		try {
			ILovDetail lov = executionInstance.getLovDetailForMax(analyticalDocumentParameter);
			if (lov != null) {
				logger.debug("A LOV for max values is defined : " + lov);
				retValue = getMaxValueFromMaxLov(executionInstance, profile, lov);
			}
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Impossible to get parameter's max values", e);
		}
		if (retValue == null) {
			retValue = new LovValue();
		}
		logger.debug("OUT");
		return retValue;
	}

	protected LovValue getMaxValueFromMaxLov(ExecutionInstance executionInstance, IEngUserProfile profile, ILovDetail lov) throws Exception, SourceBeanException {
		logger.debug("IN");
		LovValue retValue = new LovValue();

		// get from cache, if available
		LovResultCacheManager executionCacheManager = new LovResultCacheManager();
		String lovResult = executionCacheManager.getLovResult(profile, lov, new ArrayList<ObjParuse>(), executionInstance, true);
		LovResultHandler lovResultHandler = new LovResultHandler(lovResult);
		List rows = lovResultHandler.getRows();
		int size = rows.size();
		logger.debug("LOV result retrieved without errors");
		logger.debug("LOV contains " + size + " values");
		if (size != 1) {
			String msg = String.format("LOV for min and max value must provide exactly 1 value, %d founded", size);
			throw new SpagoBIRuntimeException(msg);
		}
		Iterator it = rows.iterator();
		String valueColumn = lov.getValueColumnName();
		String descriptionColumn = lov.getDescriptionColumnName();
		while (it.hasNext()) {
			SourceBean row = (SourceBean) it.next();
			retValue.setValue(row.getAttribute(valueColumn));
			retValue.setDescription(row.getAttribute(descriptionColumn));
		}
		logger.debug("OUT");
		return retValue;
	}

	public LovValue getMaxQueryValues(BIObjectParameter biparam, ExecutionInstance executionInstance, IEngUserProfile userProfile) {

		LovResultCacheManager executionCacheManager = new LovResultCacheManager();
		ILovDetail lovProvDet = executionInstance.getLovDetail(biparam);
		String columnName = null;
		String lovResult = null;
		try {
			lovResult = executionCacheManager.getLovResult(userProfile, lovProvDet, executionInstance.getDependencies(biparam), executionInstance, true);

			columnName = lovProvDet.getValueColumnName();
		} catch (Exception e) {
			logger.error("Error retriving min/max value", e);
		}

		// get all the rows of the result
		LovResultHandler lovResultHandler = null;
		try {
			lovResultHandler = new LovResultHandler(lovResult);
		} catch (SourceBeanException e) {
			logger.error("Error retriving min/max value", e);
		}

		LovValue ret = new LovValue();
		List<SourceBean> rows = lovResultHandler.getRows();

		for (SourceBean row : rows) {
			String rowValue = (String) row.getAttribute(columnName);

			ret.setValue(rowValue);
		}

		return ret;
	}

	public LovValue getMaxValueDum(AbstractDriver driver, IDrivableBIResource object, IEngUserProfile profile, Locale locale, String role) {
		logger.debug("IN");
		AbstractBIResourceRuntime dum = null;
		if (object instanceof BIObject) {
			dum = new DocumentRuntime(profile, locale);
		} else if (object instanceof MetaModel) {
			dum = new BusinessModelRuntime(profile, locale);
		}
		LovValue retValue = null;
		try {
			ILovDetail lov = dum.getLovDetailForMax(driver);
			if (lov != null) {
				logger.debug("A LOV for max values is defined : " + lov);
				retValue = getMaxValueFromMaxLovDum(object, profile, lov, locale);
			}
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Impossible to get parameter's max values", e);
		}
		if (retValue == null) {
			retValue = new LovValue();
		}
		logger.debug("OUT");
		return retValue;
	}

	protected LovValue getMaxValueFromMaxLovDum(IDrivableBIResource object, IEngUserProfile profile, ILovDetail lov, Locale locale)
			throws Exception, SourceBeanException {
		logger.debug("IN");
		LovValue retValue = new LovValue();

		// get from cache, if available
		LovResultCacheManager executionCacheManager = new LovResultCacheManager();
		String lovResult = executionCacheManager.getLovResultDum(profile, lov, new ArrayList<ObjParuse>(), object, true, locale);
		LovResultHandler lovResultHandler = new LovResultHandler(lovResult);
		List rows = lovResultHandler.getRows();
		int size = rows.size();
		logger.debug("LOV result retrieved without errors");
		logger.debug("LOV contains " + size + " values");
		if (size != 1) {
			String msg = String.format("LOV for min and max value must provide exactly 1 value, %d founded", size);
			throw new SpagoBIRuntimeException(msg);
		}
		Iterator it = rows.iterator();
		String valueColumn = lov.getValueColumnName();
		String descriptionColumn = lov.getDescriptionColumnName();
		while (it.hasNext()) {
			SourceBean row = (SourceBean) it.next();
			retValue.setValue(row.getAttribute(valueColumn));
			retValue.setDescription(row.getAttribute(descriptionColumn));
		}
		logger.debug("OUT");
		return retValue;
	}

	public LovValue getMaxQueryValuesDum(AbstractDriver biparam, AbstractBIResourceRuntime dum, IEngUserProfile userProfile,
			IDrivableBIResource object, Locale locale, String role) {

		LovResultCacheManager executionCacheManager = new LovResultCacheManager();
		ILovDetail lovProvDet = dum.getLovDetail(biparam);
		String columnName = null;
		String lovResult = null;
		try {
			lovResult = executionCacheManager.getLovResultDum(userProfile, lovProvDet, dum.getDependencies(biparam, role), object, true, locale);

			columnName = lovProvDet.getValueColumnName();
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		// get all the rows of the result
		LovResultHandler lovResultHandler = null;
		try {
			lovResultHandler = new LovResultHandler(lovResult);
		} catch (SourceBeanException e) {
			logger.error(e.getMessage());
		}

		LovValue retValue = new LovValue();
		if (lovResultHandler != null) {
			List<SourceBean> rows = lovResultHandler.getRows();
			for (SourceBean row : rows) {
				String rowValue = (String) row.getAttribute(columnName);
				
				retValue.setValue(rowValue);
			}
		}
		return retValue;
	}

}
