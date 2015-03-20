/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.hotlink.rememberme.dao;

import it.eng.spago.error.EMFInternalError;
import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.hotlink.rememberme.bo.RememberMe;

import java.util.List;

/**
 * 
 * @author Zerbetto (davide.zerbetto@eng.it)
 *
 */
public interface IRememberMeDAO extends ISpagoBIDao{
	
	/**
	 * Save remember me.
	 * 
	 * @param name the name
	 * @param description the description
	 * @param docId the doc id
	 * @param subObjId the sub obj id
	 * @param userId the user id
	 * @param parameters the parameters
	 * 
	 * @return true, if successful
	 * 
	 * @throws EMFInternalError the EMF internal error
	 */
	public boolean saveRememberMe(String name, String description, Integer docId, Integer subObjId, String userId, String parameters) throws EMFInternalError;
	
	/**
	 * Gets all user's remember me.
	 * 
	 * @param userId the user id
	 * 
	 * @return the my remember me
	 * 
	 * @throws EMFInternalError the EMF internal error
	 */
	public List<RememberMe> getMyRememberMe(String userId) throws EMFInternalError;
	
	/**
	 * Deletes the remember me with the given id.
	 * 
	 * @param rememberMeId the remember me id
	 * 
	 * @throws EMFInternalError the EMF internal error
	 */
	public void delete(Integer rememberMeId) throws EMFInternalError;
	
	/**
	 * Loads the remember me with the given id.
	 * 
	 * @param rememberMeId the remember me id
	 * 
	 * @return the required remember me
	 * 
	 * @throws EMFInternalError the EMF internal error
	 */
	public RememberMe getRememberMe(Integer rememberMeId) throws EMFInternalError;
}
