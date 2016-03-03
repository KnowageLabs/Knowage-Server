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
package it.eng.spagobi.commons.dao;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.bo.Config;
import it.eng.spagobi.commons.bo.Domain;

import java.util.List;
/**
 * Defines the Hibernate implementations for all DAO methods, for a domain.
 * 
 * @author Monia Spinelli
 */
public interface IConfigDAO extends ISpagoBIDao{

	public List loadAllConfigParameters() throws Exception;
    
	public Config loadConfigParametersById(String id) throws Exception;
	
	public Config loadConfigParametersByLabel(String label) throws Exception;
    
	public List loadConfigParametersByProperties(String prop) throws Exception;
	
	/**
	 * Save a Config
	 * 
	 * @return Save config
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public void saveConfig(Config c)throws EMFUserError;
	

	/**
	 * Delete a config
	 * 
	 * @return Delete config
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */	
	public void delete(Integer idConfig) throws EMFUserError;
	
}
