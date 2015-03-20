/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.massiveExport.dao;



import java.util.List;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.tools.massiveExport.bo.ProgressThread;

/**
 * Defines the interfaces for all methods needed to insert, modify and deleting an engine.
 */
public interface IProgressThreadDAO extends ISpagoBIDao{
	
/**
 * 
 * @param progressThreadId
 * @return
 * @throws EMFUserError
 */
	
	public ProgressThread loadProgressThreadById(Integer progressThreadId) throws EMFUserError;

	/**
	 * 
	 * @param userId
	 * @param functCd
	 * @return
	 * @throws EMFUserError
	 */
	public ProgressThread loadActiveProgressThreadByUserIdAndFuncCd(String userId, String functCd) throws EMFUserError;

	
	/**
	 * 
	 * @param userId
	 * @return
	 * @throws EMFUserError
	 */
	public List<ProgressThread> loadActiveProgressThreadsByUserId(String userId) throws EMFUserError;

	/**
	 * 
	 * @param userId
	 * @return
	 * @throws EMFUserError
	 */
	public List<ProgressThread> loadNotClosedProgressThreadsByUserId(String userId) throws EMFUserError;

	
	/**
	 * 
	 * @param progressThreadId
	 * @return
	 * @throws EMFUserError
	 */
	public boolean incrementProgressThread(Integer progressThreadId) throws EMFUserError;
	
	/**
	 * 
	 * @param progThread
	 * @return
	 * @throws EMFUserError
	 */
	public Integer insertProgressThread(ProgressThread progThread) throws EMFUserError;

	/**
	 * 
	 * @param progressThreadId
	 * @return
	 * @throws EMFUserError
	 */
	public void setDownloadProgressThread(Integer progressThreadId) throws EMFUserError;
	
	
	public void setErrorProgressThread(Integer progressThreadId) throws EMFUserError;

	public void setStartedProgressThread(Integer progressThreadId) throws EMFUserError;

	
	public boolean deleteProgressThread(Integer progressThreadId) throws EMFUserError;
}
