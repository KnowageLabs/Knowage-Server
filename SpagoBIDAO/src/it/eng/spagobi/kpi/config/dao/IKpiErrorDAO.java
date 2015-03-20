/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.kpi.config.dao;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.kpi.config.bo.KpiError;
import it.eng.spagobi.kpi.config.metadata.SbiKpiError;
import it.eng.spagobi.tools.dataset.exceptions.DatasetException;

import java.util.List;

public interface IKpiErrorDAO extends ISpagoBIDao{


	/**
	 * @param kpiError
	 * @return
	 * @throws EMFUserError
	 */
	public Integer insertKpiError(SbiKpiError kpiError) throws EMFUserError;

	/**	
	 * @return
	 * @throws EMFUserError
	 */
	public List<KpiError> loadAllKpiErrors() throws EMFUserError;

	/**
	 * 
	 * @return
	 * @throws EMFUserError
	 */
	public KpiError loadKpiErrorById(Integer id) throws EMFUserError;

	/**
	 * @param kpiError
	 * @return
	 * @throws EMFUserError
	 */
	public void updateKpiError(SbiKpiError kpiError) throws EMFUserError;

	/**
	 * 
	 * @param exception
	 * @param modelInstanceId
	 * @param resourceName
	 * @return
	 * @throws EMFUserError
	 */
	public Integer insertKpiError(	DatasetException exception, Integer modelInstanceId, String resourceName) throws EMFUserError;


}
