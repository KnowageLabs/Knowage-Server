/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.kpi.config.dao;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.kpi.config.bo.MeasureUnit;

public interface IMeasureUnitDAO extends ISpagoBIDao{

	/**
	 * Returns the MeasureUnit of the referred id
	 * 
	 * @param id of the Measure Unit
	 * @return Threshold of the referred id
	 * @throws EMFUserError If an Exception occurred
	 */
	public MeasureUnit loadMeasureUnitById(Integer id) throws EMFUserError;

	/**
	 * Returns the MeasureUnit of the referred code
	 * 
	 * @param cd of the Measure Unit
	 * @return Threshold of the referred cd
	 * @throws EMFUserError If an Exception occurred
	 */
	public MeasureUnit loadMeasureUnitByCd(String cd) throws EMFUserError;



}
