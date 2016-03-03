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
package it.eng.spagobi.wapp.dao;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.wapp.bo.MenuRoles;

import java.util.List;

/**
 * @author Antonella Giachino (antonella.giachino@eng.it)
 *
 */
public interface IMenuRolesDAO extends ISpagoBIDao{
	
	/**
	 * Loads all detail information for all menu compatible to the role specified
	 * at input. For each of them, name is stored into a <code>String</code> object.
	 * After that, all names are stored into a <code>List</code>, which is returned.
	 * 
	 * @param roleId the role id
	 * 
	 * @return A list containing all menu objects compatible with the role passed at input
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public List loadMenuByRoleId(Integer roleId) throws EMFUserError;		
}
