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
package it.eng.spagobi.tools.dataset.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.tools.dataset.bo.BIObjDataSet;

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

	void updateObjectNotDetailDatasets(BIObject biObj, ArrayList<String> dsLabels, Session currSession)
			throws EMFUserError;

	void updateObjectDetailDataset(Integer objectId, Integer dsId, Session currSession) throws EMFUserError;

	List<BIObject> getBIObjectsUsingDataset(Integer datasetId, Session currSession) throws EMFUserError;

	List<BIObject> getBIObjectsUsingDataset(Integer datasetId) throws EMFUserError;

	List<BIObjDataSet> getBiObjDataSets(Integer biObjId, Session currSession) throws EMFUserError;

	List<BIObjDataSet> getBiObjDataSets(Integer biObjId) throws EMFUserError;

	BIObjDataSet getObjectDetailDataset(Integer objectId, Session currSession) throws EMFUserError;

	List<BIObjDataSet> getObjectNotDetailDataset(Integer objectId, Session currSession) throws EMFUserError;

	void eraseBIObjDataSetByObjectId(Integer biObjId) throws EMFUserError;

	void eraseBIObjDataSetByObjectId(Integer biObjId, Session currSession) throws EMFUserError;

	void insertOrUpdateDatasetDependencies(BIObject biObject, ObjTemplate template, Session session);
}