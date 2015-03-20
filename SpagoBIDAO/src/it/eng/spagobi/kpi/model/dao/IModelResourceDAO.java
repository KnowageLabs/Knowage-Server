/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.kpi.model.dao;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.kpi.model.bo.Resource;
import it.eng.spagobi.kpi.model.metadata.SbiKpiModelResources;

import java.util.List;

public interface IModelResourceDAO extends ISpagoBIDao{

	
	/**
	 * Load all couples .
	 * 
	 * @param modelId
	 *            the id of modelInstance to check.

	 * @return list of modelResource Id
	 * 
	 * @throws EMFUserError
	 */
	List loadModelResourceByModelId(Integer modelInstId) throws EMFUserError;

	
	
	public Resource toResource(SbiKpiModelResources re) throws EMFUserError ;
	
	/**
	 * Check if a resources is associated with a modelInstance.
	 * 
	 * @param modelId
	 *            the id of modelInstance to check.
	 * @param resourceId
	 *            the id of resources to check.
	 * @return true if exist an association between the model instance id and
	 *         the resources id, false otherwise.
	 * 
	 * @throws EMFUserError
	 */
	boolean isSelected(Integer modelId, Integer resourceId) throws EMFUserError;

	/**
	 * Remove an association between a model instance and a resource.
	 * 
	 * @param modelId
	 *            the id of modelInstance.
	 * @param resourceId
	 *            the id of the resource.
	 * 
	 *@throws EMFUserError
	 */
	void removeModelResource(Integer modelId, Integer resourceId)
			throws EMFUserError;

	/**
	 * Add an association between a model instance and a resource.
	 * 
	 * @param modelId
	 *            the id of modelInstance.
	 * @param resourceId
	 *            the id of the resource.
	 * 
	 * @throws EMFUserError
	 */
	void addModelResource(Integer modelId, Integer resourceId)
			throws EMFUserError;
	
	/**
	 * Remove all association between a model and same resources.
	 * 
	 * @param modelId
	 *            the id of modelInstance.
	 * 
	 *@throws EMFUserError
	 */
	void removeAllModelResource(Integer modelId)
	throws EMFUserError;

}
