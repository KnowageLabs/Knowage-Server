/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.kpi.threshold.utils;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.kpi.threshold.bo.Threshold;

public class DetailThresholdsUtil {

	public static void selectThreshold(Integer id, SourceBean serviceResponse)
			throws SourceBeanException, EMFUserError {
		Threshold toReturn = DAOFactory.getThresholdDAO().loadThresholdById(id);
		serviceResponse.setAttribute("THRESHOLD", toReturn);
	}

	public static void updateThresholdFromRequest(SourceBean serviceRequest,
			Integer id) throws EMFUserError {
		Threshold threshold = getThresholdFromRequest(serviceRequest);
		threshold.setId(id);
		DAOFactory.getThresholdDAO().modifyThreshold(threshold);
	}

	private static Threshold getThresholdFromRequest(SourceBean serviceRequest) {
		
		String name = (String) serviceRequest.getAttribute("name");
		String description = (String) serviceRequest.getAttribute("description");
		String code = (String) serviceRequest.getAttribute("code");
		String sThresholdTypeId = (String) serviceRequest.getAttribute("threshold_type_id");
		
		Integer thresholdTypeId = null;
		
		if (sThresholdTypeId != null && ! (sThresholdTypeId.trim().equals(""))) {
			thresholdTypeId = Integer.parseInt(sThresholdTypeId);
		}

		Threshold toReturn = new Threshold();

		toReturn.setName(name);
		toReturn.setDescription(description);
		toReturn.setCode(code);
		toReturn.setThresholdTypeId(thresholdTypeId);

		return toReturn;
	}

	public static void newThreshold(SourceBean serviceRequest, SourceBean serviceResponse) throws EMFUserError, SourceBeanException {
		Threshold toCreate = getThresholdFromRequest(serviceRequest);
		
		Integer thresholdId = DAOFactory.getThresholdDAO().insertThreshold(toCreate);

		serviceResponse.setAttribute("ID", thresholdId);
		serviceResponse.setAttribute("MESSAGE",SpagoBIConstants.DETAIL_SELECT);
		selectThreshold(thresholdId, serviceResponse);
	}

	public static void restoreThreshold(Integer id, SourceBean serviceRequest,
			SourceBean serviceResponse) throws Exception {
		Threshold toReturn = getThresholdFromRequest(serviceRequest);
		if(id != null) {
			toReturn.setId(id);
		}
		serviceResponse.setAttribute("THRESHOLD", toReturn);
	}

}
