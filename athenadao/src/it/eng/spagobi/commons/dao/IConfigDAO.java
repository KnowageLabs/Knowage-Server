/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
