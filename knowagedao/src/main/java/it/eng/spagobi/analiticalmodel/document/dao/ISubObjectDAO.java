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

import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.SubObject;
import it.eng.spagobi.commons.dao.ISpagoBIDao;

import java.util.List;

import org.hibernate.Session;

public interface ISubObjectDAO extends ISpagoBIDao{

	/**
	 * Save a subObject of the object.
	 * 
	 * @param idBIObj the id of the biobject parent
	 * @param subObj the sub obj
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public Integer saveSubObject(Integer idBIObj, SubObject subObj) throws EMFUserError;
	
	/**
	 * Modify a subObject of the object.
	 * 
	 * @param idBIObj the id of the biobject parent
	 * @param subObj the sub obj
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public Integer modifySubObject(Integer idBIObj, SubObject subObj) throws EMFUserError;
	
	/**
	 * Gets the detail of all the subobjects accessible to the user.
	 * 
	 * @param idBIObj the id of the biobject parent
	 * @param profile Profile of the user
	 * 
	 * @return List of SubObject objects
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public List getAccessibleSubObjects(Integer idBIObj, IEngUserProfile profile) throws EMFUserError;
		
	/**
	 * Gets the InputStream of the subobjects content.
	 * 
	 * @param idSubObj the id of the subobject
	 * 
	 * @return SubObject the subobject loaded
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public SubObject getSubObject(Integer idSubObj) throws EMFUserError;
		
	/**
	 * Delete a subObject.
	 * 
	 * @param idSub the id sub
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public void deleteSubObject(Integer idSub) throws EMFUserError;

	/**
	 * Delete a subObject mantaining the previous connection.
	 * 
	 * @param idSub the id sub
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public void deleteSubObjectSameConnection(Integer idSub, Session aSession) throws EMFUserError;

	
	/**
	 * Gets the detail of all the biobject subobjects.
	 * 
	 * @param idBIObj the id of the biobject parent
	 * 
	 * @return List of SubObject objects
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public List getSubObjects(Integer idBIObj) throws EMFUserError;
	
	/**
	 * Gets the detail of all the public biobject subobjects.
	 * 
	 * @param idBIObj the id of the biobject parent
	 * 
	 * @return List of SubObject objects
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public List getPublicSubObjects(Integer idBIObj) throws EMFUserError;
	
	/**
	 * Gets the subobject specified by its name and document id in input
	 * 
	 * @param name the name of the subobject
	 * @param idBIObj the id of the biobject parent
	 * 
	 * @return the required subobject SubObject
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public SubObject getSubObjectByNameAndBIObjectId(String name, Integer idBIObj) throws EMFUserError;
}
