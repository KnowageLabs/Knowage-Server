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
