/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/*
 * Created on 13-mag-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package it.eng.spagobi.tools.dataset.dao;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.tools.dataset.bo.BIObjDataSet;

import java.util.ArrayList;

import org.hibernate.Session;

/**
 * Defines the interfaces for all methods needed to insert, modify and deleting a BI Object DataSet.
 * 
 * @author Gavardi
 * 
 */
public interface IBIObjDataSetDAO extends ISpagoBIDao {

	/**
	 * Update association between object and used datasets
	 * 
	 * @param biObjId
	 * @param dsLabels
	 * @return
	 * @throws EMFUserError
	 */

	public void updateObjectNotDetailDatasets(BIObject biObj, ArrayList<String> dsLabels, Session currSession) throws EMFUserError;

	public void updateObjectDetailDataset(Integer objectId, Integer dsId, Session currSession) throws EMFUserError;

	public ArrayList<BIObject> getBIObjectsUsingDataset(Integer datasetId, Session currSession) throws EMFUserError;

	public ArrayList<BIObject> getBIObjectsUsingDataset(Integer datasetId) throws EMFUserError;

	public ArrayList<BIObjDataSet> getBiObjDataSets(Integer biObjId, Session currSession) throws EMFUserError;

	public ArrayList<BIObjDataSet> getBiObjDataSets(Integer biObjId) throws EMFUserError;

	public BIObjDataSet getObjectDetailDataset(Integer objectId, Session currSession) throws EMFUserError;

	public ArrayList<BIObjDataSet> getObjectNotDetailDataset(Integer objectId, Session currSession) throws EMFUserError;

	public void eraseBIObjDataSetByObjectId(Integer biObjId) throws EMFUserError;

	public void eraseBIObjDataSetByObjectId(Integer biObjId, Session currSession) throws EMFUserError;
}