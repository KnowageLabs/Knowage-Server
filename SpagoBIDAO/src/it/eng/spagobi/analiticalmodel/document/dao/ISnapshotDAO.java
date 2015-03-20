/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.analiticalmodel.document.dao;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.Snapshot;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiSnapshots;
import it.eng.spagobi.commons.dao.ISpagoBIDao;

import java.util.List;

public interface ISnapshotDAO extends ISpagoBIDao{

	/**
	 * Save a snapshot of the object.
	 * 
	 * @param content byte array containing the content of the snapshot
	 * @param idBIObj the id of the biobject parent
	 * @param name the name of the new subobject
	 * @param description the description of the new subobject
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public void saveSnapshot(byte[] content, Integer idBIObj, String name, String description, String contentType) throws EMFUserError;
	
	/**
	 * Gets the list of the snapshot details that are children of a biobject.
	 * 
	 * @param idBIObj the id of the biobject parent
	 * 
	 * @return List of BIObject.BIObjectSnapshot objects
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public List getSnapshots(Integer idBIObj)  throws EMFUserError;
	
	/**
	 * Delete a snapshot.
	 * 
	 * @param idSnap the id of the snapshot
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public void deleteSnapshot(Integer idSnap) throws EMFUserError;
	
	/**
	 * Load a snapshot.
	 * 
	 * @param idSnap the id of the snapshot
	 * 
	 * @return Snapshot the snapshot loaded
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public Snapshot loadSnapshot(Integer idSnap) throws EMFUserError;
	
	/**
	 * Gets the last snapshot that is children of a biobject.
	 * 
	 * @param idBIObj the id of the biobject parent
	 * 
	 * @return Snapshot the snapshot loaded
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public Snapshot getLastSnapshot(Integer idBIObj)  throws EMFUserError;
}
