/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.kpi.threshold.dao;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.kpi.config.bo.KpiInstance;
import it.eng.spagobi.kpi.threshold.bo.ThresholdValue;
import it.eng.spagobi.kpi.threshold.metadata.SbiThresholdValue;

import java.util.List;

public interface IThresholdValueDAO extends ISpagoBIDao{

	List loadThresholdValueList(Integer thresholdId,String fieldOrder, String typeOrder) throws EMFUserError;

	ThresholdValue loadThresholdValueById(Integer id) throws EMFUserError;
	
	SbiThresholdValue loadSbiThresholdValueById(Integer id) throws EMFUserError;

	void modifyThresholdValue(ThresholdValue thrVal) throws EMFUserError;

	Integer insertThresholdValue(ThresholdValue thrVal) throws EMFUserError;
	
	Integer saveOrUpdateThresholdValue(ThresholdValue thrVal) throws EMFUserError;

	boolean deleteThresholdValue(Integer thresholdId) throws EMFUserError;
	
	ThresholdValue toThresholdValue(SbiThresholdValue t)throws EMFUserError;

	public List getThresholdValues(KpiInstance k) throws EMFUserError;

	public List loadThresholdValuesByThresholdId(Integer id) throws EMFUserError;

}
