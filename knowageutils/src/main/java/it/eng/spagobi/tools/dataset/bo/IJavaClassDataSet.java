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

package it.eng.spagobi.tools.dataset.bo;

import java.util.List;
import java.util.Map;

public interface IJavaClassDataSet {
	
	/**
	 * Gets the values formatted into an xml structure.
	 * 
	 * @param profile a user profile used to fill attributes required by the query
	 * 
	 * @return the xml string of the values
	 */
	public String getValues(Map profile, Map parameters);
	
	
	/**
	 * Gets the list of profile attribute names required by the class.
	 * 
	 * @return the list of profile attribute names
	 */
	public List getNamesOfProfileAttributeRequired();

}
