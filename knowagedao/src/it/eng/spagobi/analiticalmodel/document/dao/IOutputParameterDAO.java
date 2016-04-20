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
package it.eng.spagobi.analiticalmodel.document.dao;

import it.eng.spagobi.analiticalmodel.document.bo.OutputParameter;
import it.eng.spagobi.commons.dao.ISpagoBIDao;

import java.util.List;

public interface IOutputParameterDAO extends ISpagoBIDao {

	/**
	 * List SbiOutputParameter by SbiObject id
	 * 
	 * @param id
	 *            of SbiObject
	 * @return list of OutputParameter
	 */
	public List<OutputParameter> getOutputParametersByObjId(Integer id);

	public void removeParameter(Integer id);

	public Integer saveParameter(OutputParameter outputParameter);

	public OutputParameter getOutputParameter(Integer id);

	/**
	 * 
	 * @param list
	 *            of OutputParameter
	 */
	// public void saveParameterList(List<OutputParameter> list, Integer biobjId) throws EMFUserError;

}
