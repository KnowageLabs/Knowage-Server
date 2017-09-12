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
